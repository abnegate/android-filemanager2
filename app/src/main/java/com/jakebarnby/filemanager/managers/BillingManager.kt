package com.jakebarnby.filemanager.managers

import android.app.Activity
import android.content.Context
import androidx.core.os.bundleOf
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponseCode.OK
import com.android.billingclient.api.BillingClient.BillingResponseCode.USER_CANCELED
import com.google.firebase.analytics.FirebaseAnalytics
import com.jakebarnby.filemanager.util.Constants
import com.jakebarnby.filemanager.util.Constants.Billing
import com.jakebarnby.filemanager.util.Constants.Prefs
import com.jakebarnby.filemanager.util.Logger
import kotlinx.coroutines.*

/**
 * Created by Jake on 10/21/2017.
 */
class BillingManager(
    context: Context,
    private val prefs: PreferenceManager
) {

    companion object {
        private const val MAX_RETRIES = 5
    }

    private val billingClient: BillingClient
    private var retryCount = 0

    init {
        billingClient = BillingClient.newBuilder(context).setListener { billingResult, purchases ->
            if (billingResult.responseCode == OK && purchases != null) {
                GlobalScope.launch {
                    purchases.map {
                        GlobalScope.async {
                            handlePurchase(context, it)
                        }
                    }.awaitAll()
                }

            } else if (billingResult.responseCode == USER_CANCELED) {
                // TODO: show snackbar purchase cancelled
                Logger.logFirebaseEvent(
                    FirebaseAnalytics.getInstance(context),
                    Constants.Analytics.EVENT_CANCELLED_PURCHASE)
            }
        }.enablePendingPurchases().build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == OK) {
                    queryPurchases(context)
                }
            }

            override fun onBillingServiceDisconnected() {
                if (retryCount < MAX_RETRIES) {
                    billingClient.startConnection(this)
                    retryCount++
                }
            }
        })
    }

    /**
     * Start google play billing flow for the given sku
     *
     * @param activity Activity that launched the flow
     * @param sku      Sku of the product to purchase
     */
    suspend fun purchaseItem(activity: Activity?, sku: String?) {
        val params = SkuDetailsParams.newBuilder()
            .setSkusList(listOf(sku))
            .setType(BillingClient.SkuType.INAPP)
            .build()

        val skuDetailResult = withContext(Dispatchers.IO) {
            billingClient.querySkuDetails(params)
        }
        val success = skuDetailResult.billingResult.responseCode == OK
        val skuDetailList = skuDetailResult.skuDetailsList

        if (!success || skuDetailList == null) {
            // TODO: Show failed to get sku error
            return
        }

        for (detail in skuDetailResult.skuDetailsList!!) {
            if (detail.sku != sku) {
                continue
            }
            val flowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(detail)
                .build()
            billingClient.launchBillingFlow(activity, flowParams)
        }
    }

    /**
     * Handle a purchase success event
     *
     * @param context  Context for resources
     * @param purchase The purchase that was made
     */
    private suspend fun handlePurchase(context: Context, purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) {
            // TODO: LOG error
            return
        }

        when (purchase.sku) {
            Billing.SKU_PREMIUM -> {
                //TODO: Show dialog "Thanks! All ads have been disabled."
                prefs.savePref(Prefs.HIDE_ADS_KEY, true)
            }
        }

        if (purchase.isAcknowledged) {
            return
        }

        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        val acknowledgeResult = withContext(Dispatchers.IO) {
            billingClient.acknowledgePurchase(params)
        }

        if (acknowledgeResult.responseCode == OK) {
            val args = bundleOf(
                Constants.Analytics.PARAM_PURCHASE_SKU to purchase.sku
            )
            Logger.logFirebaseEvent(
                FirebaseAnalytics.getInstance(context),
                Constants.Analytics.EVENT_SUCCESS_PURCHASE,
                args)
        }
    }

    /**
     * Check a users current purchases
     *
     * @param context Context for resources
     */
    private fun queryPurchases(context: Context) {
        val purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP)
        val purchases = purchasesResult.purchasesList
        for (purchase in purchases) {
            if (purchase.sku == Billing.SKU_PREMIUM) {
                prefs.savePref(Prefs.HIDE_ADS_KEY, true)
            }
        }
    }
}