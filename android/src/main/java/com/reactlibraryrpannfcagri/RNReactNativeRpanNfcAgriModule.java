
package com.reactlibraryrpannfcagri;

import android.content.Context;
import android.widget.Toast;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.rfid.api.ADReaderInterface;
import com.rfid.def.ApiErrDefinition;

import java.util.Vector;

public class RNReactNativeRpanNfcAgriModule extends ReactContextBaseJavaModule implements LifecycleEventListener{

  private final ReactApplicationContext reactContext;
  private RNReactNativeRpanNfcAgriThread rpanNfcAgriThread = null;
  private CustomThread cThread = null;

  public RNReactNativeRpanNfcAgriModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
    this.reactContext.addLifecycleEventListener(this);
    cThread = new CustomThread(reactContext);

    this.rpanNfcAgriThread = new RNReactNativeRpanNfcAgriThread(this.reactContext) {

      @Override
      public void dispatchEvent(String name, WritableMap data) {
        RNReactNativeRpanNfcAgriModule.this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(name, data);
      }

      @Override
      public void dispatchEvent(String name, String data) {
        RNReactNativeRpanNfcAgriModule.this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(name, data);
      }

      @Override
      public void dispatchEvent(String name, WritableArray data) {
        RNReactNativeRpanNfcAgriModule.this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(name, data);
      }
    };

    rpanNfcAgriThread.start();
  }

  @Override
  public String getName() {
    return "RNReactNativeRpanNfcAgri";
  }

  @Override
  public void onHostResume() {
    if (rpanNfcAgriThread != null) {
      rpanNfcAgriThread.onHostResume();
    }
  }

  @Override
  public void onHostPause() {
    if (rpanNfcAgriThread != null) {
      rpanNfcAgriThread.onHostPause();
    }
  }

  @Override
  public void onHostDestroy() {
    if (rpanNfcAgriThread != null) {
      rpanNfcAgriThread.onHostDestroy();
    }
  }

  @Override
  public void onCatalystInstanceDestroy() {
    if (rpanNfcAgriThread != null) {
      rpanNfcAgriThread.onCatalystInstanceDestroy();
    }
  }
  @ReactMethod
  public void show(String text) {
    Context context = getReactApplicationContext();
    Toast.makeText(context, text, Toast.LENGTH_LONG).show();
  }

  @ReactMethod
  public void init() {
    if (rpanNfcAgriThread != null) {
      rpanNfcAgriThread.init(reactContext);
    }
  }

  //Connect to selected device
  @ReactMethod
  public void connect(String deviceName, Callback callback){
    if(rpanNfcAgriThread != null) {
      Boolean result = rpanNfcAgriThread.connect(deviceName);
      callback.invoke(result);
    }else{
      callback.invoke(false);
    }
  }

  @ReactMethod
  public void startScanning(Callback callback){
    if(rpanNfcAgriThread != null){
      WritableArray array = rpanNfcAgriThread.startScanning(reactContext);
      callback.invoke(array);
    }
  }

  @ReactMethod
  public void setPower(int value){
    if(rpanNfcAgriThread != null) {
      rpanNfcAgriThread.setPower(value);
    }
  }
  //Battery Status
  @ReactMethod
  public void getPower(Callback callback){
    if(rpanNfcAgriThread != null) {
      int power = rpanNfcAgriThread.getPower(reactContext);
      callback.invoke((power + 1) * 0.25);
    }
  }

  @ReactMethod
  public void disconnect(){
    if(rpanNfcAgriThread != null){
      rpanNfcAgriThread.disconnect();
    }
  }

  //Battery Status
  @ReactMethod
  public void getBattery(Callback callback){
  }



  //Devices list
  @ReactMethod
  public void devices(Callback callback){
    if (rpanNfcAgriThread != null) {
      WritableArray array = rpanNfcAgriThread.devices(reactContext);
      callback.invoke(array);
    }
  }

  @ReactMethod
  public void reconnect() {
    if (rpanNfcAgriThread != null) {
      rpanNfcAgriThread.reconnect();
    }
  }

  @ReactMethod
  public void read(ReadableMap config) {
    if (rpanNfcAgriThread != null) {
      rpanNfcAgriThread.read(config);
    }
  }

  @ReactMethod
  public void cancel() {
    if (rpanNfcAgriThread != null) {
      rpanNfcAgriThread.cancel();
    }
  }

  @ReactMethod
  public void shutdown() {
    if (rpanNfcAgriThread != null) {
      rpanNfcAgriThread.shutdown();
    }
  }
}