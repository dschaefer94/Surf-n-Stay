package de.bib.surf_n_stay;

public class Angebot {
    int id;
    int ownerId;
    double lat;
    double lon;
    String address;
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
    double distanz;

    public Angebot(int id, int ownerId, double lat, double lon, String address, String price, int beds,
                   String startDate, String endDate,boolean hasPets, boolean hasFireplace, boolean isSmoker,
                   boolean hasInternet, boolean hasSauna, boolean isPublished) {
        this.id = id;
        this.ownerId = ownerId;
        this.lat = lat;
        this.lon = lon;
        this.address = address;
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
        this.distanz = -1; // Initial distance set to -1 (unknown)
    }

    public long getId() {
        return id;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLon() {
        return lon;
    }

    public String getAddress() {
        return address;
    }

    public String getPrice() {
        return price;
    }

    public Integer getBeds() {
        return beds;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public boolean getHasPets() {
        return hasPets;
    }

    public boolean getHasFireplace() {
        return hasFireplace;
    }

    public boolean getSmoker() {
        return isSmoker;
    }

    public boolean getHasInternet() {
        return hasInternet;
    }

    public boolean getHasSauna() {
        return hasSauna;
    }

    public boolean getPublished() {
        return isPublished;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setBeds(Integer beds) {
        this.beds = beds;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public void setHasPets(boolean hasPets) {
        this.hasPets = hasPets;
    }

    public void setHasFireplace(boolean hasFireplace) {
        this.hasFireplace = hasFireplace;
    }

    public void setSmoker(boolean smoker) {
        isSmoker = smoker;
    }

    public void setHasInternet(boolean hasInternet) {
        this.hasInternet = hasInternet;
    }

    public void setHasSauna(boolean hasSauna) {
        this.hasSauna = hasSauna;
    }

    public void setPublished(boolean isPublished) {
        this.isPublished = this.isPublished;
    }
}
