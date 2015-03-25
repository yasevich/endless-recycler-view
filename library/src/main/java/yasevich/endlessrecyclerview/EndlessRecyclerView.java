package yasevich.endlessrecyclerview;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Slava Yasevich
 */
public final class EndlessRecyclerView extends RecyclerView {

    private final List<OnScrollListener> onScrollListeners = new ArrayList<>();

    private EndlessScrollListener endlessScrollListener;
    private AdapterWrapper adapterWrapper;
    private View progressView;
    private boolean refreshing;

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
        return adapterWrapper.getAdapter();
    }

    @Override
    public void setOnScrollListener(OnScrollListener listener) {
        throw new UnsupportedOperationException("use addOnScrollListener(OnScrollListener) and " +
                "removeOnScrollListener(OnScrollListener) instead");
    }

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

    public void addOnScrollListener(OnScrollListener listener) {
        if (listener == null) {
            throw new NullPointerException("listener is null");
        }
        onScrollListeners.add(listener);
    }

    public void removeOnScrollListener(OnScrollListener listener) {
        if (listener == null) {
            throw new NullPointerException("listener is null");
        }
        onScrollListeners.remove(listener);
    }

    public void setPager(Pager pager) {
        if (pager != null) {
            endlessScrollListener = new EndlessScrollListener(pager);
            addOnScrollListener(endlessScrollListener);
        } else if (endlessScrollListener != null) {
            removeOnScrollListener(endlessScrollListener);
            endlessScrollListener = null;
        }
    }

    public void setThreshold(int threshold) {
        if (endlessScrollListener == null) {
            throw new NullPointerException("no pager provided");
        }
        endlessScrollListener.setThreshold(threshold);
    }

    public void setProgressView(int layoutResId) {
        setProgressView(LayoutInflater
                .from(getContext())
                .inflate(layoutResId, this, false));
    }

    public void setProgressView(View view) {
        progressView = view;
    }

    public void setRefreshing(boolean refreshing) {
        if (this.refreshing == refreshing) {
            return;
        }
        System.out.println("refreshing=" + refreshing + ";at=" + System.currentTimeMillis());
        this.refreshing = refreshing;
        this.adapterWrapper.notifyDataSetChanged();
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
            int lastVisibleItemPosition = getLayoutManager()
                    .findLastVisibleItemPosition();
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
            System.out.println("refreshing=" + refreshing);
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

    public interface Pager {
        boolean shouldLoad();
        void loadNextPage();
    }
}
