package jdroidcoder.ua.taxi_bishkek.activity;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.ButterKnife;
import jdroidcoder.ua.taxi_bishkek.R;
import jdroidcoder.ua.taxi_bishkek.utils.ImageFilePath;
import jdroidcoder.ua.taxi_bishkek.adapters.ViewPagerAdapter;
import jdroidcoder.ua.taxi_bishkek.events.ChangeListViewEvent;
import jdroidcoder.ua.taxi_bishkek.events.ErrorMessageEvent;
import jdroidcoder.ua.taxi_bishkek.fragment.OrderFragment;
import jdroidcoder.ua.taxi_bishkek.model.UserProfileDto;
import jdroidcoder.ua.taxi_bishkek.service.LocationService;
import jdroidcoder.ua.taxi_bishkek.service.UpdateOrdersService;

/**
 * Created by jdroidcoder on 07.04.17.
 */
public class OrdersActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private ViewPager viewPager;
    public static Location myLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.orders_activity);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    123);
        }
        startService(new Intent(this, LocationService.class));
        try {
            myLocation = ((LocationManager) getSystemService(LOCATION_SERVICE)).
                    getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {

        }
        startService(new Intent(this, UpdateOrdersService.class));
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setSelectedTabIndicatorHeight(0);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new OrderFragment(), "Доступные заказы");
        adapter.addFragment(new OrderFragment(), "Принятые заказы");
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
    public void onMessageEvent(ErrorMessageEvent event) {
        Toast.makeText(this, event.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        UpdateOrdersService.isRun = false;
        stopService(new Intent(this, LocationService.class));
        stopService(new Intent(this, UpdateOrdersService.class));
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == R.id.onlinePay) {
//            startActivity(new Intent(this, PayActivity.class));
//        }
//        if (item.getItemId() == R.id.uploadCheck) {
//            selectCheck();
//        } else

        if (item.getItemId() == R.id.rules) {
            final View view = LayoutInflater.from(this).inflate(R.layout.replenish_balance_alert, null);
            final AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setView(view).create();
            TextView userPhone = (TextView) view.findViewById(R.id.user_phone);
            userPhone.setText(userPhone.getText() + UserProfileDto.User.getPhone());
            view.findViewById(R.id.ok_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });

            alertDialog.show();
        } else if (item.getItemId() == R.id.replenishBalance) {
            final View view = LayoutInflater.from(this).inflate(R.layout.replenish_balance_alert, null);
            final AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setView(view).create();
            TextView userPhone = (TextView) view.findViewById(R.id.user_phone);
            userPhone.setText(userPhone.getText() + UserProfileDto.User.getPhone());
            view.findViewById(R.id.ok_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });

            alertDialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void selectCheck() {
        Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
        fileIntent.setType("image/*");
        try {
            startActivityForResult(Intent.createChooser(fileIntent, "select check image"), 102);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 102 && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                return;
            }
            Uri uri = data.getData();
            String path = ImageFilePath.getPath(this, uri);
            if (path == null) {
                Toast.makeText(this, "Path is null", Toast.LENGTH_SHORT).show();
                return;
            }
            Uri pathUri = Uri.parse("file://" + path);
            Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
            emailIntent.setType("plain/text");
            emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"max-navsegda@mail.ru"});
            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Пополнение");
            emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Мой номер:" + UserProfileDto.User.getPhone());
            emailIntent.putExtra(Intent.EXTRA_STREAM, pathUri);
            startActivity(Intent.createChooser(emailIntent, "Отправка письма..."));
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.getItem(0).setTitle(getString(R.string.balance) + " " + UserProfileDto.User.getBalance());
        return super.onPrepareOptionsMenu(menu);
    }
}