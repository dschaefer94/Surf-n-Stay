package model;

public class Offer {

    private int id;
    private int ownerId;
    private Double lat;
    private Double lon;
    private String price;
    private int beds;
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

    public int getId() {
        return id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLon() {
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

    public String getEndDate(){
        return endDate;
    }

    public boolean getHasFireplace() {
        return hasFireplace;
    }

    public boolean getSmoker() {
        return isSmoker;
    }

    public boolean getHasPets(){
        return hasPets;
    }

    public boolean getHasInternet() {
        return hasInternet;
    }

    public boolean getHasSauna(){
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

    public void setPrice(String price) {
        this.price = price;
    }

    public void setBeds(int beds) {
        this.beds = beds;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(String endDate){
        this.endDate = endDate;
    }

    public void setHasPets(boolean hasPets){
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

    public void setHasSauna(boolean hasSauna){
       this.hasSauna = hasSauna;
    }

    public void setPublished(boolean published) {
        this.isPublished = published;
    }


}
