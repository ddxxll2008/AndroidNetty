package com.phoenix.androidnettyclient.netty;

import android.os.Handler;
import android.util.Log;

import com.phoenix.androidnettyclient.Constants;
import com.phoenix.androidnettyclient.MessageEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * client
 * Created by phoenix on 2017/12/29.
 */
@ChannelHandler.Sharable
public class ControlClient extends SimpleChannelInboundHandler<MessageProtocol> {
    private static final String TAG = "ControlClient";

    private TcpProtoCodec protoCodec;

    ArrayBlockingQueue<MessageProtocol> messageQueque;

    /**
     * Timeout,
     * if no feedback is received after this time,
     * it indicates that the transmission failed and needs to be re-send
     */
    private final long timeoutTime = 2000;
    private Channel channel;
    private boolean isConnected = false;
    private volatile int serverStatus;
    private long connectedTime = 0;

    /**
     * Timing task, used to deal with timeout re-send
     */
    private ScheduledExecutorService timerExecutorService;

    /**
     * The number of messages sent,
     * failed message allow to be re-send
     */
    private Map<String, Integer> type2sendCountMap;

    private String serverIp;
    private int serverPort;

    private EventLoopGroup group;
    private volatile Bootstrap bootstrap;

    private List<StatusListener> listeners = new ArrayList<>();

    public void init(String host, int port) throws ServerException {
        messageQueque = new ArrayBlockingQueue<>(100);
        timerExecutorService = new ScheduledThreadPoolExecutor(20);
        type2sendCountMap = new HashMap<>();
        group = new NioEventLoopGroup();
        protoCodec = new TcpProtoCodec();
        this.serverIp = host;
        this.serverPort = port;
        try {
            //create and init netty client and bootstrap
            bootstrap = configureBootstrap(new Bootstrap(), group);
            bootstrap.option(ChannelOption.TCP_NODELAY, true);
            doConnect(bootstrap);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServerException("connect remote control server error!", e.getCause());
        }
    }

