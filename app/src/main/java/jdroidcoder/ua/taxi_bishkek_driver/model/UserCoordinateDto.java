package jdroidcoder.ua.taxi_bishkek_driver.model;

/**
 * Created by jdroidcoder on 13.04.17.
 */
public class UserCoordinateDto {
    private String userEmail;
    private Double lat;
    private Double lng;

    public UserCoordinateDto() {
    }

    public UserCoordinateDto(String userEmail, Double lat, Double lng) {
        this.userEmail = userEmail;
        this.lat = lat;
        this.lng = lng;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
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
