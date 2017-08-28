package jdroidcoder.ua.taxi_bishkek_driver.service;

/**
 * Created by Maxim on 6/12/2017.
 */

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import jdroidcoder.ua.taxi_bishkek_driver.R;
import jdroidcoder.ua.taxi_bishkek_driver.activity.OrdersActivity;
import jdroidcoder.ua.taxi_bishkek_driver.events.UpdateNotificationEvent;
import jdroidcoder.ua.taxi_bishkek_driver.model.OrderDto;


public class NotificationService extends Service {
    Notification notification;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        if (EventBus.getDefault().hasSubscriberForEvent(UpdateNotificationEvent.class)) {
            Log.e("TAG", "SUBSCRIBED");
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Subscribe
    public void OnNotificationEvent(UpdateNotificationEvent event) {
        int count = 0;
        for (int i = 0; i < OrderDto.Oreders.getOrders().size(); i++) {
            if (OrderDto.Oreders.getOrders().get(i).getDistance() <= 3000) {
                count++;
            } else if (OrdersActivity.myLocation == null) {
                count++;
            }
        }
        Log.e("NOTIFICATION", "NOTIFICATION");
        Intent intentOrdersActivity = new Intent(this, OrdersActivity.class);
        intentOrdersActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1,
                intentOrdersActivity, 0);
        notification = new android.app.Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Заказы поблизости")
                .setContentIntent(pendingIntent)
                .setContentText("Доступных заказов: " + count)
                .build();
        startForeground(107, notification);
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        try {
            EventBus.getDefault().unregister(this);
        } catch (Exception e) {

        }
        super.onDestroy();
    }
}

