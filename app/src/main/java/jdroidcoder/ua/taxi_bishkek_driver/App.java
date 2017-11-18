package jdroidcoder.ua.taxi_bishkek_driver;

import android.support.multidex.MultiDexApplication;

import jdroidcoder.ua.taxi_bishkek_driver.utils.Settings;

/**
 * Created by Maxim on 8/29/2017.
 */

public class App extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        Settings.init(this);
    }
}
