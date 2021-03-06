package com.example.dailymeal.historyRecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailymeal.R;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryViewHolders> {
    private List<HistoryObject> itemList;
    private Context context;


    public HistoryAdapter(List<HistoryObject> itemList, Context context) {
        this.itemList = itemList;
        this.context = context;
    }

    public HistoryViewHolders onCreateViewHolder( ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);
        HistoryViewHolders rcv = new HistoryViewHolders(layoutView);
        return rcv;
    }


    public void onBindViewHolder( HistoryViewHolders holder, int position) {
        holder.rideId.setText(itemList.get(position).getRideId());
        if(itemList.get(position).getTime()!=null){
            holder.time.setText(itemList.get(position).getTime());
        }
    }

    @Override
    public int getItemCount() {
        return this.itemList.size();
    }
}
