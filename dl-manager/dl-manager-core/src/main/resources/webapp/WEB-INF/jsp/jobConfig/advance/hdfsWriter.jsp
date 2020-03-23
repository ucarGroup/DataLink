<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-content-inner">
    <div class="page-content">
        <div class="row">

            <form id="add_form" class="form-horizontal" role="form">

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-hdfs_path_type">定时任务路径类型</label>
                    <div class="col-sm-9">
                        ‍‍<select id="form-add-hdfs_path_type" style="width:350px;height:35px" class="chosen-select col-sm-5" onchange="reloadJson()">
                        <option grade="0" value="false" selected>天类型</option>
                        <option grade="1" value="true" >小时类型</option>
                    </select>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-hdfs_pre_del">写之前先清空数据？</label>
                    <div class="col-sm-9">
                        ‍‍<select id="form-add-hdfs_pre_del" style="width:350px;height:35px" class="chosen-select col-sm-5" onchange="reloadJson()">
                        <option grade="0" value="false" selected>否</option>
                        <option grade="1" value="true" >是</option>
                    </select>
                    </div>
                </div>




            </form>
        </div>

    </div>
    <!-- /.page-content -->
</div>
<script type="text/javascript">

</script>
