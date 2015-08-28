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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.yasevich.endlessrecyclerview.EndlessRecyclerView;

/**
 * @author Slava Yasevich
 */
public final class MainActivity extends Activity implements EndlessRecyclerView.Pager {

    public static final int LAYOUT_LINEAR = 0;
    public static final int LAYOUT_STAGGERED = 1;

    private static final int ITEMS_ON_PAGE = 8;
    private static final int TOTAL_PAGES = 10;
    private static final long DELAY = 1000L;
    private static final String PARAM_LAYOUT = "PARAM_LAYOUT";

    private final Adapter adapter = new Adapter();
    private final Handler handler = new Handler();

    private EndlessRecyclerView list;
    private boolean loading = false;


    public static void lanuchActivity(Context context,int type)
    {
        Intent i = new Intent(context,MainActivity.class);
        i.putExtra(PARAM_LAYOUT,type);
        context.startActivity(i);
    }

    private static int intent2type(Intent intent)
    {
        if ( intent != null ) return intent.getIntExtra(PARAM_LAYOUT,0);
        else return 0;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView.LayoutManager layoutManager;
        int type = intent2type(getIntent());
        switch (type)
        {

            case LAYOUT_STAGGERED:
                layoutManager = new StaggeredGridLayoutManager(3
                        , StaggeredGridLayoutManager.VERTICAL);
                break;
            case LAYOUT_LINEAR:
            default:
                layoutManager = new LinearLayoutManager(this);
                break;
        }

        list = (EndlessRecyclerView) findViewById(android.R.id.list);
        list.setLayoutManager(layoutManager);
        list.setProgressView(R.layout.item_progress);
        list.setAdapter(adapter);
        list.setPager(this);

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

    private void addItems() {
        adapter.setCount(adapter.getItemCount() + ITEMS_ON_PAGE);
    }



    private static final class Adapter extends RecyclerView.Adapter<ViewHolder> {

        private int count;
        private static final String[] dummy = {"To be, or not to be: that is the question."
                , "Whether 'tis nobler in the mind to suffer The slings and arrows of outrageous fortune, "
                , "Or to take arms against a sea of troubles, And by opposing end them."
                , "To die: to sleep; No more; and by a sleep to say we end The heart-ache, "
                , "and the thousand natural shocks That flesh is heir to"
                , "'tis a consummation Devoutly to be wish'd. To die, to sleep;"
                , "To sleep: perchance to dream: aye, there's the rub; "
                , "For in that sleep of death what dreams may come, When we have shuffled off this mortal coil, "
                , "Must give us pause: there's the respect That makes calamity of so long life;"
            };

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.text.setText( dummy[position % dummy.length]
                    + "\nItem: " + (position + 1) + "\n");
        }

        @Override
        public int getItemCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
            notifyDataSetChanged();
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
