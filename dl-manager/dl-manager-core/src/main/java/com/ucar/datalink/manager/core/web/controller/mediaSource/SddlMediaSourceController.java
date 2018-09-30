package com.ucar.datalink.manager.core.web.controller.mediaSource;

import com.google.common.collect.Sets;
import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.biz.utils.DataSourceFactory;
import com.ucar.datalink.common.errors.ValidationException;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.sddl.SddlMediaSrcParameter;
import com.ucar.datalink.manager.core.coordinator.ClusterState;
import com.ucar.datalink.manager.core.coordinator.GroupMetadataManager;
import com.ucar.datalink.manager.core.server.ServerContainer;
import com.ucar.datalink.manager.core.web.dto.mediaSource.SddlMediaSourceView;
import com.ucar.datalink.manager.core.web.util.Page;
import org.apache.commons.lang.StringUtils;
import org.apache.oro.text.regex.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by csf on 17/5/23.
 */
@Controller
@RequestMapping(value = "/sddl/")
public class SddlMediaSourceController {

    private static final Logger logger = LoggerFactory.getLogger(SddlMediaSourceController.class);

    @Autowired
    private MediaSourceService mediaSourceService;

    @RequestMapping(value = "/sddlList")
    public ModelAndView sddlList() {
        ModelAndView mav = new ModelAndView("sddlMediaSource/list");
        return mav;
    }

    @RequestMapping(value = "/initSddl")
    @ResponseBody
    public Page<SddlMediaSourceView> initSddl() {
        Set<MediaSourceType> mediaSourceType = new HashSet<>();
        mediaSourceType.add(MediaSourceType.SDDL);
        List<MediaSourceInfo> mediaSourceInfoList = mediaSourceService.getListByType(mediaSourceType);
        //构造view
        List<SddlMediaSourceView> sddlViews = mediaSourceInfoList.stream().map(i -> {
            SddlMediaSourceView view = new SddlMediaSourceView();
            view.setId(i.getId());
            view.setSddlName(i.getName());
            view.setCreateTime(i.getCreateTime());
            return view;
        }).collect(Collectors.toList());
        return new Page<>(sddlViews);
    }


    @RequestMapping(value = "/toAdd")
    public ModelAndView toAdd() {
        Set<MediaSourceType> mediaSourceType = new HashSet<>();
        mediaSourceType.add(MediaSourceType.MYSQL);
        List<MediaSourceInfo> mediaSourceInfoList = mediaSourceService.getListByType(mediaSourceType);
        ModelAndView mav = new ModelAndView("sddlMediaSource/add");
        mav.addObject("mediaSourceInfoList", mediaSourceInfoList);
        return mav;
    }

    @ResponseBody
    @RequestMapping(value = "/doAdd")
    public String doAdd(@ModelAttribute("sddlMediaSourceView") SddlMediaSourceView sddlMediaSourceView) {
        try {
            List<Long> primaryIdList = Arrays.asList(sddlMediaSourceView.getPrimaryRdbId().split(","))
                    .stream()
                    .sorted()
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
            Long proxyDbId = Long.valueOf(sddlMediaSourceView.getProxyDbId());
            checkProxyDB(proxyDbId, primaryIdList);

            MediaSourceInfo mediaSourceInfo = new MediaSourceInfo();
            SddlMediaSrcParameter sddlMediaSrcParameter = new SddlMediaSrcParameter();
            sddlMediaSrcParameter.setMediaSourceType(MediaSourceType.SDDL);
            sddlMediaSrcParameter.setNamespace(buildNamespace(primaryIdList));
            sddlMediaSrcParameter.setPrimaryDbsId(primaryIdList);
            sddlMediaSrcParameter.setProxyDbId(proxyDbId);
            if (StringUtils.isNotBlank(sddlMediaSourceView.getSecondaryRdbId())) {
                sddlMediaSrcParameter.setSecondaryDbsId(
                        Arrays.asList(sddlMediaSourceView.getSecondaryRdbId().split(","))
                                .stream()
                                .sorted()
                                .map(Long::valueOf)
                                .collect(Collectors.toList()));
            }

            mediaSourceInfo.setName(sddlMediaSourceView.getSddlName());
            mediaSourceInfo.setParameter(sddlMediaSrcParameter.toJsonString());
            mediaSourceInfo.setDesc(sddlMediaSourceView.getSddlDesc());
            mediaSourceInfo.setType(MediaSourceType.SDDL);
            Boolean isSuccess = mediaSourceService.insert(mediaSourceInfo);
            if (isSuccess) {
                return "success";
            }
        } catch (Exception e) {
            logger.error("Add Sddl Media Source Error.", e);
            return e.getMessage();
        }
        return "fail";
    }

