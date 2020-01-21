<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div id="div-restart" class="modal-dialog">
    <div class="modal-content">
        <div id="modal-wizard-container_update">
            <div class="modal-header">

                <div class="modal-header no-padding">
                    <div class="table-header">
                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">
                            <span class="white">&times;</span>
                        </button>

                        数据库列表
                    </div>
                </div>
            </div>

            <div class="modal-body">
                                    <table id="mediaSourceTable" class="table table-striped table-bordered table-hover"
                                           style="text-align: center;width:100%">
                                        <thead>
                                        <tr>
                                            <td>单选</td>
                                            <td>业务线</td>
                                            <td>数据库名</td>
                                        </tr>
                                        </thead>
                                    </table>
                                </div>

        </div>

        <div class="modal-footer wizard-actions">
            <button class="btn btn-success" type="button" onclick="confimDB()">
                <i class="ace-icon fa fa-save"></i>
                确定
            </button>

            <button class="btn btn-danger" type="button" data-dismiss="modal">
                取消
                <i class="ace-icon fa fa-times"></i>
            </button>
        </div>
    </div>
</div>

<script type="text/javascript">

$(document).ready(function(){
    for(var i=0;i<databaseInfo.length;i++){
      var html = "<tr>"+
                  "<td><input type='radio' name='id' value='"+databaseInfo[i].id+"' /></td>"+
                  "<td>"+databaseInfo[i].product_name+"</td>"+
                  "<td>"+databaseInfo[i].db_name+"</td>"+
                  "</tr>";
      $("#mediaSourceTable").append(html);
    }
});


function confimDB() {
 for(var i=0;i<databaseInfo.length;i++){
    if(databaseInfo[i].id == $("input[type='radio']:checked").val()) {
       $("#dbInfo").val(JSON.stringify(databaseInfo[i]));
       $(".modal-header button").click();
       return;
    }
 }
 $(".modal-header button").click();
}

</script>