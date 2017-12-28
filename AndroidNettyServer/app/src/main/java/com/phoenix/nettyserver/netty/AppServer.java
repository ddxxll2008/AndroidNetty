package com.phoenix.nettyserver.netty;

import android.content.Context;
import android.util.Log;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * AppServer
 * Create and start netty server
 * Created by phoenix on 2017/12/28.
 */

public class AppServer implements IServer {
    private static final String TAG = "AppServer";
    private Context mContext;
    /**
     * port
     */
    private int port = 9090;
    /**
     * server init
     */
    private APPServerInitializer serverInitializer;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workGroup;

    private ChannelFuture channelFuture;

    public AppServer(Context mContext) {
        this.mContext = mContext;
        bossGroup = new NioEventLoopGroup();
        workGroup = new NioEventLoopGroup();
    }

    @Override
    public void start() throws Exception {
        serverInitializer = new APPServerInitializer(mContext);

        try {
            /**
            * 构建serverbootstrap对象
            * bootstrap.group(bossGroup, workerGroup);   //设置时间循环对象，前者用来处理accept事件，后者用于处理已经建立的连接的io
            * bootstrap.channel(NioServerSocketChannel.class);   //用它来建立新accept的连接，用于构造serversocketchannel的工厂类
            * bootstrap.childHandler(serverInitializer);    //为accept channel的pipeline预添加的inboundhandler
             */
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(serverInitializer);
            Log.d(TAG, "start: Starting AppServer... Port: " + port);

            channelFuture = bootstrap.bind(port).sync();
        } finally {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    shutdown();
                }
            });
        }

    }

    @Override
    public void restart() throws Exception {
        shutdown();
        start();
    }

    @Override
    public void shutdown() {
        if (channelFuture != null) {
            channelFuture.channel().closeFuture();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workGroup != null) {
            workGroup.shutdownGracefully();
        }
    }

    public int getPort() {
        return port;
    }
}
