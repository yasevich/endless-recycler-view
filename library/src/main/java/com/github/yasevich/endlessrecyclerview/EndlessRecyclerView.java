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

package com.github.yasevich.endlessrecyclerview;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code EndlessRecyclerView} lets you to load new pages when a user scrolls down to the bottom of
 * a list. If no {@link Pager} provided {@code EndlessRecyclerView} behaves as {@link RecyclerView}.
 * <p>
 * {@code EndlessRecyclerView} supports only {@link LinearLayoutManager} and its subclasses.
 * <p>
 * Implement {@link Pager} interface to determine when {@code EndlessRecyclerView} should start
 * loading process and a way to perform async operation. Use {@link #setPager(Pager)} method to set
 * or reset current pager. When async operation complete you may want to call
 * {@link #setRefreshingPrev(boolean)} or {@link #setRefreshingNext(boolean)} method to hide
 * progress view if it was provided.
 * <p>
 * By default {@code EndlessRecyclerView} starts loading operation when you are at the very bottom
 * of a list but you can opt this behaviour using {@link #setThreshold(int)} method.
 * <p>
 * If you want to show progress on the top or bottom of a list you may set a progress view using
 * {@link #setProgressView(int)}.
 * You should keep in mind that in order to show progress view on the bottom of
 * {@code EndlessRecyclerView} it will wrap provided adapter and add new
 * {@link ViewHolder}'s view type. Its value is -1.
 * <p>
 * If you need to set {@link OnScrollListener} with this view you must use
 * {@link #addOnScrollListener(OnScrollListener)} and
 * {@link #removeOnScrollListener(OnScrollListener)} methods instead of
 * {@link #setOnScrollListener(OnScrollListener)}. Calling
 * {@link #setOnScrollListener(OnScrollListener)} will cause {@link UnsupportedOperationException}.
 * <p>
 * If you use {@link Adapter} with stable ids and want to show progress view, you
 * should keep in mind that view holder of progress view will have {@code NO_ID}.
 *
 */
public final class EndlessRecyclerView extends RecyclerView {

    private final List<RecyclerView.OnScrollListener> onScrollListeners = new ArrayList<>();

    private EndlessScrollListener endlessScrollListener;
    private AdapterWrapper adapterWrapper;

    private boolean refreshingNext;
    private boolean refreshingPrev;

    private int threshold = 1;

    private Integer progressViewResId = null;

    public EndlessRecyclerView(Context context) {
        this(context, null);
    }

    public EndlessRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EndlessRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        super.setOnScrollListener(new OnScrollListenerImpl());
    }

    @Override
    public void setAdapter(Adapter adapter) {
        //noinspection unchecked
        adapterWrapper = new AdapterWrapper(adapter);
        super.setAdapter(adapterWrapper);
    }

    @Override
    public Adapter getAdapter() {
        return adapterWrapper;
    }

    /**
     * Use {@link #addOnScrollListener(OnScrollListener)} and
     * {@link #removeOnScrollListener(OnScrollListener)} methods instead. Calling this method will
     * cause {@link UnsupportedOperationException}.
     */
    @Override
    public void setOnScrollListener(OnScrollListener listener) {
        throw new UnsupportedOperationException("use addOnScrollListener(OnScrollListener) and " +
                "removeOnScrollListener(OnScrollListener) instead");
    }

    /**
     * @param layout instances of {@link LinearLayoutManager} only
     */
    @Override
    public void setLayoutManager(LayoutManager layout) {
        if (layout instanceof LinearLayoutManager) {
            super.setLayoutManager(layout);
        } else {
            throw new IllegalArgumentException(
                    "layout manager must be an instance of LinearLayoutManager");
        }
    }

    @Override
    public LinearLayoutManager getLayoutManager() {
        return (LinearLayoutManager) super.getLayoutManager();
    }

    /**
     * Adds {@link OnScrollListener} to use with this view.
     *
     * @param listener listener to add
     */
    public void addOnScrollListener(OnScrollListener listener) {
        if (listener == null) {
            throw new NullPointerException("listener is null");
        }
        onScrollListeners.add(listener);
    }

    /**
     * Removes {@link OnScrollListener} to use with this view.
     *
     * @param listener listener to remove
     */
    public void removeOnScrollListener(OnScrollListener listener) {
        if (listener == null) {
            throw new NullPointerException("listener is null");
        }
        onScrollListeners.remove(listener);
    }

    /**
     * Sets {@link EndlessRecyclerView.Pager} to use with the view.
     *
     * @param pager pager to set or {@code null} to clear current pager
     */
    public void setPager(Pager pager) {
        if (pager != null) {
            endlessScrollListener = new EndlessScrollListener(pager);
            endlessScrollListener.setThreshold(threshold);
            addOnScrollListener(endlessScrollListener);
        } else if (endlessScrollListener != null) {
            removeOnScrollListener(endlessScrollListener);
            endlessScrollListener = null;
        }
    }

    /**
     * Sets threshold to use. Only positive numbers are allowed. This value is used to determine if
     * loading should start when user scrolls the view down. Default value is 1.
     *
     * @param threshold positive number
     */
    public void setThreshold(int threshold) {
        this.threshold = threshold;
        if (endlessScrollListener != null) {
            endlessScrollListener.setThreshold(threshold);
        }
    }

    /**
     * Sets progress view to show on the top or bottom of the list when loading starts.
     *
     * @param viewResId view resource ID
     */
    public void setProgressView(int viewResId) {
        progressViewResId = viewResId;
    }

    public boolean isRefreshingPrev() {
        return refreshingPrev;
    }

    /**
     * If async operation completed you may want to call this method to hide progress view.
     *
     * @param refreshingNext {@code true} if list is currently refreshingNext, {@code false} otherwise
     */
    public void setRefreshingNext(boolean refreshingNext) {
        if (this.refreshingNext == refreshingNext) {
            return;
        }
        this.refreshingNext = refreshingNext;
        if (!refreshingNext)
            this.adapterWrapper.notifyItemRemoved(adapterWrapper.getItemCount() - 1);
        else
            this.adapterWrapper.notifyItemRangeInserted(adapterWrapper.getItemCount() - 1, 1);
    }

    public boolean isRefreshingNext() {
        return refreshingNext;
    }

    /**
     * If async operation completed you may want to call this method to hide progress view.
     *
     * @param refreshingPrev {@code true} if list is currently refreshingPrev, {@code false} otherwise
     */
    public void setRefreshingPrev(boolean refreshingPrev) {
        if (this.refreshingPrev == refreshingPrev) {
            return;
        }
        this.refreshingPrev = refreshingPrev;
        if (!refreshingPrev)
            this.adapterWrapper.notifyItemRemoved(0);
        else
            this.adapterWrapper.notifyItemRangeInserted(0, 1);
    }

    private final class OnScrollListenerImpl extends OnScrollListener {

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            for (OnScrollListener listener : onScrollListeners) {
                listener.onScrolled(recyclerView, dx, dy);
            }
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            for (OnScrollListener listener : onScrollListeners) {
                listener.onScrollStateChanged(recyclerView, newState);
            }
        }
    }

    private final class EndlessScrollListener extends OnScrollListener {

        private final Pager pager;

        private int threshold = 1;

        public EndlessScrollListener(Pager pager) {
            if (pager == null) {
                throw new NullPointerException("pager is null");
            }
            this.pager = pager;
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            int lastVisibleItemPosition = getLayoutManager().findLastVisibleItemPosition();
            int lastItemPosition = getAdapter().getItemCount();

            if (lastItemPosition - lastVisibleItemPosition <= threshold && !refreshingNext && pager.shouldLoadNext()) {
                setRefreshingNext(true);
                pager.loadNextPage();
            }

            int firstVisibleItemPosition = getLayoutManager().findFirstVisibleItemPosition();
            int firstItemPosition = 0;

            if (firstVisibleItemPosition - firstItemPosition <= threshold && !refreshingPrev && pager.shouldLoadPrev()) {
                setRefreshingPrev(true);
                pager.loadPrevPage();
            }
        }

        public void setThreshold(int threshold) {
            if (threshold <= 0) {
                throw new IllegalArgumentException("illegal threshold: " + threshold);
            }
            this.threshold = threshold;
        }
    }

    private final class AdapterWrapper extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int PROGRESS_VIEW_TYPE = -1;

        private final RecyclerView.Adapter<RecyclerView.ViewHolder> adapter;

        private List<ProgressViewHolder> progressViewHolders = new ArrayList<>();

        public AdapterWrapper(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
            if (adapter == null) {
                throw new NullPointerException("adapter is null");
            }
            this.adapter = adapter;
            setHasStableIds(adapter.hasStableIds());
        }

        @Override
        public int getItemCount() {
            int bottom = refreshingNext && progressViewResId != null ? 1 : 0;
            int top = refreshingPrev && progressViewResId != null ? 1 : 0;
            return adapter.getItemCount() + top + bottom;
        }

        @Override
        public long getItemId(int position) {
            if (position == 0) {
                return refreshingPrev ? NO_ID : adapter.getItemId(0);
            } else {
                if (refreshingPrev) {
                    return position - 1 >= adapter.getItemCount() ? NO_ID : adapter.getItemId(position - 1);
                } else {
                    return position >= adapter.getItemCount() ? NO_ID : adapter.getItemId(position);
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (refreshingPrev) {
                return ((refreshingNext && position - 1 >= adapter.getItemCount()) || position == 0) ? PROGRESS_VIEW_TYPE : adapter.getItemViewType(position - 1);
            } else {
                return ((refreshingNext && position >= adapter.getItemCount())) ? PROGRESS_VIEW_TYPE : adapter.getItemViewType(position);
            }
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
            adapter.onAttachedToRecyclerView(recyclerView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (refreshingPrev) {
                if (position != 0 && position - 1 < adapter.getItemCount()) {
                    adapter.onBindViewHolder(holder, position - 1);
                }
            } else {
                if (position < adapter.getItemCount()) {
                    adapter.onBindViewHolder(holder, position);
                }
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == PROGRESS_VIEW_TYPE) {
                View view = LayoutInflater.from(getContext()).inflate(progressViewResId, parent, false);
                ProgressViewHolder holder = new ProgressViewHolder(view);
                progressViewHolders.add(holder);
                return holder;
            } else {
                return adapter.onCreateViewHolder(parent, viewType);
            }
        }

        @Override
        public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
            super.onDetachedFromRecyclerView(recyclerView);
            adapter.onDetachedFromRecyclerView(recyclerView);
        }

        @Override
        public boolean onFailedToRecycleView(ViewHolder holder) {
            return progressViewHolders.contains(holder) || adapter.onFailedToRecycleView(holder);
        }

        @Override
        public void onViewAttachedToWindow(ViewHolder holder) {
            if (!progressViewHolders.contains(holder)) {
                adapter.onViewAttachedToWindow(holder);
            }
        }

        @Override
        public void onViewDetachedFromWindow(ViewHolder holder) {
            if (!progressViewHolders.contains(holder)) {
                adapter.onViewDetachedFromWindow(holder);
            }
        }

        @Override
        public void onViewRecycled(ViewHolder holder) {
            if (!progressViewHolders.contains(holder)) {
                adapter.onViewRecycled(holder);
            }
        }

        @Override
        public void registerAdapterDataObserver(AdapterDataObserver observer) {
            super.registerAdapterDataObserver(observer);
            adapter.registerAdapterDataObserver(observer);
        }

        @Override
        public void unregisterAdapterDataObserver(AdapterDataObserver observer) {
            super.unregisterAdapterDataObserver(observer);
            adapter.unregisterAdapterDataObserver(observer);
        }

        public RecyclerView.Adapter<RecyclerView.ViewHolder> getAdapter() {
            return adapter;
        }

        private final class ProgressViewHolder extends ViewHolder {
            public ProgressViewHolder(View view) {
                super(view);
            }
        }
    }

    /**
     * Pager interface.
     */
    public interface Pager {
        /**
         * @return {@code true} if pager should load next page
         */
        boolean shouldLoadNext();

        /**
         * Starts loading next page.
         */
        void loadNextPage();

        /**
         * @return {@code true} if pager should load new previous page
         */
        boolean shouldLoadPrev();

        /**
         * Starts loading previous page.
         */
        void loadPrevPage();
    }
}

