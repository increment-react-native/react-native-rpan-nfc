package com.reactlibraryrpannfcagri;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.rfid.api.ADReaderInterface;
import com.rfid.api.BluetoothCfg;
import com.rfid.api.GFunction;
import com.rfid.api.ISO14443AInterface;
import com.rfid.api.ISO14443ATag;
import com.rfid.api.ISO15693Interface;
import com.rfid.api.ISO15693Tag;
import com.rfid.def.ApiErrDefinition;
import com.rfid.def.RfidDef;

import java.io.Reader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Vector;

public abstract class RNReactNativeRpanNfcAgriThread extends Thread{
    private ReactApplicationContext context;
    WritableMap dataMap = null;
    Boolean reading = false;
    Reader readers = null;
    private  int selectedIndex = 0;
    private ReadableMap config = null;
    static ADReaderInterface m_reader = new ADReaderInterface();
    private String conStr = null;
    private Thread getScanRecord = null;
    private Boolean bGetScanRecordFlg = false;
    private static final int INVENTORY_MSG = 1;
    private static final int GETSCANRECORD = 2;
    private static final int INVENTORY_FAIL_MSG = 4;
    private static final int THREAD_END = 3;

    public RNReactNativeRpanNfcAgriThread(ReactApplicationContext context){
        this.context = context;
    }

    @Override
    public void run() {

    }

    public String connect(String deviceName){
        String device = String.format("RDType=RPAN;CommType=BLUETOOTH;Name=%s", deviceName);
        this.conStr = device;
        int iret = m_reader.RDR_Open(device);
        if (iret == ApiErrDefinition.NO_ERROR) {
            Toast.makeText(context,
                    device + " successfully connected",
                    Toast.LENGTH_SHORT).show();
            return "connected";
        } else {
            Toast.makeText(context,
                    device + " error",
                    Toast.LENGTH_SHORT).show();
            return "error";
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

    public String  startScanning(){
        int nret = 0;
        bGetScanRecordFlg = true;
        String strData = "";
        byte gFlg = 0x00;// ���βɼ����ݻ�����һ�βɼ�����ʧ��ʱ����־λΪ0x00
        Object dnhReport = null;
        while (bGetScanRecordFlg)
        {
            nret = m_reader.RDR_BuffMode_FetchRecords(gFlg);
            if (nret != ApiErrDefinition.NO_ERROR)
            {
                gFlg = 0x00;
                continue;
            }
            gFlg = 0x01;
            dnhReport = m_reader
                    .RDR_GetTagDataReport(RfidDef.RFID_SEEK_FIRST);
            Vector<String> dataList = new Vector<String>();
            while (dnhReport != null)
            {
                byte[] byData = new byte[32];
                int[] len = new int[1];
                len[0] = byData.length;
                if (ADReaderInterface.RDR_ParseTagDataReportRaw(dnhReport, byData,
                        len) == 0)
                {
                    if (len[0] > 0)
                    {
                        return GFunction.encodeHexStr(byData,len[0]);
                    }
                }
                dnhReport = m_reader
                        .RDR_GetTagDataReport(RfidDef.RFID_SEEK_NEXT);
            }
        }
        bGetScanRecordFlg = false;
        return strData;
    }

    public int getPower(ReactApplicationContext reactContext) {
        int nret = -1;
        Byte mPower = new Byte((byte) 0);
        nret = m_reader.RDR_GetRFPower(mPower);
        if (nret != ApiErrDefinition.NO_ERROR) {
            return -1;
        }
        return mPower.byteValue() - 1;
//        return nret;
    }

    public void setPower(int value) {
        byte powerIndex = (byte) (value);
        m_reader.RDR_SetRFPower(powerIndex);
    }

    public WritableArray devices(ReactApplicationContext reactContext) {
        WritableArray array = Arguments.createArray();
        ArrayList<BluetoothCfg> m_blueList = ADReaderInterface
                .GetPairBluetooth();
        if (m_blueList != null)
        {
            for (BluetoothCfg bluetoolCfg : m_blueList)
            {
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
