package com.example.candyfisher.models;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.candyfisher.R;
import com.example.candyfisher.interfaces.CollectionAccessContract;


public class CollectionListAdapter extends RecyclerView.Adapter<CollectionListAdapter.ViewHolder> {

    protected CollectionAccessContract.CollectionPresenter myCollectionPresenter;
    public CollectionListAdapter(CollectionAccessContract.CollectionPresenter presenter) {

        myCollectionPresenter = presenter;

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

        holder.textView.setText(myCollectionPresenter.getItemDescription(position));
        holder.textView2.setText((myCollectionPresenter.getCollectedStatus(position) ? "Collected" : "Not Collected"));
        holder.imageView.setImageResource(myCollectionPresenter.getImageId(position));
        holder.relativeLayout.setOnClickListener(view -> myCollectionPresenter.onClick(position));
    }
//Toast.makeText(view.getContext(), "Click on item: " + listData.getDescription(),Toast.LENGTH_LONG ).show()
    @Override
    public int getItemCount() {
        return myCollectionPresenter.getCollectionSize();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView textView;
        public TextView textView2;
        public RelativeLayout relativeLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.imageView = (ImageView) itemView.findViewById(R.id.imageView);
            this.textView = (TextView) itemView.findViewById(R.id.textView);
            this.textView2 = (TextView) itemView.findViewById(R.id.textView2);
            relativeLayout = (RelativeLayout) itemView.findViewById(R.id.relativeLayout);
        }
    }
}
