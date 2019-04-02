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
package com.codelab.skulist;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codelab.billing.Skus;
import com.codelab.sample.R;
import com.codelab.billing.BillingProvider;
import com.codelab.skulist.row.RowViewHolder;
import com.codelab.skulist.row.SkuRowData;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for a RecyclerView that shows SKU details for the app.
 * <p>
 *     Note: It's done fragment-specific logic independent and delegates control back to the
 *     specified handler (implemented inside AcquireFragment in this example)
 * </p>
 */
public class SkusAdapter extends RecyclerView.Adapter<RowViewHolder>
        implements RowViewHolder.OnButtonClickListener {
    private List<SkuRowData> mListData;
    private BillingProvider mBillingProvider;

    public SkusAdapter(BillingProvider billingProvider) {
        mBillingProvider = billingProvider;
        setHasStableIds(true);
    }

    public void clearData() {
        mListData = null;
        notifyDataSetChanged();
    }

    public void updateData(List<SkuRowData> data) {
        if (mListData == null) {
            mListData = new ArrayList<>();
        }
        mListData.addAll(data);
        notifyDataSetChanged();
    }

    public void setData(List<SkuRowData> data) {
        mListData = data;
        notifyDataSetChanged();
    }

    @Override
    public RowViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sku_details_row, parent, false);
        return new RowViewHolder(item, this);
    }

    @Override
    public void onBindViewHolder(RowViewHolder holder, int position) {
        SkuRowData data = getData(position);
        if (data != null) {
            holder.title.setText(data.getTitle());
            holder.description.setText(data.getDescription());
            holder.price.setText(data.getPrice());
            holder.button.setEnabled(true);
        }
        switch (data.getSku()) {
            case Skus.SKU_INAPP_GAS:
                holder.skuIcon.setImageResource(R.drawable.gas_icon);
                break;
            case Skus.SKU_INAPP_PREMIUM:
                holder.skuIcon.setImageResource(R.drawable.premium_icon);
                break;
            case Skus.SKU_SUB_GOLD_MONTHLY:
            case Skus.SKU_SUB_GOLD_YEARLY:
                holder.skuIcon.setImageResource(R.drawable.gold_icon);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mListData == null ? 0 : mListData.size();
    }

    @Override
    public void onButtonClicked(int position) {
        SkuRowData data = getData(position);
        mBillingProvider.getBillingManager().startPurchaseFlow(data.getSku());
    }

    private SkuRowData getData(int position) {
        return mListData == null ? null : mListData.get(position);
    }

    @Override
    public long getItemId(int position) {
        if (mListData == null || mListData.size() <= position) {
            return super.getItemId(position);
        }
        return mListData.get(position).hashCode();
    }
}

