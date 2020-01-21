<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-content-inner">
    <div class="page-content">
        <div class="row">

            <form id="add_form" class="form-horizontal" role="form">

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-hbase_regions">region数量</label>
                    <div class="col-sm-9">
                        <input type="text" id="form-add-hbase_regions" name="form-add-hbase_regions" value=""
                               class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50" />
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-hbase_split_num">拆分的任务数</label>
                    <div class="col-sm-9">
                        <input type="text" id="form-add-hbase_split_num" name="form-add-hbase_split_num" value="" onblur="checkHBaseSplitNum()"
                               class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50" />
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-hbase_specified_num">读取指定的条数后再解析表结构</label>
                    <div class="col-sm-9">
                        <input type="text" id="form-add-hbase_specified_num" name="form-add-hbase_specified_num" value="" onblur="reloadJson()"
                               class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50" />
                    </div>
                </div>

            </form>
        </div>

    </div>
    <!-- /.page-content -->
</div>
<script type="text/javascript">

    function checkHBaseSplitNum() {
        var regionCount = $("#form-add-hbase_regions").val();
        var splitCount = $("#form-add-hbase_split_num").val();
        if(splitCount!=null && regionCount!=null) {
            //split 必须 小于等于region数量
        }

    }

</script>
