package jdroidcoder.ua.taxi_bishkek.events;

import java.io.Serializable;

/**
 * Created by jdroidcoder on 13.04.17.
 */

public class ShowMapEvent implements Serializable{
    private Double lat;
    private Double lng;

    public ShowMapEvent() {
    }

    public ShowMapEvent(Double lat, Double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }
}