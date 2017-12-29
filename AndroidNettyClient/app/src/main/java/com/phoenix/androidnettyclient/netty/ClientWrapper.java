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

    private Handler mHandler;

    public static void setInstance(ClientWrapper wrapper) {
        clientWrapper = wrapper;
    }

    public static ClientWrapper instance() {
        return clientWrapper;
    }

    public ClientWrapper(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        status = ServerStatus.Initialize;
        clients = new ArrayList<io.netty.channel.Channel>();
    }

    public void init() throws ServerException {
        controlClient = new ControlClient();
        mHandler = new Handler();
        controlClient.registerStatusListener(new StatusListener() {
            @Override
            public void onStatusChange(int status) {
                if (ClientWrapper.this.status != status) {
                    ClientWrapper.this.status = status;
                    ClientWrapper.this.notifyStatusChange(status);
                } else {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            EventBus.getDefault().post(new MessageEvent("disconnect"));
                        }
                    });
                }
            }
        });
        controlClient.init(serverHost, serverPort);
    }

    /**
     * close connect
     *
     * @param ctx
     * @throws Exception
     */
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        clients.remove(ctx.channel());
        Log.d(TAG, "handlerRemoved: ");
    }

    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        clients.add(ctx.channel());
        //notify current status
        ctx.channel().writeAndFlush(new MessageProtocol(Constants.STATE_CHANGE_INDEX));
        Log.d(TAG, "handlerAdded: ");
    }

    /**
     * process message
     * @param ctx
     * @param message
     * @throws Exception
     */
    protected void processMessage(ChannelHandlerContext ctx, MessageProtocol message) throws Exception {
        Log.d(TAG, "processMessage: " + message.getMessageContent());
        controlClient.sendMessage(message.getMessageContent());
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
