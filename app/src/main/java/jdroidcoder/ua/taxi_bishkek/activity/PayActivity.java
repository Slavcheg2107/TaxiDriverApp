package jdroidcoder.ua.taxi_bishkek.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.cloudipsp.android.Card;
import com.cloudipsp.android.CardInputView;
import com.cloudipsp.android.Currency;
import com.cloudipsp.android.Cloudipsp;
import com.cloudipsp.android.CloudipspWebView;
import com.cloudipsp.android.Order;
import com.cloudipsp.android.Receipt;

import jdroidcoder.ua.taxi_bishkek.BuildConfig;
import jdroidcoder.ua.taxi_bishkek.R;
import jdroidcoder.ua.taxi_bishkek.model.UserProfileDto;
import jdroidcoder.ua.taxi_bishkek.network.NetworkService;

/**
 * Created by jdroidcoder on 08.05.17.
 */
public class PayActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int MERCHANT_ID = 1396424;

    private EditText editAmount;
    private Spinner spinnerCcy;
//    private EditText editEmail;
//    private EditText editDescription;
    private CardInputView cardInput;
    private CloudipspWebView webView;

    private Cloudipsp cloudipsp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pay_activity);

        findViewById(R.id.btn_amount).setOnClickListener(this);
        editAmount = (EditText) findViewById(R.id.edit_amount);
        spinnerCcy = (Spinner) findViewById(R.id.spinner_ccy);
//        editEmail = (EditText) findViewById(R.id.edit_email);
//        editDescription = (EditText) findViewById(R.id.edit_description);
        cardInput = (CardInputView) findViewById(R.id.card_input);
        cardInput.setHelpedNeeded(BuildConfig.DEBUG);
        findViewById(R.id.btn_pay).setOnClickListener(this);

        webView = (CloudipspWebView) findViewById(R.id.web_view);
        cloudipsp = new Cloudipsp(MERCHANT_ID, webView);

        spinnerCcy.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new Currency[]{Currency.RUB}));
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_amount:
//                fillTest();
                break;
            case R.id.btn_pay:
                processPay();
                break;
        }
    }

    private void processPay() {
        editAmount.setError(null);

        final int amount;
        try {
            amount = Integer.valueOf(editAmount.getText().toString());
        } catch (Exception e) {
            editAmount.setError(getString(R.string.e_invalid_amount));
            return;
        }

        final String email = UserProfileDto.User.getEmail();
        final String description = "Баланс";
//        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
//            editEmail.setError(getString(R.string.e_invalid_email));
//        } else if (TextUtils.isEmpty(description)) {
//            editDescription.setError(getString(R.string.e_invalid_description));
//        } else {
            final Card card = cardInput.confirm(new CardInputView.ConfirmationErrorHandler() {
                @Override
                public void onCardInputErrorClear(CardInputView view, EditText editText) {
                }

                @Override
                public void onCardInputErrorCatched(CardInputView view, EditText editText, String error) {
                }
            });

            if (card != null) {
                final Currency currency = (Currency) spinnerCcy.getSelectedItem();
                final Order order = new Order(amount, currency, "vb_" + System.currentTimeMillis(), description, email);

                cloudipsp.pay(card, order, new Cloudipsp.PayCallback() {
                    @Override
                    public void onPaidProcessed(Receipt receipt) {
                        new NetworkService().editBalance((int) (receipt.amount * 0.1) + receipt.amount);
                        finish();
                    }

                    @Override
                    public void onPaidFailure(Cloudipsp.Exception e) {
                        if (e instanceof Cloudipsp.Exception.Failure) {
                            Cloudipsp.Exception.Failure f = (Cloudipsp.Exception.Failure) e;

                            Toast.makeText(PayActivity.this, "Failure\nErrorCode: " +
                                    f.errorCode + "\nMessage: " + f.getMessage() + "\nRequestId: " + f.requestId, Toast.LENGTH_LONG).show();
                        } else if (e instanceof Cloudipsp.Exception.NetworkSecurity) {
                            Toast.makeText(PayActivity.this, "Network security error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        } else if (e instanceof Cloudipsp.Exception.ServerInternalError) {
                            Toast.makeText(PayActivity.this, "Internal server error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        } else if (e instanceof Cloudipsp.Exception.NetworkAccess) {
                            Toast.makeText(PayActivity.this, "Network error", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(PayActivity.this, "Payment Failed", Toast.LENGTH_LONG).show();
                        }
                        e.printStackTrace();
                    }
                });
            }
//        }
    }

    @Override
    public void onBackPressed() {
        if (webView.waitingForConfirm()) {
            webView.skipConfirm();
        } else {
            super.onBackPressed();
        }

    }
}