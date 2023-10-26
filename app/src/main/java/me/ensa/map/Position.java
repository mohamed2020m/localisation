package me.ensa.map;

public class Position {
    private Double longitude, latitude;
    private String date, imei;
    private int id;

    public Position(Double longitude, Double latitude, String date, String imei) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.date = date;
        this.imei = imei;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
