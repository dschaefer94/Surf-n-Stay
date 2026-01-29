package com.example.pfeil;

public class Offer {
    private String location;
    private String dateRange;
    private String price;
    private int beds;
    private boolean sauna;
    private boolean kamin;
    private boolean smoker;
    private boolean animals;
    private boolean wifi;
    private boolean isExpanded;
    private boolean isPublished;

    public Offer(String location, String dateRange, String price, int beds, boolean sauna, boolean kamin, boolean smoker, boolean animals, boolean wifi, boolean isPublished) {
        this.location = location;
        this.dateRange = dateRange;
        this.price = price;
        this.beds = beds;
        this.sauna = sauna;
        this.kamin = kamin;
        this.smoker = smoker;
        this.animals = animals;
        this.wifi = wifi;
        this.isExpanded = false;
        this.isPublished = isPublished;
    }

    public String getLocation() {
        return location;
    }

    public String getDateRange() {
        return dateRange;
    }

    public String getPrice() {
        return price;
    }

    public int getBeds() {
        return beds;
    }

    public boolean hasSauna() {
        return sauna;
    }

    public boolean hasKamin() {
        return kamin;
    }

    public boolean hasSmoker() {
        return smoker;
    }

    public boolean hasAnimals() {
        return animals;
    }

    public boolean hasWifi() {
        return wifi;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public boolean isPublished() {
        return isPublished;
    }
}
