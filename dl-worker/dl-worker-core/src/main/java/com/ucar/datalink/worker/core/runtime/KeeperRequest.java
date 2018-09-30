package com.ucar.datalink.worker.core.runtime;

import com.ucar.datalink.common.utils.Callback;

import java.util.concurrent.Callable;

/**
 * Created by lubiao on 2018/3/9.
 */
public class KeeperRequest implements Comparable<KeeperRequest> {
    private final long at;
    private final Callable<Void> action;
    private final Callback<Void> callback;

    public KeeperRequest(long at, Callable<Void> action, Callback<Void> callback) {
        this.at = at;
        this.action = action;
        this.callback = callback;
    }

    public long at() {
        return at;
    }

    public Callable<Void> action() {
        return action;
    }

    public Callback<Void> callback() {
        return callback;
    }

    @Override
    public int compareTo(KeeperRequest o) {
        return Long.compare(at, o.at);
    }
}