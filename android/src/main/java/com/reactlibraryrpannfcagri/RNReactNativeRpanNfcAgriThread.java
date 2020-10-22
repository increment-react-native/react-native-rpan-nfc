package com.reactlibraryrpannfcagri;

import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.io.Reader;

public abstract class RNReactNativeRpanNfcAgriThread extends Thread{
    private ReactApplicationContext context;
    WritableMap dataMap = null;
    Boolean reading = false;
    Reader readers = null;
    private  int selectedIndex = 0;
    private ReadableMap config = null;

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
        WritableArray test = null;
        return test;
    }

    public void init(ReactApplicationContext reactContext) {
    }
}
