package com.demo.rpc.transport;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.demo.rpc.Constants;
import com.demo.rpc.protocol.Message;
import com.demo.rpc.protocol.Request;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Created on 2020-06-21
 */
public class DemoRpcServerHandler extends SimpleChannelInboundHandler<Message<Request>> {

    // 业务线程池
    private static Executor executor = Executors.newCachedThreadPool();

    @Override
    protected void channelRead0(final ChannelHandlerContext channelHandlerContext, Message<Request> message) throws Exception {
        byte extraInfo = message.getHeader().getExtraInfo();
        if (Constants.isHeartBeat(extraInfo)) { // 心跳消息，直接返回即可
            channelHandlerContext.writeAndFlush(message);
            return;
        }
        // 非心跳消息，直接封装成Runnable提交到业务线程池
        executor.execute(new InvokeRunnable(message, channelHandlerContext));
    }
}