package com.phoenix.androidnettyclient;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.phoenix.androidnettyclient.netty.ControlClient;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText ipEditText;
    private EditText portEditText;
    private EditText sendMessage;

    private ImageView connectStatus;
    private Button connectBtn;
    private Button sendBtn;

    private ControlClient controlClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        ipEditText = findViewById(R.id.et_ip);
        ipEditText.setSelection(ipEditText.getText().length());
        portEditText = findViewById(R.id.et_port);
        portEditText.setSelection(portEditText.getText().length());
        sendMessage = findViewById(R.id.et_send_message);
        sendMessage.setSelection(sendMessage.getText().length());

        connectStatus = findViewById(R.id.iv_status);
        connectBtn = findViewById(R.id.btn_connect);
        sendBtn = findViewById(R.id.btn_send);
        connectBtn.setOnClickListener(this);
        sendBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_connect:
                connectServer();
                break;
            case R.id.btn_send:
                controlClient.sendMessage(sendMessage.getText().toString().trim());
                break;
            default:
                break;
        }
    }

    private void connectServer() {
        //single mode
        try {
            String serverHostParam = ipEditText.getText().toString().trim();
            int serverPortParam = Integer.valueOf(portEditText.getText().toString().trim());
            controlClient = ControlClient.getInstance();
            controlClient.connectServer(serverHostParam, serverPortParam);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent messageEvent) {
        String message = messageEvent.getMessageContent();
        Snackbar.make(ipEditText, "server receive: "+ message, Snackbar.LENGTH_LONG).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectEvent(String message) {
        switch (message) {
            case "disconnect":
                connectStatus.setBackgroundResource(R.color.red);
                break;
            case "connect":
                connectStatus.setBackgroundResource(R.color.green);
                break;
            default:
                break;
        }
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
