package model;

public class Offer {

    private long id;
    private long ownerId;
    private Double lat;
    private Double lon;
    private String address;
    private String price;
    private Integer beds;
    private String startDate;  // oder LocalDate, wenn du willst
    private String endDate;
    private boolean hasSauna;
    private boolean hasFireplace;
    private boolean isSmoker;
    private boolean hasPets;
    private boolean hasInternet;
    private boolean isPublished;

    public Offer() {
    }

    // Optional: Full constructor
    public Offer(long id, long ownerId, Double lat, Double lon, String address, String price,
                 Integer beds, String startDate, String endDate,
                 boolean hasSauna, boolean hasFireplace, boolean isSmoker,
                 boolean hasPets, boolean hasInternet, boolean isPublished) {
        this.id = id;
        this.ownerId = ownerId;
        this.lat = lat;
        this.lon = lon;
        this.address = address;
        this.price = price;
        this.beds = beds;
        this.startDate = startDate;
        this.endDate = endDate;
        this.hasSauna = hasSauna;
        this.hasFireplace = hasFireplace;
        this.isSmoker = isSmoker;
        this.hasPets = hasPets;
        this.hasInternet = hasInternet;
        this.isPublished = isPublished;
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

    public boolean getHasFireplace() {
        return hasFireplace;
    }

    public boolean getSmoker() {
        return isSmoker;
    }

    public boolean getHasInternet() {
        return hasInternet;
    }

    public boolean getPublished() {
        return isPublished;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setOwnerId(long ownerId) {
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

    public void setHasFireplace(boolean hasFireplace) {
        this.hasFireplace = hasFireplace;
    }

    public void setSmoker(boolean smoker) {
        isSmoker = smoker;
    }

    public void setHasInternet(boolean hasInternet) {
        this.hasInternet = hasInternet;
    }

    public void setPublished(boolean published) {
        isPublished = published;
    }


}