    @ResponseBody
    @RequestMapping(value = "/doDelete")
    public String doDelete(HttpServletRequest request) {
        String id = request.getParameter("id");
        if (StringUtils.isBlank(id)) {
            return "fail";
        }
        try {
            Boolean isSuccess = mediaSourceService.delete(Long.valueOf(id));
            if (isSuccess) {
                return "success";
            }
        } catch (ValidationException e) {
            return e.getMessage();
        }
        return "fail";
    }

    @RequestMapping(value = "/toEdit")
    public ModelAndView toEdit(HttpServletRequest request) {
        String id = request.getParameter("id");
        MediaSourceInfo mediaSourceInfo = new MediaSourceInfo();
        ModelAndView mav = new ModelAndView("sddlMediaSource/edit");
        if (StringUtils.isNotBlank(id)) {
            mediaSourceInfo = mediaSourceService.getById(Long.valueOf(id));
        }
        SddlMediaSrcParameter sddlMediaSrcParameter = mediaSourceInfo.getParameterObj();
        SddlMediaSourceView sddlMediaSourceView = new SddlMediaSourceView();
        sddlMediaSourceView.setId(Long.valueOf(id));
        sddlMediaSourceView.setSddlName(mediaSourceInfo.getName());
        sddlMediaSourceView.setSddlDesc(mediaSourceInfo.getDesc());
        sddlMediaSourceView.setPrimaryRdbId(StringUtils.join(sddlMediaSrcParameter.getPrimaryDbsId().toArray(), ","));
        sddlMediaSourceView.setProxyDbId(sddlMediaSrcParameter.getProxyDbId().toString());
        sddlMediaSourceView.setSecondaryRdbId(StringUtils.join(sddlMediaSrcParameter.getSecondaryDbsId().toArray(), ","));

        List<MediaSourceInfo> mediaSourceInfoList = mediaSourceService.getListByType(Sets.newHashSet(MediaSourceType.MYSQL));
        mav.addObject("mediaSourceInfoList", mediaSourceInfoList);
        mav.addObject("sddlMediaSourceView", sddlMediaSourceView);
        return mav;
    }

    @ResponseBody
    @RequestMapping(value = "/doEdit")
    public String doEdit(@ModelAttribute("sddlMediaSourceView") SddlMediaSourceView sddlMediaSourceView, Long sddlMediaSourceId) {
        try {
            if (sddlMediaSourceId == null) {
                throw new RuntimeException("sddlMediaSourceId is empty");
            }
            List<Long> primaryIdList = Arrays.asList(sddlMediaSourceView.getPrimaryRdbId().split(","))
                    .stream()
                    .sorted()
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
            Long proxyDbId = Long.valueOf(sddlMediaSourceView.getProxyDbId());
            checkProxyDB(proxyDbId, primaryIdList);

            MediaSourceInfo mediaSourceInfo = new MediaSourceInfo();
            SddlMediaSrcParameter sddlMediaSrcParameter = new SddlMediaSrcParameter();
            sddlMediaSrcParameter.setMediaSourceType(MediaSourceType.SDDL);
            sddlMediaSrcParameter.setNamespace(buildNamespace(primaryIdList));
            sddlMediaSrcParameter.setPrimaryDbsId(primaryIdList);
            sddlMediaSrcParameter.setProxyDbId(proxyDbId);
            if (StringUtils.isNotBlank(sddlMediaSourceView.getSecondaryRdbId())) {
                sddlMediaSrcParameter.setSecondaryDbsId(Arrays.asList(sddlMediaSourceView.getSecondaryRdbId().split(","))
                        .stream()
                        .sorted()
                        .map(Long::valueOf)
                        .collect(Collectors.toList()));
            }

            mediaSourceInfo.setId(sddlMediaSourceId);
            mediaSourceInfo.setName(sddlMediaSourceView.getSddlName());
            mediaSourceInfo.setParameter(sddlMediaSrcParameter.toJsonString());
            mediaSourceInfo.setDesc(sddlMediaSourceView.getSddlDesc());
            mediaSourceInfo.setType(MediaSourceType.SDDL);
            Boolean isSuccess = mediaSourceService.update(mediaSourceInfo);
            toReloadDB(sddlMediaSourceId.toString());
            if (isSuccess) {
                return "success";
            }
        } catch (Throwable e) {
            logger.error("sddl介质更新报错，", e);
            return "sddl介质更新报错，"+e.getMessage();
        }
        return "fail";
    }

