/*
* MediaItemAdapter
* RecyclerView adapter used to display the MediaItem objects
 */


package com.zybooks.judsonsinventory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MediaItemAdapter extends RecyclerView.Adapter<MediaItemAdapter.ViewHolder> {

    //callbacks so the fragment can handle buttons
    public interface OnItemActionListener {
        void onEditClick(MediaItem item);
        void onDeleteClick(MediaItem item);
    }

    private List<MediaItem> mItems;
    private final OnItemActionListener mListener;


    public MediaItemAdapter(List<MediaItem> items, OnItemActionListener listener) {
        mItems = items;
        mListener = listener;
    }


    //swap a new list and refresh (used for search and sort)
    public void updateItems(List<MediaItem> newItems) {
        mItems = newItems;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MediaItem item = mItems.get(position);

        holder.titleText.setText(item.getTitle());
        holder.categoryText.setText(item.getCategory());

        //show year or dash if no year
        holder.yearText.setText(item.getYear() > 0 ? String.valueOf(item.getYear()) : "-");

        //rating goes from 0-5
        holder.ratingBar.setRating(item.getRating());

        holder.editButton.setOnClickListener(v -> mListener.onEditClick(item));
        holder.deleteButton.setOnClickListener(v -> mListener.onDeleteClick(item));
    }


    @Override
    public int getItemCount() {
        return mItems.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView categoryText;
        TextView yearText;
        RatingBar ratingBar;
        ImageButton editButton;
        ImageButton deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.item_title);
            categoryText = itemView.findViewById(R.id.item_category);
            yearText = itemView.findViewById(R.id.item_year);
            ratingBar = itemView.findViewById(R.id.item_rating_bar);
            editButton = itemView.findViewById(R.id.btn_edit);
            deleteButton = itemView.findViewById(R.id.btn_delete);
        }
    }
}
