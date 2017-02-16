package com.amuday.yeelightcontrol;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


public class SelectSsidDialogFragment extends DialogFragment {

    // controls:
    TextView SelectSsidText;
    ListView SsidListView;
    Button updateSsidButton;

    View view;

    String ITEM_KEY = "key";

    FragmentListener fragmentListener = new FragmentListener();

    WifiManager wifiManager;
    List<ScanResult> resultsList;
    ArrayList<HashMap<String, Object>> arraylist = new ArrayList<HashMap<String, Object>>();
    public SimpleAdapter listAdapter;

    private OnSsidSelectedListener mListener;

    public SelectSsidDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState == null){
            wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
            if (!wifiManager.isWifiEnabled())
            {
                Toast.makeText(getActivity(), "wifi is disabled..making it enabled", Toast.LENGTH_LONG).show();
                wifiManager.setWifiEnabled(true);
            }

            listAdapter = new SimpleAdapter(getActivity(), arraylist, R.layout.list_item_ssid, new String[] { "ItemSSID", "ItemBSSID", "ItemRSSI" }, new int[] { R.id.item_ssid, R.id.item_bssid, R.id.item_rssi });

            getActivity().registerReceiver(new BroadcastReceiver()
            {
                @Override
                public void onReceive(Context c, Intent intent)
                {
                    resultsList = wifiManager.getScanResults();
                }
            }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        }



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.dialog_fragment_select_ssid, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeControls();
        UpdateRssiList();
    }

    private void initializeControls() {
        SelectSsidText = (TextView) view.findViewById(R.id.text_select_ssid_from_list);
        SsidListView = (ListView) view.findViewById(R.id.list_ssid);
        updateSsidButton = (Button) view.findViewById(R.id.button_update_ssid);
        SsidListView.setAdapter(listAdapter);

        updateSsidButton.setOnClickListener(fragmentListener);
        SsidListView.setOnItemClickListener(fragmentListener);
    }

    private class FragmentListener implements View.OnClickListener, AdapterView.OnItemClickListener {

        @Override
        public void onClick(View v) {
            if(v == updateSsidButton) {
                //new UpdateSsidListThread().start(); //TODO: use thread here
                UpdateRssiList();
            }
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(parent == SsidListView){
                HashMap<String, Object> hmap = (HashMap<String, Object>) parent.getItemAtPosition(position);
                String ssidString = hmap.get("ItemSSID").toString();
                mListener.onSsidSelected(ssidString );
                dismiss();
            }
        }
    }

    private class UpdateSsidListThread extends Thread {

        @Override
        public void run() {
            Toast.makeText(getActivity(), "Scanning", Toast.LENGTH_SHORT).show();
            wifiManager.startScan();
            resultsList = wifiManager.getScanResults();
            int size = resultsList.size();

            try
            {
                arraylist.clear();

                Iterator<ScanResult> iscan = resultsList.iterator();
                while (iscan.hasNext()) {
                    ScanResult next = iscan.next();
                    HashMap<String, Object> map = new HashMap<String, Object>();
                    map.put("ItemSSID", next.SSID);
                    map.put("ItemBSSID", next.BSSID);
                    map.put("ItemRSSI", next.level);
                    arraylist.add(map);
                }
                listAdapter.notifyDataSetChanged();
            }
            catch (Exception e)
            { }


        }
    }

    public void UpdateRssiList() {
        wifiManager.startScan();
        if(resultsList!=null){
            Toast.makeText(getActivity(), "Scanning", Toast.LENGTH_SHORT).show();
        }
        resultsList = wifiManager.getScanResults();
        try
        {
            arraylist.clear();

            Iterator<ScanResult> iscan = resultsList.iterator();
            while (iscan.hasNext()) {
                ScanResult next = iscan.next();
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("ItemSSID", next.SSID);
                map.put("ItemBSSID", next.BSSID);
                map.put("ItemRSSI", next.level);
                arraylist.add(map);
            }
            listAdapter.notifyDataSetChanged();
        }
        catch (Exception e)
        { }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSsidSelectedListener) {
            mListener = (OnSsidSelectedListener) context;
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


    public interface OnSsidSelectedListener {
        public void onSsidSelected(String ssidString);
    }



}
