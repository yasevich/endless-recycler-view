package com.github.yasevich.endlessrecyclerview;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by Asha on 15-7-30.
 * Asha ashqalcn@gmail.com
 */
public final class LinearLayoutScrollListener extends EndlessScrollListener
{

    private LinearLayoutManager mLayoutManager;

    public LinearLayoutScrollListener(EndlessRecyclerView context
            ,EndlessRecyclerView.Pager pager) {
        super(context,pager);
        mLayoutManager = (LinearLayoutManager) getLayoutManager();
    }
    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy)
    {
        if (isRefreshing()) return;
        int lastVisibleItemPosition = mLayoutManager
                .findLastVisibleItemPosition();
        int lastItemPosition = mLayoutManager.getItemCount();
        if (getPager().shouldLoad()
                && lastItemPosition - lastVisibleItemPosition <= threshold) {
            setRefreshing(true);
            getPager().loadNextPage();
        }
    }
}
