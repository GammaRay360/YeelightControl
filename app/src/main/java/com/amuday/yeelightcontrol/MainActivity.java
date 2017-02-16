package com.amuday.yeelightcontrol;

import android.net.Uri;
import android.net.wifi.ScanResult;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SelectSsidDialogFragment.OnSsidSelectedListener,SelectBulbDialogFragment.OnBulbSelectedListener, PreferencesFragment.onStateChangeListener,DisplayFragment.OnFragmentInteractionListener {
    // handler messages:
    private static final int MSG_TOAST = 1;
    private static final int MSG_STOP_SEARCH = 2;
    private static final int MSG_DISCOVER_FINISH = 3;


    PreferencesFragment preferencesFragment;
    DisplayFragment displayFragment;
    private String ssidString;
    private String bulbIpString;
    private final int bulbPortString = 55443;
    ControlYeelight controlYeelight;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            preferencesFragment = new PreferencesFragment();
            displayFragment = new DisplayFragment();
            FragmentTransaction SupportTransaction = getSupportFragmentManager().beginTransaction();
            SupportTransaction.replace(R.id.frame_preferences, preferencesFragment ,"PreferencesFragmentTag");
            SupportTransaction.replace(R.id.frame_display, displayFragment ,"DisplayFragmentTag");
            SupportTransaction.commit();


        }
        else{
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction SupportTransaction = fragmentManager.beginTransaction();
            SupportTransaction.replace(R.id.frame_preferences, fragmentManager.findFragmentByTag("PreferencesFragmentTag"));
            SupportTransaction.replace(R.id.frame_display,fragmentManager.findFragmentByTag("DisplayFragmentTag"));
            SupportTransaction.commit();
        }
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what){
                case MSG_TOAST:
                    Toast.makeText(MainActivity.this,(String) msg.obj , Toast.LENGTH_SHORT).show();
                    break;


            }

        }
    };



    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onSsidSelected(String ssidString) {
        preferencesFragment.setSelectedSsid(ssidString);
        this.ssidString = ssidString;

    }

    @Override
    public void onBulbSelected(String BulbIpString) {
        preferencesFragment.setSelectedBulb(bulbIpString);
        this.bulbIpString = ssidString;
        controlYeelight = new ControlYeelight(bulbIpString,bulbPortString,handler);
    }

    @Override
    public void onTriggered() {
        Message msg = handler.obtainMessage(MSG_TOAST,"Triggered");
        handler.sendMessage(msg);
        controlYeelight.write(controlYeelight.parseSwitch(true));
    }

    @Override
    public void onStopped() {
        Message msg = handler.obtainMessage(MSG_TOAST,"Stopped");
        handler.sendMessage(msg);
        controlYeelight.write(controlYeelight.parseSwitch(false));
    }

    @Override
    public void onUpdateRssi(int rssi) {
        displayFragment.updateRssiDisplay(rssi);
    }
}
