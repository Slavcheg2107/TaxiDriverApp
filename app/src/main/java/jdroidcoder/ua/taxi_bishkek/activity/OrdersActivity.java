package jdroidcoder.ua.taxi_bishkek.activity;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.Date;

import butterknife.ButterKnife;
import jdroidcoder.ua.taxi_bishkek.R;
import jdroidcoder.ua.taxi_bishkek.Utils.ImageFilePath;
import jdroidcoder.ua.taxi_bishkek.adapters.ViewPagerAdapter;
import jdroidcoder.ua.taxi_bishkek.events.ChangeListViewEvent;
import jdroidcoder.ua.taxi_bishkek.events.ChangeLocationEvent;
import jdroidcoder.ua.taxi_bishkek.events.ErrorMessageEvent;
import jdroidcoder.ua.taxi_bishkek.events.OrderEvent;
import jdroidcoder.ua.taxi_bishkek.events.UpdateAdapterEvent;
import jdroidcoder.ua.taxi_bishkek.fragment.OrderFragment;
import jdroidcoder.ua.taxi_bishkek.model.UserProfileDto;
import jdroidcoder.ua.taxi_bishkek.network.NetworkService;
import jdroidcoder.ua.taxi_bishkek.service.UpdateOrdersService;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Created by jdroidcoder on 07.04.17.
 */
public class OrdersActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private ViewPager viewPager;
    public static Location myLocation;
    private File checkFile;

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
        myLocation = ((LocationManager) getSystemService(LOCATION_SERVICE)).
                getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//        startService(new Intent(this, LocationService.class));
        startService(new Intent(this, UpdateOrdersService.class));
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
        UpdateOrdersService.isRun = false;
        stopService(new Intent(this, UpdateOrdersService.class));
//        stopService(new Intent(this, LocationService.class));
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
        if (item.getItemId() == R.id.changeNumber) {
            final View view = LayoutInflater.from(this).inflate(R.layout.alert_style, null);
            final EditText phoneET = (EditText) view.findViewById(R.id.phone);
            phoneET.setText(UserProfileDto.User.getPhone());
            new AlertDialog.Builder(this)
                    .setView(view)
                    .setCancelable(false)
                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (!TextUtils.isEmpty(phoneET.getText().toString())) {
                                UserProfileDto.User.setPhone(phoneET.getText().toString());
                                new NetworkService().setDataToProfile(UserProfileDto.User.getEmail(),
                                        UserProfileDto.User.getFirstName(),
                                        UserProfileDto.User.getLastName(),
                                        UserProfileDto.User.getPhone());
                                dialog.dismiss();
                            }
                        }
                    }).show();
        } else if (item.getItemId() == R.id.uploadCheck) {
            selectCheck();
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
            checkFile = new File(path);
            sendFileBrochure();
        }
    }

    private void sendFileBrochure() {
        String type = null;
        String extension = null;

        String fileName = checkFile.getName();
        int i = checkFile.getName().lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i + 1);
        }

        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }

        if (type == null) {
            Toast.makeText(this, "Type of file incorrect", Toast.LENGTH_SHORT).show();
            return;
        }
        MediaType mediaType = MediaType.parse("image/jpeg");

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", UserProfileDto.User.getEmail() + "_" + new Date(),
                        RequestBody.create(mediaType, checkFile))
                .build();
        new NetworkService().uploadCheck(requestBody);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.getItem(0).setTitle(getString(R.string.balance) + " " + UserProfileDto.User.getBalance());
        return super.onPrepareOptionsMenu(menu);
    }
}