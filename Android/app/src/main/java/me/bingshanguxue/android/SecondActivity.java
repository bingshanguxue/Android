package me.bingshanguxue.android;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ApplicationErrorReport;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.widget.TextView;

import com.igexin.sdk.PushManager;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SecondActivity extends AppCompatActivity {


    @Bind(R.id.tv_build)
    TextView tvBuild;
    @Bind(R.id.tv_app)
    TextView tvApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_os);

        ButterKnife.bind(this);

        loadOSInfo();
        loadAppInfo();

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

    private void loadOSInfo(){
        //Build
        StringBuilder sb = new StringBuilder();
        sb.append("系统信息:\n");
        sb.append(String.format("主板:%s\n", Build.BOARD));
        sb.append(String.format("系统定制商:%s\n", Build.BRAND));
        sb.append(String.format("设备参数:%s\n", Build.DEVICE));
        sb.append(String.format("显示屏:%s\n", Build.DISPLAY));
        sb.append(String.format("手机产品名:%s\n", Build.PRODUCT));
        tvBuild.setText(sb.toString());
    }

    private void loadAppInfo(){
        //Build
        StringBuilder sb = new StringBuilder();
        sb.append("APK信息:\n");
//        ApplicationInfo ai = getApplicationInfo();
//        if (ai.flags == ApplicationInfo.FLAG_SYSTEM){
//            sb.append("系统应用:\n");
//        }else{
//            sb.append("第三方应用\n");
//        }

        PackageManager pm = getPackageManager();
//        sb.append(String.format("包名:%s\n", pm.packagename));
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
        for (ApplicationInfo app : apps){
            sb.append(String.format("%s\n", app.packageName));
            if ((app.flags & ApplicationInfo.FLAG_SYSTEM) != 0){
                sb.append("系统应用/");
            }
            if ((app.flags & ApplicationInfo.FLAG_SYSTEM) <= 0){
                sb.append("第三方应用/");
            }
            if ((app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0){
                sb.append("系统升级应用/");
            }
            if ((app.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) <= 0){
                sb.append("SDCard应用/");
            }
        }


        ActivityManager am =  (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(memoryInfo);


        sb.append("内存使用情况:\n");
        sb.append(String.format("系统总内存:%dG\n", memoryInfo.totalMem/1024/1024/1024));
        sb.append(String.format("系统可用内存:%dM\n", memoryInfo.availMem/1024/1024));
        sb.append(String.format("低内存阀值:%dM\n", memoryInfo.threshold/1024/1024));
        sb.append(String.format("是否处于低内存:%b\n", memoryInfo.lowMemory));


        tvApp.setText(sb.toString());
    }
}
