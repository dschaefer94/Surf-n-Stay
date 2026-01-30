package com.example.pfeil;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OfferAdapter extends RecyclerView.Adapter<OfferAdapter.ViewHolder> {
    private static final DateTimeFormatter INPUT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter OUTPUT = DateTimeFormatter.ofPattern("d.M");

    private final List<Offer> angebote = new ArrayList<>();

    public OfferAdapter(List<Offer> initial) {
        if (initial != null) {
            this.angebote.addAll(initial);
        }
        setHasStableIds(true);
    }

    public void setAngebote(List<Offer> neueAngebote) {
        this.angebote.clear();
        if (neueAngebote != null) {
            this.angebote.addAll(neueAngebote);
        }
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        Offer a = getItem(position);
        return a != null ? a.getId() : RecyclerView.NO_ID;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_offer, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Offer a = getItem(position);
        if (a == null) return;

        if (a.distance > 0) {
            h.tvDistance.setText(String.format(Locale.getDefault(), "%.1f km", a.distance / 1000.0));
        } else {
            h.tvDistance.setText("–");
        }

        h.tvPrice.setText((a.price == null || a.price.isEmpty()) ? "n/a" : a.price + " €");

        if (a.startDate != null && a.endDate != null) {
            try {
                LocalDate start = LocalDate.parse(a.startDate, INPUT);
                LocalDate end = LocalDate.parse(a.endDate, INPUT);
                h.tvDate.setText(OUTPUT.format(start) + " – " + OUTPUT.format(end));
            } catch (Exception e) {
                h.tvDate.setText("–");
            }
        } else {
            h.tvDate.setText("–");
        }

        h.iconInternet.setAlpha(a.hasWifi() ? 1.0f : 0.2f);
        h.iconPets.setAlpha(a.hasAnimals() ? 1.0f : 0.2f);
        h.iconSauna.setAlpha(a.hasSauna() ? 1.0f : 0.2f);
        h.iconFireplace.setAlpha(a.hasKamin() ? 1.0f : 0.2f);
        h.iconSmoking.setAlpha(a.hasSmoker() ? 1.0f : 0.2f);
    }

    @Override
    public int getItemCount() {
        return angebote.size();
    }

    private Offer getItem(int position) {
        if (position < 0 || position >= angebote.size()) return null;
        return angebote.get(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDistance, tvPrice, tvDate;
        ImageView iconInternet, iconPets, iconSauna, iconSmoking, iconFireplace;

        ViewHolder(View v) {
            super(v);
            tvDistance = v.findViewById(R.id.tvDistance);
            tvPrice = v.findViewById(R.id.tvPrice);
            tvDate = v.findViewById(R.id.tvDate);
            iconInternet = v.findViewById(R.id.iconInternet);
            iconPets = v.findViewById(R.id.iconPets);
            iconSauna = v.findViewById(R.id.iconSauna);
            iconSmoking = v.findViewById(R.id.iconSmoker);
            iconFireplace = v.findViewById(R.id.iconFireplace);
        }
    }
}
