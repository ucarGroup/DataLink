<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-container ace-save-state" id="main-container">
    <div class="main-content">
        <div class="main-content-inner">
            <div class="page-content">
                <div class="col-xs-12">
                    <form class="form-horizontal">
                        <div class="col-xs-12">
                            <div class="form-group col-xs-4">
                                <label class="col-sm-3 control-label">所在数据源</label>

                                <div class="col-sm-9">
                                    <select class="width-20 chosen-select" id="mediaSourceId"
                                            style="width:100%">
                                        <c:forEach items="${mediaSourceList}" var="item">
                                            <option value="${item.id}">${item.name}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                            <div class="col-xs-2" id="OperPanel">

                            </div>
                        </div>
                        <div class="col-xs-12">
                            <div id="accordion" class="accordion-style1 panel-group">
                                <div class="panel panel-default">
                                    <div class="panel-heading">
                                        <h4 class="panel-title">
                                            <a class="accordion-toggle collapsed" data-toggle="collapse"
                                               data-parent="#accordion" href="#collapseSix">
                                                <i class="ace-icon fa fa-angle-right bigger-110"
                                                   data-icon-hide="ace-icon fa fa-angle-down"
                                                   data-icon-show="ace-icon fa fa-angle-right"></i>
                                                Sql脚本
                                            </a>
                                        </h4>
                                    </div>

                                    <div class="panel-collapse collapse in" id="collapseSix">
                                        <div class="panel-body">
                                        <textarea name="sqlForCheck" id="sqlForCheck" class="col-xs-12"
                                                  rows="20"/></textarea>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </form>

                    <div class="col-xs-12">
                        <span id="span1"> </span>

                        <p>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="${basePath}/assets/echarts2/echarts.js"></script>
