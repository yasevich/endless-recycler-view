/*
 * Copyright 2018 Slava Yasevich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.yasevich.endlessrecyclerview.sample;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.yasevich.endlessrecyclerview.EndlessRecyclerView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

/**
 * @author Slava Yasevich
 */
public final class MainActivity extends Activity implements EndlessRecyclerView.Pager {

    private static final int ITEMS_ON_PAGE = 8;
    private static final int TOTAL_PAGES = 10;
    private static final long DELAY = 1000L;

    private final Adapter adapter = new Adapter();
    private final Handler handler = new Handler();

    private EndlessRecyclerView list;
    private boolean loading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        list = findViewById(android.R.id.list);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setProgressView(R.layout.item_progress);
        list.setAdapter(adapter);
        list.setPager(this);

        findViewById(android.R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateLayoutManager(new LinearLayoutManager(v.getContext()));
            }
        });

        findViewById(android.R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final GridLayoutManager manager = new GridLayoutManager(v.getContext(), 3);

                // we want progress view to fill entire row
                manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        return list.isRefreshing() && position == manager.getItemCount() ?
                                manager.getSpanCount() : 1;
                    }
                });

                updateLayoutManager(manager);
            }
        });

        findViewById(android.R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateLayoutManager(new StaggeredGridLayoutManager(
                        3, StaggeredGridLayoutManager.VERTICAL));
            }
        });

        addItems();
    }

    @Override
    public boolean shouldLoad() {
        return !loading && adapter.getItemCount() / ITEMS_ON_PAGE < TOTAL_PAGES;
    }

    @Override
    public void loadNextPage() {
        loading = true;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                list.setRefreshing(false);
                loading = false;
                addItems();
            }
        }, DELAY);
    }

    private void updateLayoutManager(@NonNull RecyclerView.LayoutManager layoutManager) {
        list.setLayoutManager(layoutManager);
        adapter.setCount(0);
        addItems();
    }

    private void addItems() {
        adapter.setCount(adapter.getItemCount() + ITEMS_ON_PAGE);
    }

    private static final class Adapter extends RecyclerView.Adapter<ViewHolder> {

        private int count;

        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.text.setText(String.format("Item: %1$s", position + 1));
        }

        @Override
        public int getItemCount() {
            return count;
        }

        void setCount(int count) {
            this.count = count;
            notifyDataSetChanged();
        }
    }

    private static final class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView text;

        ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(android.R.layout.simple_list_item_1, parent, false));
            text = itemView.findViewById(android.R.id.text1);
        }
    }
}
