package com.reactlibraryrpannfcagri;

import android.util.Log;
import android.widget.ArrayAdapter;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.rfid.api.ADReaderInterface;
import com.rfid.api.BluetoothCfg;

import java.io.Reader;
import java.util.ArrayList;

public abstract class RNReactNativeRpanNfcAgriThread extends Thread{
    private ReactApplicationContext context;
    WritableMap dataMap = null;
    Boolean reading = false;
    Reader readers = null;
    private  int selectedIndex = 0;
    private ReadableMap config = null;
    static ADReaderInterface m_reader = new ADReaderInterface();

    public RNReactNativeRpanNfcAgriThread(ReactApplicationContext context){
        this.context = context;
    }

    public void run(){

    }

    public WritableMap connect(int index){
        dataMap = Arguments.createMap();
        return dataMap;
    }

    public  void disconnect(){
    }

    public void onHostResume() {
        if (readers != null){
            this.connect(selectedIndex);
        } else {
            Log.e("RFID", "Can't resume - reader is null");
        }
    }

    public void onHostPause() {
        if (this.reading){
            this.cancel();
        }
        this.disconnect();
    }

    public void onHostDestroy() {
        if (this.reading){
            this.cancel();
        }
        shutdown();
    }

    public void onCatalystInstanceDestroy() {
        if (this.reading){
            this.cancel();
        }
        shutdown();
    }

    public void shutdown(){
        //
    }

    public void cancel(){
        //
    }

    public void reconnect(){

    }

    public void read(ReadableMap config) {
    }

    public int getBattery() {
        return 1;
    }

    public void power(int value) {
    }

    public WritableArray devices(ReactApplicationContext reactContext) {
        ArrayList<CharSequence> m_bluetoolNameList = null;
        WritableArray array = Arguments.createArray();
        WritableMap event = Arguments.createMap();
        ArrayList<BluetoothCfg> m_blueList = ADReaderInterface
                .GetPairBluetooth();
        if (m_blueList != null)
        {
            for (BluetoothCfg bluetoolCfg : m_blueList)
            {
                m_bluetoolNameList.add(bluetoolCfg.GetName());
                WritableMap map = Arguments.createMap();
                map.putString("model", null);
                map.putString("serial", null);
                map.putString("battery", null);
                map.putString("host", bluetoolCfg.GetName());
                array.pushMap(map);
            }
        }

        return array;
    }

    public void init(ReactApplicationContext reactContext) {
    }
}
