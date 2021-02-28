package com.run.bluetoothconnect;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothClientActivity extends BaseActivity {

    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothSocket socket;

    private EditText sendText;
    private Button sendButton;
    private TextView receiveText;

    //要连接的目标蓝牙设备。
    //private final String TARGET_DEVICE_NAME = "DESKTOP-S22563C";
    private final String TARGET_DEVICE_NAME = "COL-AL10";

    private final String TAG = "蓝牙调试";
    private final String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    // 广播接收发现蓝牙设备。
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                assert device != null;
                String name = device.getName();
                if (name != null)
                    Log.d(TAG, "发现设备:" + name);

                if (name != null && name.equals(TARGET_DEVICE_NAME)) {
                    Log.d(TAG, "发现目标设备，开始线程连接!");

                    // 蓝牙搜索是非常消耗系统资源开销的过程，一旦发现了目标感兴趣的设备，可以考虑关闭扫描。
                    mBluetoothAdapter.cancelDiscovery();

                    startConnect(device);
                }
            }
        }
    };

    private void sendDataToServer(String data) {
        new Thread(() -> {
            try {
                OutputStream os = socket.getOutputStream();
                os.write(data.getBytes());
                os.flush();
                //os.close();
                runOnUiThread(() -> receiveText.append("发送消息:" + data + "\n"));
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> receiveText.append("发送消息:" + "失败！" + "\n"));
            }
        }).start();
    }

    @Override
    public void initParams(Bundle params) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_bluetooth_client;
    }

    @Override
    public void initView(View view) {
        sendText = $(R.id.input);
        sendButton = $(R.id.send);
        receiveText = $(R.id.receive);
    }

    @Override
    public void initData() {

    }

    @Override
    public void doBusiness(Context mContext) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = getPairedDevices();
        if (device == null) {
            // 注册广播接收器。
            // 接收蓝牙发现讯息。
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver, filter);

            if (mBluetoothAdapter.startDiscovery()) {
                receiveText.append("启动蓝牙扫描设备...\n");
            }
        } else {
            startConnect(device);
        }

        sendButton.setOnClickListener(v -> {
            String data = sendText.getText().toString();
            if (!TextUtils.isEmpty(data)) {
                sendDataToServer(data);
            }
        });
    }

    private BluetoothDevice getPairedDevices() {
        // 获得和当前Android已经配对的蓝牙设备。
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices != null && pairedDevices.size() > 0) {
            // 遍历
            for (BluetoothDevice device : pairedDevices) {
                // 把已经取得配对的蓝牙设备名字和地址打印出来。
                Log.d(TAG, device.getName() + " : " + device.getAddress());
                if (TextUtils.equals(TARGET_DEVICE_NAME, device.getName())) {
                    receiveText.append("已配对目标设备 -> " + TARGET_DEVICE_NAME + "\n");
                    return device;
                }
            }
        }

        return null;
    }

    private void startConnect(BluetoothDevice device) {
        new Thread(() -> {
            try {
                socket = device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
                socket.connect();
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> receiveText.append("创建socket失败！\n"));
            }
            if (socket != null && socket.isConnected()) {
                runOnUiThread(() -> receiveText.append("创建socket成功！\n"));
                try {
                    InputStream inputStream;
                    while(socket != null) {

                        inputStream = socket.getInputStream();
                        byte[] buffer = new byte[2560];
                        int len = inputStream.read(buffer, 0, 2560);
                        if (len <= 0) {
                            Thread.sleep(1000);
                            continue;
                        }

                        runOnUiThread(()->receiveText.append("收到消息：" + new String(buffer, 0, len) + "\n"));
                    }
                } catch(IOException e) {
                    e.printStackTrace();
                    Log.e("TAG", "startConnect: " + e.getMessage());
                    runOnUiThread(() -> receiveText.append("socket异常！\n"));
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            socket.close();
            unregisterReceiver(mBroadcastReceiver);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
