package com.codelab.billing;

import android.util.ArrayMap;

import com.android.billingclient.api.BillingClient;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Skus {
    public static final String SKU_INAPP_GAS = "gas";
    public static final String SKU_INAPP_PREMIUM = "premium";
    public static final String SKU_SUB_GOLD_MONTHLY = "gold_monthly";
    public static final String SKU_SUB_GOLD_YEARLY = "gold_yearly";

    private static final Map<String, List<String>> SKU_MAP;

    static {
        SKU_MAP = new ArrayMap<>();
        SKU_MAP.put(BillingClient.SkuType.INAPP, Arrays
                .asList(SKU_INAPP_GAS, SKU_INAPP_PREMIUM));
        SKU_MAP.put(BillingClient.SkuType.SUBS, Arrays
                .asList(SKU_SUB_GOLD_MONTHLY, SKU_SUB_GOLD_YEARLY));
    }

    public static List<String> getSkus(@BillingClient.SkuType String skuType) {
        return SKU_MAP.get(skuType);
    }
}
