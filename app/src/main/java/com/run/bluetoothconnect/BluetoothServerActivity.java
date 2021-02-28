package com.run.bluetoothconnect;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothServerActivity extends BaseActivity {

    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothServerSocket serverSocket;

    private List<BluetoothSocket> bluetoothSockets = new ArrayList<>();

    private EditText sendText;
    private Button sendButton;
    private TextView receiveText;

    private final String tag = "蓝牙调试";
    private final String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    private class ServerThread extends Thread {

        private int index;

        private ServerThread(int index) {
            this.index = index;
        }

        @Override
        public void run() {
            try {
                InputStream inputStream;
                BluetoothSocket remotePeer = bluetoothSockets.get(index);
                //noinspection LoopConditionNotUpdatedInsideLoop
                while(remotePeer != null) {

                    inputStream = remotePeer.getInputStream();
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
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void initParams(Bundle params) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_bluetooth_server;
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
        receiveText.setText("等待客户端连接...\n");
        if (mBluetoothAdapter != null) {
            new Thread(() -> {
                try {
                    serverSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(tag, UUID.fromString(MY_UUID));
                    while (true) {
                        BluetoothSocket remotePeer = serverSocket.accept();
                        bluetoothSockets.add(remotePeer);
                        BluetoothDevice device = remotePeer.getRemoteDevice();
                        runOnUiThread(()->receiveText.append("连接客户端成功:" + device.getName() + "\n"));
                        new ServerThread(bluetoothSockets.size() - 1).start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }

        sendButton.setOnClickListener(v -> {
            String data = sendText.getText().toString();
            if (!TextUtils.isEmpty(data)) {
                for (BluetoothSocket socket : bluetoothSockets) {
                    new Thread(()->{
                        try {
                            socket.getOutputStream().write(data.getBytes());
                            runOnUiThread(()->receiveText.append("发送消息：" + data + "\n"));
                        } catch (IOException e) {
                            runOnUiThread(()->receiveText.append("发送消息：" + "失败！" + "\n"));
                            e.printStackTrace();
                        }
                    }).start();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            for (BluetoothSocket socket : bluetoothSockets) {
                socket.close();
            }
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
