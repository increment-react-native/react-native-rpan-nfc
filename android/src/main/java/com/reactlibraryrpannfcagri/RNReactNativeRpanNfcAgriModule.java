
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

public class RNReactNativeRpanNfcAgriModule extends ReactContextBaseJavaModule implements LifecycleEventListener{

  private final ReactApplicationContext reactContext;
  private RNReactNativeRpanNfcAgriThread rpanNfcAgriThread = null;

  public RNReactNativeRpanNfcAgriModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
    this.reactContext.addLifecycleEventListener(this);

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
    if (this.rpanNfcAgriThread != null) {
      this.rpanNfcAgriThread.onHostResume();
    }
  }

  @Override
  public void onHostPause() {
    if (this.rpanNfcAgriThread != null) {
      this.rpanNfcAgriThread.onHostPause();
    }
  }

  @Override
  public void onHostDestroy() {
    if (this.rpanNfcAgriThread != null) {
      this.rpanNfcAgriThread.onHostDestroy();
    }
  }

  @Override
  public void onCatalystInstanceDestroy() {
    if (this.rpanNfcAgriThread != null) {
      this.rpanNfcAgriThread.onCatalystInstanceDestroy();
    }
  }
  @ReactMethod
  public void show(String text) {
    Context context = getReactApplicationContext();
    Toast.makeText(context, text, Toast.LENGTH_LONG).show();
  }

  @ReactMethod
  public void init() {
    if (this.rpanNfcAgriThread != null) {
      this.rpanNfcAgriThread.init(reactContext);
    }
  }

  @ReactMethod
  public void startScanning(Callback callback){
    if(this.rpanNfcAgriThread != null){
      String code = this.rpanNfcAgriThread.startScanning();
      callback.invoke(code);
    }
  }

  @ReactMethod
  public void setPower(int value){
    if(this.rpanNfcAgriThread != null) {
      this.rpanNfcAgriThread.setPower(value);
    }
  }
  //Battery Status
  @ReactMethod
  public void getPower(Callback callback){
    if(this.rpanNfcAgriThread != null) {
      int power = this.rpanNfcAgriThread.getPower(reactContext);
      callback.invoke((power + 1) * 0.25);
    }
  }

  @ReactMethod
  public void disconnect(){
    if(this.rpanNfcAgriThread != null){
      this.rpanNfcAgriThread.disconnect();
    }
  }

  //Battery Status
  @ReactMethod
  public void getBattery(Callback callback){
  }

  //Connect to selected device
  @ReactMethod
  public void connect(String deviceName, Callback callback){
    if(this.rpanNfcAgriThread != null) {
      Boolean result = this.rpanNfcAgriThread.connect(deviceName);
      callback.invoke(result);
    }
  }

  //Devices list
  @ReactMethod
  public void devices(Callback callback){
    if (this.rpanNfcAgriThread != null) {
      WritableArray array = this.rpanNfcAgriThread.devices(reactContext);
      callback.invoke(array);
    }
  }

  @ReactMethod
  public void reconnect() {
    if (this.rpanNfcAgriThread != null) {
      this.rpanNfcAgriThread.reconnect();
    }
  }

  @ReactMethod
  public void read(ReadableMap config) {
    if (this.rpanNfcAgriThread != null) {
      this.rpanNfcAgriThread.read(config);
    }
  }

  @ReactMethod
  public void cancel() {
    if (this.rpanNfcAgriThread != null) {
      this.rpanNfcAgriThread.cancel();
    }
  }

  @ReactMethod
  public void shutdown() {
    if (this.rpanNfcAgriThread != null) {
      this.rpanNfcAgriThread.shutdown();
    }
  }
}