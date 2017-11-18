package jdroidcoder.ua.taxi_bishkek_driver.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.ButterKnife;
import butterknife.OnClick;
import jdroidcoder.ua.taxi_bishkek_driver.R;
import jdroidcoder.ua.taxi_bishkek_driver.events.ConnectionErrorEvent;
import jdroidcoder.ua.taxi_bishkek_driver.events.ErrorMessageEvent;
import jdroidcoder.ua.taxi_bishkek_driver.events.MoveNextEvent;
import jdroidcoder.ua.taxi_bishkek_driver.events.SimChangedEvent;
import jdroidcoder.ua.taxi_bishkek_driver.events.TypePhoneEvent;
import jdroidcoder.ua.taxi_bishkek_driver.model.UserProfileDto;
import jdroidcoder.ua.taxi_bishkek_driver.network.NetworkService;
import jdroidcoder.ua.taxi_bishkek_driver.utils.Settings;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private final static int RC_SIGN_IN = 101;
    private GoogleApiClient googleApiClient;
    private NetworkService networkService;
    private UserProfileDto userProfileDto = new UserProfileDto();
    private String email;
    private boolean isSend = false;
    public static int count = 1;
    SharedPreferences.Editor editor;
    String say;
    public static TelephonyManager m;
    GoogleSignInAccount acct;
    public static String prevGEmail;
    int count1 = 0;
    Snackbar snackbar;
    View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        networkService = new NetworkService();
        editor = Settings.getSettings(this).edit();

//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestEmail()
//                .build();
//
//        googleApiClient = new GoogleApiClient.Builder(this)
//                .enableAutoManage(this, this)
//                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
//                .build();
        view = findViewById(R.id.activity_main);
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
//            m = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//            if (m.getSimSerialNumber() == null || m.getSimSerialNumber().equals("null")) {
//                Toast.makeText(getApplicationContext(), "Вставьте симкарту в слот 1", Toast.LENGTH_SHORT).show();
//                finish();
//            } else {
                enter();
