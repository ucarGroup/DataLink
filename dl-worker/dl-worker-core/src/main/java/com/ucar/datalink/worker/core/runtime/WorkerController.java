package com.ucar.datalink.worker.core.runtime;

import com.ucar.datalink.worker.core.runtime.rest.RestServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Datalink Worker 中央控制器
 * Created by lubiao on 2016/11/24.
 */
public class WorkerController {
    private static final Logger logger = LoggerFactory.getLogger(WorkerController.class);

    private final Keeper keeper;
    private final RestServer rest;

    public WorkerController(Keeper keeper, RestServer rest) {
        this.keeper = keeper;
        this.rest = rest;
    }

    public void startup() {
        keeper.start();
        rest.start(keeper);
        logger.info("Worker Controller started.");
    }

    public void shutdown() {
        rest.stop();
        keeper.stop();

        logger.info("Worker Controller stopped.");
    }
}
