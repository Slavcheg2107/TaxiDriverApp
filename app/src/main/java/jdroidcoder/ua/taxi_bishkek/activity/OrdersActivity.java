package jdroidcoder.ua.taxi_bishkek.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.ButterKnife;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.config.LocationAccuracy;
import io.nlopez.smartlocation.location.config.LocationParams;
import jdroidcoder.ua.taxi_bishkek.R;
import jdroidcoder.ua.taxi_bishkek.events.UpdateAdapterEvent;
import jdroidcoder.ua.taxi_bishkek.events.UpdateNotificationEvent;
import jdroidcoder.ua.taxi_bishkek.model.OrderDto;
import jdroidcoder.ua.taxi_bishkek.service.NotificationService;
import jdroidcoder.ua.taxi_bishkek.utils.ImageFilePath;
import jdroidcoder.ua.taxi_bishkek.adapters.ViewPagerAdapter;
import jdroidcoder.ua.taxi_bishkek.events.ChangeListViewEvent;
import jdroidcoder.ua.taxi_bishkek.events.ErrorMessageEvent;
import jdroidcoder.ua.taxi_bishkek.fragment.OrderFragment;
import jdroidcoder.ua.taxi_bishkek.model.UserProfileDto;
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
        try {
            Log.e("CREATE", "CREATE");
            myLocation = SmartLocation.with(this).location().getLastLocation();
            startLocation();
            startService(new Intent(this, UpdateOrdersService.class));
            startService(new Intent(this, NotificationService.class));

            LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (!manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                showSettingsDialog();
            }
        } catch (Exception e) {
            Log.e("ORDERSACTIVITY", e.getMessage());
        }

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setSelectedTabIndicatorHeight(0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startService(new Intent(this, UpdateOrdersService.class));
            startLocation();
            startService(new Intent(this, NotificationService.class));
        }
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
    protected void onStop() {
        if (SmartLocation.with(getApplicationContext()).location().state().locationServicesEnabled()) {

            SmartLocation.with(this).location().stop();
            SmartLocation.with(this).activity().stop();
            SmartLocation.with(this).geofencing().stop();
            Log.e("STOP", "STOP");
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        myLocation = SmartLocation.with(this).location().getLastLocation();
            Log.e("RESUME", "RESUME");
        startLocation();
        if(!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
        startService(new Intent(this, UpdateOrdersService.class));
        startService(new Intent(this, NotificationService.class));
        UpdateOrdersService.isRun = true;
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            showSettingsDialog();
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        UpdateOrdersService.isRun = false;
        stopService(new Intent(this, UpdateOrdersService.class));
        stopService(new Intent(this, NotificationService.class));
        EventBus.getDefault().unregister(this);
        SmartLocation.with(this).location().stop();
        SmartLocation.with(this).activity().stop();
        SmartLocation.with(this).geofencing().stop();
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

//        if (item.getItemId() == R.id.rules) {
//            final View view = LayoutInflater.from(this).inflate(R.layout.rules_alert, null);
//            final AlertDialog alertDialog = new AlertDialog.Builder(this)
//                    .setView(view).create();
////            TextView userPhone = (TextView) view.findViewById(R.id.user_phone);
////            userPhone.setText(userPhone.getText() + UserProfileDto.User.getPhone());
//            view.findViewById(R.id.ok_button).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    alertDialog.dismiss();
//                }
//            });
//
//            alertDialog.show();
//            startActivity(new Intent(this, PayActivity.class).putExtra("isRusels",true));
//        } else
        if (item.getItemId() == R.id.replenishBalance) {
//            final View view = LayoutInflater.from(this).inflate(R.layout.replenish_balance_alert, null);
//            final AlertDialog alertDialog = new AlertDialog.Builder(this)
//                    .setView(view).create();
//            TextView userPhone = (TextView) view.findViewById(R.id.user_phone);
//            userPhone.setText(userPhone.getText() + UserProfileDto.User.getPhone());
//            view.findViewById(R.id.ok_button).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    alertDialog.dismiss();
//                }
//            });
//
//            alertDialog.show();
            startActivity(new Intent(this, PayActivity.class).putExtra("isRusels", false));
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

    public void showSettingsDialog() {
        AlertDialog.Builder phoneInput = new AlertDialog.Builder(this);
        final TextView hint = new TextView(this);

        LinearLayout rel = new LinearLayout(this);
        rel.setOrientation(LinearLayout.HORIZONTAL);
        rel.addView(hint);


        hint.setText("Если вы не включите поиск места положения то вам не будет видно расстояние до заказа");
        hint.setTextSize(18);
        hint.setTextColor(Color.rgb(0, 0, 0));
        hint.setGravity(Gravity.CENTER);

        phoneInput.setPositiveButton("Подтвердить", null);
        phoneInput.setNegativeButton("Отмена", null);
        phoneInput.setView(rel);

        phoneInput.setTitle("Определение места положения выключено");

        final AlertDialog mAlertDialog = phoneInput.create();
        mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {

                Button yes = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                yes.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        Intent gpsOptionsIntent = new Intent(
                                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(gpsOptionsIntent);

                    }
                });
                Button no = mAlertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAlertDialog.dismiss();
                    }
                });
            }
        });
        mAlertDialog.show();
    }

    public void startLocation (){
        long mLocTrackingInterval = 1000 * 5; // 5 sec
        float trackingDistance = 0;
        LocationAccuracy trackingAccuracy = LocationAccuracy.HIGH;

        LocationParams.Builder builder = new LocationParams.Builder()
                .setAccuracy(trackingAccuracy)
                .setDistance(trackingDistance)
                .setInterval(mLocTrackingInterval);

        SmartLocation.with(this)
                .location()
                .continuous()
                .config(builder.build())
                .start(new OnLocationUpdatedListener() {
                    @Override
                    public void onLocationUpdated(Location location) {
                        myLocation = location;
                        EventBus.getDefault().post(new UpdateAdapterEvent());
                        EventBus.getDefault().post(new UpdateNotificationEvent());
                        Log.e("MYLOCATION", myLocation.toString());
                    }
                });
    }

}