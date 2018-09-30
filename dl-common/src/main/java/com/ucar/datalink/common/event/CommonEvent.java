package com.ucar.datalink.common.event;

import com.ucar.datalink.common.utils.FutureCallback;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lubiao on 2017/11/21.
 */
public class CommonEvent extends CallbackEvent {
    public static final String EVENT_NAME_KEY = "EVENT_NAME_KEY";

    private final String eventName;
    private Map<String, Object> payload = new HashMap<>();

    public CommonEvent(FutureCallback callback, String eventName, Map<String, Object> payload) {
        super(callback);
        this.eventName = eventName;
        this.payload = payload;
    }


    public String getEventName() {
        return eventName;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }
}
