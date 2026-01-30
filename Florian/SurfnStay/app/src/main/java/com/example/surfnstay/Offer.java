package com.example.surfnstay;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

// Implementiert Serializable, damit wir das Objekt später an eine andere Activity übergeben können (beim Bearbeiten)
public class Offer implements Serializable {

    public int id;

    @SerializedName("owner_id") // Map json "owner_id" zu java "ownerId"
    public int ownerId;

    transient public String address;

    public String price; // String, wegen "Kiste Bier"

    public double lat;
    public double lon;

    // Diese Felder müssen vom Server im JSON gesendet werden, damit sie gefüllt sind:
    public int beds;

    @SerializedName("has_sauna")
    public boolean hasSauna;

    @SerializedName("has_fireplace")
    public boolean hasFireplace;
    @SerializedName("is_smoker")
    public boolean isSmoker;

    @SerializedName("has_pets")
    public boolean hasPets;

    @SerializedName("has_internet")
    public boolean hasInternet;

    @SerializedName("start_date")
    public String startDate;

    @SerializedName("is_published")
    public boolean isPublished; // 0 = Entwurf, 1 = Online

    // Leerer Konstruktor wird von Gson benötigt
    public Offer() {}

    // Hilfsmethode: Checkt ob es ein eigenes Angebot ist (anhand einer gespeicherten User-ID)
    public boolean isMine(String myUserId) {
        return String.valueOf(ownerId).equals(myUserId);
    }
}