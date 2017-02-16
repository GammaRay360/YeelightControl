package com.amuday.yeelightcontrol;

import android.app.FragmentTransaction;
import android.content.Context;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PreferencesFragment.onStateChangeListener} interface
 * to handle interaction events.
 * Use the {@link PreferencesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PreferencesFragment extends Fragment {

    // controls:
    TextView selectSsidText;
    TextView selectedSsidText;
    TextView selectBulbText;
    TextView selectedBulbText;
    TextView samplingRateText;
    SeekBar samplingRateSeekBar;
    TextView samplingRateInputText;
    TextView thresholdText;
    SeekBar thresholdSeekBar;
    TextView thresholdInputText;
    Button startButton;
    LinearLayout ssidItem;
    LinearLayout bulbItem;
    LinearLayout samplingRateItem;
    LinearLayout thresholdItem;

    View view;

    String ssidString = null;
    String bulbIpString = null;
    FragmentListener fragmentListener = new FragmentListener();

    TrackingThread trackingThread;
    boolean trackingStarted=false;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private onStateChangeListener mListener;

    public PreferencesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PreferencesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PreferencesFragment newInstance(String param1, String param2) {
        PreferencesFragment fragment = new PreferencesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }




    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_preferences, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeControls();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof onStateChangeListener) {
            mListener = (onStateChangeListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface onStateChangeListener {
        void onTriggered();
        void onStopped();
        void onUpdateRssi(int rssi);
    }

    private void initializeControls(){
        selectSsidText = (TextView) view.findViewById(R.id.text_select_ssid);
        selectedSsidText = (TextView) view.findViewById(R.id.text_selected_ssid);
        selectBulbText = (TextView) view.findViewById(R.id.text_select_bulb);
        selectedBulbText = (TextView) view.findViewById(R.id.text_selected_bulb);
        samplingRateText = (TextView) view.findViewById(R.id.text_sampling_rate);
        samplingRateSeekBar = (SeekBar)view.findViewById(R.id.seek_bar_sampling_rate);
        samplingRateInputText = (TextView) view.findViewById(R.id.text_input_sampling_rate);
        thresholdText = (TextView) view.findViewById(R.id.text_threshold);
        thresholdSeekBar = (SeekBar) view.findViewById(R.id.seek_bar_threshold);
        thresholdInputText = (TextView) view.findViewById(R.id.text_input_threshold);
        startButton = (Button) view.findViewById(R.id.button_start_stop);
        ssidItem = (LinearLayout) view.findViewById(R.id.item_ssid);
        bulbItem = (LinearLayout) view.findViewById(R.id.item_bulb);
        samplingRateItem = (LinearLayout) view.findViewById(R.id.item_sampling_rate);;
        thresholdItem = (LinearLayout) view.findViewById(R.id.item_threshold);;
        ssidItem.setOnClickListener(fragmentListener);
        bulbItem.setOnClickListener(fragmentListener);
        samplingRateSeekBar.setOnSeekBarChangeListener(fragmentListener);
        thresholdSeekBar.setOnSeekBarChangeListener(fragmentListener);
        startButton.setOnClickListener(fragmentListener);
        samplingRateInputText.setText((samplingRateSeekBar.getProgress()+1)+"samples/s");
        thresholdInputText.setText(thresholdSeekBar.getProgress()+" |RSSI|");




        }

    private class FragmentListener implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
        @Override
        public void onClick(View v) {
            if(v == ssidItem){
                if(trackingStarted)
                {
                    stopTracking();
                }
                FragmentManager fm = getFragmentManager();
                SelectSsidDialogFragment dialogFragment = new SelectSsidDialogFragment ();
                dialogFragment.show(fm, "SelectSsidDialogFragmentTag");
            }
            if(v == bulbItem){
                if(trackingStarted)
                {
                    stopTracking();
                }
                FragmentManager fm = getFragmentManager();
                SelectBulbDialogFragment dialogFragment = new SelectBulbDialogFragment ();
                dialogFragment.show(fm, "SelectBulbDialogFragmentTag");
            }

            if(v == startButton){
                if(!trackingStarted) {
                    startTracking();
                }
                else // if tracking started
                {
                    stopTracking();
                }
            }
        }

        public void startTracking(){
            trackingStarted = true;
            startButton.setText(getResources().getString(R.string.stop_tracking));
            trackingThread = new TrackingThread(ssidString, thresholdSeekBar.getProgress(), samplingRateSeekBar.getProgress() + 1);
            trackingThread.start();
        }

        public void stopTracking(){
            trackingStarted = false;
            startButton.setText(getResources().getString(R.string.start_tracking));
            mListener.onUpdateRssi(0);
            if(trackingThread != null) {
                trackingThread.interrupt();
                trackingThread = null;
            }
        }


        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(seekBar == samplingRateSeekBar){
                samplingRateInputText.setText((samplingRateSeekBar.getProgress()+1)+"samples/s");
                if(trackingStarted) {
                    trackingThread.setSamplingRate(progress + 1);
                }
            }
            else if(seekBar == thresholdSeekBar){
                thresholdInputText.setText(thresholdSeekBar.getProgress()+" |RSSI|");
                if(trackingStarted) {
                    trackingThread.setThreshold(progress);
                }

            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            if(seekBar == samplingRateSeekBar){
            }
            else if(seekBar == thresholdSeekBar){
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if(seekBar == samplingRateSeekBar){
            }
            else if(seekBar == thresholdSeekBar){
            }
        }
    }

    void setSelectedSsid(String ssidString){
        selectedSsidText.setText(ssidString);
        this.ssidString = ssidString;
        if(this.bulbIpString != null){
            startButton.setEnabled(true);
        }
    }

    void setSelectedBulb(String bulbIpString){
        selectedBulbText.setText(bulbIpString);
        this.bulbIpString = ssidString;
        if(this.ssidString != null){
            startButton.setEnabled(true);
        }
    }

    class TrackingThread extends Thread{
        private boolean running = true;
        private String ssidString;
        private WifiManager wifiManager;
        private List<ScanResult> resultsList;
        private int samplingRate;
        private int threshold;
        boolean triggered = false;
        boolean triggeredRecheck = false;  // todo change if needed
        boolean stoppedRecheck = true;

        public synchronized int getSamplingRate() {
            return samplingRate;
        }

        public synchronized void setSamplingRate(int samplingRate) {
            this.samplingRate = samplingRate;

        }

        public synchronized int getThreshold() {
            return threshold;
        }

        public synchronized void setThreshold(int threshold) {
            this.threshold = threshold;
        }




        public TrackingThread(String ssidString,int threshold, int samplingRate){
            wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
            if (!wifiManager.isWifiEnabled())
            {
                Toast.makeText(getActivity(), "wifi is disabled..making it enabled", Toast.LENGTH_LONG).show(); //TODO fixme
                wifiManager.setWifiEnabled(true);
            }

            this.ssidString = ssidString;
            this.threshold = threshold;
            this.samplingRate = samplingRate;
        }
        @Override
        public void run() {
            while(running){

                wifiManager.startScan();
                resultsList = wifiManager.getScanResults();
                for(ScanResult result : resultsList) {
                    if (result.SSID.compareTo(ssidString) == 0) {
                        mListener.onUpdateRssi(-result.level);

                        if(triggered && -result.level > getThreshold() ){
                            if(stoppedRecheck)
                            {
                                stoppedRecheck = false;
                                try {
                                    sleep(1000);
                                } catch (InterruptedException e) {
                                    running = false;
                                }
                            }
                            else {
                                triggered = false;
                                mListener.onStopped();
                                try {
                                    sleep(2000);
                                } catch (InterruptedException e) {
                                    running = false;
                                }
                            }
                        }
                        else if(!triggered &&  -result.level <= getThreshold())
                        {
                            if(triggeredRecheck)
                            {
                                triggeredRecheck = false;
                                try {
                                    sleep(1000);
                                } catch (InterruptedException e) {
                                    running = false;
                                }
                            }
                            else {
                                triggered = true;
                                mListener.onTriggered();
                                try {
                                    sleep(2000);
                                } catch (InterruptedException e) {
                                    running = false;
                                }
                            }
                        }
                        else
                        {
                            triggeredRecheck = false; // todo change if needed
                            stoppedRecheck = true;
                        }

                        break;
                    }
                }
                try {
                    sleep(1000/samplingRate);
                } catch (InterruptedException e) {
                    running = false;
                }


            }

        }
    }
}
