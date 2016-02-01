package me.bingshanguxue.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.widget.EditText;
import android.widget.TextView;

import com.igexin.sdk.PushManager;

import java.io.IOException;
import java.io.OutputStream;

import android_serialport_api.SerialPort;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.bingshanguxue.android.R;

public class MainActivity extends AppCompatActivity {


    @Bind(R.id.tv_resolution)
    TextView tvResolution;
    @Bind(R.id.tv_display)
    TextView tvDisplay;
    @Bind(R.id.et_com)
    EditText etCom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        // SDK初始化，第三方程序启动时，都要进行SDK初始化工作
        Log.d("GetuiSdkDemo", "initializing sdk...");
        PushManager.getInstance().initialize(this.getApplicationContext());

//        TextDrawable myDrawable = TextDrawable.builder().beginConfig()
//                .textColor(Color.WHITE)
//                .useFont(Typeface.DEFAULT)
//                .toUpperCase()
//                .endConfig()
//                .buildRound(item.getToDoText().substring(0,1),item.getTodoColor());

        try {
            Resources resources = this.getResources();

            tvResolution.setText(String.format("Resolutions: %d x %d (%f)",
                    resources.getDisplayMetrics().widthPixels, resources.getDisplayMetrics().heightPixels,
                    resources.getDisplayMetrics().density));
            int resourceId = resources.getIdentifier("navigation_bar_height",
                    "dimen", "android");
            //获取NavigationBar的高度
            tvDisplay.setText(String.format("navigation_bar_height:%d px", resources.getDimensionPixelSize(resourceId)));
        } catch (Exception e) {
            Log.e("Mfh", e.toString());
        }

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
//                MainActivity.this.startActivity(intent);
////                finish();
//            }
//        }, 500);
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

    @SuppressLint("NewApi")
    public static boolean checkDeviceHasNavigationBar(Context activity) {

        //通过判断设备是否有返回键、菜单键(不是虚拟键,是手机屏幕外的按键)来确定是否有navigation bar
        boolean hasMenuKey = ViewConfiguration.get(activity)
                .hasPermanentMenuKey();
        boolean hasBackKey = KeyCharacterMap
                .deviceHasKey(KeyEvent.KEYCODE_BACK);

        if (!hasMenuKey && !hasBackKey) {
            // 做任何你需要做的,这个设备有一个导航栏
            return true;
        }
        return false;
    }

    @OnClick(R.id.button_serial)
    public void testSerial(){
        serial_V();
//        Intent intent = new Intent(MainActivity.this, TabbedActivity.class);
//        MainActivity.this.startActivity(intent);
    }

    @OnClick(R.id.button_setting)
    public void reidrectToSettings(){
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        MainActivity.this.startActivity(intent);
    }

    @OnClick(R.id.button_drawer)
    public void reidrectToDrawer(){
        Intent intent = new Intent(MainActivity.this, DrawerActivity.class);
        MainActivity.this.startActivity(intent);
    }

    private OutputStream mOutputStream_V;
    private SerialPort mSerialPort_V;
    private void serial_V() {
        try {
            mSerialPort_V = AppContext.getInstance().getSerialPort("VFD", "BAUD_VFD");
            if (mSerialPort_V == null){
                DisplayError("串口未打开");
                return;
            }
            mOutputStream_V = mSerialPort_V.getOutputStream();
            /*
			 * Max add : Initialize device
			 */
            mOutputStream_V.write(0x1b);
            mOutputStream_V.write(0x40);

			/*
			mOutputStream_V.write(new String("WELCOME").getBytes());
			mOutputStream_V.write('\r');
			mOutputStream_V.write('\n');
			mOutputStream_V.write(new String("Thank  You").getBytes());
			mOutputStream_V.write('\r');
			mOutputStream_V.write('\n');
			*/
            String text;
            if (etCom == null){
                text = "1266";
            }
            else{
                text = etCom.getText().toString();
            }
            mOutputStream_V.write(text.getBytes());
            mOutputStream_V.write('\r');
            mOutputStream_V.write('\n');

            AppContext.getInstance().closeSerialPort();

        } catch(Exception e) {
            DisplayError(e.toString());
        }
    }

    private void DisplayError(String msg) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Error");
        //b.setMessage(resourceId);
        b.setMessage(msg);
        b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //SerialPortPreferences.this.finish();
            }
        });
        b.show();
    }
}
