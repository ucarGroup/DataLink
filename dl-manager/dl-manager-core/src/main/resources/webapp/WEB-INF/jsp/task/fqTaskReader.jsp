<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div id="fqTaskReader" class="col-sm-12">
    <form class="form-horizontal" role="form">
        <div class="col-sm-6">
            <div class="form-group">
                <label class="col-sm-5 control-label no-padding-right"
                       for="fqReader-mediaSourceId">关联数据源</label>

                <div class="col-sm-7">
                    <select multiple="" name="mediaSourceId" id="fqReader-mediaSourceId" class="mediaSourceId tag-input-style"
                            data-placeholder="Click to Choose..." >
                        <c:forEach items="${taskModel.mediaSourceList}" var="bean">
                            <c:if test="${bean.id==taskModel.fqReaderParameter.mediaSourceId}">
                                <option selected="selected" value="${bean.id}">${bean.name}</option>
                            </c:if>
                            <c:if test="${bean.id!=taskModel.fqReaderParameter.mediaSourceId}">
                                <option value="${bean.id}">${bean.name}</option>
                            </c:if>
                        </c:forEach>
                    </select>
                </div>
            </div>

            <div class="form-group">
                <label class="col-sm-5 control-label no-padding-right"
                       for="fqReader-originalMediaSourceId">原始数据源</label>

                <div class="col-sm-7">
                    <select multiple="" name="originalMediaSourceId" id="fqReader-originalMediaSourceId"
                            class="originalMediaSourceId tag-input-style"
                            data-placeholder="Click to Choose...">
                        <c:forEach items="${taskModel.originalMediaSourceList}" var="bean">
                            <c:if test="${bean.id==taskModel.fqReaderParameter.originalMediaSourceId}">
                                <option selected="selected" value="${bean.id}">${bean.name}</option>
                            </c:if>
                            <c:if test="${bean.id!=taskModel.fqReaderParameter.originalMediaSourceId}">
                                <option value="${bean.id}">${bean.name}</option>
                            </c:if>
                        </c:forEach>
                    </select>
                </div>
            </div>

            <div class="form-group">
                <label class="col-sm-5 control-label no-padding-right"
                       for="fqReader-group">group</label>

                <div class="col-sm-7">
                    <input type="text" style="width:100%;" id="fqReader-group"
                           name="group" class="col-xs-10 col-sm-5"
                           value="${taskModel.fqReaderParameter.group}"/>
                </div>
            </div>

            <div class="form-group">
                <label class="col-sm-5 control-label no-padding-right"
                       for="fqReader-offset">offset</label>

                <div class="col-sm-7">
                    <input type="text" style="width:100%;" id="fqReader-offset"
                           name="offset" class="col-xs-10 col-sm-5"
                           value="${taskModel.fqReaderParameter.offset}"/>
                </div>
            </div>

            <div class="form-group">
                <label class="col-sm-5 control-label no-padding-right"
                       for="fqReader-maxFetchRetries">maxFetchRetries</label>

                <div class="col-sm-7">
                    <input type="text" style="width:100%;" id="fqReader-maxFetchRetries"
                           name="maxFetchRetries" class="col-xs-10 col-sm-5"
                           value="${taskModel.fqReaderParameter.maxFetchRetries}"/>
                </div>
            </div>

            <div class="form-group">
                <label class="col-sm-5 control-label no-padding-right"
                       for="fqReader-commitOffsetPeriodInMills">commitOffsetPeriodInMills</label>

                <div class="col-sm-7">
                    <input type="text" style="width:100%;" id="fqReader-commitOffsetPeriodInMills"
                           name="commitOffsetPeriodInMills" class="col-xs-10 col-sm-5"
                           value="${taskModel.fqReaderParameter.commitOffsetPeriodInMills}"/>
                </div>
            </div>

            <div class="form-group">
                <label class="col-sm-5 control-label no-padding-right"
                       for="fqReader-maxDelayFetchTimeInMills">maxDelayFetchTimeInMills</label>

                <div class="col-sm-7">
                    <input type="text" style="width:100%;" id="fqReader-maxDelayFetchTimeInMills"
                           name="maxDelayFetchTimeInMills" class="col-xs-10 col-sm-5"
                           value="${taskModel.fqReaderParameter.maxDelayFetchTimeInMills}"/>
                </div>
            </div>

            <div class="form-group">
                <label class="col-sm-5 control-label no-padding-right"
                       for="fqReader-bufferSize">bufferSize</label>

                <div class="col-sm-7">
                    <input type="text" style="width:100%;" id="fqReader-bufferSize"
                           name="bufferSize" class="col-xs-10 col-sm-5"
                           value="${taskModel.fqReaderParameter.bufferSize}"/>
                </div>
            </div>

            <div class="form-group">
                <label class="col-sm-5 control-label no-padding-right"
                       for="fqReader-dump">是否dump</label>

                <div class="col-sm-7">
                    <select name="dump" id="fqReader-dump" class="col-sm-12">
                        <c:if test="${taskModel.fqReaderParameter.dump==true}">
                            <option value="false">否</option>
                            <option selected="selected" value="true">是</option>
                        </c:if>
                        <c:if test="${taskModel.fqReaderParameter.dump==false}">
                            <option selected="selected" value="false">否</option>
                            <option value="true">是</option>
                        </c:if>

                    </select>
                </div>
            </div>

        </div>

    </form>
</div>
<script type="text/javascript">
    $('.mediaSourceId').css('min-width', '100%').select2({allowClear: false, maximumSelectionLength: 1, width: '100%'});
    $('.originalMediaSourceId').css('min-width', '100%').select2({allowClear: false, maximumSelectionLength: 1, width: '100%'});

    function getFqReaderObj() {

        var mediaSourceArr = $("#fqReader-mediaSourceId").val();
        var mediaSourceId;
        if(mediaSourceArr && mediaSourceArr.length >= 1){
            mediaSourceId = mediaSourceArr[0];
        }

        var originalMediaSourceArr = $("#fqReader-originalMediaSourceId").val();
        var originalMediaSourceId;
        if(originalMediaSourceArr && originalMediaSourceArr.length >= 1){
            originalMediaSourceId = originalMediaSourceArr[0];
        }

        var obj = {
            mediaSourceId: mediaSourceId,
            originalMediaSourceId: originalMediaSourceId,
            group: $("#fqReader-group").val(),
            offset: $("#fqReader-offset").val(),
            maxFetchRetries: $("#fqReader-maxFetchRetries").val(),
            commitOffsetPeriodInMills: $("#fqReader-commitOffsetPeriodInMills").val(),
            maxDelayFetchTimeInMills: $("#fqReader-maxDelayFetchTimeInMills").val(),
            bufferSize: $("#fqReader-bufferSize").val(),
            dump: $("#fqReader-dump").val()
        };
        return obj;
    }

    $('#fqReader-mediaSourceId').change( function(){
        var mediaSourceId = $('#fqReader-mediaSourceId').val();
        $.ajax({
            type: "post",
            url: "${basePath}/fqTask/getListenerGroup",
            async: true,
            dataType: "json",
            data: "&mediaSourceId=" + mediaSourceId,
            success: function (result) {
                if (result != null && result != '') {
                    $('input[name=group]').val('');
                    $('input[name=group]').val(result.group);
                }
            }
        });
    });

</script>