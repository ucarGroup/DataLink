<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-content-inner">
    <div class="page-content">
        <div class="row">
            <form id="add_form" class="form-horizontal" role="form">
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-sqlserver_where">where语句</label>
                    <div class="col-sm-9">
                        <textarea id="form-add-sqlserver_where" name="form-add-sqlserver_where" onblur="reloadJson()" class="col-xs-10 col-sm-5" rows="10" cols="150" >
                        </textarea>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-sqlserver_querysql">querySql</label>
                    <div class="col-sm-9">
                        <textarea id="form-add-sqlserver_querysql" name="form-add-sqlserver_querysql" onblur="reloadJson()" class="col-xs-10 col-sm-5" rows="10" cols="150" >
                        </textarea>
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-sqlserver_jdbcurl">高可用url</label>
                    <div class="col-sm-9">
                        <input type="text" id="form-add-sqlserver_jdbcurl" name="form-add-sqlserver_jdbcurl" value="" onblur="reloadJson()"
                               class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50" />
                    </div>
                </div>

            </form>
        </div>

    </div>
    <!-- /.page-content -->
</div>
<script type="text/javascript">

</script>
