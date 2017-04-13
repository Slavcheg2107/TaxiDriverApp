package jdroidcoder.ua.taxi_bishkek.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import butterknife.ButterKnife;
import jdroidcoder.ua.taxi_bishkek.R;
import jdroidcoder.ua.taxi_bishkek.adapters.MarkerAdapter;
import jdroidcoder.ua.taxi_bishkek.events.ShowMapEvent;
import jdroidcoder.ua.taxi_bishkek.fragment.OrderFragment;

/**
 * Created by jdroidcoder on 07.04.17.
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private GoogleMap mMap;
    private MarkerOptions markerOptions;
    private ShowMapEvent showMapEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps_activity);
        showMapEvent = (ShowMapEvent) getIntent().getSerializableExtra("userCoordinate");
        ButterKnife.bind(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onBackPressed() {
        OrderFragment.isShowMap = false;
        super.onBackPressed();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (showMapEvent != null) {
            try {
                LatLng sydney = new LatLng(showMapEvent.getLat(),
                        showMapEvent.getLng());
                markerOptions = new MarkerOptions().position(sydney);
                mMap.addMarker(markerOptions);
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(sydney, 17f, 0f, 0f)));
                mMap.setInfoWindowAdapter(new MarkerAdapter(this));
                mMap.setOnMarkerClickListener(this);
            } catch (Exception e) {

            }
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return false;
    }
}