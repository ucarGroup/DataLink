<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div id="roleAuthority" class="col-sm-12">
    <div class="row">
        <div class="col-xs-6">
            <div class="col-xs-12">
                <div style="width: 750px">
                    <table id="menuTable" class="table table-striped table-bordered table-hover">
                        <%--<thead>--%>
                        <%--<tr>--%>
                            <%--<th>选择</th>--%>
                            <%--<th>编码</th>--%>
                            <%--<th>名称</th>--%>
                            <%--<th>菜单类型</th>--%>
                        <%--</tr>--%>
                        <%--</thead>--%>
                    </table>
                </div>
            </div>
        </div>
    </div>

</div>

<div class="clearfix form-actions">
    <div class="col-md-offset-5 col-md-7">
        <button class="btn btn-info" type="button" onclick="doUpdate()">
            <i class="ace-icon fa fa-check bigger-110"></i>
            保存
        </button>
        &nbsp; &nbsp; &nbsp;
        <button class="btn" type="reset" onclick="refresh()">
            返回
            <i class="ace-icon fa fa-undo bigger-110"></i>
        </button>
    </div>
</div>

<script type="text/javascript">
    var menuListMyTable;
    $(".chosen-select").chosen();
    var roleId = $("#id").val();
    menuListMyTable = $('#menuTable').DataTable({
        "paging": false,
        "bAutoWidth":false,
        "sScrollY": "500",
        "ajax": {
            "url": "${basePath}/role/initAuthority?roleId=" + roleId,
            "data": {},
            "type": "post"
        },
        "columns": [
            {
                "data": "id",
                "bSortable": false,
                "sTitle":"选择",
                "fnCreatedCell": function (nTd, sData, oData, iRow, iCol) {
                    var isChecked = oData.checkFlag == 1 ? "checked" : "";

                    $(nTd).html("<div class='checkbox'>" +
                    "<label>" +
                    "<input name='menuIds' id='menuIds' class='ids' type='checkbox' value='" + sData + "' onclick='verify(\"" + oData.id + "\",\"" + oData.checkFlag + "\")' " + isChecked + "/>" +
                    "</label>" +
                    "</div>");
                    if (oData.checkFlag) {
                        $(nTd).find("#menuIds").prop("checked", true);
                    }
                }
            },
            {"data": "code", "sWidth" :150,"sTitle":"编码"},
            {"data": "name", "sWidth" :300 ,"sTitle":"名称"},
            {"data": "type", "sWidth" :200 ,"sTitle":"菜单类型"}

        ]
    });
    function verify(id, check) {
        if (check == "true") {
            var checkbox = $(this).find('input');
            checkbox.prop("checked", false);
        } else if (check == "false") {
            $(this).addClass('selected');
            var checkbox = $(this).find('input');
            checkbox.prop("checked", true);
        }
    }

    function doUpdate() {
        var param = [];
        obj = document.getElementsByName("menuIds");
        for (k in obj) {
            if (obj[k].checked)
                param.push(obj[k].value);
        }
        var object = {roleId: roleId, menuIds: param};
        $.ajax({
            type: "post",
            url: "${basePath}/role/doEditAuthority",
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            data: JSON.stringify(object),
            async: false,
            error: function (xhr, status, err) {
                alert(err);
            },
            success: function (data) {
                if (data == "success") {
                    alert("修改成功！");
                    refresh();
                } else {
                    alert(data);
                }
            }
        });
    }
    function refresh() {
        $("#edit").hide();
        $("#mainContentInner").show();
        msgAlarmListMyTable.ajax.reload();
    }
</script>