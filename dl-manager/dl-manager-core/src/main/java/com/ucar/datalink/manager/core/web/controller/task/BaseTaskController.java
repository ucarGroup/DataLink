package com.ucar.datalink.manager.core.web.controller.task;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.ucar.datalink.domain.Position;
import com.ucar.datalink.manager.core.coordinator.ClusterState;
import com.ucar.datalink.manager.core.coordinator.GroupMetadataManager;
import com.ucar.datalink.manager.core.server.ServerContainer;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Created by lubiao on 2018/1/9.
 */
public abstract class BaseTaskController {

    @SuppressWarnings("unchecked")
    protected void sendRestartCommand(String taskId, Position position) {
        GroupMetadataManager groupManager = ServerContainer.getInstance().getGroupCoordinator().getGroupManager();
        ClusterState clusterState = groupManager.getClusterState();
        ClusterState.MemberData memberData = clusterState.getMemberData(Long.valueOf(taskId));
        String url = "http://" + memberData.getWorkerState().url() + "/tasks/" + taskId + "/restart";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity request = new HttpEntity(
                position != null ? JSONObject.toJSONString(position, SerializerFeature.WriteClassName) : null,
                headers);
        Map<String, String> result = new RestTemplate().postForObject(url, request, Map.class);
    }
}
