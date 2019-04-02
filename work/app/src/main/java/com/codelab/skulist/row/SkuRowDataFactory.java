package com.codelab.skulist.row;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.billingclient.api.SkuDetails;

import java.util.ArrayList;
import java.util.List;

public class SkuRowDataFactory {
    public static final String TAG = SkuRowDataFactory.class.getSimpleName();

    public static List<SkuRowData> fromSkuDetailsList(@Nullable List<SkuDetails> skuDetailsList) {
        final List<SkuRowData> result = new ArrayList<>();
        if (skuDetailsList == null) {
            Log.w(TAG, "Null parameter", new Throwable());
            return result;
        }
        for (SkuDetails sd : skuDetailsList) {
            result.add(fromSkuDetails(sd));
        }
        return result;
    }

    public static SkuRowData fromSkuDetails(@NonNull SkuDetails skuDetails) {
        return new SkuRowData(skuDetails.getSku(), skuDetails.getTitle(), skuDetails.getPrice(),
                skuDetails.getDescription());
    }
}
