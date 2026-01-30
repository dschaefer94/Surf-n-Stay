// java
package de.bib.surf_n_stay;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class OfferAdapter extends RecyclerView.Adapter<OfferAdapter.ViewHolder> {
    private static final DateTimeFormatter INPUT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter OUTPUT =
            DateTimeFormatter.ofPattern("d.M");

    private final List<Angebot> angebote = new ArrayList<>();

    public OfferAdapter(List<Angebot> initial) {
        if (initial != null) {
            this.angebote.addAll(initial);
        }
        setHasStableIds(true);
    }

    public void setAngebote(List<Angebot> neueAngebote) {
        this.angebote.clear();
        if (neueAngebote != null) {
            this.angebote.addAll(neueAngebote);
        }
        notifyDataSetChanged();
    }

    public void addAngebote(List<Angebot> more) {
        if (more == null || more.isEmpty()) return;
        this.angebote.addAll(more);
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        Angebot a = getItem(position);
        return a != null ? a.getId() : RecyclerView.NO_ID;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_offer, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder h, int position) {
        Angebot a = getItem(position);
        if (a == null) return;

        // Distanz: falls noch nicht berechnet (<= 0) ein Platzhalter anzeigen
        if (a.distanz > 0) {
            h.tvDistance.setText(String.format(Locale.getDefault(), "%.1f km", a.distanz / 1000.0));
        } else {
            h.tvDistance.setText("–");
        }

        h.tvPrice.setText((a.price == null || a.price.isEmpty()) ? "n/a" : a.price + " €");

        if (a.startDate != null && a.endDate != null) {
            try {
                LocalDate start = LocalDate.parse(a.startDate, INPUT);
                LocalDate end = LocalDate.parse(a.endDate, INPUT);

                h.tvDate.setText(
                        OUTPUT.format(start) + " – " + OUTPUT.format(end)
                );
            } catch (Exception e) {
                h.tvDate.setText("–");
            }
        } else {
            h.tvDate.setText("–");
        }

        h.iconInternet.setVisibility(a.hasInternet ? View.VISIBLE : View.GONE);
        h.iconPets.setVisibility(a.hasPets ? View.VISIBLE : View.GONE);
        h.iconSauna.setVisibility(a.hasSauna ? View.VISIBLE : View.GONE);
        h.iconFireplace.setVisibility(a.hasFireplace ? View.VISIBLE : View.GONE);
        h.iconSmoking.setVisibility(a.isSmoker ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return angebote == null ? 0 : angebote.size();
    }

    private Angebot getItem(int position) {
        if (angebote == null || position < 0 || position >= angebote.size()) return null;
        return angebote.get(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDistance, tvPrice,tvDate;
        ImageView iconInternet, iconPets, iconSauna, iconSmoking, iconFireplace;

        ViewHolder(View v) {
            super(v);
            tvDistance = v.findViewById(R.id.tvDistance);
            tvPrice = v.findViewById(R.id.tvPrice);
            tvDate=v.findViewById(R.id.tvDate);
            iconInternet = v.findViewById(R.id.iconInternet);
            iconPets = v.findViewById(R.id.iconPets);
            iconSauna = v.findViewById(R.id.iconSauna);
            iconSmoking= v.findViewById(R.id.iconSmoker);
            iconFireplace = v.findViewById(R.id.iconFireplace);
        }
    }
}
