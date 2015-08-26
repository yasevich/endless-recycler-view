package com.github.yasevich.endlessrecyclerview;

import android.support.v7.widget.RecyclerView;

/**
 * Created by Asha on 15-7-30.
 * Asha ashqalcn@gmail.com
 *
 * moved from EndlessRecyclerView
 */
public abstract class EndlessScrollListener  extends RecyclerView.OnScrollListener
{
    public static final int LINEAR = 1;
    public static final int STAGGERED = 2;

    private final EndlessRecyclerView.Pager pager;
    private final EndlessRecyclerView endlessRecyclerView;
    protected int threshold = 0;

    public EndlessScrollListener(EndlessRecyclerView endlessRecyclerView,EndlessRecyclerView.Pager pager) {
        if (pager == null) {
            throw new NullPointerException("pager is null");
        }
        this.endlessRecyclerView = endlessRecyclerView;
        this.pager = pager;
    }

    protected EndlessRecyclerView.Pager getPager() {
        return pager;
    }

    protected void setThreshold(int threshold) {
        if (threshold <= 0) {
            throw new IllegalArgumentException("illegal threshold: " + threshold);
        }
        this.threshold = threshold;
    }

    protected void setRefreshing(boolean refreshing)
    {
        if (endlessRecyclerView != null)
            endlessRecyclerView.setRefreshing(refreshing);
    }

    protected boolean isRefreshing()
    {
        if ( endlessRecyclerView == null ) return false;
        return endlessRecyclerView.isRefreshing();
    }
    protected RecyclerView.LayoutManager getLayoutManager()
    {
        if (endlessRecyclerView == null) return null;
        return endlessRecyclerView.getLayoutManager();
    }

    protected RecyclerView.Adapter getAdapter()
    {
        if (endlessRecyclerView == null) return null;
        return endlessRecyclerView.getAdapter();
    }

    public static EndlessScrollListener newInstance(int layoutType
            , EndlessRecyclerView context
            , EndlessRecyclerView.Pager pager)
    {
        switch (layoutType)
        {
            case LINEAR:
                return new LinearLayoutScrollListener(context,pager);
            case STAGGERED:
                return new StaggeredLayoutScrollListener(context,pager);
        }
        return null;
    }
}
