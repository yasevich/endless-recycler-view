/*
 * Copyright 2015 Slava Yasevich
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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.yasevich.endlessrecyclerview.EndlessRecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Slava Yasevich
 */
public final class MainActivity extends Activity implements EndlessRecyclerView.Pager {
    
    private static final long DELAY = 1000L;

    private List<Integer> data = generateData();
    private final Adapter adapter = new Adapter(data);
    private final Handler handler = new Handler();

    private EndlessRecyclerView list;

    private boolean loadingBottom = false;
    private boolean loadingTop = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        list = (EndlessRecyclerView) findViewById(android.R.id.list);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setProgressView(R.layout.item_progress);
        list.setAdapter(adapter);
        list.setPager(this);
        list.scrollToPosition(15);
    }

    private List<Integer> generateData() {
        List<Integer> initialData = new ArrayList<>(20);
        for (int i = 0; i < 20; i++) {
            initialData.add(randomInt());
        }
        return initialData;
    }

    private Integer randomInt() {
        Random rand = new Random();
        return rand.nextInt(1000);
    }

    @Override
    public boolean shouldLoadBottom() {
        return !loadingBottom;
    }

    @Override
    public void loadNextBottomPage() {
        loadingBottom = true;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                list.setRefreshingBottom(false);
                loadingBottom = false;
                adapter.addBottom(generateData());
            }
        }, DELAY);
    }

    @Override
    public boolean shouldLoadTop() {
        return !loadingTop;
    }

    @Override
    public void loadNextTopPage() {
        loadingTop = true;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                list.setRefreshingTop(false);
                loadingTop = false;
                adapter.addTop(generateData());
            }
        }, DELAY);
    }

    private static final class Adapter extends RecyclerView.Adapter<ViewHolder> {

        private final List<Integer> data;

        public Adapter(List<Integer> data) {
            this.data = data;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.text.setText("Generated item: " + (data.get(position)));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public void addBottom(List<Integer> newBottomData) {
            int oldSize = data.size();
            data.addAll(newBottomData);
            notifyItemRangeChanged(oldSize, newBottomData.size() - 1);
        }

        public void addTop(List<Integer> newTopData) {
            data.addAll(0, newTopData);
            notifyItemRangeChanged(0, newTopData.size() - 1);
        }
    }

    private static final class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView text;

        public ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(android.R.layout.simple_list_item_1, parent, false));
            text = (TextView) itemView.findViewById(android.R.id.text1);
        }
    }
}
