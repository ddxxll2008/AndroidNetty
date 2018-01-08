package com.phoenix.nettyserver.netty;

import android.content.Context;
import android.util.Log;

import com.phoenix.nettyserver.MessageEvent;

import org.greenrobot.eventbus.EventBus;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * App Server handle
 * Created by phoenix on 2017/12/28.
 */
@ChannelHandler.Sharable
public class AppServerHandler extends SimpleChannelInboundHandler<MessageProtocol> {
    private static final String TAG = "AppServerHandler";
    private Context mContext;

    public AppServerHandler(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * create channel
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Log.i(TAG, "handlerAdded:" + ctx.channel());
        EventBus.getDefault().post("connect");
        super.handlerAdded(ctx);
    }

    /**
     * channel remove
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Log.i(TAG, "handlerRemoved:" + ctx.channel());
        EventBus.getDefault().post("disconnect");
        super.handlerRemoved(ctx);
    }

    /**
     * Server monitor client
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Log.d(TAG, "channelActive: channelActive:" + ctx.channel());
        super.channelActive(ctx);
    }

    /**
     * Server read message
     * @param channelHandlerContext
     * @param messageProtocol
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MessageProtocol messageProtocol) throws Exception {
        Log.d(TAG, "channelRead0: " + messageProtocol.getMessageContent());
        //handle message
        handleMessage(channelHandlerContext.channel(), messageProtocol.getMessageContent());
    }

    /**
     * Handle message here
     */
    private void handleMessage(Channel channel, String messageContent) {
        EventBus.getDefault().post(new MessageEvent(messageContent));
        //response, send message to client
        MessageProtocol messageProtocol= new MessageProtocol();
        messageProtocol.setMessageContent(messageContent);
        channel.writeAndFlush(messageProtocol);
    }

    /**
     * Exception handling
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
