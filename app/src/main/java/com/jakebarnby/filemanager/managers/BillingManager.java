package com.jakebarnby.filemanager.managers;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.jakebarnby.filemanager.util.Constants;
import com.jakebarnby.filemanager.util.LogUtils;
import com.jakebarnby.filemanager.util.PreferenceUtils;
import com.jakebarnby.filemanager.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jake on 10/21/2017.
 */

public class BillingManager {

    private BillingClient mBillingClient;
    private int mRetryCount;


    public BillingManager(Context context) {
        mRetryCount = 0;

        mBillingClient = BillingClient.newBuilder(context).setListener((billingResult, purchases) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (Purchase purchase : purchases) {
                    handlePurchase(context, purchase);
                }
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                LogUtils.logFirebaseEvent(
                    FirebaseAnalytics.getInstance(context),
                    Constants.Analytics.EVENT_CANCELLED_PURCHASE);
            }
        }).enablePendingPurchases().build();

        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    queryPurchases(context);
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                if (mRetryCount < 5) {
                    mBillingClient.startConnection(this);
                    mRetryCount++;
                }
            }
        });
    }

    /**
     * Start google play billing flow for the given sku
     *
     * @param activity Activity that launched the flow
     * @param sku      Sku of the product to purchase
     */
    public void purchaseItem(Activity activity, String sku) {
        List<String> skuList = new ArrayList<>();
        skuList.add(sku);

        SkuDetailsParams params = SkuDetailsParams
            .newBuilder()
            .setSkusList(skuList)
            .setType(BillingClient.SkuType.INAPP)
            .build();

        mBillingClient.querySkuDetailsAsync(params,
            (billingResult, skuDetailsList) -> {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    for (SkuDetails detail : skuDetailsList) {
                        if (detail.getSku().equals(sku)) {
                            BillingFlowParams flowParams = BillingFlowParams
                                .newBuilder()
                                .setSkuDetails(detail)
                                .build();

                            mBillingClient.launchBillingFlow(activity, flowParams);
                        }
                    }
                }
            });
    }

    /**
     * Handle a purchase success event
     *
     * @param context  Context for resources
     * @param purchase The purchase that was made
     */
    private void handlePurchase(Context context, Purchase purchase) {
        if (purchase.getPurchaseState() != Purchase.PurchaseState.PURCHASED) {
            // TODO: LOG error
            return;
        }

        switch (purchase.getSku()) {
            case Constants.Billing.SKU_PREMIUM:
                //TODO: Show dialog "Thanks! All ads have been disabled."
                PreferenceUtils.savePref(context, Constants.Prefs.HIDE_ADS_KEY, true);
                break;
        }

        if (!purchase.isAcknowledged()) {
            AcknowledgePurchaseParams acknowledgePurchaseParams =
                AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.getPurchaseToken())
                    .build();
            mBillingClient.acknowledgePurchase(
                acknowledgePurchaseParams,
                billingResult -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        Bundle params = new Bundle();
                        params.putString(Constants.Analytics.PARAM_PURCHASE_SKU, purchase.getSku());
                        LogUtils.logFirebaseEvent(
                            FirebaseAnalytics.getInstance(context),
                            Constants.Analytics.EVENT_SUCCESS_PURCHASE,
                            params);
                    }
                }
            );
        }
    }

    /**
     * Check a users current purchases
     *
     * @param context Context for resources
     */
    private void queryPurchases(Context context) {
        Purchase.PurchasesResult purchasesResult = mBillingClient.queryPurchases(BillingClient.SkuType.INAPP);
        List<Purchase> purchases = purchasesResult.getPurchasesList();
        for (Purchase purchase : purchases) {
            if (purchase.getSku().equals(Constants.Billing.SKU_PREMIUM)) {
                PreferenceUtils.savePref(context, Constants.Prefs.HIDE_ADS_KEY, true);
            }
        }
    }
}
