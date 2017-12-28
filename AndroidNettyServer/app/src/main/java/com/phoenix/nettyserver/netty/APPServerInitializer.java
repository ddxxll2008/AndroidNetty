package com.phoenix.nettyserver.netty;

import android.content.Context;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

/**
 * netty channel init
 * Created by phoenix on 2017/12/28.
 */

public class APPServerInitializer extends ChannelInitializer {
    /**
     * tcp protocol codec
     */
    private TcpProtoCodec protoCodec;
    /**
     * server handler
     */
    private AppServerHandler serverHandler;
    private Context mContext;

    public APPServerInitializer(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * init
     * @param channel
     * @throws Exception
     */
    @Override
    protected void initChannel(Channel channel) throws Exception {
        protoCodec = new TcpProtoCodec();
        serverHandler = new AppServerHandler(mContext);
        //add protocol codec
        channel.pipeline().addLast(protoCodec);
        //add server handler
        channel.pipeline().addLast(serverHandler);
    }
}
