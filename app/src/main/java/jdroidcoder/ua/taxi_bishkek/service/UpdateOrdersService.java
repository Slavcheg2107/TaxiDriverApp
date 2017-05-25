package jdroidcoder.ua.taxi_bishkek.service;

import android.app.IntentService;
import android.content.Intent;

import java.util.concurrent.TimeUnit;

import jdroidcoder.ua.taxi_bishkek.model.UserProfileDto;
import jdroidcoder.ua.taxi_bishkek.network.NetworkService;

/**
 * Created by jdroidcoder on 13.04.17.
 */
public class UpdateOrdersService extends IntentService {
    public static boolean isRun = true;

    public UpdateOrdersService() {
        super("OrdersService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRun) {
                    new NetworkService().getOrders();
                    new NetworkService().getAllAcceptOrders(UserProfileDto.User.getPhone());
                    try {
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
