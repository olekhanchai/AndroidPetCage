package com.example.vetwellvet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.SpiDevice;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private String settingsDelimiter;
    public static boolean shown = false;
    private String receiveDataFormat;
    private String delimiter;
    private TextView tempText;
    private TextView oxygenText;
    private TextView carbonText;
    private TextView humidText;
    private TextView timeValue;

    private float tempVal;
    private float oxygenVal;
    private float carbonVal;
    private float humidVal;

    public static boolean isSending = false;
    public static int counter = 1;
    public static byte[] txBytes = new byte[32];
    public static byte[] rxBytes = new byte[32];

    SpiDevice mDevice;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        PeripheralManager pioService = PeripheralManager.getInstance();
        try {
            mDevice = pioService.openSpiDevice("SPI0.0");
            mDevice.setFrequency(500000);
            mDevice.setMode(0);
            mDevice.setBitsPerWord(8);
            mDevice.setBitJustification(SpiDevice.BIT_JUSTIFICATION_MSB_FIRST);
        } catch (RuntimeException | IOException e) {
            Log.e("err", "Error" + e.getMessage());
        }

        tempText = (TextView) findViewById(R.id.txtTempValue);
        carbonText = (TextView) findViewById(R.id.txtCabonValue);
        humidText = (TextView) findViewById(R.id.humidityValue);
        oxygenText = (TextView) findViewById(R.id.txtOxygenValue);
        timeValue = (TextView) findViewById(R.id.txtTimeValue);

        ImageButton buttonHome= (ImageButton) findViewById(R.id.btn_home);
        buttonHome.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                LinearLayout homeLayout = (LinearLayout) findViewById(R.id.home_group);
                LinearLayout settingLayout = (LinearLayout) findViewById(R.id.settings_group);
                homeLayout.setVisibility(View.VISIBLE);
                settingLayout.setVisibility(View.GONE);
            }
        });

        ImageButton buttonSetting= (ImageButton) findViewById(R.id.btn_settings);
        buttonSetting.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                LinearLayout homeLayout = (LinearLayout) findViewById(R.id.home_group);
                LinearLayout settingLayout = (LinearLayout) findViewById(R.id.settings_group);
                homeLayout.setVisibility(View.GONE);
                settingLayout.setVisibility(View.VISIBLE);
            }
        });

        Button buttonDegreeUnit = (Button) findViewById(R.id.btnDegreeUnit);
        buttonDegreeUnit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (buttonDegreeUnit.getText() == "C/[F]") {
                    buttonDegreeUnit.setText("[C]/F");
                } else {
                    buttonDegreeUnit.setText("C/[F]");
                }
            }
        });

        Button buttonTurn = (Button) findViewById(R.id.btnTurn);
        buttonTurn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (buttonTurn.getText() == "ON") {
                    buttonTurn.setText("OFF");

                    isSending = true;
                    txBytes[1] = (byte)0x00;
//                    try {
//                        mDevice.transfer(txBytes, rxBytes, txBytes.length);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }

                } else {
                    buttonTurn.setText("ON");

                    isSending = true;
                    txBytes[1] = (byte)0x00;
//                    try {
//                        mDevice.transfer(txBytes, rxBytes, txBytes.length);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                }
            }
        });

        AppCompatSeekBar sliderWarm = (AppCompatSeekBar) findViewById(R.id.sliderWarm);
        sliderWarm.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                carbonText.setText(progress+"");
//                isSending = true;
//                txBytes[1] = (byte)progress;
//                try {
//                    mDevice.transfer(txBytes, rxBytes, txBytes.length);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        AppCompatSeekBar sliderWhite = (AppCompatSeekBar) findViewById(R.id.sliderWhite);
        sliderWhite.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                carbonText.setText(progress+"");
