package com.phoenix.androidnettyclient.netty;

import android.os.Handler;
import android.util.Log;

import com.phoenix.androidnettyclient.Constants;
import com.phoenix.androidnettyclient.MessageEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

/**
 * Single case mode client
 * Created by phoenix on 2017/12/29.
 */

public class ClientWrapper {
    private static final String TAG = "ClientWrapper";

    private static ClientWrapper clientWrapper;
    private String serverHost;
    private int serverPort;
    private ControlClient controlClient;
    private int status;
    private List<Channel> clients;

    public static void setInstance(ClientWrapper wrapper) {
        clientWrapper = wrapper;
    }

    public ClientWrapper(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        status = ServerStatus.Initialize;
        clients = new ArrayList<>();
    }

    public void init() throws ServerException {
        controlClient = new ControlClient();
        controlClient.registerStatusListener(new StatusListener() {
            @Override
            public void onStatusChange(int status) {
                if (ClientWrapper.this.status != status) {
                    ClientWrapper.this.status = status;
                    ClientWrapper.this.notifyStatusChange(status);
                } else {
                    EventBus.getDefault().post("disconnect");
                }
            }
        });
        controlClient.init(serverHost, serverPort);
    }

    /**
     * notify client server status change
     *
     * @param status
     */
    private void notifyStatusChange(int status) {
        for (Channel channel : this.clients) {
            try {
                channel.writeAndFlush(new MessageProtocol(String.valueOf(status)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String message) {
        Log.d(TAG, "sendMessage: " + message);
        controlClient.sendMessage(message);
    }
}
