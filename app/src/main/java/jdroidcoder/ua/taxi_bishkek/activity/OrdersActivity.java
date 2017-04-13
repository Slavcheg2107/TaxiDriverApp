package jdroidcoder.ua.taxi_bishkek.activity;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.ButterKnife;
import jdroidcoder.ua.taxi_bishkek.R;
import jdroidcoder.ua.taxi_bishkek.adapters.ViewPagerAdapter;
import jdroidcoder.ua.taxi_bishkek.events.ChangeListViewEvent;
import jdroidcoder.ua.taxi_bishkek.events.ChangeLocationEvent;
import jdroidcoder.ua.taxi_bishkek.events.ErrorMessageEvent;
import jdroidcoder.ua.taxi_bishkek.events.OrderEvent;
import jdroidcoder.ua.taxi_bishkek.events.UpdateAdapterEvent;
import jdroidcoder.ua.taxi_bishkek.fragment.OrderFragment;

/**
 * Created by jdroidcoder on 07.04.17.
 */
public class OrdersActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.orders_activity);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    123);
        }

//        startService(new Intent(this, LocationService.class));
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new OrderFragment(), "Orders");
        adapter.addFragment(new OrderFragment(), "Accept orders");
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                EventBus.getDefault().post(new ChangeListViewEvent(position == 1));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Subscribe
    public void onChangeLocationEvent(ChangeLocationEvent changeLocationEvent) {

    }

    @Subscribe
    public void onUpdateAdapterEvent(UpdateAdapterEvent updateAdapterEvent) {
    }

    @Subscribe
    public void onMessageEvent(ErrorMessageEvent event) {
        Toast.makeText(this, event.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Subscribe
    public void onNewOrderEvent(OrderEvent event) {
    }

    @Override
    protected void onDestroy() {
//        stopService(new Intent(this, LocationService.class));
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}