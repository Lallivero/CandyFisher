package com.example.candyfisher.models;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.candyfisher.R;
import com.example.candyfisher.interfaces.OnItemClickListener;

import java.util.ArrayList;


public class CollectionListAdapter extends RecyclerView.Adapter<CollectionListAdapter.ViewHolder> {


    //    protected CollectionAccessContract.CollectionPresenter myCollectionPresenter;
    private final ArrayList<CollectionListData> collection;
    private final OnItemClickListener myCallback;


    public CollectionListAdapter(ArrayList<CollectionListData> collection, OnItemClickListener callback) {

//        myCollectionPresenter = presenter;
        this.collection = collection;
        myCallback = callback;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.list_item, parent, false);
        return new ViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CollectionListData item = collection.get(position);
        holder.textView.setText(item.getDescription());
        holder.textView2.setText((item.getCollected() ? "Collected" : "Not Collected"));
        holder.imageView.setImageResource(item.getImageId());
//        holder.itemView.setOnClickListener(myCallback.onClick(position));
        holder.relativeLayout.setOnClickListener(view -> myCallback.onClick(holder.getAdapterPosition()));
    }

    //Toast.makeText(view.getContext(), "Click on item: " + listData.getDescription(),Toast.LENGTH_LONG ).show()
    @Override
    public int getItemCount() {
        return collection.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView textView;
        public TextView textView2;
        public RelativeLayout relativeLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.imageView = itemView.findViewById(R.id.imageView);
            this.textView = itemView.findViewById(R.id.textView);
            this.textView2 = itemView.findViewById(R.id.textView2);
            relativeLayout = itemView.findViewById(R.id.relativeLayout);
        }
    }
}
