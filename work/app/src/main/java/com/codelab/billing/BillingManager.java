/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.codelab.billing;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.ArrayMap;
import android.util.Log;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Role: Provides implementation for BillingManager, which will handle all the interactions with
 * Play Store (via Billing library), maintain connection to it through BillingClient and cache
 * temporary states/data if needed.
 */
public class BillingManager implements PurchasesUpdatedListener {
    private static final String TAG = BillingManager.class.getSimpleName();

    /**
     * Use {@link #executeOnConnection(Runnable)} when calling {@link BillingClient}'s methods
     * (except when ending the connection)
     */
    private final BillingClient billingClient;

    private final Activity activity;
    private Map<String, SkuDetails> skuDetailsMap;
    private Queue<Runnable> runnableQueue;
    private boolean isClientConnecting;

    public BillingManager(Activity activity) {
        this.activity = activity;
        skuDetailsMap = new ArrayMap<>();
        runnableQueue = new ArrayDeque<>();
        billingClient = BillingClient.newBuilder(activity).setListener(this).build();
    }

    /**
     * The method should remain private and public methods should be created in this class
     * for exposing different functionality for the billing client
     */
    private void executeOnConnection(@NonNull final Runnable runnable) {
        if (isClientConnecting) {
            Log.i(TAG, "Client is already connecting, adding the current " +
                    "runnable in the queue");
            // we add the runnable to a queue, to be executed after the client is connected
            runnableQueue.offer(runnable);
        } else if (billingClient.isReady()) {
            runnable.run();
        } else {
            Log.i(TAG, "executeOnConnection() -> Starting new connection");
            isClientConnecting = true;
            billingClient.startConnection(new BillingClientStateListener() {
                @Override
                public void onBillingSetupFinished(int responseCode) {
                    isClientConnecting = false;
                    if (responseCode == BillingClient.BillingResponse.OK) {
                        Log.i(TAG, "onBillingSetupFinished() response: " + responseCode);
                        runnable.run();
                        // if Runnables were added to the queue while the client was in the
                        // "connecting" process, run them as well, in the order they were submitted
                        executeQueuedRunnables();
                    } else {
                        Log.w(TAG, "onBillingSetupFinished() error code: " + responseCode);
                        runnableQueue.clear();
                    }
                }

                @Override
                public void onBillingServiceDisconnected() {
                    isClientConnecting = false;
                    runnableQueue.clear();
                    Log.w(TAG, "onBillingServiceDisconnected()");
                }
            });
        }
    }

    private void executeQueuedRunnables() {
        Log.i(TAG, "executeQueuedRunnables() -> " + runnableQueue.size() + " Runnables");
        while (runnableQueue.peek() != null) {
            runnableQueue.poll().run();
        }
    }

    public void startPurchaseFlow(String skuId) {
        Log.i(TAG, "startPurchaseFlow(" + skuId + ")");
        final SkuDetails skuDetails = skuDetailsMap.get(skuId);
        if (skuDetails == null) {
            Log.w(TAG, "skuDetails is null", new Throwable());
            return;
        }
        executeOnConnection(new Runnable() {
            @Override
            public void run() {
                BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                        .setSkuDetails(skuDetails)
                        .build();
                billingClient.launchBillingFlow(activity, billingFlowParams);
            }
        });
    }

    public SkuDetails getSkuDetails(String sku) {
        return skuDetailsMap.get(sku);
    }

    public void addSkuDetails(@NonNull List<SkuDetails> skuDetailsList) {
        for (SkuDetails sd : skuDetailsList) {
            skuDetailsMap.put(sd.getSku(), sd);
        }
    }

    @Override
    public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {
        Log.i(TAG, "onPurchasesUpdated() response: " + responseCode);
    }

    public void querySkuGetDetailsAsync(@BillingClient.SkuType final String skuType,
                                         final List<String> skuList,
                                         final SkuDetailsResponseListener listener) {
        executeOnConnection(new Runnable() {
            @Override
            public void run() {
                SkuDetailsParams skuDetailsParams = SkuDetailsParams.newBuilder().setSkusList(skuList)
                        .setType(skuType).build();
                billingClient.querySkuDetailsAsync(skuDetailsParams, listener);
            }
        });
    }

    public void endConnection() {
        Log.i(TAG, "endConnection()");
        billingClient.endConnection();
    }
}
