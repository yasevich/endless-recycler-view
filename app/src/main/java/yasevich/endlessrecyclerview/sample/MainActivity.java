package yasevich.endlessrecyclerview.sample;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import yasevich.endlessrecyclerview.EndlessRecyclerView;

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

        list = (EndlessRecyclerView) findViewById(android.R.id.list);
        list.setLayoutManager(new LinearLayoutManager(this));
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

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.text.setText("Item: " + (position + 1));
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