<script type="text/javascript">
    $(document).ready(function () {
        $('.chosen-select').chosen({allow_single_deselect: true, width: "100%"});
    });

    getButtons([{
            code:"003002001",
            html:'<div>'+
            '<p> <button type="button" id="checkBtn" class="btn btn-sm btn-purple">检测</button> </p>'+
            '</div>'
    }],$("#OperPanel"));

    require.config({
        paths: {
            'echarts': '${basePath}/assets/echarts2',
            'jquery': '${basePath}/assets/js/jquery-1.11.3.min'
        }
    });

    require(
            [
                'jquery',
                'echarts',
                'echarts/chart/chord',
                'echarts/chart/tree',
                'echarts/chart/force'
            ],

            function ($, ec) {
                var button = document.getElementById('checkBtn');
                if(button != null) {
                    button.onclick = function (obj) {
                        check();
                    };
                }

                function check() {
                    if ($.trim($('#sqlForCheck').val()) == '') {
                        alert('检测脚本为必输项!');
                        return false;
                    }

                    //将上次的检测结果清空
                    var divs = document.getElementById("span1").childNodes;
                    var length = divs.length;
                    for (var i = 0; i < length; i++) {
                        document.getElementById("span1").removeChild(
                                divs[0]);
                    }

                    //构造查询对象
                    var obj = {};
                    obj.mediaSourceId = $("#mediaSourceId").val();
                    obj.sqls = $("#sqlForCheck").val();

                    $.ajax({
                        type: "post",
                        url: "${basePath}/sync/relation/checkSql",
                        contentType: "application/json; charset=utf-8",
                        dataType: "json",
                        data: JSON.stringify(obj),
                        async: false,
                        error: function (xhr, status, err) {
                            alert(err);
                        },
                        success: function (data) {
                            if (data.length > 0) {
                                showTree(data);
                            } else {
                                showMsg("不影响数据同步.");
                            }
                        }
                    });
                }

                function showMsg(msg) {
                    var mydiv = document.createElement("div");
                    mydiv.setAttribute("id", "itemdiv");
                    mydiv.setAttribute("name", "itemdiv");
                    mydiv.innerHTML = "<p style=\"text-indent:0;font-size:15px;\">"
                            + msg + "</p>";
                    mydiv.style.width = "1200px";
                    mydiv.style.height = "500px";
                    mydiv.style.textAlign = "center";
                    mydiv.style.border = "1px solid lightgray";
                    document.getElementById("span1").appendChild(mydiv);
                }

                function showTree(data) {
                    for (var i = 0; i < data.length; i++) {
                        var treeView = data[i];
                        var option = buildOption();
                        option.series[0].data = getSeriedata(treeView);

                        //Title
                        var title = '';
                        title += "<p style=\"text-indent:0;font-size:16px;line-height:16px\">"
                                + "【SQL】- " + treeView.sqlString + "</p><br/>";
                        title += "<p style=\"text-indent:0;font-size:16px;line-height:16px\">"
                                + "【Table】- " + treeView.tableName + "</p><br/>";

                        for (var j = 0; j < treeView.sqlCheckNotes.length; j++) {
                            var note = treeView.sqlCheckNotes[j];
                            var str = getLevelDesc(note.noteLevel) + getRoleDesc(note.roleType) + note.desc;

                            if (note.noteLevel == 'ERROR') {
                                title += "<p style=\"text-indent:0;color:red;font-size:16px;line-height:16px\">"
                                        + str + "</p><br/>";
                            } else {
                                if (note.noteLevel == 'WARN') {
                                    title += "<p style=\"text-indent:0;color:#FF9900;font-size:16px;line-height:16px\">"
                                            + str + "</p><br/>";
                                } else {
                                    title += "<p style=\"text-indent:0;font-size:16px;line-height:16px\">"
                                            + str + "</p><br/>";
                                }
                            }
                        }
                        title += "<p style=\"text-indent:0;font-size:16px;line-height:16px\">"
                                + "【层级】-" + "</p><br/>";
                        for (var x = 0; x < treeView.hierarchy.length; x++) {
                            var str = treeView.hierarchy[x];
                            title += "<p style=\"text-indent:20px;font-size:16px;line-height:16px\">"
                                    + str + "</p><br/>";
                        }

                        //标题
                        var mydivTitle = document.createElement("div");
                        mydivTitle.setAttribute("id", "divTitle" + i);
                        mydivTitle.setAttribute("name", "divTitle" + i);
                        mydivTitle.innerHTML = title;
                        document.getElementById("span1").appendChild(
                                mydivTitle);

                        //同步关系树
                        var mydivChart = document.createElement("div");
                        mydivChart.setAttribute("id", "divChart" + i);
                        mydivChart.setAttribute("name", "divChart" + i);
                        mydivChart.style.width = "1200px";
                        mydivChart.style.height = "500px";
                        mydivChart.style.border = "1px solid lightgray";
                        document.getElementById("span1").appendChild(mydivChart);
                        var myChart = ec.init(mydivChart);
                        myChart.setOption(option, true);
                    }
                }

                function getLevelDesc(key) {
                    if (key == 'ERROR') {
                        return '【错误】- ';
                    } else if (key == 'WARN') {
                        return '【警告】- ';
                    } else if (key == 'INFO') {
                        return '【通知】- ';
                    }
                }

                function getRoleDesc(key) {
                    if (key == 'ALL') {
                        return '【所有管理员】- ';
                    } else if (key == 'DBA') {
                        return '【DBA】- ';
                    } else if (key == 'ESA') {
                        return '【ES管理员】- ';
                    } else if (key == 'DLA') {
                        return '【DATALINK管理员】- ';
                    } else if (key == 'SPARKA') {
                        return '【SPARK管理员】- ';
                    }
                }

                function buildOption() {
                    option = {
                        title: {
                            text: ''
                        },
                        series: [{
                            name: '检测树',
                            type: 'tree',
                            orient: 'horizontal',
                            rootLocation: {x: 100, y: 250},
                            nodePadding: 20,
                            layerPadding: 340,
                            roam: true,
                            symbolSize: 17,
                            itemStyle: {
                                normal: {
                                    color: '#4883b4',
                                    label: {
                                        show: true,
                                        position: 'right',
                                        formatter: "{b}",
                                        textStyle: {
                                            color: '#000',
                                            fontSize: 14
                                        }
                                    },
                                    lineStyle: {
                                        color: '#ccc',
                                        type: 'curve' // 'curve'|'broken'|'solid'|'dotted'|'dashed'

                                    }
                                },
                                emphasis: {
                                    color: '#4883b4',
                                    label: {
                                        show: true
                                    },
                                    borderWidth: 8
                                }
                            },
                            data: []
                        }]
                    };
                    return option;
                }

                function getSeriedata(obj) {
                    var arr = [];
                    arr.push(obj.rootNode);
                    return arr;
                }

                var BUILTIN_OBJECT = {
                    '[object Function]': 1,
                    '[object RegExp]': 1,
                    '[object Date]': 1,
                    '[object Error]': 1,
                    '[object CanvasGradient]': 1
                };

                var objToString = Object.prototype.toString;

                function isDom(obj) {
                    return obj && obj.nodeType === 1
                            && typeof (obj.nodeName) == 'string';
                }

                function clone(source) {
                    if (typeof source == 'object' && source !== null) {
                        var result = source;
                        if (source instanceof Array) {
                            result = [];
                            for (var i = 0, len = source.length; i < len; i++) {
                                result[i] = clone(source[i]);
                            }
                        } else if (!BUILTIN_OBJECT[objToString.call(source)]
                                    // 是否为 dom 对象
                                && !isDom(source)) {
                            result = {};
                            for (var key in source) {
                                if (source.hasOwnProperty(key)) {
                                    result[key] = clone(source[key]);
                                }
                            }
                        }
                        return result;
                    }
                    return source;
                }
            }
    );
</script>