    private Bootstrap configureBootstrap(Bootstrap bootstrap, EventLoopGroup group) {
        bootstrap.group(group).channel(NioSocketChannel.class)
                .remoteAddress(serverIp, serverPort)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        //protocol codec
                        pipeline.addLast(protoCodec);
                        //handle message
                        pipeline.addLast(ControlClient.this);
                    }
                });
        return bootstrap;
    }

    private void doConnect(Bootstrap bootstrap) {
        try {
            ChannelFuture future = bootstrap.connect();
            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
                        Log.d(TAG, "operationComplete: Started Tcp Client: " + serverIp);
                        EventBus.getDefault().post(new MessageEvent("Started Tcp Client: " + serverIp));
                    } else {
                        Log.d(TAG, "operationComplete: Started Tcp Client Failed");
                        EventBus.getDefault().post(new MessageEvent("Started Tcp Client Failed"));
                    }
                    if (channelFuture.cause() != null) {
                        channelFuture.cause().printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.channel = ctx.channel();
        //re-connect success
        try {
            this.connect();
        } catch (ServerException e) {
            e.printStackTrace();
        }
    }

    private void connect() throws ServerException {
        while (!isConnectionReady()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.send(new MessageProtocol(Constants.CONNECT_INDEX));
    }

    private boolean isConnectionReady() {
        if (channel == null) {
            Log.d(TAG, "isConnectionReady: not connect server yet!");
            return false;
        }
        if (!channel.isOpen() && !channel.isWritable()) {
            Log.d(TAG, "isConnectionReady: not connect server yet!");
            return false;
        }
        return true;
    }

    private synchronized void send(MessageProtocol message) throws ServerException {
        send(message, true);
    }

    private synchronized void send(final MessageProtocol message, boolean autoResend) throws ServerException {
        if (!isConnectionReady()) {
            Log.d(TAG, "send: not connect");
            return;
        }
        // if session is not created, need to connect first
        if (!(Constants.CONNECT_INDEX == message.getMessageContent()) && !isConnected) {
            Log.d(TAG, "send: not connect yet");
            return;
        }

        try {
            final String type = message.getMessageContent();
            //send count
            if (!type2sendCountMap.containsKey(type)) {
                type2sendCountMap.put(type, 1);
            } else if (type2sendCountMap.get(type) < 5) {
                type2sendCountMap.put(type, 1 + type2sendCountMap.get(type));
            } else {
                Log.d(TAG, "send: send count more than 5");
                EventBus.getDefault().post("resend count more than 5, server not response");
                return;
            }

            if (autoResend) {
                //for timeout resend
                timerExecutorService.schedule(new Runnable() {
                    @Override
                    public void run() {
                        ControlClient client = ControlClient.this;
                        if (client.type2sendCountMap.containsKey(type)) {
                            try {
                                client.send(message, true);
                            } catch (ServerException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, timeoutTime, TimeUnit.MILLISECONDS);
            }
            Log.d(TAG, "send: " + message.getMessageContent());
            //sync
            channel.writeAndFlush(message);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "send: " + e.getMessage());
        }
    }

    /**
     * Calls {@link ChannelHandlerContext#fireChannelUnregistered()} to forward
     * to the next {@link ChannelInboundHandler} in the {@link ChannelPipeline}.
     * <p>
     * Sub-classes may override this method to change behavior.
     *
     * @param ctx
     */
    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        // 状态重置
        isConnected = false;
        this.serverStatus = -1;
        this.notifyStatusChange();

        final EventLoop loop = ctx.channel().eventLoop();
        loop.schedule(new Runnable() {
            @Override
            public void run() {
                doConnect(configureBootstrap(new Bootstrap(), loop));
            }
        }, 1, TimeUnit.SECONDS);
    }

    private void notifyStatusChange() {
        for (StatusListener listener : listeners) {
            listener.onStatusChange(this.serverStatus);
        }
    }

    public void registerStatusListener(StatusListener listener) {
        this.listeners.add(listener);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MessageProtocol messageProtocol) throws Exception {
        Log.d(TAG, "channelRead0: " + messageProtocol.getMessageContent());
        /**
         * if accept message, clean send conut
         * 收到回应,清除发送次数(不减1，直接清除)
         */
        if (type2sendCountMap.containsKey(messageProtocol.getMessageContent())) {
            type2sendCountMap.remove(messageProtocol.getMessageContent());
        }

        //process message
        processServerMessage(messageProtocol);
    }

    /**
     * process server response message
     * @param messageProtocol
     */
    private void processServerMessage(MessageProtocol messageProtocol) {
        switch (messageProtocol.getMessageContent()) {
            case Constants.CONNECT_INDEX:
                this.onConnected(messageProtocol);
                break;
            case Constants.STATE_CHANGE_INDEX:
                this.onStatusChange(messageProtocol);
                break;
            default:
                EventBus.getDefault().post(new MessageEvent(messageProtocol.getMessageContent()));
                break;
        }
    }

    private void onConnected(MessageProtocol messageProtocol) {
        Log.d(TAG, "onConnected: "+ messageProtocol.getMessageContent());
        this.isConnected = true;
        this.connectedTime = System.currentTimeMillis();
        this.messageQueque.clear();
        this.type2sendCountMap.clear();
        EventBus.getDefault().post("connect");
    }

    /**
     * server status change
     * @param messageProtocol
     */
    private void onStatusChange(MessageProtocol messageProtocol) {
        //notify
        notifyStatusChange();

        //TODO send wait message, To be optimized
        if (this.serverStatus > ServerStatus.Ready) {
            sendQueqeMessage();
        }
    }

    private void sendQueqeMessage() {
        /**
         * 取第一个,先判断是否可发送，如果不能发送则等待下次状态变化
         * if first message can not be send, then will wait next status change
         */
        MessageProtocol messageProtocol = messageQueque.peek();
        if (messageProtocol!= null) {
            try {
                messageProtocol = messageQueque.take();
                try {
                    this.send(messageProtocol);
                } catch (ServerException e) {
                    e.printStackTrace();
                }
                sendQueqeMessage();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String messageContent) {
        Log.d(TAG, "sendMessage: " + messageContent);
        MessageProtocol messageProtocol = new MessageProtocol(messageContent);
        try {
            this.send(messageProtocol);
        } catch (ServerException e) {
            e.printStackTrace();
        }
    }
}
