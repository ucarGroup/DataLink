<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>


    <div class="page-content">
        <div class="row">
            <div class="col-xs-12">

                <div class="col-xs-12">
                    <!-- 查询 选项 -->
                    <div class="row">
                        <form class="form-horizontal">
                            <div class="row">
                                <div class="form-group">
                                    <label class="col-sm-3 control-label no-padding-right" for="db_type">数据源类型</label>
                                    <div class="col-sm-8">
                                        <select multiple=""
                                                class="db_type col-sm-5"
                                                data-placeholder="Click to Choose..." id="db_type"
                                                style="width:350px;height:35px">
                                            <option grade="1" value="HBase" >HBase</option>
                                            <option grade="2" value="MySql" >MySql</option>
                                            <option grade="3" value="SqlServer" >SqlServer</option>
                                            <option grade="4" value="HDFS"  >HDFS</option>
                                            <option grade="5" value="ElasticSearch" >ElasticSearch</option>
                                            <option grade="5" value="PostgreSql" >PostgreSql</option>
                                        </select>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="col-sm-3 control-label no-padding-right" for="db_name">数据源名称</label>
                                    <div class="col-sm-8">
                                        <select  multiple="" id="db_name" style="width:350px;height:35px" class="db_name col-sm-5"
                                                 data-placeholder="Click to Choose..." name="mediaName">
                                        </select>
                                    </div>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>


                <div class="row">
                    <table id="jobModifyListTable" class="table table-striped table-bordered table-hover">
                        <thead>
                        <tr>
                            <td>任务ID</td>
                            <td>任务名称</td>
                            <td>源库名称</td>
                            <td>目标库名称</td>
                            <td>介质名称</td>
                            <td>是否定时</td>
                            <td>创建时间</td>
                        </tr>
                        </thead>
                    </table>
                </div>
            </div>

            <div class="clearfix form-actions">
                <div class="col-md-offset-5 col-md-7">
                    <button class="btn btn-info" type="button" onclick="modify();">
                        <i class="ace-icon fa fa-check bigger-110"></i>
                        替换
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

    </div>

<script type="text/javascript">

    //$("#job_list").empty();
    //$("#job_list").hide();

    $('.db_type').css('min-width', '100%').select2({
        allowClear: false,
        maximumSelectionLength: 1,
        width: '50%'
    });

    $('.db_name').css('min-width', '100%').select2({
        allowClear: false,
        maximumSelectionLength: 1,
        width: '50%'
    });

    function back2Main() {
        $("#reloadjob").hide();
        $("#mainContentInner").show();
        //jobListTable.ajax.reload();
    }

    var jobModifyListTable
    $(".chosen-select").chosen();

    jobModifyListTable = $('#jobModifyListTable').DataTable({
        //"bAutoWidth": true,
        "serverSide" : true,//开启服务器模式:启用服务器分页
        "paging" : true,//是否分页
        "pagingType" : "full_numbers",//除首页、上一页、下一页、末页四个按钮还有页数按钮
        "bScrollInfinite":"true",
        "sScrollX":"100%",
        "ajax": {
            "url": "${basePath}/jobConfig/reloadJobList",
            "data": function (d) {
                d.db_name = $("#db_name").val();
                return JSON.stringify(d);
            },
            "dataType": 'json',
            "contentType": 'application/json',
            "type": 'POST'
        },
        "columns": [
            {"data": "id"},
            {"data": "job_name"},
            {"data": "job_src_media_source_name"},
            {"data": "job_target_media_source_name"},
            {"data": "job_media_name"},
            {"data": "timing_yn"},
            {"data": "create_time"}
        ]
    });


    function modify() {
        var id = $("#db_name").val();
        if(id==null || id=="") {
            return;
        }
        if (confirm("确定要替换所有job吗？")) {
            $.ajax({
                type: "post",
                url: "${basePath}/jobConfig/doReoloadJob?id=" + id,
                dataType: "json",
                async: false,
                error: function (xhr, status, err) {
                    alert(err);
                },
                success: function (data) {
                    if (data == "success") {
                        alert("替换成功！");
                        back2Main();
                    } else {
                        alert(data);
                    }
                }
            });
        }

    }


    $("#db_type").change(function () {
        var type_name = $('#db_type').val();
        if(type_name==null || type_name=="") {
            $('#db_name').innerHTML = "";
            $('#db_name').html('');
            $(".db_name").val('').select2({allowClear: false, maximumSelectionLength: 1, width: '50%'});

            jobModifyListTable.fnClearTable(); //清空一下table
            jobModifyListTable.fnDestroy(); //还原初始化了的datatable
            jobModifyListTable = null;
            return;
        }
        $.ajax({
            type: "post",
            url: "${basePath}/jobConfig/dbTypeChange?name="+type_name,
            async: true,
            dataType: "json",
            success: function (result) {
                if (result != null && result != '') {
                    var value = "";
                    //value += all;
                    for(i=0;i<result.num.length;i++) {
                        var option = "<option value=" +"'"+ result.num[i] +"'>"+ result.val[i] +"</option>";
                        value += option;
                    }
                    document.getElementById("db_name").innerHTML = value;
                }
                else {
                    alert(result);
                }
            }
        });
    })


    $("#db_name").change(function () {
        var name_id = $('#db_name').val();
        if(name_id==null || name_id=="") {
            jobModifyListTable.fnClearTable(); //清空一下table
            jobModifyListTable.fnDestroy(); //还原初始化了的datatable
            jobModifyListTable = null;
            return;
        }
        jobModifyListTable.ajax.reload();
    })





</script>