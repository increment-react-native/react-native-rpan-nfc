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
        String conStr = "";
        String devName = "";
        devName = sn_devName.getSelectedItem().toString();
        int mIdx = sn_commType.getSelectedItemPosition();
        if (mIdx == 0)
        {
            if (sn_bluetooth.getAdapter().isEmpty())
            {
                MessageBox(getString(R.string.tx_select_device),
                        getString(R.string.tx_select_bluetooth));
                return;
            }
            String bluetoolName = sn_bluetooth.getSelectedItem().toString();
            if (bluetoolName == "")
            {
                MessageBox(getString(R.string.tx_select_device),
                        getString(R.string.tx_select_bluetooth));
                return;
            }
            conStr = String.format("RDType=%s;CommType=BLUETOOTH;Name=%s",
                    devName, bluetoolName);
        }
        else if (mIdx == 1)// ����
        {
            if (sn_comName.getAdapter().isEmpty())
            {
                MessageBox(getString(R.string.tx_msg_selectCom),
                        getString(R.string.tx_msg_selectComTip));
                return;
            }
            conStr = String
                    .format("RDType=%s;CommType=COM;ComPath=%s;Baund=%s;Frame=%s;Addr=255",
                            devName, sn_comName.getSelectedItem().toString(),
                            sn_comBaud.getSelectedItem().toString(),
                            sn_comFrame.getSelectedItem().toString());
        }
        else if (mIdx == 2)// (commTypeStr.equals(getString(R.string.tx_type_net)))//
                            // ����
        {
            String sRemoteIp = ed_ipAddr.getText().toString();
            String sRemotePort = ed_port.getText().toString();
            conStr = String.format(
                    "RDType=%s;CommType=NET;RemoteIp=%s;RemotePort=%s",
                    devName, sRemoteIp, sRemotePort);
        }
        else if (mIdx == 3)// (commTypeStr.equals("USB"))
        {
            // ע�⣺ʹ��USB��ʽʱ��������Ҫö������USB�豸
            // Note: Before using USB, you must enumerate all USB devices first.
            int usbCnt = ADReaderInterface.EnumerateUsb(this);  
            if (usbCnt <= 0)
            {
                Toast.makeText(this, getString(R.string.tx_msg_noUsb),
                        Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (!ADReaderInterface.HasUsbPermission("0"))
            {
                Toast.makeText(this,
                        getString(R.string.tx_msg_noUsbPermission),
                        Toast.LENGTH_SHORT).show();
                ADReaderInterface.RequestUsbPermission("0");
                return;
            }
            conStr = String.format("RDType=%s;CommType=USB;Description=0",
                    devName);
        }
        else if (mIdx == 4)// (commTypeStr.equals(getString(R.string.tx_type_usb_com)))
        {
            // Attention: Only support Z-TEK
            // ע�⣺Ŀ¼ֻ֧��Z-TEK�ͺŵ�USBת������
            int mUsbCnt = ADReaderInterface.EnumerateZTEK(this, 0x0403, 0x6001);
            if (mUsbCnt <= 0)
            {
                Toast.makeText(this, getString(R.string.tx_msg_noUsb),
                        Toast.LENGTH_SHORT).show();
                return;
            }
            conStr = String
                    .format("RDType=%s;CommType=Z-TEK;port=1;Baund=%s;Frame=%s;Addr=255",
                            devName, sn_comBaud.getSelectedItem().toString(),
                            sn_comFrame.getSelectedItem().toString());

        }
        else
        {
            return;
        }
        int iret = m_reader.RDR_Open(conStr);
        if (iret == ApiErrDefinition.NO_ERROR)
        {
            // ///////////////////////ֻ��RPAN�豸֧��ɨ��ģʽ/////////////////////////////
            if (!isLoadScanfMode && devName.equals("RPAN"))
            {
                findViewById(layRes[4]).setVisibility(View.VISIBLE);
                TabSpec myTab = myTabhost.newTabSpec("tab" + 4);
                myTab.setIndicator(layTittle[4]);
                myTab.setContent(layRes[4]);
                myTabhost.addTab(myTab);
                isLoadScanfMode = true;
            }
        }
        else
        {
            Toast.makeText(this, getString(R.string.tx_msg_openDev_fail),
                    Toast.LENGTH_SHORT).show();
        }
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

    public abstract void dispatchEvent(String name, WritableMap data);

    public abstract void dispatchEvent(String name, String data);

    public abstract void dispatchEvent(String name, WritableArray data);
}
