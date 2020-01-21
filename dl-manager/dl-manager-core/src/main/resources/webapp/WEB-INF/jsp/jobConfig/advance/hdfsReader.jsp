<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-content-inner">
    <div class="page-content">
        <div class="row">

            <form id="add_form" class="form-horizontal" role="form">

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-hdfs_compress">compress</label>
                    <div class="col-sm-9">
                        <select id="form-add-hdfs_compress" class="width-100 tag-input-style" name="form-add-hdfs_compress" onchange="reloadJson()">
                            <option grade="1" value="none" >none</option>
                            <option grade="2" value="gzip" >gzip</option>
                            <option grade="3" value="snappy" >snappy</option>
                            <option grade="4" value="lzo" >lzo</option>
                        </select>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-hdfs_path">path</label>
                    <div class="col-sm-9">
                        <input type="text" id="form-add-hdfs_path" name="form-add-hdfs_path" value="" onblur="reloadJson()"
                               class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50" />
                    </div>
                </div>


                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-hdfs_compress">读路径为空时忽略异常</label>
                    <div class="col-sm-9">
                        <select id="form-add-hdfs_ignoreException" class="width-100 tag-input-style" name="form-add-hdfs_compress" onchange="reloadJson()">
                            <option grade="1" value="true" >是</option>
                            <option grade="2" value="false" >否</option>
                        </select>
                    </div>
                </div>


                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-hdfs_user">hadoop user</label>
                    <div class="col-sm-9">
                        <input type="text" id="form-add-hdfs_user" name="form-add-hdfs_user" value="" onblur="reloadJson()"
                               class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50" />
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-hdfs_path">读取前N天的数据</label>
                    <div class="col-sm-9">
                        <input type="text" id="form-add-hdfs_specifiedPreDate" name="form-add-hdfs_specifiedPreDate" value="" onblur="reloadJson()"
                               class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50" />
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-hdfs_paths">读取指定的多个path</label>
                    <div class="col-sm-9">
                        <textarea id="form-add-hdfs_paths" name="form-add-hdfs_paths" onblur="reloadJson()" class="col-xs-10 col-sm-5" rows="10" cols="150" >
                        </textarea>
                    </div>
                </div>

            </form>
        </div>

    </div>
    <!-- /.page-content -->
</div>
<script type="text/javascript">

    function checkSpecifiedPreDate() {
        var specifiedPreDate = $.trim($("#form-add-hdfs_specifiedPreDate").val());
        //判断是否为数字
        reloadJson();

    }


</script>
