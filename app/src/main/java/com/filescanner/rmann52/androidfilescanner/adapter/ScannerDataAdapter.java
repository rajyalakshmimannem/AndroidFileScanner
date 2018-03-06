package com.filescanner.rmann52.androidfilescanner.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.filescanner.rmann52.androidfilescanner.model.RecyclerItemInfo;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import com.filescanner.rmann52.androidfilescanner.R;

/**
 * Created by rmann52 on 3/2/18.
 */

public class ScannerDataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "ScannerDataAdapter";

    private static final int RECYCLER_ITEM_HEADER = 0;

    private static final int RECYCLER_ITEM_ROW = 1;

    private Set<Integer> mHeaderSet;

    private ArrayList<RecyclerItemInfo> mRecyclerItemInfo;

    public ScannerDataAdapter() {
        mHeaderSet = new TreeSet<>();
        mRecyclerItemInfo = new ArrayList<>();
    }

    public void reset() {
        mRecyclerItemInfo.clear();
        mHeaderSet.clear();
    }

    public void addItem(String item, String count) {
        mRecyclerItemInfo.add(new RecyclerItemInfo(item, count));
    }

    public void addHeader(String title) {
        mRecyclerItemInfo.add(new RecyclerItemInfo(title, null));
        mHeaderSet.add(mRecyclerItemInfo.size() - 1);
    }

    private boolean isHeader(int position) {
        return mHeaderSet.contains(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == RECYCLER_ITEM_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.scan_item_header, parent, false);
            return new ScannerDataAdapter.HeaderViewHolder(view);
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.scan_item_row, parent, false);
        return new ScannerDataAdapter.ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        RecyclerItemInfo fileInfo = mRecyclerItemInfo.get(position);
        Log.v(TAG, "Itemn position: " + position);
        if (isHeader(position)) {
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
       //     headerViewHolder.headerTitle.setText(fileInfo.getmFilename());

            SpannableString headerContent = new SpannableString(fileInfo.getmFilename());
            headerContent.setSpan(new UnderlineSpan(), 0, headerContent.length(), 0);
            headerViewHolder.headerTitle.setText(headerContent);

        } else {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            itemViewHolder.fileName.setText(fileInfo.getmFilename());
            itemViewHolder.fileCount.setText(fileInfo.getmFileCount());
        }
    }

    @Override
    public int getItemCount() {
        return mRecyclerItemInfo.size();
    }

    @Override
    public int getItemViewType(int position) {
        return isHeader(position) ? RECYCLER_ITEM_HEADER : RECYCLER_ITEM_ROW;
    }

    private static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView fileName, fileCount;
        ItemViewHolder(View view) {
            super(view);
            fileName = (TextView) view.findViewById(R.id.file_name);
            fileCount = (TextView) view.findViewById(R.id.file_count);
        }
    }

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerTitle;
        HeaderViewHolder(View view) {
            super(view);
            headerTitle = (TextView) view.findViewById(R.id.header_title);
        }
    }
}
