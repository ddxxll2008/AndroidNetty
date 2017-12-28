package com.phoenix.nettyserver.netty;

/**
 * server interface
 * Created by phoenix on 2017/12/28.
 */

public interface IServer {
    void start() throws Exception;

    void restart() throws Exception;

    void shutdown();
}
