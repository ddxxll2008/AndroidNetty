package com.phoenix.nettyserver;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import com.phoenix.nettyserver.netty.AppServer;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends AppCompatActivity {
    private TextView ipTextView;
    private TextView portTextView;
    private TextView messageTextView;

    private AppServer appServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        startAppServer();
        showIpAndPort();
    }

    private void initView() {
        ipTextView = findViewById(R.id.tv_ip);
        portTextView = findViewById(R.id.tv_port);
        messageTextView = findViewById(R.id.tv_message);
    }

    /**
     * start netty server
     */
    private void startAppServer() {
        try {
            appServer = new AppServer(this);
            appServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showIpAndPort() {
        //get wifi service
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        String ip = intToIp(ipAddress);
        ipTextView.setText(ip);
        portTextView.setText("端口："+ appServer.getPort());
    }

    private String intToIp(int i) {
        return (i & 0xFF ) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16 ) & 0xFF) + "." +
                ( i >> 24 & 0xFF) ;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent messageEvent) {
        messageTextView.setText(messageEvent.getMessageContent());
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}
