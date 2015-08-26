package com.github.yasevich.endlessrecyclerview;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

/**
 * Created by Asha on 15-7-30.
 * Asha ashqalcn@gmail.com
 *
 */
public final class StaggeredLayoutScrollListener extends EndlessScrollListener
{

    private int pastVisibleItems;
    private int[] firstVisibleItems;
    private StaggeredGridLayoutManager mLayoutManager;
    private int spanCount;

    public StaggeredLayoutScrollListener(EndlessRecyclerView context
            ,EndlessRecyclerView.Pager pager) {
        super(context,pager);
        mLayoutManager = (StaggeredGridLayoutManager) getLayoutManager();
        spanCount = mLayoutManager.getSpanCount();
    }
    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy)
    {
        if (isRefreshing()) return;
        int visibleItemCount = mLayoutManager.getChildCount();
        int totalItemCount = mLayoutManager.getItemCount();
        firstVisibleItems = mLayoutManager.findFirstVisibleItemPositions(firstVisibleItems);
        if(firstVisibleItems != null && firstVisibleItems.length > 0)
        {
            pastVisibleItems = firstVisibleItems[0];
            for (int i = 1 ; i < spanCount ; i++ )
            {
                pastVisibleItems = pastVisibleItems > firstVisibleItems[i]
                        ? pastVisibleItems : firstVisibleItems[i];
            }
        }

        if (getPager().shouldLoad()
                && (visibleItemCount + pastVisibleItems - spanCount + threshold ) >= totalItemCount)
        {
            setRefreshing(true);
            getPager().loadNextPage();
        }
    }

}
