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

public abstract class RNReactNativeRpanNfcAgriThread{
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


    private Handler mHandler = new MyHandler(this, context);

    private static class MyHandler extends Handler
    {
        private final WeakReference<RNReactNativeRpanNfcAgriThread> mActivity;
        private final Context context;

        public MyHandler(RNReactNativeRpanNfcAgriThread activity, Context context)
        {
            mActivity = new WeakReference<RNReactNativeRpanNfcAgriThread>(activity);
            this.context = context;
        }

        @Override
        public void handleMessage(Message msg)
        {
            RNReactNativeRpanNfcAgriThread pt = mActivity.get();
            if (pt == null)
            {
                return;
            }
            boolean b_find = false;
            switch (msg.what)
            {
                case INVENTORY_MSG:
                    break;
                case INVENTORY_FAIL_MSG:
                    break;
                case GETSCANRECORD:// ɨ�赽��¼
                    @SuppressWarnings("unchecked")
                    Vector<String> dataList = (Vector<String>) msg.obj;
                    for (String str : dataList)
                    {
                        Log.d("READ TAGS", str);
//                        Toast.makeText(context, str , Toast.LENGTH_LONG).show();
//                        b_find = false;
//                        for (int i = 0; i < pt.scanfReportList.size(); i++)
//                        {
//                            ScanReport mReport = pt.scanfReportList.get(i);
//                            if (str.equals(mReport.getDataStr()))
//                            {
//                                mReport.setFindCnt(mReport.getFindCnt() + 1);
//                                b_find = true;
//                            }
//                        }
//                        if (!b_find)
//                        {
//                            pt.scanfReportList.add(new ScanReport(str));
//                        }
//                        call output here
                    }
//                    pt.tv_scanRecordInfo.setText(pt
//                            .getString(R.string.tx_info_scanfCnt)
//                            + pt.scanfReportList.size());
//                    pt.scanfAdapter.notifyDataSetChanged();
                    break;
                case THREAD_END:// �߳̽���
                    pt.FinishInventory();
                    break;
                default:
                    break;
            }
        }
    }

    private void FinishInventory()
    {
        bGetScanRecordFlg = false;
    }

    private class GetScanRecordThrd implements Runnable
    {
        public void run()
        {
            int nret = 0;
            bGetScanRecordFlg = true;
            byte gFlg = 0x00;// ���βɼ����ݻ�����һ�βɼ�����ʧ��ʱ����־λΪ0x00
            Object dnhReport = null;
            while (bGetScanRecordFlg)
            {
                if (mHandler.hasMessages(GETSCANRECORD))
                {
                    continue;
                }
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
                    String strData = "";
                    byte[] byData = new byte[32];
                    int[] len = new int[1];
                    len[0] = byData.length;
                    if (ADReaderInterface.RDR_ParseTagDataReportRaw(dnhReport, byData,
                            len) == 0)
                    {
                        if (len[0] > 0)
                        {
                            strData = GFunction.encodeHexStr(byData,len[0]);
                            dataList.add(strData);
                        }
                    }
                    dnhReport = m_reader
                            .RDR_GetTagDataReport(RfidDef.RFID_SEEK_NEXT);
                }
                if (!dataList.isEmpty())
                {
                    Message msg = mHandler.obtainMessage();
                    msg.what = GETSCANRECORD;
                    msg.obj = dataList;
                    mHandler.sendMessage(msg);
                }
            }
            bGetScanRecordFlg = false;
            mHandler.sendEmptyMessage(THREAD_END);// ����
        }
    };

    public boolean connect(String deviceName){
//        RDType=RPAN;CommType=BLUETOOTH;Name=%s
        this.conStr = String.format("RDType=RPAN;CommType=BLUETOOTH;Name=%s;", deviceName);
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

    public void  start(){
        if(!this.reading){
            this.getScanRecord = new Thread(new GetScanRecordThrd());
            this.getScanRecord.start();
        }
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
