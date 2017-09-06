package com.eagles.android.exchangerate;

import android.content.Context;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.net.ConnectivityManager;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity{

    public static final String TAG = MainActivity.class.getSimpleName();

    private double mRate;
    private EditText mInput;
    private Spinner mInitialChoices;
    private TextView mConvertedAmt;
    private Spinner mConvertedChoices;
    private Button mButton;
    String initialText;
    String finalText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mInput = (EditText) findViewById(R.id.inputText);
        mInitialChoices = (Spinner)findViewById(R.id.initialAmtChoices);
        mConvertedAmt = (TextView)findViewById(R.id.convertedText);
        mConvertedChoices = (Spinner) findViewById(R.id.convertedChoices);
        mButton = (Button) findViewById(R.id.button);


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.exchange_rate_choices, R.layout.spinner);

        adapter.setDropDownViewResource(R.layout.spinner);
        mInitialChoices.setAdapter(adapter);
        mConvertedChoices.setAdapter(adapter);
        mInitialChoices.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                initialText = parent.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mConvertedChoices.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                finalText = parent.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(containsOnlyNumbers(mInput.getText().toString())) {
                    if (initialText == finalText) {
                        mConvertedAmt.setText(mInput.getText());
                    } else {
                        getExchangeRates(initialText, finalText);
                        Log.d(TAG, "" + initialText + finalText + "   " + mRate);


                        double init = Integer.valueOf(mInput.getText().toString());
                        mConvertedAmt.setText("" + init * mRate);
                    }
                }
                else
                {
                    Toast.makeText(MainActivity.this, "Please input numbers", Toast.LENGTH_SHORT).show();
                }


            }

        });


    }
    private   boolean containsOnlyNumbers(String str) {
        if(str.isEmpty() || str ==null)
        {
            return false;
        }
        else {
            for (int i = 0; i < str.length(); i++) {
                if (!Character.isDigit(str.charAt(i)))
                    return false;
            }
        }
        return true;
    }

        private void getExchangeRates(String currencyInitial, final String currencyFinal) {


        String forecastUrl = "http://api.fixer.io/latest?base=" + currencyInitial;


        if (isNetworkAvailable()) {


            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(forecastUrl)
                    .build();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        }
                    });
                  //  errorPopUp();

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        }
                    });

                    try {
                        String jsonData = response.body().string();
                        Log.v(TAG, jsonData);
                        if (response.isSuccessful()) {
                            mRate = getRate(jsonData, currencyFinal);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                  //  updateDisplay();

                                }
                            });

                        } else {
                           // errorPopUp();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    }
                    catch (JSONException e) {
                        Log.e(TAG, "Exception caught: ");

                    }
                }
            });
        } else {
            Toast.makeText(this, R.string.nework_unavailable_message, Toast.LENGTH_LONG).show();
        }
    }

    private double getRate(String jsonData, String currencyFinal) throws  JSONException {

        double rate;
        JSONObject jobj = new JSONObject(jsonData);
        JSONObject rates = jobj.getJSONObject("rates");
        rate = rates.getDouble(currencyFinal);

        if (rate == 0)
        {
            Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
        }
        return  rate;

    }
    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }

        return isAvailable;
    }
}
