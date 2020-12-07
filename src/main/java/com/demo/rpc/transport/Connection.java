package com.demo.rpc.transport;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.demo.rpc.Constants;
import com.demo.rpc.protocol.Header;
import com.demo.rpc.protocol.Message;
import com.demo.rpc.protocol.Request;
import com.demo.rpc.protocol.Response;

import io.netty.channel.ChannelFuture;
import io.netty.channel.DefaultEventLoop;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;


public class Connection implements Closeable {
    // 用于生成消息ID，全局唯一
    private final static AtomicLong ID_GENERATOR = new AtomicLong(0);

    // TODO 时间轮定时删除
    public final static Map<Long, NettyResponseFuture<Response>> IN_FLIGHT_REQUEST_MAP
            = new ConcurrentHashMap<>();

    private ChannelFuture future;

    private AtomicBoolean isConnected = new AtomicBoolean();

    public Connection() {
        this.isConnected.set(false);
        this.future = null;
    }

    public Connection(ChannelFuture future, boolean isConnected) {
        this.future = future;
        this.isConnected.set(isConnected);
    }

    public ChannelFuture getFuture() {
        return future;
    }

    public void setFuture(ChannelFuture future) {
        this.future = future;
    }

    public boolean isConnected() {
        return isConnected.get();
    }

    public void setIsConnected(boolean isConnected) {
        this.isConnected.set(isConnected);
    }

    public NettyResponseFuture<Response> request(Message<Request> message, long timeOut) {
        // 生成并设置消息ID
        long messageId = ID_GENERATOR.incrementAndGet();
        message.getHeader().setMessageId(messageId);
        // 创建消息关联的Future
        NettyResponseFuture responseFuture = new NettyResponseFuture(System.currentTimeMillis(),
                timeOut, message, future.channel(), new DefaultPromise(new DefaultEventLoop()));
        // 将消息ID和关联的Future记录到IN_FLIGHT_REQUEST_MAP集合中
        IN_FLIGHT_REQUEST_MAP.put(messageId, responseFuture);
        try {
            future.channel().writeAndFlush(message); // 发送请求
        } catch (Exception e) {
            // 发送请求异常时，删除对应的Future
            IN_FLIGHT_REQUEST_MAP.remove(messageId);
            throw e;
        }
        return responseFuture;
    }

    public boolean ping() {
        Header heartBeatHeader = new Header(Constants.MAGIC, Constants.VERSION_1);
        heartBeatHeader.setExtraInfo(Constants.HEART_EXTRA_INFO);
        Message message = new Message(heartBeatHeader, null);
        NettyResponseFuture<Response> request = request(message, Constants.DEFAULT_TIMEOUT);
        try {
            Promise<Response> await = request.getPromise().await();
            return await.get().getCode() == Constants.HEARTBEAT_CODE;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void close() throws IOException {
        future.channel().close();
    }
}
