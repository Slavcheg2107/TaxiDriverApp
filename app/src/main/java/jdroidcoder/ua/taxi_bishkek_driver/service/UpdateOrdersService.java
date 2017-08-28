package jdroidcoder.ua.taxi_bishkek_driver.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import jdroidcoder.ua.taxi_bishkek_driver.R;
import jdroidcoder.ua.taxi_bishkek_driver.model.UserProfileDto;
import jdroidcoder.ua.taxi_bishkek_driver.network.NetworkService;

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
                        TimeUnit.SECONDS.sleep(8);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.e("RUNSERVICE", "RUNSERVICE");
                }
            }
        }).start();
    }

}
