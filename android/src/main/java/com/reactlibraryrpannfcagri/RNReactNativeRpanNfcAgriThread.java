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
import com.rfid.def.ApiErrDefinition;

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
    private String conStr = null;

    public RNReactNativeRpanNfcAgriThread(ReactApplicationContext context){
        this.context = context;
    }

    public void run(){

    }

    public boolean connect(String conStr){
//        RDType=RPAN;CommType=BLUETOOTH;Name=%s
        this.conStr = conStr;
        int iret = m_reader.RDR_Open(conStr);
        if (iret == ApiErrDefinition.NO_ERROR) {
            return true;
        } else {
            return false;
        }
    }

    public  void disconnect(){
        m_reader.RDR_Close();
    }

    public void onHostResume() {
        if (readers != null){
            this.connect(this.conStr);
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
        this.disconnect();
    }

    public void cancel(){
        this.disconnect();
    }

    public void reconnect(){
        if(conStr != null){
            this.connect(conStr);
        }
    }

    public void read(ReadableMap config) {
    }

    public int getPower(ReactApplicationContext reactContext) {
        int nret = -1;
        Byte mPower = new Byte((byte) 0);
        nret = m_reader.RDR_GetRFPower(mPower);
        if (nret != ApiErrDefinition.NO_ERROR) {
            return -1;
        }
        return mPower.byteValue() - 1;
    }

    public void setPower(int value) {
        byte powerIndex = (byte) (value);
        m_reader.RDR_SetRFPower(powerIndex);
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

    public abstract void dispatchEvent(String name, WritableMap data);

    public abstract void dispatchEvent(String name, String data);

    public abstract void dispatchEvent(String name, WritableArray data);
}
