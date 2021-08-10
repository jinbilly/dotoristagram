package org.techtown.dotoristagram.util;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public interface OnItemClickListener {
    public void onItemClick(RecyclerView.ViewHolder holder, View view, int position);
    public void onItemLongClick(RecyclerView.ViewHolder holder, View view, int position);
}
