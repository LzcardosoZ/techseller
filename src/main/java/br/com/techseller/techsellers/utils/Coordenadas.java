package br.com.techseller.techsellers.utils;

public class Coordenadas {

    private double latitude;
    private double longitude;

    // Construtor
    public Coordenadas(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters e Setters
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

    @Override
    public String toString() {
        return "Coordenadas{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
