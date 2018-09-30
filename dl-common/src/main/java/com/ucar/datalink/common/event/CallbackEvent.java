package com.ucar.datalink.common.event;

import com.ucar.datalink.common.utils.FutureCallback;

/**
 * Created by sqq on 2017/6/20.
 */
public class CallbackEvent extends EventBasic {

    private FutureCallback callback;

    public CallbackEvent(FutureCallback callback){
        this.callback = callback;
    }

    public FutureCallback getCallback() {
        return callback;
    }
}
