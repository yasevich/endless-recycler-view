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
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * {@code EndlessRecyclerView} lets you load new pages when a user scrolls down to the bottom of a
 * list. If no {@link Pager} provided {@code EndlessRecyclerView} behaves as {@link RecyclerView}.
 * <p>
 * {@code EndlessRecyclerView} supports {@link LinearLayoutManager} and its subclasses and
 * {@link StaggeredGridLayoutManager} and its subclasses.
 * <p>
 * Implement {@link Pager} interface to determine when {@code EndlessRecyclerView} should start
 * loading process and a way to perform async operation. Use {@link #setPager(Pager)} method to set
 * or reset current pager. When async operation complete you may want to call
 * {@link #setRefreshing(boolean)} method to hide progress view if it was provided.
 * <p>
 * By default {@code EndlessRecyclerView} starts loading operation when you are at the very bottom
 * of a list but you can opt this behaviour using {@link #setThreshold(int)} method.
 * <p>
 * If you want to show progress on the bottom of a list you may set a progress view using
 * {@link #setProgressView(int)} or {@link #setProgressView(View)} methods. You should keep in mind
 * that in order to show progress view on the bottom of {@code EndlessRecyclerView} it will wrap
 * provided adapter and add new {@link RecyclerView.ViewHolder}'s view type. Its value is -1.
 * <p>
 * If you need to set {@link RecyclerView.OnScrollListener} with this view you must use
 * {@link #addOnScrollListener(OnScrollListener)} and
 * {@link #removeOnScrollListener(OnScrollListener)} methods instead of
 * {@link #setOnScrollListener(OnScrollListener)}. Calling
 * {@link #setOnScrollListener(OnScrollListener)} will cause {@link UnsupportedOperationException}.
 * <p>
 * If you use {@link RecyclerView.Adapter} with stable ids and want to show progress view, you
 * should keep in mind that view holder of progress view will have {@link #NO_ID}.
 *
 * @author Slava Yasevich
 */
public final class EndlessRecyclerView extends RecyclerView {

    private final Handler handler = new Handler();
    private final Runnable notifyDataSetChangedRunnable = new Runnable() {
        @Override
        public void run() {
            adapterWrapper.notifyDataSetChanged();
        }
    };

    private EndlessScrollListener endlessScrollListener;
    private LayoutManagerWrapper layoutManagerWrapper;
    private AdapterWrapper adapterWrapper;
    private View progressView;
    private boolean refreshing;
    private int threshold = 1;

    public EndlessRecyclerView(Context context) {
        this(context, null);
    }

    public EndlessRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EndlessRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        //noinspection unchecked
        adapterWrapper = new AdapterWrapper(adapter);
        super.setAdapter(adapterWrapper);
    }

    @Override
    public Adapter getAdapter() {
        return adapterWrapper.getAdapter();
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
    public void setLayoutManager(@Nullable LayoutManager layout) {
        layoutManagerWrapper = layout == null ? null : new LayoutManagerWrapper(layout);
        super.setLayoutManager(layout);
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
     * Sets progress view to show on the bottom of the list when loading starts.
     *
     * @param layoutResId layout resource ID
     */
    public void setProgressView(int layoutResId) {
        setProgressView(LayoutInflater
                .from(getContext())
                .inflate(layoutResId, this, false));
    }

    /**
     * Sets progress view to show on the bottom of the list when loading starts.
     *
     * @param view the view
     */
    public void setProgressView(View view) {
        progressView = view;
    }

    /**
     * If async operation completed you may want to call this method to hide progress view.
     *
     * @param refreshing {@code true} if list is currently refreshing, {@code false} otherwise
     */
    public void setRefreshing(boolean refreshing) {
        if (this.refreshing == refreshing) {
            return;
        }
        this.refreshing = refreshing;
        notifyDataSetChanged();
    }

    private void notifyDataSetChanged() {
        if (isComputingLayout()) {
            handler.post(notifyDataSetChangedRunnable);
        } else {
            adapterWrapper.notifyDataSetChanged();
        }
    }

    private static final class LayoutManagerWrapper {

        @NonNull
        final LayoutManager layoutManager;

        @NonNull
        private final LayoutManagerResolver resolver;

        public LayoutManagerWrapper(@NonNull LayoutManager layoutManager) {
            this.layoutManager = layoutManager;
            this.resolver = getResolver(layoutManager);
        }

        @NonNull
        private static LayoutManagerResolver getResolver(@NonNull LayoutManager layoutManager) {
            if (layoutManager instanceof LinearLayoutManager) {
                return new LayoutManagerResolver() {
                    @Override
                    public int findLastVisibleItemPosition(@NonNull LayoutManager layoutManager) {
                        return ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                    }
                };
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                return new LayoutManagerResolver() {
                    @Override
                    public int findLastVisibleItemPosition(@NonNull LayoutManager layoutManager) {
                        int[] lastVisibleItemPositions =
                                ((StaggeredGridLayoutManager) layoutManager)
                                        .findLastVisibleItemPositions(null);
                        int lastVisibleItemPosition = lastVisibleItemPositions[0];
                        for (int i = 1; i < lastVisibleItemPositions.length; ++i) {
                            if (lastVisibleItemPosition < lastVisibleItemPositions[i]) {
                                lastVisibleItemPosition = lastVisibleItemPositions[i];
                            }
                        }
                        return lastVisibleItemPosition;
                    }
                };
            } else {
                throw new IllegalArgumentException("unsupported layout manager: " + layoutManager);
            }
        }

        public int findLastVisibleItemPosition() {
            return resolver.findLastVisibleItemPosition(layoutManager);
        }

        private interface LayoutManagerResolver {
            int findLastVisibleItemPosition(@NonNull LayoutManager layoutManager);
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
            int lastVisibleItemPosition = layoutManagerWrapper.findLastVisibleItemPosition();
            int lastItemPosition = getAdapter().getItemCount();

            if (pager.shouldLoad() && lastItemPosition - lastVisibleItemPosition <= threshold) {
                setRefreshing(true);
                pager.loadNextPage();
            }
        }

        public void setThreshold(int threshold) {
            if (threshold <= 0) {
                throw new IllegalArgumentException("illegal threshold: " + threshold);
            }
            this.threshold = threshold;
        }
    }

    private final class AdapterWrapper extends Adapter<ViewHolder> {

        private static final int PROGRESS_VIEW_TYPE = -1;

        private final Adapter<ViewHolder> adapter;

        private ProgressViewHolder progressViewHolder;

        public AdapterWrapper(Adapter<ViewHolder> adapter) {
            if (adapter == null) {
                throw new NullPointerException("adapter is null");
            }
            this.adapter = adapter;
            setHasStableIds(adapter.hasStableIds());
        }

        @Override
        public int getItemCount() {
            return adapter.getItemCount() + (refreshing && progressView != null ? 1 : 0);
        }

        @Override
        public long getItemId(int position) {
            return position == adapter.getItemCount() ? NO_ID : adapter.getItemId(position);
        }

        @Override
        public int getItemViewType(int position) {
            return refreshing & position == adapter.getItemCount() ? PROGRESS_VIEW_TYPE :
                    adapter.getItemViewType(position);
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
            adapter.onAttachedToRecyclerView(recyclerView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (position < adapter.getItemCount()) {
                adapter.onBindViewHolder(holder, position);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return viewType == PROGRESS_VIEW_TYPE ? progressViewHolder = new ProgressViewHolder() :
                    adapter.onCreateViewHolder(parent, viewType);
        }

        @Override
        public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
            super.onDetachedFromRecyclerView(recyclerView);
            adapter.onDetachedFromRecyclerView(recyclerView);
        }

        @Override
        public boolean onFailedToRecycleView(ViewHolder holder) {
            return holder == progressViewHolder || adapter.onFailedToRecycleView(holder);
        }

        @Override
        public void onViewAttachedToWindow(ViewHolder holder) {
            if (holder == progressViewHolder) {
                return;
            }
            adapter.onViewAttachedToWindow(holder);
        }

        @Override
        public void onViewDetachedFromWindow(ViewHolder holder) {
            if (holder == progressViewHolder) {
                return;
            }
            adapter.onViewDetachedFromWindow(holder);
        }

        @Override
        public void onViewRecycled(ViewHolder holder) {
            if (holder == progressViewHolder) {
                return;
            }
            adapter.onViewRecycled(holder);
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

        public Adapter<ViewHolder> getAdapter() {
            return adapter;
        }

        private final class ProgressViewHolder extends ViewHolder {
            public ProgressViewHolder() {
                super(progressView);
            }
        }
    }

    /**
     * Pager interface.
     */
    public interface Pager {
        /**
         * @return {@code true} if pager should load new page
         */
        boolean shouldLoad();

        /**
         * Starts loading operation.
         */
        void loadNextPage();
    }
}