//                isSending = true;
//                txBytes[1] = (byte)progress;
//                try {
//                    mDevice.transfer(txBytes, rxBytes, txBytes.length);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        Handler timerHandler = new Handler();
        Runnable timerRunnable = new Runnable() {
            @Override
            public void run() {
                String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                timeValue.setText(currentTime);

                counter++;

                if (counter == 5 && !isSending) {
                    rxBytes[0] = (byte) 0x0F; // RX header
                    rxBytes[1] = (byte) 0xFF; // output I/O
                    rxBytes[2] = (byte) 0x0F; // cooler
                    rxBytes[3] = (byte) 0x01; // alarm

                    rxBytes[4] = (byte) 0x0F; // adc0 byte1
                    rxBytes[5] = (byte) 0x0F; // adc0 byte2

                    rxBytes[6] = (byte) 0x0F; // adc1 byte1
                    rxBytes[7] = (byte) 0x0F; // adc1 byte2

                    rxBytes[8] = (byte) 0x0F; // adc2 byte1
                    rxBytes[9] = (byte) 0x0F; // adc2 byte2

                    rxBytes[10] = (byte) 0x42; // O2 byte1
                    rxBytes[11] = (byte) 0x43; // O2 byte2
                    rxBytes[12] = (byte) 0x3A; // O2 byte3
                    rxBytes[13] = (byte) 0x74; // O2 byte4

                    rxBytes[14] = (byte) 0x42; // humid byte1
                    rxBytes[15] = (byte) 0x43; // humid byte2
                    rxBytes[16] = (byte) 0x3A; // humid byte3
                    rxBytes[17] = (byte) 0x84; // humid byte4

                    rxBytes[18] = (byte) 0x41; // temp byte1
                    rxBytes[19] = (byte) 0xD9; // temp byte2
                    rxBytes[20] = (byte) 0xE7; // temp byte3
                    rxBytes[21] = (byte) 0xFF; // temp byte4

                    rxBytes[22] = (byte) 0x43; // CO2 byte1
                    rxBytes[23] = (byte) 0xDB; // CO2 byte2
                    rxBytes[24] = (byte) 0x8C; // CO2 byte3
                    rxBytes[25] = (byte) 0x2E; // CO2 byte4

                    rxBytes[26] = (byte) 0x0F; // reserved
                    rxBytes[27] = (byte) 0x0F; // reserved
                    rxBytes[28] = (byte) 0x0F; // reserved
                    rxBytes[29] = (byte) 0x0F; // reserved
                    rxBytes[30] = (byte) 0x0F; // reserved
                    rxBytes[31] = (byte) 0x0F; // reserved

                    byte[] temp = new byte[4];
                    byte[] humid = new byte[4];
                    byte[] carbon = new byte[4];
                    byte[] oxygen = new byte[4];

                    temp[0] = rxBytes[18];
                    temp[1] = rxBytes[19];
                    temp[2] = rxBytes[20];
                    temp[3] = rxBytes[21];

                    carbon[0] = rxBytes[22];
                    carbon[1] = rxBytes[23];
                    carbon[2] = rxBytes[24];
                    carbon[3] = rxBytes[25];

                    humid[0] = rxBytes[14];
                    humid[1] = rxBytes[15];
                    humid[2] = rxBytes[16];
                    humid[3] = rxBytes[17];

                    oxygen[0] = rxBytes[10];
                    oxygen[1] = rxBytes[11];
                    oxygen[2] = rxBytes[12];
                    oxygen[3] = rxBytes[13];

                    tempVal = byteToFlat(temp);
                    oxygenVal = byteToFlat(oxygen);
                    humidVal = byteToFlat(humid);
                    carbonVal = byteToFlat(carbon);

                    tempText.setText((int)tempVal + " °C");
                    oxygenText.setText((int)oxygenVal + " %");
                    humidText.setText((int)humidVal + " %");
                    carbonText.setText((int)carbonVal + " ppm");

//                    try {
//                        mDevice.transfer(txBytes, rxBytes, txBytes.length);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }

                    counter = 1;
                    isSending = false;
                }

                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.postDelayed(timerRunnable, 0);
    }

    float byteToFlat(byte[] bytes)
    {
        int asInt = ((bytes[0] & 0xFF) << 24)
                | ((bytes[1] & 0xFF) << 16)
                | ((bytes[2] & 0xFF) << 8)
                | (bytes[3] & 0xFF);
        return Float.intBitsToFloat(asInt);
    }
}