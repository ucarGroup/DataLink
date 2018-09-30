package com.ucar.datalink.reader.mysql;

import com.ucar.datalink.biz.service.MediaService;
import com.ucar.datalink.domain.media.MediaInfo.ModeValue;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.domain.media.ModeUtils;
import com.ucar.datalink.worker.api.task.TaskReaderContext;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by user on 2017/3/20.
 */
public class CanalFilterBuilder {

    public static String makeFilterExpression(TaskReaderContext context) {
        List<MediaMappingInfo> mappings = context.getService(MediaService.class).getMediaMappingsByTask(Long.valueOf(context.taskId()), false);
        if (mappings.isEmpty()) {
            return "";
        }

        Set<String> mediaNames = new HashSet<>();
        for (MediaMappingInfo mediaMapping : mappings) {
            ModeValue namespaceMode = mediaMapping.getSourceMedia().getNamespaceMode();
            ModeValue nameMode = mediaMapping.getSourceMedia().getNameMode();

            if (namespaceMode.getMode().isSingle()) {
                buildFilter(mediaNames, namespaceMode.getSingleValue(), nameMode, false);
            } else if (namespaceMode.getMode().isMulti()) {
                for (String namespace : namespaceMode.getMultiValue()) {
                    buildFilter(mediaNames, namespace, nameMode, false);
                }
            } else if (namespaceMode.getMode().isWildCard()) {
                buildFilter(mediaNames, namespaceMode.getSingleValue(), nameMode, true);
            }
        }

        StringBuilder result = new StringBuilder();
        Iterator<String> iter = mediaNames.iterator();
        int i = -1;
        while (iter.hasNext()) {
            i++;
            if (i == 0) {
                result.append(iter.next());
            } else {
                result.append(",").append(iter.next());
            }
        }

        return result.toString();
    }

    private static void buildFilter(Set<String> mediaNames, String namespace, ModeValue nameMode, boolean wildcard) {
        String splitChar = ".";
        if (wildcard) {
            splitChar = "\\.";
        }

        if (nameMode.getMode().isSingle()) {
            mediaNames.add(namespace + splitChar + nameMode.getSingleValue());
        } else if (nameMode.getMode().isMulti()) {
            for (String name : nameMode.getMultiValue()) {
                mediaNames.add(namespace + splitChar + name);
            }
        } else if (nameMode.getMode().isWildCard()) {
            mediaNames.add(namespace + "\\." + nameMode.getSingleValue());
        } else if (nameMode.getMode().isYearly()) {//按年分表和按月分表的情况，将所有匹配prefix的表都加到名单里
            mediaNames.add(namespace + "\\." + ModeUtils.getYearlyPrefix(nameMode.getSingleValue()) + ".*");
        } else if (nameMode.getMode().isMonthly()) {
            mediaNames.add(namespace + "\\." + ModeUtils.getMonthlyPrefix(nameMode.getSingleValue()) + ".*");
        }
    }
}
