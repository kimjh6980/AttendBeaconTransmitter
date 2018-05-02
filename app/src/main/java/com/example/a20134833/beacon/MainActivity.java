package com.example.a20134833.beacon;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final String TAG = "sampleCreateBeacon";

    TextView status;
    boolean statusbool = false;
    Button button;

    BeaconTransmitter beaconTransmitter;
    BeaconParser beaconParser;
    BeaconManager beaconManager;
    Beacon beacon;

    EditText t1, t2, t3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        status = findViewById(R.id.status);
        button = findViewById(R.id.button);

        t1 = findViewById(R.id.t1);
        t2 = findViewById(R.id.t2);
        t3 = findViewById(R.id.t3);

        // 퍼미션 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                        }
                    }
                });
                builder.show();
            }
        }

        beaconManager = BeaconManager.getInstanceForApplication(this);
    }

    Long[] datafield = {4487004180502l};

    public void button(View view)    {
        //String uuid = "2f234454-cf6d-4a0f-adf2-"+t1.getText().toString()+t2.getText().toString()+t3.getText().toString();
        String uuid = "2F234454-CF6D-4A0F-ADF2-F4911BA9FFA6";

        if (!statusbool) {   // false = 꺼진상태 -> 켜야됨
            // 비콘 생성 후 시작. 실제 가장 필요한 소스
            beacon = new Beacon.Builder()
                    .setId1(uuid)  // uuid for beacon
                    .setId2("44870")  // major
                    .setId3("04")  // minor
                    .setManufacturer(0x004C)  // Radius Networks. 0x0118 : Change this for other beacon layouts // 0x004C : for iPhone
                    .setTxPower(-59)  // Power in dB
                    //.setDataFields(Arrays.asList(new Long[]{0l}))  // Remove this for beacon layouts without d: fields
                    .setDataFields(Arrays.asList(new Long[]{180503L}))
                    .build();
            Log.e("beacon data =", String.valueOf(beacon.getDataFields()));
            beaconParser = new BeaconParser()
                    .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-27");
        /* beacon layout
        ALTBEACON   "m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"
        EDDYSTONE  TLM  "x,s:0-1=feaa,m:2-2=20,d:3-3,d:4-5,d:6-7,d:8-11,d:12-15"
        EDDYSTONE  UID  "s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19"
        EDDYSTONE  URL  "s:0-1=feaa,m:2-2=10,p:3-3:-41,i:4-20v"
        IBEACON  "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"
        m: 에는 비턴 타입을 나타내는 문자열을 매칭하며 한번만 기술한다.
        i: ID에 해당하는 필드로 여러 개를 정의하여 매칭 할 수 있다.
        p: power calibration 필드로 한번만 기술한다.
        d: 추가 데이터 필드로 여러 개를 정의하여 매칭 할 수 있다.
         */

            beaconTransmitter = new BeaconTransmitter(getApplicationContext(), beaconParser);
            Log.e("beaconParser/", String.valueOf(beaconParser));
            Log.e("Transmitter/", String.valueOf(beaconTransmitter));

            beaconTransmitter.startAdvertising(beacon, new AdvertiseCallback() {
                @Override
                public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                    super.onStartSuccess(settingsInEffect);
                    Log.d(TAG, "onStartSuccess: ");
                    status.setText("running");
                    statusbool = true;
                }

                @Override
                public void onStartFailure(int errorCode) {
                    super.onStartFailure(errorCode);
                    Log.d(TAG, "onStartFailure: " + errorCode);
                    status.setText("Error = " + errorCode);
                }
            });
        } else    {   // true = 켜진상태 -> 꺼야됨
            beaconTransmitter.stopAdvertising();
            statusbool = false;
            status.setText("stop");
        }

    }

    // 퍼미션 요청후 callback
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("", "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }
}