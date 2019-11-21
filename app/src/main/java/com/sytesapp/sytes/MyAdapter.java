package com.sytesapp.sytes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private ArrayList<String> items;
    private ArrayList<AdView> ads = new ArrayList<>();
    private OnItemClickListener listener;

    MyAdapter(Context context, ArrayList<String> receivedItems) {
        items = receivedItems;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView nameText;
        private TextView categoryText;
        private TextView idText;
        private TextView latLongText;
        private FrameLayout adSpace;

        MyViewHolder(final View view) {
            super(view);

            nameText = view.findViewById(R.id.nameText);
            categoryText = view.findViewById(R.id.categoryText);
            idText = view.findViewById(R.id.idText);
            latLongText = view.findViewById(R.id.latLongText);
            adSpace = view.findViewById(R.id.listAdSpace);

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

        holder.idText.setText(id);

        if (id.equals("Ad")) {
            holder.nameText.setVisibility(View.GONE);
            holder.categoryText.setVisibility(View.GONE);
            holder.adSpace.setVisibility(View.VISIBLE);

            ExtraneousMethods.AddListAdToFrame(holder.adSpace);
        }
        else {
            holder.nameText.setVisibility(View.VISIBLE);
            holder.categoryText.setVisibility(View.VISIBLE);
            holder.adSpace.setVisibility(View.GONE);

            holder.nameText.setText(name);
            holder.categoryText.setText(category);

            if (id.equals("0")) {
                String latLong = split[3];

                holder.latLongText.setText(latLong);
            }
        }

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