//            }
//        } else {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.READ_PHONE_STATE,}, 123);
//        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            m = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//            if (m.getSimSerialNumber() == null || m.getSimSerialNumber().equals("null")) {
//                Toast.makeText(getApplicationContext(), "Проверьте симкарту", Toast.LENGTH_LONG).show();
//                finish();
//            } else {
//                snackbar = Snackbar.make(view, "Если вы сменили номер телефона не забудьте добавить другой аккаунт", Snackbar.LENGTH_INDEFINITE);
//                snackbar.show();
//                CountDownTimer timer = new CountDownTimer(5000, 4000) {
//
//                    @Override
//                    public void onTick(long millisUntilFinished) {
//                    }
//
//                    @Override
//                    public void onFinish() {
//                        snackbar.dismiss();
//                        enter();
//                    }
//                };
//                timer.start();
//            }
//        }
//    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @OnClick(R.id.sign_in_button)
    public void enter() {

        if (Settings.getSettings().getString(Settings.APP_PREFERENCES_USER_PHONE, "").equals("")) {
            showPhoneNumberInput();
        } else {
            userProfileDto.setPassword(Settings.getSettings().getString(Settings.APP_PREFERENCES_USER_PASSWORD, ""));
            userProfileDto.setPhone(Settings.getSettings().getString(Settings.APP_PREFERENCES_USER_PHONE, ""));
            networkService.register(userProfileDto.getPhone(), Settings.getSettings().getString(Settings.APP_PREFERENCES_USER_PASSWORD, ""));
        }
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == RC_SIGN_IN) {
//            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
//            handleSignInResult(result);
//        }
//    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

//    private void handleSignInResult(GoogleSignInResult result) {
//        if (result.isSuccess()) {
//            if (!isSend) {
//                try {
//                    acct = result.getSignInAccount();
//                    userProfileDto.setFirstName(acct.getGivenName());
//                    userProfileDto.setLastName(acct.getFamilyName());
//                    email = acct.getEmail();
//                    networkService.register(acct.getEmail(), acct.getId());
//
//                } catch (Exception e) {
//                    Toast.makeText(this, "Google signin failed", Toast.LENGTH_LONG).show();
//                }
//            } else {
//                isSend = false;
//            }
//        } else {
//            Toast.makeText(this, getString(R.string.internet_error), Toast.LENGTH_LONG).show();
//        }
//    }

    @Subscribe
    public void onMessageEvent(ErrorMessageEvent event) {
        Toast.makeText(this, event.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Subscribe
    public void onMoveToNext(MoveNextEvent event) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                networkService.getOrders();
                networkService.getAllAcceptOrders(Settings.currentUser.getPhone());
            }
        }).start();
        startActivity(new Intent(this, OrdersActivity.class));
        finish();
    }

    @Subscribe
    public void onTypeEvent(TypePhoneEvent event) {
        showPhoneNumberInput();
    }

    public void showPhoneNumberInput() {
        AlertDialog.Builder phoneInput = new AlertDialog.Builder(this);
        final EditText phoneNumber = new EditText(this);
        final EditText password = new EditText(this);


        LinearLayout rel = new LinearLayout(this);
        rel.setOrientation(LinearLayout.VERTICAL);
        rel.setGravity(Gravity.CENTER);
        rel.addView(phoneNumber);
        rel.addView(password);
        final String phoneCode = "996";
        phoneNumber.setInputType(InputType.TYPE_CLASS_PHONE);

        phoneNumber.setHint("Например: 701222333");

        phoneNumber.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        phoneNumber.setMaxLines(1);
        password.setHint("Пароль, например: 1234");
        password.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        password.setInputType(InputType.TYPE_CLASS_PHONE);
        int maxLength = 10;
        phoneNumber.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
        password.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        phoneInput.setPositiveButton("Подтвердить", null);
        phoneInput.setView(rel);

        phoneInput.setTitle("Введите номер телефона и пароль");

        final AlertDialog mAlertDialog = phoneInput.create();
        mAlertDialog.setCanceledOnTouchOutside(false);
        mAlertDialog.setCancelable(false);
        mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {

                Button b = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (phoneNumber.getText().toString().length() == 10 && phoneNumber.getText().charAt(0) == '0'
                                && password.getText().toString().length() == 4) {
                            if (count1 == 0) {
                                mAlertDialog.setTitle("Сохранить данный номер?");
                                mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setText("Сохранить");
                                count1 = 1;
                            } else if (count1 == 1) {
                                String phoneWithoutZero;
                                phoneWithoutZero = phoneNumber.getText().toString().substring(1);
                                Log.e("Phone", phoneWithoutZero);
                                say = phoneCode + phoneWithoutZero;
                                userProfileDto.setPhone(say);
                                userProfileDto.setPassword(password.getText().toString());
                                editor.putString(Settings.APP_PREFERENCES_USER_PHONE, say);
                                editor.putString(Settings.APP_PREFERENCES_USER_PASSWORD, password.getText().toString());
                                editor.apply();

                                Log.e("settings", Settings.getSettings().getString(Settings.APP_PREFERENCES_USER_PHONE, "  "));
//
                                enter();
                                mAlertDialog.dismiss();
                                count1 = 0;
                            }
                        } else if (phoneNumber.getText().toString().length() != 10) {
                            phoneNumber.setError("Допишите цифры");
                        } else if (phoneNumber.getText().charAt(0) != '0') {
                            phoneNumber.setError("Пишите номер начиная с ноля");
                        } else if (password.getText().toString().length() != 4) {
                            password.setError("Длинна пинкода 4 символа");
                        }
                    }
                });
            }
        });
        mAlertDialog.show();
    }

    @Subscribe
    public void onConnectionErrorEvent(ConnectionErrorEvent connectionErrorEvent) {
        Snackbar.make(findViewById(R.id.sign_in_button), "Подключите интернет", Snackbar.LENGTH_INDEFINITE).show();
    }


    @Subscribe
    public void OnSimChangedEvent(SimChangedEvent simChangedEvent) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            if (Settings.getSettings(this).getString(Settings.APP_PREFERENCES_SIMID, "").isEmpty()
                    || Settings.getSettings(this).getString(Settings.APP_PREFERENCES_SIMID, "").equals("")) {
                editor.putString(Settings.APP_PREFERENCES_SIMID, m.getSimSerialNumber());
                editor.apply();

                enter();
            } else if (!Settings.getSettings(this).getString(Settings.APP_PREFERENCES_SIMID, "").equals(m.getSimSerialNumber())) {
                if (acct.getEmail().equals(prevGEmail)) {
                    count++;
//                Toast.makeText(getApplicationContext(), "Если вы сменили номер телефона нажмите \"Другой аккаунт\"", Toast.LENGTH_LONG).show();
//                googleApiClient.clearDefaultAccountAndReconnect();
                    enter();
                } else {
                    count = 0;
                    editor.putString(Settings.APP_PREFERENCES_SIMID, m.getSimSerialNumber());
                    editor.apply();
                    enter();
                }
            }
        }
    }
}