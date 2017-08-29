package com.blueridgebinary.terra.data;

/**
 * Created by dorra on 8/25/2017.
 */

public class CurrentLocality extends CurrentDataset{
    private int localityId;
    private double latitude;
    private double longitude;
    private double accuracy;
    private double elevation;
    private String localityNotes;
    private String createdTimestamp;
    private String updatedTimestamp;

    public CurrentLocality(int localityId, double latitude, double longitude, double accuracy, double elevation, String localityNotes, String createdTimestamp, String updatedTimestamp) {
        this.localityId = localityId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.elevation = elevation;
        this.localityNotes = localityNotes;
        this.createdTimestamp = createdTimestamp;
        this.updatedTimestamp = updatedTimestamp;
    }

    public int getLocalityId() {
        return localityId;
    }

    public void setLocalityId(int localityId) {
        this.localityId = localityId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public double getElevation() {
        return elevation;
    }

    public void setElevation(double elevation) {
        this.elevation = elevation;
    }

    public String getLocalityNotes() {
        return localityNotes;
    }

    public void setLocalityNotes(String localityNotes) {
        this.localityNotes = localityNotes;
    }

    public String getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(String createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public String getUpdatedTimestamp() {
        return updatedTimestamp;
    }

    public void setUpdatedTimestamp(String updatedTimestamp) {
        this.updatedTimestamp = updatedTimestamp;
    }
}
