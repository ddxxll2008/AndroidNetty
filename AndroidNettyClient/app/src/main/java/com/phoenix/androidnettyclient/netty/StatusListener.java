package com.phoenix.androidnettyclient.netty;

/**
 * Created by phoenix on 2017/12/29.
 */

public abstract class StatusListener {
    /**
     * listen and handle server status change
     * @param status
     */
    public abstract void onStatusChange(int status);
}
