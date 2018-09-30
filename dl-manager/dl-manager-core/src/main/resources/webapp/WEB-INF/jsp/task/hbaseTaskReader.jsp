<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div id="hbaseTaskReader" class="col-sm-12">
    <form class="form-horizontal" role="form">
        <div class="col-sm-6">
            <div class="form-group">
                <label class="col-sm-5 control-label no-padding-right"
                       for="hbaseReader-mediaSourceId">HBase</label>

                <div class="col-sm-7">
                    <select name="mediaSourceId" id="hbaseReader-mediaSourceId"
                            class="chosen-select">
                        <option value="-1">请选择...</option>
                        <c:forEach items="${taskModel.mediaSourceList}" var="bean">
                            <c:if test="${bean.id==taskModel.hbaseReaderParameter.mediaSourceId}">
                                <option selected="selected" value="${bean.id}">${bean.name}</option>
                            </c:if>
                            <c:if test="${bean.id!=taskModel.hbaseReaderParameter.mediaSourceId}">
                                <option value="${bean.id}">${bean.name}</option>
                            </c:if>
                        </c:forEach>
                    </select>
                </div>
            </div>

            <div class="form-group">
                <label class="col-sm-5 control-label no-padding-right"
                       for="hbaseReader-replZkMediaSourceId">Repl-ZK</label>

                <div class="col-sm-7">
                    <select name="replZkMediaSourceId" id="hbaseReader-replZkMediaSourceId"
                            class="chosen-select">
                        <c:forEach items="${taskModel.zkMediaSourceList}" var="bean">
                            <c:if test="${bean.id==taskModel.hbaseReaderParameter.replZkMediaSourceId}">
                                <option selected="selected" value="${bean.id}">${bean.name}</option>
                            </c:if>
                            <c:if test="${bean.id!=taskModel.hbaseReaderParameter.replZkMediaSourceId}">
                                <option value="${bean.id}">${bean.name}</option>
                            </c:if>
                        </c:forEach>
                    </select>
                </div>
            </div>

            <div class="form-group">
                <label class="col-sm-5 control-label no-padding-right"
                       for="hbaseReader-replZnodeParent">Repl-ZNode-Parent</label>

                <div class="col-sm-7">
                    <input type="text" style="width:100%;" id="hbaseReader-replZnodeParent"
                           name="replZnodeParent" class="col-xs-10 col-sm-5"
                           value="${taskModel.hbaseReaderParameter.replZnodeParent}"/>
                </div>
            </div>

            <div class="form-group">
                <label class="col-sm-5 control-label no-padding-right"
                       for="hbaseReader-dump">是否dump</label>

                <div class="col-sm-7">
                    <select name="dump" id="hbaseReader-dump" class="col-sm-12">
                        <c:if test="${taskModel.hbaseReaderParameter.dump==true}">
                            <option value="false">否</option>
                            <option selected="selected" value="true">是</option>
                        </c:if>
                        <c:if test="${taskModel.hbaseReaderParameter.dump==false}">
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
    $("#hbaseReader-mediaSourceId").chosen({allow_single_deselect: true, width: "100%"});
    $("#hbaseReader-replZkMediaSourceId").chosen({allow_single_deselect: true, width: "100%"});

    function getHbaseReaderObj() {
        var obj = {
            mediaSourceId: $("#hbaseReader-mediaSourceId").val(),
            replZkMediaSourceId: $("#hbaseReader-replZkMediaSourceId").val(),
            replZnodeParent: $("#hbaseReader-replZnodeParent").val(),
            dump: $("#hbaseReader-dump").val()
        };
        return obj;
    }

    $('#hbaseReader-mediaSourceId').change(function () {
        var mediaSourceId = $('#hbaseReader-mediaSourceId').val();
        $.ajax({
            type: "post",
            url: "${basePath}/hbaseTask/getReplZnodeParent",
            async: true,
            dataType: "json",
            data: "&mediaSourceId=" + mediaSourceId,
            success: function (result) {
                if (result != null && result != '') {
                    $('input[name=replZnodeParent]').val('');
                    $('input[name=replZnodeParent]').val(result.replZnodeParent);
                }
            }
        });
    });

</script>