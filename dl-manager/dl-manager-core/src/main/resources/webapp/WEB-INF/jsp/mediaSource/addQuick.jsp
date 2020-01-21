<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div id="chooseDatabase" class="modal">

</div>

<div class="page-content">
    <div class="row">
        <form id="add_form" class="form-horizontal" role="form">
        <input type="hidden" name="dbInfo" id="dbInfo"/>
            <div class="tabbable">
                <ul class="nav nav-tabs padding-12 tab-color-blue background-blue" id="myTab4">
                    <li class="active">
                        <a data-toggle="tab" href="#basicId">基础配置</a>
                    </li>
                </ul>
                <div class="tab-content" style="border: 0px">
                    <!--基础配置-->
                    <div id="basicId" class="tab-pane in active">
                        <div class="col-sm-12 col-md-offset-4">
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-3 control-label no-padding-right" for="form-add-name">数据库类型</label>

                                <div class="col-sm-7">
                                    <select name="mediaSourceType" id="form-add-mediaSourceType"
                                            class="chosen-select col-sm-12">
                                        <option value="MYSQL">MYSQL</option>
                                        <option value="SQLSERVER">SQLSERVER</option>
                                        <%--<option value="ORACLE">ORACLE</option>--%>
                                    </select>
                                </div>
                            </div>
                        </div>
                        <div class="col-sm-12 col-md-offset-4">
                            <div class="col-sm-4 form-group">
                                <label class="col-sm-3 control-label no-padding-right" for="form-add-schema">schema</label>

                                <div class="col-sm-7">
                                     <input type="text" name="namespace" class="col-sm-12"
                                            id="form-add-namespace"/>
                                </div>
                             </div>
                         </div>
                                                 <div class="col-sm-12 col-md-offset-4">
                                                     <div class="col-sm-4 form-group">
                                                         <label class="col-sm-3 control-label no-padding-right" for="form-add-name">数据源名称</label>

                                                         <div class="col-sm-7">
                                                              <input type="text" name="name" class="col-sm-12"
                                                                     id="form-add-name"/>
                                                         </div>
                                                      </div>
                                                  </div>
                     </div>
                </div>
            </div>
        </form>
    </div>
    <div class="clearfix form-actions">
        <div class="col-md-offset-5 col-md-7">
            <button class="btn btn-info" type="button" onclick="doAddQuick();">
                <i class="ace-icon fa fa-check bigger-110"></i>
                保存
            </button>

            &nbsp; &nbsp; &nbsp;
            <button class="btn" type="reset" onclick="back2Main();">
                返回
                <i class="ace-icon fa fa-undo bigger-110"></i>
            </button>
        </div>
    </div>
</div>
<!-- /.page-content -->

<script type="text/javascript">
    var databaseInfo = null;

    function doAddQuick() {
            if(databaseInfo == null ){
                initData();
            }
            if (!validateForm()) {
                return;
            }
            $.ajax({
                type: "post",
                url: "${basePath}/mediaSource/doAddQuick",
                dataType: 'json',
                data: $("#add_form").serialize(),
                async: false,
                error: function (xhr, status, err) {
                    alert(err);
                },
                success: function (data) {
                    if (data == "success") {
                        alert("添加成功！");
                        back2Main();
                    }else if(data == "old"){
                        alert("操作成功，因存在旧任务，需执行一键改造旧任务");
                        back2Main();
                     }else {
                        alert(data);
                    }
                }
            });
        }

    $('#form-add-namespace').blur(function(){
        initData();
    });

function initData(){
  var dbName = $("#form-add-namespace").val();
          var dbType = $("#form-add-mediaSourceType").val();
          if(dbName == undefined || dbName == null || dbName == ''){
              return;
          }
          $.ajax({
              type: "post",
              url: "${basePath}/mediaSource/getDbInfo?dbName="+dbName+"&dbType="+dbType,
              dataType: 'json',
              data: $("#add_form").serialize(),
              async: false,
              error: function (xhr, status, err) {
                  alert(err);
              },
              success: function (data) {
                  if (data.code == 200) {
                      databaseInfo = data.result;
                      if(databaseInfo.length<1) {
                         alert("没有获取到数据库ip信息");
                         return;
                      }
                      if(data.isMultiDb == true) {
                          var migrateDiv = $("#chooseDatabase");
                          migrateDiv.empty();
                          migrateDiv.load("${basePath}/mediaSource/toDatabaseList?id=66" + "&random=" + Math.random());
                          migrateDiv.modal('show');
                      }else{
                          $("#dbInfo").val(JSON.stringify(databaseInfo[0]));
                      }
                  } else {
                      alert("没有获取到数据库ip信息");
                  }
              }
          });
}

function validateForm() {
        if ($.trim($('#form-add-namespace').val()) == '') {
            alert('schema不能为空');
            return false;
        }
        return true;
    }

    function back2Main() {
        $("#addQuick").hide();
        $("#mainContentInner").show();
        msgAlarmListMyTable.ajax.reload();
    }
</script>
