package com.ucar.datalink.domain.event;

import com.ucar.datalink.common.event.CallbackEvent;
import com.ucar.datalink.common.utils.FutureCallback;
import com.ucar.datalink.domain.media.MediaSourceInfo;

/**
 * Created by qianqian.shi on 2018/11/21.
 */
public class HBaseConfigClearEvent extends CallbackEvent {

    private MediaSourceInfo mediaSourceInfo;

    public HBaseConfigClearEvent(FutureCallback callback, MediaSourceInfo mediaSourceInfo) {
        super(callback);
        this.mediaSourceInfo = mediaSourceInfo;
    }

    public MediaSourceInfo getMediaSourceInfo() {
        return mediaSourceInfo;
    }

    public void setMediaSourceInfo(MediaSourceInfo mediaSourceInfo) {
        this.mediaSourceInfo = mediaSourceInfo;
    }
}
