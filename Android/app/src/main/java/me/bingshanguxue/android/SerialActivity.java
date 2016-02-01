package me.bingshanguxue.android;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import android_serialport_api.SerialPortFinder;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.bingshanguxue.android.com.ComBean;
import me.bingshanguxue.android.com.SerialConf;
import me.bingshanguxue.android.com.SerialHelper;
import me.bingshanguxue.android.utils.DataConvertUtil;
import me.bingshanguxue.android.utils.ZLog;

public class SerialActivity extends AppCompatActivity {


    @Bind(R.id.text_receive_data)
    TextView tvRecvData;
    @Bind(R.id.spinner_port)
    Spinner spinnerPort;
    @Bind(R.id.spinner_baudtrate)
    Spinner spinnerBaundRate;
    @Bind(R.id.switchCompat_toggle)
    SwitchCompat switchCompatToggle;


    private SerialPortFinder mSerialPortFinder;//串口设备搜索
    private SerialControl comMeasure;//串口

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serial);

        ButterKnife.bind(this);

        spinnerPort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try{
                    CloseComPort(comMeasure);
                }
                catch (Exception e){
                    ShowMessage(e.toString());
                }
                switchCompatToggle.setChecked(false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        switchCompatToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked){
                    comMeasure.setPort(spinnerPort.getSelectedItem().toString());
                    comMeasure.setBaudRate(9600);
                    OpenComPort(comMeasure);
                }
                else{
                    CloseComPort(comMeasure);
                }
            }
        });
        initCOM();
        searchDevices();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //关闭串口
        CloseComPort(comMeasure);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * poslab: devices [/dev/ttyGS3, /dev/ttyGS2, /dev/ttyGS1, /dev/ttyGS0, /dev/ttymxc4, /dev/ttymxc3, /dev/ttymxc2, /dev/ttymxc1, /dev/ttymxc0]
     * JOOYTEC: devices:[/dev/ttyGS3, /dev/ttyGS2, /dev/ttyGS1, /dev/ttyGS0, /dev/ttyS3, /dev/ttyS1, /dev/ttyS0, /dev/ttyFIQ0]
     */
    @OnClick(R.id.button_search_devices)
    public void searchDevices() {
        if (mSerialPortFinder == null){
            mSerialPortFinder = new SerialPortFinder();
        }
        String[] entryValues = mSerialPortFinder.getAllDevicesPath();
        List<String> allDevices = new ArrayList<>();
        for (int i = 0; i < entryValues.length; i++) {
            allDevices.add(entryValues[i]);
        }
        ZLog.d("devices:" + allDevices.toString());

//        StringBuilder sb = new StringBuilder();
//        for (String device : allDevices){
//            sb.append(device + "\n");
//        }

        ArrayAdapter<String> aspnDevices = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, allDevices);
        aspnDevices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPort.setAdapter(aspnDevices);
        spinnerPort.setSelection(0);

        String[] entryValues2 = mSerialPortFinder.getAllDevices();
        List<String> allDevices2 = new ArrayList<>();
        for (int i = 0; i < entryValues2.length; i++) {
            allDevices2.add(entryValues2[i]);
        }
        ZLog.d("devices:" + allDevices.toString());
        ArrayAdapter<String> aspnDevices2 = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, allDevices2);
        aspnDevices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBaundRate.setAdapter(aspnDevices2);
        spinnerBaundRate.setSelection(0);
    }


    private class SerialControl extends SerialHelper {

        public SerialControl(String sPort, String sBaudRate) {
            super(sPort, sBaudRate);
        }
        public SerialControl(String sPort, int iBaudRate) {
            super(sPort, iBaudRate);
        }

        public SerialControl() {
        }

        @Override
        protected void onDataReceived(final ComBean ComRecData) {
            //数据接收量大或接收时弹出软键盘，界面会卡顿,可能和6410的显示性能有关
            //直接刷新显示，接收数据量大时，卡顿明显，但接收与显示同步。
            //用线程定时刷新显示可以获得较流畅的显示效果，但是接收数据速度快于显示速度时，显示会滞后。
            //最终效果差不多-_-，线程定时刷新稍好一些。
//            DispQueue.AddQueue(ComRecData);//线程定时刷新显示(推荐)
            try{
                final StringBuilder sMsg = new StringBuilder();
                sMsg.append(ComRecData.sRecTime);
                sMsg.append("[");
                sMsg.append(ComRecData.sComPort);
                sMsg.append("]");
                sMsg.append("[Hex] ");
                sMsg.append(DataConvertUtil.ByteArrToHex(ComRecData.bRec));
                sMsg.append("\r\n");
                ZLog.d("onDataReceived: " + sMsg);
//
                runOnUiThread(new Runnable()//直接刷新显示
                {
                    public void run()
                    {
                        tvRecvData.setText(sMsg);
                    }
                });
            }
            catch(Exception e){
                ZLog.e(e.toString());
            }

        }
    }

    /**
     * 初始化串口
     */
    private void initCOM() {
        comMeasure = new SerialControl(SerialConf.PORT_WEIGH, SerialConf.BAUDRATE_WEIGH);

        //关闭串口
//        OpenComPort(comMeasure);
    }

    /**
     * 打开串口
     */
    private void OpenComPort(SerialHelper ComPort) {
        try {
            ComPort.open();
        } catch (SecurityException e) {
            ZLog.d("打开串口失败:没有串口读/写权限!");
            ShowMessage("打开串口失败:没有串口读/写权限!");
        } catch (IOException e) {
            ZLog.d("打开串口失败:未知错误!");
            ShowMessage("打开串口失败:未知错误!");
        } catch (InvalidParameterException e) {
            ZLog.d("打开串口失败:参数错误!");
            ShowMessage("打开串口失败:参数错误!");
        }
    }

    /**
     * 关闭串口
     */
    private void CloseComPort(SerialHelper ComPort) {
        if (ComPort != null) {
            ComPort.stopSend();
            ComPort.close();
        }
    }

    /**
     * 串口发送
     */
    private void sendPortData(SerialHelper ComPort, String sOut, boolean bHex) {
        if (ComPort != null && ComPort.isOpen()) {
            if (bHex) {
                ComPort.sendHex(sOut);
            } else {
                ComPort.sendTxt(sOut);
            }
        }
    }
    /**
     * 串口发送
     */
    private void sendPortData(SerialHelper ComPort, byte[] bOutArray) {
        if (ComPort != null && ComPort.isOpen()) {
            ComPort.send(bOutArray);
        }
    }

    private void ShowMessage(String sMsg)
    {
        Toast.makeText(this, sMsg, Toast.LENGTH_SHORT).show();
    }


}
