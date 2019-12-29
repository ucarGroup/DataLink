package com.ucar.datalink.domain.event;

import com.ucar.datalink.common.event.CallbackEvent;
import com.ucar.datalink.common.utils.FutureCallback;
import com.ucar.datalink.domain.media.MediaSourceInfo;

/**
 * Created by sqq on 2017/6/22.
 */
public class KuduConfigClearEvent extends CallbackEvent {
    private MediaSourceInfo mediaSourceInfo;
    public KuduConfigClearEvent(FutureCallback callback, MediaSourceInfo mediaSourceInfo) {
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
