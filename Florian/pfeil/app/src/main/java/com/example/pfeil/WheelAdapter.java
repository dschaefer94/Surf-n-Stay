package com.example.pfeil;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class WheelAdapter extends RecyclerView.Adapter<WheelAdapter.ViewHolder> {

    private List<String> dataList;

    // Konstruktor: Hier übergeben wir später die Daten
    public WheelAdapter(List<String> dataList) {
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Hier laden wir das Design für eine einzelne Zeile
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_number, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String item = dataList.get(position);
        if (item.isEmpty()) {
            holder.itemView.setVisibility(View.INVISIBLE);
        } else {
            holder.itemView.setVisibility(View.VISIBLE);
            holder.textView.setText(item);
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    // Die ViewHolder-Klasse hält die Referenzen zu den Views
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.tvNumber);
        }
    }
}