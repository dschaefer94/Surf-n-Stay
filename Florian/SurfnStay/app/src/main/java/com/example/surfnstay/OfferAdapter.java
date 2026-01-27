package com.example.surfnstay;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.surfnstay.databinding.ItemOfferBinding;
import java.util.ArrayList;
import java.util.List;

public class OfferAdapter extends RecyclerView.Adapter<OfferAdapter.OfferViewHolder> {

    private List<Offer> offerList = new ArrayList<>();
    private final OnOfferClickListener listener;

    // Interface für Klick-Events (damit die MainActivity reagieren kann)
    public interface OnOfferClickListener {
        void onOfferClick(Offer offer);
    }

    public OfferAdapter(OnOfferClickListener listener) {
        this.listener = listener;
    }

    // Methode um neue Daten in die Liste zu laden
    public void setOffers(List<Offer> offers) {
        this.offerList = offers;
        notifyDataSetChanged(); // Aktualisiert die UI
    }

    @NonNull
    @Override
    public OfferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Nutzt das generierte Binding für item_offer.xml
        ItemOfferBinding binding = ItemOfferBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new OfferViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OfferViewHolder holder, int position) {
        Offer offer = offerList.get(position);
        holder.bind(offer, listener);
    }

    @Override
    public int getItemCount() {
        return offerList.size();
    }

    // Innere Klasse: Hält die Referenzen zu den Views einer Zeile
    static class OfferViewHolder extends RecyclerView.ViewHolder {
        private final ItemOfferBinding binding;

        public OfferViewHolder(ItemOfferBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Offer offer, OnOfferClickListener listener) {
            // 1. Grunddaten setzen
            binding.tvPrice.setText(offer.price);
            binding.tvLocation.setText(offer.address != null && !offer.address.isEmpty() ? offer.address : "GPS Standort");

            // 2. Ausstattungs-String bauen
            StringBuilder details = new StringBuilder();
            details.append(offer.beds).append(" Betten");

            if (offer.hasSauna == 1) details.append(" • Sauna");
            if (offer.hasFireplace == 1) details.append(" • Kamin");
            if (offer.hasInternet == 1) details.append(" • WLAN");
            if (offer.hasPets == 1) details.append(" • 🐾");

            binding.tvDetails.setText(details.toString());

            // 3. Status Anzeige (Online vs Entwurf)
            if (offer.isPublished == 1) {
                binding.tvStatus.setText("ONLINE");
                binding.tvStatus.setTextColor(Color.parseColor("#4CAF50")); // Grün
            } else {
                binding.tvStatus.setText("ENTWURF (Nicht sichtbar)");
                binding.tvStatus.setTextColor(Color.GRAY);
            }

            // 4. Klick-Listener setzen
            binding.getRoot().setOnClickListener(v -> listener.onOfferClick(offer));
        }
    }
}