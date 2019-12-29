package com.ucar.datalink.manager.core.handler;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.ucar.datalink.biz.service.SyncRelationService;
import com.ucar.datalink.biz.service.TaskConfigService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.common.event.EventBusFactory;
import com.ucar.datalink.common.event.EventHandler;
import com.ucar.datalink.common.utils.NamedThreadFactory;
import com.ucar.datalink.domain.event.MediaMappingChangeEvent;
import com.ucar.datalink.domain.task.TaskInfo;
import com.ucar.datalink.manager.core.coordinator.ClusterState;
import com.ucar.datalink.manager.core.coordinator.GroupMetadataManager;
import com.ucar.datalink.manager.core.server.ServerContainer;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by qianqian.shi on 2018/8/22.
 */
@EventHandler
public class MediaMappingChangeEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(MediaMappingChangeEventHandler.class);

    @Autowired
    private SyncRelationService syncRelationService;

    private static final ExecutorService pool = new ThreadPoolExecutor(
            2,
            Runtime.getRuntime().availableProcessors() * 2 - 1,
            60L,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(20),
            new NamedThreadFactory("MediaMappingChangeThreadPool"),
            new ThreadPoolExecutor.CallerRunsPolicy()
            );

    public MediaMappingChangeEventHandler() {
        EventBus eventBus = EventBusFactory.getEventBus();
        eventBus.register(new Object() {
            @Subscribe
            public void listener(MediaMappingChangeEvent event) {
                logger.info("Receive an mediaMapping change event with task-id " + event.getTaskId());
                try {
                    cleanMediaMapping(event.getTaskId());
                    event.getCallback().onCompletion(null, null);
                } catch (Throwable t) {
                    logger.error("something goes wrong when clean mediaMapping cache.", t);
                    event.getCallback().onCompletion(t, null);
                }
            }
        });
    }

    private void cleanMediaMapping(Long taskId) throws Exception {
        syncRelationService.clearSyncRelationCache();//清空同步检测关系中的缓存

        TaskConfigService taskConfigService = DataLinkFactory.getObject(TaskConfigService.class);
        List<TaskInfo> followerTasks = taskConfigService.getFollowerTasksForLeaderTask(taskId);
        List<Long> tasks = Lists.newArrayList(taskId);
        if (CollectionUtils.isNotEmpty(followerTasks)) {
            followerTasks.stream().forEach(t -> tasks.add(t.getId()));
        }

        GroupMetadataManager groupManager = ServerContainer.getInstance().getGroupCoordinator().getGroupManager();
        ClusterState clusterState = groupManager.getClusterState();
        if (clusterState == null) {
            return;
        }
        List<Future<?>> futures = new ArrayList<>();
        for (Long task : tasks) {

            ClusterState.MemberData memberData = clusterState.getMemberData(task);
            if (memberData == null) {
                return;
            }
            String url = "http://" + memberData.getWorkerState().url() + "/tasks/" + task + "/restart";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity request = new HttpEntity(null, headers);
            futures.add(pool.submit(new RestFeture(url, request)));
            logger.info("Prepare to send a restart task request to woker, url is : " + url);
        }
        for(Future f: futures) {
            try {
                f.get();
            } catch (Exception e) {
                logger.error(e.getMessage(),e);
                throw new RuntimeException("restart task failed for clean media mapping.", e);
            }
        }

    }

    private class RestFeture implements Callable {
        final String url;
        final HttpEntity request;

        RestFeture(String url,HttpEntity request) {
            this.url = url;
            this.request = request;
        }

        @Override
        public Object call() throws Exception {
            return new RestTemplate().postForObject(url, request, Map.class);
        }
    }
}
