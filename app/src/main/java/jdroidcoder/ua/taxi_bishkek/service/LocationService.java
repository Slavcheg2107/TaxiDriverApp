package jdroidcoder.ua.taxi_bishkek.service;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import jdroidcoder.ua.taxi_bishkek.R;
import jdroidcoder.ua.taxi_bishkek.activity.OrdersActivity;
import jdroidcoder.ua.taxi_bishkek.events.UpdateNotificationEvent;
import jdroidcoder.ua.taxi_bishkek.model.OrderDto;

/**
 * Created by jdroidcoder on 07.04.17.
 */
public class LocationService extends Service implements LocationListener {
    private Notification notification;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            super.onStartCommand(intent, flags, startId);
        }
        ((LocationManager) getSystemService(LOCATION_SERVICE))
                .requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        EventBus.getDefault().register(this);
        return super.onStartCommand(intent, flags, startId);
    }

    @Subscribe
    public void onUpdateNotificationEvent(UpdateNotificationEvent updateNotificationEvent) {
        int count = 0;
        for (int i = 0; i < OrderDto.Oreders.getOrders().size(); i++) {
            try {
                if (OrderDto.Oreders.getOrders().get(i).getDistance() <= 3000) {
                    count++;
                }
            } catch (Exception e) {

            }
        }
        Intent intentOrdersActivity = new Intent(this, OrdersActivity.class);
        intentOrdersActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                intentOrdersActivity, 0);
        notification = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Orders")
                .setContentIntent(pendingIntent)
                .setContentText("Available orders: " + count)
                .build();
        startForeground(106, notification);
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onLocationChanged(Location location) {
        OrdersActivity.myLocation = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
