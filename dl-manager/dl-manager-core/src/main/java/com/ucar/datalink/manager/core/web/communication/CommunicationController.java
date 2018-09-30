package com.ucar.datalink.manager.core.web.communication;

import com.ucar.datalink.manager.core.monitor.impl.TaskExceptionMonitor;
import com.ucar.datalink.manager.core.web.annotation.LoginIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * Created by csf on 17/5/12.
 */
@Controller
@RequestMapping(value = "/communication/")
@LoginIgnore
public class CommunicationController {

    private static Logger logger = LoggerFactory.getLogger(CommunicationController.class);

    @Autowired
    TaskExceptionMonitor taskExceptionMonitor;

    @RequestMapping(value = "/putExceptionAndCache", method = RequestMethod.POST)
    @ResponseBody
    public void putExceptionAndCache(@RequestBody final Map<Long, String> map) {
        logger.debug("Receive Exception Monitor Info : {}", map);
        taskExceptionMonitor.addException(map);
    }

    @RequestMapping(value = "/putExceptionAndSend", method = RequestMethod.POST)
    @ResponseBody
    public void putExceptionAndSend(@RequestBody final Map<Long, String> map) {
        logger.debug("Receive Exception Monitor Info : {}", map);

        if (map != null && map.size() > 0) {
            taskExceptionMonitor.sendException(map);
        }
    }

    @RequestMapping(value = "/clearException", method = RequestMethod.POST)
    @ResponseBody
    public void clearException() {
        taskExceptionMonitor.clearExceptionMap();
    }
}

