package com.sytesapp.sytes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private ArrayList<String> items;
    private OnItemClickListener listener;

    MyAdapter(ArrayList<String> receivedItems) {
        items = receivedItems;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView nameText;
        private TextView categoryText;
        private TextView idText;

        MyViewHolder(final View view) {
            super(view);

            nameText = view.findViewById(R.id.nameText);
            categoryText = view.findViewById(R.id.categoryText);
            idText = view.findViewById(R.id.idText);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Triggers click upwards to the adapter on click
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(view, position);
                        }
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item, parent, false));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        String[] split = items.get(position).split("\n");
        String id = split[0];
        String name = split[1];
        String category = split[2];

        TextView nameText = holder.nameText;
        TextView categoryText = holder.categoryText;
        TextView idText = holder.idText;

        nameText.setText(name);
        categoryText.setText(category);
        idText.setText(id);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
