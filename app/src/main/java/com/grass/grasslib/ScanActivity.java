package com.grass.grasslib;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.grass.views.zbar.ZbarCallback;
import com.grass.views.zbar.ZbarUtils;
import com.grass.views.zbar.view.ZbarViewfinderView;

public class ScanActivity extends AppCompatActivity {

    private ZbarViewfinderView zbarViewfinderView;
    private SurfaceView preview_view;
    private ZbarUtils zbarUtils;
    private SurfaceHolder mHolder;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        zbarViewfinderView = findViewById(R.id.scan_border);
        preview_view = findViewById(R.id.preview_view);
        mHolder = preview_view.getHolder();

        zbarUtils = new ZbarUtils(handler, zbarViewfinderView);

        zbarUtils.setZbarCallback(zbarCallback);
        zbarUtils.initCamera(mHolder);
        zbarUtils.setContinue(true);
        zbarUtils.openDriver();
    }

    private ZbarCallback zbarCallback = new ZbarCallback() {
        @Override
        public void barCodeResult(String result) {
        }

        @Override
        public void qrCodeResult(String result) {
//            zbarUtils.restartScan();
//            Toast.makeText(YZScanActivity.this,"二维条码："+result,Toast.LENGTH_LONG).show();
            Log.i("tag",result);
        }
    };

    @Override
    protected void onRestart() {
        super.onRestart();
        zbarUtils.restartScan();
    }

    @Override
    protected void onPause() {
        super.onPause();
        zbarUtils.pauseScan();
    }

    @Override
    protected void onResume() {
        super.onResume();
        zbarUtils.restartScan();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        zbarUtils.closeDriver();
    }
}
