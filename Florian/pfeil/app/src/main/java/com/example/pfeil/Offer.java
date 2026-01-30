package com.example.pfeil;

public class Offer {
    // Fields from your JSON structure & combined from Angebot
    int id;
    int ownerId;
    double lat;
    double lon;
    // String address; // Removed as requested
    String price;
    int beds;
    String startDate;
    String endDate;
    boolean hasPets;
    boolean hasFireplace;
    boolean isSmoker;
    boolean hasInternet;
    boolean hasSauna;
    boolean isPublished;

    // Fields for runtime state
    transient double distance;
    transient Long selectedStartDate = null;
    transient Long selectedEndDate = null;

    // Constructor from Angebot
    public Offer(int id, int ownerId, double lat, double lon, String price, int beds,
                 String startDate, String endDate, boolean hasPets, boolean hasFireplace, boolean isSmoker,
                 boolean hasInternet, boolean hasSauna, boolean isPublished) {
        this.id = id;
        this.ownerId = ownerId;
        this.lat = lat;
        this.lon = lon;
        this.price = price;
        this.beds = beds;
        this.startDate = startDate;
        this.endDate = endDate;
        this.hasPets = hasPets;
        this.hasFireplace = hasFireplace;
        this.isSmoker = isSmoker;
        this.hasInternet = hasInternet;
        this.hasSauna = hasSauna;
        this.isPublished = isPublished;
        this.distance = -1; // Default value for transient field
    }

    // --- Getters ---

    public int getId() {
        return id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String getPrice() {
        return price;
    }

    public int getBeds() {
        return beds;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public boolean hasAnimals() {
        return hasPets;
    }

    public boolean hasKamin() {
        return hasFireplace;
    }

    public boolean hasSmoker() {
        return isSmoker;
    }

    public boolean hasWifi() {
        return hasInternet;
    }

    public boolean hasSauna() {
        return hasSauna;
    }


    public boolean isPublished() {
        return isPublished;
    }

    public String getDateRange() {
        if (this.startDate != null && this.endDate != null) {
            return this.startDate + " - " + this.endDate;
        }
        return "N/A";
    }

    // --- Setters ---

    public void setId(int id) {
        this.id = id;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setBeds(int beds) {
        this.beds = beds;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public void setHasAnimals(boolean hasPets) {
        this.hasPets = hasPets;
    }

    public void setHasKamin(boolean hasFireplace) {
        this.hasFireplace = hasFireplace;
    }

    public void setHasSmoker(boolean smoker) {
        this.isSmoker = smoker;
    }

    public void setHasWifi(boolean hasInternet) {
        this.hasInternet = hasInternet;
    }

    public void setHasSauna(boolean hasSauna) {
        this.hasSauna = hasSauna;
    }

    public void setPublished(boolean isPublished) {
        this.isPublished = isPublished;
    }
}
