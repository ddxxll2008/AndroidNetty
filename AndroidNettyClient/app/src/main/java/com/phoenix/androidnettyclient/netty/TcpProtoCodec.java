package com.phoenix.androidnettyclient.netty;

import android.util.Log;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

/**
 * tcp 协议编解码器
 * encode and decode message
 * Created by phoenix on 2017/12/29.
 */
@ChannelHandler.Sharable
public class TcpProtoCodec extends MessageToMessageCodec<ByteBuf, MessageProtocol> {
    private static final String TAG = "TcpProtoCodec";

    private static byte[] R = "R".getBytes();

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, MessageProtocol proto, List<Object> list) throws Exception {
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
        if (proto.getMessageContent() != null) {
            byteBuf.writeBytes(R);
            // 小端，方便C++ 端处理
            byteBuf.writeIntLE(proto.getMessageContent().getBytes().length);
            byteBuf.writeBytes(proto.getMessageContent().getBytes());
        }

        list.add(byteBuf);

        Log.d(TAG, "encode: encode: {}" + proto);
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        MessageProtocol proto = new MessageProtocol();

        byte r = byteBuf.readByte();
        if (r == R[0]) {
            int dataLength = byteBuf.readIntLE(); // 小端
            Log.d(TAG, "decode: " +dataLength);
            byte[] data = new byte[dataLength];
            Log.d(TAG, "decode: " + data);
            byteBuf.readBytes(data);
            proto.setPacketLen(dataLength);
            proto.setMessageContent(new String(data));
            list.add(proto);
        }
        
        Log.d(TAG, "decode: decode: {}" + proto);
    }
}