    private void checkProxyDB(Long proxyDbId, List<Long> primaryDbs) {
        if (!primaryDbs.contains(proxyDbId)) {
            throw new ValidationException("ProxyDb must be one of the primary dbs.");
        }
    }

    private String buildNamespace(List<Long> idList) throws MalformedPatternException {
        TreeSet<String> schemas = new TreeSet<>();
        idList.forEach(i -> {
            MediaSourceInfo mediaSource = mediaSourceService.getById(i);
            schemas.add(mediaSource.getParameterObj().getNamespace());
        });

        if (schemas.size() == 1) {//兼容所有分库的schema都相同的情况
            return schemas.stream().collect(Collectors.toList()).get(0);
        } else {
            if (schemas.size() != idList.size()) {
                throw new ValidationException("size of schemas not equals size of idList");
            } else {
                return buildNamespace(schemas);
            }
        }
    }

    private String buildNamespace(TreeSet<String> schemas) throws MalformedPatternException {
        String prefix = null;
        Integer suffix = -1;

        String patternStr = "(.*)(\\d+)";
        PatternCompiler pc = new Perl5Compiler();
        Pattern pattern = pc.compile(patternStr, Perl5Compiler.CASE_INSENSITIVE_MASK | Perl5Compiler.READ_ONLY_MASK);
        for (String s : schemas) {
            PatternMatcher matcher = new Perl5Matcher();
            if (!matcher.matches(s, pattern)) {
                throw new ValidationException("invliad schema :" + s);
            }
            MatchResult matchResult = matcher.getMatch();

            String tempPrefix = matchResult.group(1);
            if (prefix == null) {
                prefix = tempPrefix;
            } else {
                if (!tempPrefix.equals(prefix)) {
                    throw new ValidationException("The prefix of the primary db schemas is not same.");
                }
            }

            String tempSuffix = matchResult.group(2);
            if (!Integer.valueOf(tempSuffix).equals(++suffix)) {
                throw new ValidationException("suffix number of the schemas must be continuous");
            }
        }

        return prefix + "[" + 0 + "-" + (schemas.size() - 1) + "]";
    }

    @ResponseBody
    @RequestMapping(value = "/toReloadDB")
    public String toReloadDB(String mediaSourceId) {
        try {
            if (StringUtils.isBlank(mediaSourceId)) {
                throw new RuntimeException("mediaSourceId is empty");
            }
            GroupMetadataManager groupMetadataManager = ServerContainer.getInstance().getGroupCoordinator().getGroupManager();
            ClusterState clusterState = groupMetadataManager.getClusterState();
            if (clusterState == null) {
                return "success";
            }
            List<ClusterState.MemberData> memberDatas = clusterState.getAllMemberData();
            if (memberDatas == null || memberDatas.size() == 0) {
                return "success";
            }
            MediaSourceInfo mediaSourceInfo = mediaSourceService.getById(Long.valueOf(mediaSourceId));
            List<Long> primaryDbsId = ((SddlMediaSrcParameter) mediaSourceInfo.getParameterObj()).getPrimaryDbsId();
            List<Long> secondaryDbsId = ((SddlMediaSrcParameter) mediaSourceInfo.getParameterObj()).getSecondaryDbsId();
            for (Long primaryDbId : primaryDbsId) {
                MediaSourceInfo ms = mediaSourceService.getById(primaryDbId);
                DataSourceFactory.invalidate(ms, () -> null);
            }
            for (Long secondaryDbId : secondaryDbsId) {
                MediaSourceInfo ms = mediaSourceService.getById(secondaryDbId);
                DataSourceFactory.invalidate(ms, () -> null);
            }
            for (ClusterState.MemberData mem : memberDatas) {
                String url = "http://" + mem.getWorkerState().url() + "/flush/reloadMediaSource/" + mediaSourceId;
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity request = new HttpEntity(null, headers);
                new RestTemplate().postForObject(url, request, Map.class);
            }
            return "success";
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
