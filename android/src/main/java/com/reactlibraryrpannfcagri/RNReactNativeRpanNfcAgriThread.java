package com.reactlibraryrpannfcagri;

import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.util.Log;
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
import com.rfid.transport.BufferPack;

import java.io.Reader;
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
    private boolean bOnlyReadNew = false;
    private boolean bUseISO15693 = false;
    private boolean bUseISO14443A = false;
    private long mAntCfg = 0x000000;
    private boolean bMathAFI = false;
    private byte mAFIVal = 0x00;

    public RNReactNativeRpanNfcAgriThread(ReactApplicationContext context){
        this.context = context;
    }

    @Override
    public void run() {

    }

    public Boolean connect(String deviceName){
        int iret = m_reader.RDR_Open("RDType=RPAN;CommType=BLUETOOTH;Name=" + deviceName);
        if (iret == ApiErrDefinition.NO_ERROR) {
            Toast.makeText(context,
                    deviceName + ": successfully connected",
                    Toast.LENGTH_SHORT).show();
            return true;
        } else {
            Toast.makeText(context,
                    deviceName + ": error",
                    Toast.LENGTH_SHORT).show();
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

    public String UiReadBlock(ISO15693Interface mTag)
    {
        int blkAddr = 40;
        int numOfBlksToRead = 40;
        Integer numOfBlksRead = new Integer(0);
        Long bytesBlkDatRead = new Long(0);
        byte bufBlocks[] = new byte[4 * numOfBlksToRead];
        int iret = mTag.ISO15693_ReadMultiBlocks(false, blkAddr,
                numOfBlksToRead, numOfBlksRead, bufBlocks, bytesBlkDatRead);
        if (iret != ApiErrDefinition.NO_ERROR)
        {
            return null;
        }
        String strData = GFunction.encodeHexStr(bufBlocks);
        return strData;
    }

    public WritableArray startScanning(ReactApplicationContext context){
        bGetScanRecordFlg = true;
        byte useAnt[] = null;
        Object hInvenParamSpecList = null;
        WritableArray tagList = Arguments.createArray();
        byte newAI = RfidDef.AI_TYPE_NEW;
        int failedCnt = 0;// ����ʧ�ܴ���

        if (bOnlyReadNew)
        {
            newAI = RfidDef.AI_TYPE_CONTINUE;
        }

        if (mAntCfg != 0)
        {
            Vector<Byte> vAntList = new Vector<Byte>();
            for (int i = 0; i < 32; i++)
            {
                if ((mAntCfg & (1 << i)) != 0)
                {
                    vAntList.add((byte) (i + 1));
                }
            }

            useAnt = new byte[vAntList.size()];
            for (int i = 0; i < useAnt.length; i++)
            {
                useAnt[i] = vAntList.get(i);
            }
        }

        if (bUseISO14443A || bUseISO15693)
        {
            hInvenParamSpecList = ADReaderInterface
                    .RDR_CreateInvenParamSpecList();
            if (bUseISO15693)
            {
                ISO15693Interface.ISO15693_CreateInvenParam(
                        hInvenParamSpecList, (byte) 0, bMathAFI, mAFIVal,
                        (byte) 0);
            }
            if (bUseISO14443A)
            {
                ISO14443AInterface.ISO14443A_CreateInvenParam(
                        hInvenParamSpecList, (byte) 0);
            }
        }

        int iret = m_reader.RDR_TagInventory(RfidDef.AI_TYPE_NEW, useAnt, 0,
                hInvenParamSpecList);
        if (iret == ApiErrDefinition.NO_ERROR
                || iret == -ApiErrDefinition.ERR_STOPTRRIGOCUR)
        {
            Object tagReport = m_reader
                    .RDR_GetTagDataReport(RfidDef.RFID_SEEK_FIRST);

            while (tagReport != null)
            {
                ISO15693Tag ISO15693TagData = new ISO15693Tag();
                ISO15693Interface mTag = new ISO15693Interface ();
                byte connectMode = 1;
                iret = ISO15693Interface.ISO15693_ParseTagDataReport(
                        tagReport, ISO15693TagData);
                if (iret == ApiErrDefinition.NO_ERROR)
                {
                    // ISO15693 TAG
                    BufferPack bufferPack = ISO15693TagData.m_rawData;
                    String data = GFunction.encodeHexStr(bufferPack.GetBuffer());
                    WritableMap map = Arguments.createMap();
                    map.putString("data", data);
                    map.putString("ant_id", String.valueOf(ISO15693TagData.ant_id));
                    map.putString("tag_id", ISO15693Interface.GetTagNameById(ISO15693TagData.tag_id));
                    map.putString("uid", GFunction.encodeHexStr(ISO15693TagData.uid));
                    map.putString("aip_id", String.valueOf(ISO15693TagData.aip_id));

                    map.putString("length", String.valueOf(bufferPack.readable_length()));
                    map.putString("buffer length", String.valueOf(bufferPack.getBufferLen()));
                    mTag.ISO15693_Connect(m_reader, 1, connectMode, ISO15693TagData.uid);
                    map.putString("tag", readBlock(mTag));
                    tagList.pushMap(map);
                    Toast.makeText(context, "Tags: " + ISO15693TagData, Toast.LENGTH_SHORT).show();
                    tagReport = m_reader.RDR_GetTagDataReport(RfidDef.RFID_SEEK_NEXT);
                }

                ISO14443ATag ISO14444ATagData = new ISO14443ATag();
                iret = ISO14443AInterface.ISO14443A_ParseTagDataReport(
                        tagReport, ISO14444ATagData);
                if (iret == ApiErrDefinition.NO_ERROR)
                {
                    // ISO14443A TAG
                    WritableMap map = Arguments.createMap();
                    map.putString("data", tagReport.toString());
                    map.putString("ant_id", String.valueOf(ISO14444ATagData.ant_id));
                    map.putString("tag_id", ISO14443AInterface.GetTagNameById(ISO14444ATagData.tag_id));
                    map.putString("uid", GFunction.encodeHexStr(ISO14444ATagData.uid));
                    map.putString("aip_id", String.valueOf(ISO14444ATagData.aip_id));
                    tagList.pushMap((WritableMap) map);
                    Toast.makeText(context, "Tags: " + ISO14444ATagData, Toast.LENGTH_SHORT).show();

                    tagReport = m_reader.RDR_GetTagDataReport(RfidDef.RFID_SEEK_NEXT);
                }
            }
        }

        return tagList;
    }

    public String readBlock(ISO15693Interface mTag) {
        int blkAddr = 0;
        int numOfBlksToRead = 79;
        long[] numOfBlksRead = new long[1];
        long[] bytesBlkDatRead = new long[1];
        String data  = "";

        numOfBlksRead[0] = 0;
        bytesBlkDatRead[0] = 0;
        byte bufBlocks[] = new byte[4 * numOfBlksToRead];
        int iret = mTag.ISO15693_ReadMultiBlocks(false, blkAddr, numOfBlksToRead, numOfBlksRead, bufBlocks, bytesBlkDatRead);
        if (iret != ApiErrDefinition.NO_ERROR)
        {
            return null;
        }
        boolean flag = true;
        for (int i = 0; i < bufBlocks.length; i++){
            if(Character.toString((char) (bufBlocks[i] & 0xff)).equalsIgnoreCase("þ")){
                flag = false;
            }
            if(bufBlocks[i] != 0 && i > 12 && flag == true){
                data += Character.toString((char) (bufBlocks[i] & 0xff));
            }
        }
        return data;
    }


    public int getPower(ReactApplicationContext reactContext) {
        int nret = -1;
        Byte mPower = new Byte((byte) 0);
        if(m_reader != null){
            nret = m_reader.RDR_GetRFPower(mPower);
            if (nret != ApiErrDefinition.NO_ERROR) {
                return -1;
            }
            return mPower.byteValue() - 1;
        }
        return  -1;
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
