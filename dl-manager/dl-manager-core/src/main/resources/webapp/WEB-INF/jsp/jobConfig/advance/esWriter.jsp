<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-content-inner">
    <div class="page-content">
        <div class="row">

            <form id="add_form" class="form-horizontal" role="form">

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-es_joinColumn">joinColumn</label>
                    <div class="col-sm-9">
                        <input type="text" id="form-add-es_joinColumn" name="form-add-es_joinColumn" value="" onblur="reloadJson()"
                               class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50" />
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-es_writer_indextype">index.type</label>
                    <div class="col-sm-9">
                        <input type="text" id="form-add-es_writer_indextype" name="form-add-es_writer_indextype" value="" onblur="reloadJson()"
                               class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50" />
                    </div>
                </div>


                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-es_writer_predel">同步前先删除索引？</label>
                    <div class="col-sm-9">
                        <select id="form-add-es_writer_predel" class="width-100 tag-input-style" style="width:350px;height:35px" name="form-add-es_writer_predel" onchange="reloadJson()">
                            <option grade="1" value="false" selected >否</option>
                            <option grade="2" value="true" >是</option>
                        </select>
                    </div>
                </div>



<!--
                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-es_isAddTablePrefix">isAddTablePrefix</label>
                    <div class="col-sm-9">
                        <input type="text" id="form-add-es_isAddTablePrefix" name="form-add-es_isAddTablePrefix" value=""
                               class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50" />
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" for="form-add-es_operateType">operateType</label>
                    <div class="col-sm-9">
                        <input type="text" id="form-add-es_operateType" name="form-add-es_operateType" value=""
                               class="col-xs-10 col-sm-5" style="width:350px;height:35px" maxlength="50" />
                    </div>
                </div>
-->

            </form>
        </div>

    </div>
    <!-- /.page-content -->
</div>
<script type="text/javascript">

</script>
