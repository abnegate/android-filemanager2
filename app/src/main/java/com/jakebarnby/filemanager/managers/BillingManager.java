package com.jakebarnby.filemanager.managers;

import android.app.Activity;
import android.content.Context;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.Purchase;
import com.jakebarnby.filemanager.util.Constants;
import com.jakebarnby.filemanager.util.PreferenceUtils;

import java.util.List;

/**
 * Created by Jake on 10/21/2017.
 */

public class BillingManager {

    private BillingClient mBillingClient;
    private int mRetryCount;

    public BillingManager(Context context) {
        mRetryCount = 0;

        mBillingClient = BillingClient.newBuilder(context).setListener((responseCode, purchases) -> {
            if (responseCode == BillingClient.BillingResponse.OK
                    && purchases != null) {
                for (Purchase purchase : purchases) {
                    handlePurchase(context, purchase);
                }
            } else if (responseCode == BillingClient.BillingResponse.USER_CANCELED) {
                // Handle an error caused by a user cancelling the purchase flow.
            } else {
                // Handle any other error codes.
            }
        }).build();

        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@BillingClient.BillingResponse int billingResponseCode) {
                if (billingResponseCode == BillingClient.BillingResponse.OK) {
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

    private void handlePurchase(Context context, Purchase purchase) {
        switch (purchase.getSku()) {
            case Constants.Billing.SKU_PREMIUM:
                PreferenceUtils.savePref(context, Constants.Prefs.SHOW_ADS_KEY, true);
                break;
        }
    }

    public void purchaseItem(Activity activity, String sku) {
        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setSku(sku)
                .setType(BillingClient.SkuType.INAPP)
                .build();
        int responseCode = mBillingClient.launchBillingFlow(activity, flowParams);
    }

    public void queryPurchases(Context context) {
        Purchase.PurchasesResult purchasesResult = mBillingClient.queryPurchases(BillingClient.SkuType.INAPP);
        List<Purchase> purchases = purchasesResult.getPurchasesList();
        for(Purchase purchase: purchases) {
            if (purchase.getSku().equals(Constants.Billing.SKU_PREMIUM)) {
                PreferenceUtils.savePref(context, Constants.Prefs.SHOW_ADS_KEY, true);
            }
        }
    }
}
