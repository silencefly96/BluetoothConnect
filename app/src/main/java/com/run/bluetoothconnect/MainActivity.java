package com.run.bluetoothconnect;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import java.util.List;

public class MainActivity extends BaseActivity {

    private boolean permissionGranted = false;

    @Override
    public void initParams(Bundle params) {
        setShowBacking(false);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_main;
    }

    @Override
    public void initView(View view) {

    }

    @Override
    public void initData() {

    }

    @Override
    public void widgetClick(View v) {
        if (!permissionGranted) {
            checkPermission();
        }else {
            switch (v.getId()) {
                case R.id.openServer:
                    startServerActivity();
                    break;
                case R.id.openClient:
                    startClientActivity();
                    break;
                default:
            }
        }
    }

    private void checkPermission() {
        new AlertDialog.Builder(this)
                .setTitle("警告")
                .setMessage("未获得蓝牙依赖权限！")
                .setPositiveButton("授权", (dialog, which) -> requestPermission())
                .setNegativeButton("取消", null)
                .create()
                .show();
    }

    private void startServerActivity() {
        startActivity(BluetoothServerActivity.class);
    }

    private void startClientActivity() {
        startActivity(BluetoothClientActivity.class);
    }

    @Override
    public void doBusiness(Context mContext) {
        requestPermission();

    }


    private void requestPermission() {
        requestRunTimePermission(new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                new PermissionListener() {
                    @Override
                    public void onGranted() {
                        permissionGranted = true;
                    }

                    @Override
                    public void onGranted(List<String> grantedPermission) {

                    }

                    @Override
                    public void onDenied(List<String> deniedPermission) {
                        permissionGranted = false;
                    }
                });
    }
}
