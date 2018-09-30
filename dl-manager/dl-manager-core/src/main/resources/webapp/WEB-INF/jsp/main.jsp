<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>
<style>
    .list_lh {
        height: 160px;
        overflow: hidden;
    }

    ul, li, dl, ol {
        list-style: none;
    }

    .table-notice > tbody > tr > td, .table-notice > tfoot > tr > td {
        text-align: left;
        border: none;
        height: 80px;
        vertical-align: middle;
        overflow: hidden;
    }

    a:hover, a:focus {
        color: #2a6496;
        text-decoration: underline;
    }
</style>
<div class="main-container" id="mainContentInner">
    <div class="page-content">

        <table id="statisticTable" class="table table-striped table-bordered table-hover"
               style="text-align: left;width:100%">
            <thead>
            <tr>
                <td>分组总数</td>
                <td>机器总数</td>
                <td>数据源总数</td>
                <td>任务总数</td>
                <td>映射总数</td>
            </tr>
            </thead>
        </table>

        <div class="space"></div>

        <div class="row">

            <form class="form-horizontal">
                <div class="form-group col-xs-3">
                    <label class="col-sm-2 control-label">分组</label>

                    <div class="col-sm-8">
                        <select class="width-100 chosen-select" id="groupId"
                                style="width:100%">
                            <option value="-1">全部</option>
                            <c:forEach items="${groupList}" var="item">
                                <option value="${item.id}">${item.groupName}</option>
                            </c:forEach>
                        </select>
                    </div>
                </div>
                <div class="col-xs-1">
                    <button type="button" onclick="doSearch()" class="btn btn-sm btn-purple">查询</button>
                </div>
            </form>

            <div class="col-md-3"></div>

            <div class="col-md-4">
                <div class="form-group">
                    <label class="col-sm-12 control-label no-padding-right" id="allManagers"
                           for="allManagers" style="font-size:15px;">Managers : ${allManagers}</label>
                </div>
                <div class="form-group">
                    <label class="col-sm-12 control-label no-padding-right" id="activeManager"
                           for="activeManager" style="font-size:15px;">Active : ${activeManager} (startTime:${startTime})</label>
                </div>
            </div>

        </div>

        <div class="space"></div>

        <div class="row">

            <div class="col-md-6">

                <div style="padding-left: 10px;">
                    <div id="taskSizeStatistic" style="width: 720px;height: 350px;"></div>
                </div>

            </div>

            <div class="col-md-6">
                <div style="padding-left: 10px;">
                    <div id="taskRecordStatistic" style="width: 720px;height: 350px;"></div>
                </div>
            </div>


        </div>

        <div class="row">

            <div class="col-md-6">
                <div style="padding-left: 10px;">
                    <div id="taskDelayStatistic" style="width: 720px;height: 350px;"></div>
                </div>
            </div>
            <div class="col-md-6">
                <div style="padding-left: 10px;">
                    <div id="workerJvmUsedStatistic" style="width: 720px;height: 350px;"></div>
                </div>

            </div>
        </div>

        <div class="row">

            <div class="col-md-6">
                <div style="padding-left: 10px;">
                    <div id="workerNetTrafficStatistic" style="width: 720px;height: 350px;"></div>
                </div>
            </div>
            <div class="col-md-6">
                <div style="padding-left: 10px;">
                    <div id="workerYoungGCCountStatistic" style="width: 720px;height: 350px;"></div>
                </div>

            </div>
        </div>

    </div>
</div>
<script src="${basePath}/assets/echarts3/echarts.js"></script>
<script type="text/javascript">
    $('.chosen-select').chosen({allow_single_deselect: true, width: "100%"});

    $('#statisticTable').DataTable({
        "dom": '<"top"f >rt<"bottom"ilp><"clear">',//dom定位
        "bAutoWidth": true,
        "bPaginate": false, //翻页功能
        "bLengthChange": false, //改变每页显示数据数量
        "bFilter": false, //过滤功能
        "bSort": false, //排序功能
        "bInfo": false,//页脚信息
        "ajax": {
            "url": "${basePath}/home/count",
            "data": {}
        },
        "columns": [
            {"data": "groupCount"},
            {"data": "workerCount"},
            {"data": "msCount"},
            {"data": "taskCount"},
            {"data": "mappingCount"}
        ]

    });


    var myChart1, myChart2, myChart3, myChart4, myChart5, myChart6;

    function doSearch() {
        var groupId = $('#groupId').val();
        $.ajax({
            type: "post",
            url: "${basePath}/home/statis",
            dataType: "json",
            data: {groupId: groupId},
            async: true,
            success: function (data) {
                showStatistic(data);
            }
        });
    }

    $("#groupId").change(function () {
        doSearch();
    });

    $.ajax({
        type: "post",
        url: "${basePath}/home/statis",
        dataType: "json",
        data: {groupId: $('#groupId').val()},
        async: true,
        success: function (data) {
            showStatistic(data);
        }
    });
    function showStatistic(data) {
        var taskNameList = data.taskNameList;//dataAxis必须是String类型的数组
        var taskSizeList = data.taskSizeList;
        var taskRecordsList = data.taskRecordsList;
        var taskNameListDelay = data.taskNameListDelay;
        var taskDelayTimeList = data.taskDelayTimeList;
        var workerNameList = data.workerNameList;
        var workerJvmUsedList = data.workerJvmUsedList;
        var workerNameListNet = data.workerNameListNet;
        var workerNetTrafficList = data.workerNetTrafficList;
        var workerNameListGC = data.workerNameListGC;
        var workerYoungGCCountList = data.workerYoungGCCountList;

        option1 = {
            title: {
                x: 'center',
                text: 'Task同步流量排行(1小时)'
            },
            tooltip: {
                show: true,
                trigger: 'axis'//tooltip触发方式:axis以X轴线触发,item以每一个数据项触发
            },
            toolbox: {
                show: true,
                feature: {
                    mark: {show: true},
                    dataView: {show: true, readOnly: false},
                    magicType: {show: true, type: ['line', 'bar']},
                    restore: {show: true},
                    saveAsImage: {show: true}
                }
            },
            xAxis: {
                name: 'Task',
                data: taskNameList,
                axisTick: {
                    show: false
                },
                axisLine: {
                    show: true,
                    lineStyle: {
                        type: 'solid',
                        color: '#999',
                        width: '1'
                    }
                },
                axisLabel: {
                    textStyle: {
                        color: '#999'
                    }
                },
                z: 10
            },
            yAxis: {
                name: 'byte',
                axisLine: {
                    show: true,
                    lineStyle: {
                        type: 'solid',
                        color: '#999',
                        width: '1'
                    }
                },
                axisTick: {
                    show: false
                },
                axisLabel: {
                    textStyle: {
                        color: '#999'
                    }
                }
            },
            dataZoom: [
                {
                    type: 'inside'
                }
            ],
            series: [
                {
                    name: '平均流量',
                    type: 'bar',
                    symbol: 'none',
                    itemStyle: {
                        normal: {
                            color: new echarts.graphic.LinearGradient(
                                    0, 0, 0, 1,
                                    [
                                        {offset: 0, color: '#83bff6'},
                                        {offset: 0.5, color: '#188df0'},
                                        {offset: 1, color: '#188df0'}
                                    ]
                            )
                        },
                        emphasis: {
                            color: new echarts.graphic.LinearGradient(
                                    0, 0, 0, 1,
                                    [
                                        {offset: 0, color: '#2378f7'},
                                        {offset: 0.7, color: '#2378f7'},
                                        {offset: 1, color: '#83bff6'}
                                    ]
                            )
                        }
                    },
                    data: taskSizeList
                }
            ]
        };

        if (myChart1 != null && myChart1 != "" && myChart1 != undefined) {
            myChart1.dispose();
        }
        myChart1 = echarts.init(document.getElementById('taskSizeStatistic'));

        myChart1.setOption(option1, true);
        // Enable data zoom when user click bar.
        var zoomSize = 6;
        myChart1.on('click', function (params) {
            console.log(taskNameList[Math.max(params.dataIndex - zoomSize / 2, 0)]);
            console.log(taskNameList[Math.min(params.dataIndex + zoomSize / 2, taskNameList.length - 1)]);
            myChart1.dispatchAction({
                type: 'dataZoom',
                startValue: taskNameList[Math.max(params.dataIndex - zoomSize / 2, 0)],
                endValue: taskNameList[Math.min(params.dataIndex + zoomSize / 2, taskNameList.length - 1)]
            });
        });


        option2 = {
            title: {
                x: 'center',
                text: 'Task同步记录排行(1小时)'
            },
            tooltip: {
                show: true,
                trigger: 'axis'
            },
            toolbox: {
                show: true,
                feature: {
                    mark: {show: true},
                    dataView: {show: true, readOnly: false},
                    magicType: {show: true, type: ['line', 'bar']},
                    restore: {show: true},
                    saveAsImage: {show: true}
                }
            },
            xAxis: {
                name: 'Task',
                data: taskNameList,
                axisTick: {
                    show: false
                },
                axisLine: {
                    show: true,
                    lineStyle: {
                        type: 'solid',
                        color: '#999',
                        width: '1'
                    }
                },
                axisLabel: {
                    textStyle: {
                        color: '#999'
                    }
                },
                z: 10
            },
            yAxis: {
                name: '条',
                axisLine: {
                    show: true,
                    lineStyle: {
                        type: 'solid',
                        color: '#999',
                        width: '1'
                    }
                },
                axisTick: {
                    show: false
                },
                axisLabel: {
                    textStyle: {
                        color: '#999'
                    }
                }
            },
            dataZoom: [
                {
                    type: 'inside'
                }
            ],
            series: [
                {
                    name: '平均条数',
                    type: 'bar',
                    symbol: 'none',
                    itemStyle: {
                        normal: {
                            color: new echarts.graphic.LinearGradient(
                                    0, 0, 0, 1,
                                    [
                                        {offset: 0, color: '#83bff6'},
                                        {offset: 0.5, color: '#188df0'},
                                        {offset: 1, color: '#188df0'}
                                    ]
                            )
                        },
                        emphasis: {
                            color: new echarts.graphic.LinearGradient(
                                    0, 0, 0, 1,
                                    [
                                        {offset: 0, color: '#2378f7'},
                                        {offset: 0.7, color: '#2378f7'},
                                        {offset: 1, color: '#83bff6'}
                                    ]
                            )
                        }
                    },
                    data: taskRecordsList
                }
            ]
        };
        if (myChart2 != null && myChart2 != "" && myChart2 != undefined) {
            myChart2.dispose();
        }
        myChart2 = echarts.init(document.getElementById('taskRecordStatistic'));
        myChart2.setOption(option2, true);
        // Enable data zoom when user click bar.
//        var zoomSize = 6;
        myChart2.on('click', function (params) {
            console.log(taskNameList[Math.max(params.dataIndex - zoomSize / 2, 0)]);
            console.log(taskNameList[Math.min(params.dataIndex + zoomSize / 2, taskNameList.length - 1)]);
            myChart2.dispatchAction({
                type: 'dataZoom',
                startValue: taskNameList[Math.max(params.dataIndex - zoomSize / 2, 0)],
                endValue: taskNameList[Math.min(params.dataIndex + zoomSize / 2, taskNameList.length - 1)]
            });
        });

        option3 = {
            title: {
                x: 'center',
                text: 'Task延迟时间排行(1小时)'
            },
            tooltip: {
                show: true,
                trigger: 'axis'
            },
            toolbox: {
                show: true,
                feature: {
                    mark: {show: true},
                    dataView: {show: true, readOnly: false},
                    magicType: {show: true, type: ['line', 'bar']},
                    restore: {show: true},
                    saveAsImage: {show: true}
                }
            },
            xAxis: {
                name: 'Task',
                data: taskNameListDelay,
                axisTick: {
                    show: false
                },
                axisLine: {
                    show: true,
                    lineStyle: {
                        type: 'solid',
                        color: '#999',
                        width: '1'
                    }
                },
                axisLabel: {
                    textStyle: {
                        color: '#999'
                    }
                },
                z: 10
            },
            yAxis: {
                name: 'ms',
                axisLine: {
                    show: true,
                    lineStyle: {
                        type: 'solid',
                        color: '#999',
                        width: '1'
                    }
                },
                axisTick: {
                    show: false
                },
                axisLabel: {
                    textStyle: {
                        color: '#999'
                    }
                }
            },
            dataZoom: [
                {
                    type: 'inside'
                }
            ],
            series: [
                {
                    name: '延迟时间',
                    type: 'bar',
                    symbol: 'none',
                    itemStyle: {
                        normal: {
                            color: new echarts.graphic.LinearGradient(
                                    0, 0, 0, 1,
                                    [
                                        {offset: 0, color: '#83bff6'},
                                        {offset: 0.5, color: '#188df0'},
                                        {offset: 1, color: '#188df0'}
                                    ]
                            )
                        },
                        emphasis: {
                            color: new echarts.graphic.LinearGradient(
                                    0, 0, 0, 1,
                                    [
                                        {offset: 0, color: '#2378f7'},
                                        {offset: 0.7, color: '#2378f7'},
                                        {offset: 1, color: '#83bff6'}
                                    ]
                            )
                        }
                    },
                    data: taskDelayTimeList
                }
            ]
        };

        if (myChart3 != null && myChart3 != "" && myChart3 != undefined) {
            myChart3.dispose();
        }
        myChart3 = echarts.init(document.getElementById('taskDelayStatistic'));
        myChart3.setOption(option3, true);
        // Enable data zoom when user click bar.
//        var zoomSize = 6;
        myChart3.on('click', function (params) {
            console.log(taskNameListDelay[Math.max(params.dataIndex - zoomSize / 2, 0)]);
            console.log(taskNameListDelay[Math.min(params.dataIndex + zoomSize / 2, taskNameListDelay.length - 1)]);
            myChart3.dispatchAction({
                type: 'dataZoom',
                startValue: taskNameListDelay[Math.max(params.dataIndex - zoomSize / 2, 0)],
                endValue: taskNameListDelay[Math.min(params.dataIndex + zoomSize / 2, taskNameListDelay.length - 1)]
            });
        });

        option4 = {
            title: {
                x: 'center',
                text: 'Worker内存使用率排行(1小时)'
            },
            tooltip: {
                show: true,
                trigger: 'axis'
            },
            toolbox: {
                show: true,
                feature: {
                    mark: {show: true},
                    dataView: {show: true, readOnly: false},
                    magicType: {show: true, type: ['line', 'bar']},
                    restore: {show: true},
                    saveAsImage: {show: true}
                }
            },
            xAxis: {
                name: 'Worker',
                data: workerNameList,
                axisTick: {
                    show: false
                },
                axisLine: {
                    show: true,
                    lineStyle: {
                        type: 'solid',
                        color: '#999',
                        width: '1'
                    }
                },
                axisLabel: {
                    textStyle: {
                        color: '#999'
                    }
                },
                z: 10
            },
            yAxis: {
                name: '%',
                axisLine: {
                    show: true,
                    lineStyle: {
                        type: 'solid',
                        color: '#999',
                        width: '1'
                    }
                },
                axisTick: {
                    show: false
                },
                axisLabel: {
                    textStyle: {
                        color: '#999'
                    }
                }
            },
            dataZoom: [
                {
                    type: 'inside'
                }
            ],
            series: [
                {
                    name: 'JVM使用率',
                    type: 'bar',
                    symbol: 'none',
                    itemStyle: {
                        normal: {
                            color: new echarts.graphic.LinearGradient(
                                    0, 0, 0, 1,
                                    [
                                        {offset: 0, color: '#83bff6'},
                                        {offset: 0.5, color: '#188df0'},
                                        {offset: 1, color: '#188df0'}
                                    ]
                            )
                        },
                        emphasis: {
                            color: new echarts.graphic.LinearGradient(
                                    0, 0, 0, 1,
                                    [
                                        {offset: 0, color: '#2378f7'},
                                        {offset: 0.7, color: '#2378f7'},
                                        {offset: 1, color: '#83bff6'}
                                    ]
                            )
                        }
                    },
                    data: workerJvmUsedList
                }
            ]
        };

        if (myChart4 != null && myChart4 != "" && myChart4 != undefined) {
            myChart4.dispose();
        }
        myChart4 = echarts.init(document.getElementById('workerJvmUsedStatistic'));
        myChart4.setOption(option4, true);
        // Enable data zoom when user click bar.
//        var zoomSize = 6;
        myChart4.on('click', function (params) {
            console.log(workerNameList[Math.max(params.dataIndex - zoomSize / 2, 0)]);
            console.log(workerNameList[Math.min(params.dataIndex + zoomSize / 2, workerNameList.length - 1)]);
            myChart4.dispatchAction({
                type: 'dataZoom',
                startValue: workerNameList[Math.max(params.dataIndex - zoomSize / 2, 0)],
                endValue: workerNameList[Math.min(params.dataIndex + zoomSize / 2, workerNameList.length - 1)]
            });
        });

        option5 = {
            title: {
                x: 'center',
                text: 'Worker网卡流量排行(1小时)'
            },
            tooltip: {
                show: true,
                trigger: 'axis'
            },
            toolbox: {
                show: true,
                feature: {
                    mark: {show: true},
                    dataView: {show: true, readOnly: false},
                    magicType: {show: true, type: ['line', 'bar']},
                    restore: {show: true},
                    saveAsImage: {show: true}
                }
            },
            xAxis: {
                name: 'Worker',
                data: workerNameListNet,
                axisTick: {
                    show: false
                },
                axisLine: {
                    show: true,
                    lineStyle: {
                        type: 'solid',
                        color: '#999',
                        width: '1'
                    }
                },
                axisLabel: {
                    textStyle: {
                        color: '#999'
                    }
                },
                z: 10
            },
            yAxis: {
                name: 'Mbps',
                axisLine: {
                    show: true,
                    lineStyle: {
                        type: 'solid',
                        color: '#999',
                        width: '1'
                    }
                },
                axisTick: {
                    show: false
                },
                axisLabel: {
                    textStyle: {
                        color: '#999'
                    }
                }
            },
            dataZoom: [
                {
                    type: 'inside'
                }
            ],
            series: [
                {
                    name: '网卡流量',
                    type: 'bar',
                    symbol: 'none',
                    itemStyle: {
                        normal: {
                            color: new echarts.graphic.LinearGradient(
                                    0, 0, 0, 1,
                                    [
                                        {offset: 0, color: '#83bff6'},
                                        {offset: 0.5, color: '#188df0'},
                                        {offset: 1, color: '#188df0'}
                                    ]
                            )
                        },
                        emphasis: {
                            color: new echarts.graphic.LinearGradient(
                                    0, 0, 0, 1,
                                    [
                                        {offset: 0, color: '#2378f7'},
                                        {offset: 0.7, color: '#2378f7'},
                                        {offset: 1, color: '#83bff6'}
                                    ]
                            )
                        }
                    },
                    data: workerNetTrafficList
                }
            ]
        };

        if (myChart5 != null && myChart5 != "" && myChart5 != undefined) {
            myChart5.dispose();
        }
        myChart5 = echarts.init(document.getElementById('workerNetTrafficStatistic'));
        myChart5.setOption(option5, true);
        // Enable data zoom when user click bar.
//        var zoomSize = 6;
        myChart5.on('click', function (params) {
            console.log(workerNameListNet[Math.max(params.dataIndex - zoomSize / 2, 0)]);
            console.log(workerNameListNet[Math.min(params.dataIndex + zoomSize / 2, workerNameListNet.length - 1)]);
            myChart5.dispatchAction({
                type: 'dataZoom',
                startValue: workerNameListNet[Math.max(params.dataIndex - zoomSize / 2, 0)],
                endValue: workerNameListNet[Math.min(params.dataIndex + zoomSize / 2, workerNameListNet.length - 1)]
            });
        });

        option6 = {
            title: {
                x: 'center',
                text: 'YoungGC次数排行(1小时)'
            },
            tooltip: {
                show: true,
                trigger: 'axis'
            },
            toolbox: {
                show: true,
                feature: {
                    mark: {show: true},
                    dataView: {show: true, readOnly: false},
                    magicType: {show: true, type: ['line', 'bar']},
                    restore: {show: true},
                    saveAsImage: {show: true}
                }
            },
            xAxis: {
                name: 'Worker',
                data: workerNameListGC,
                axisTick: {
                    show: false
                },
                axisLine: {
                    show: true,
                    lineStyle: {
                        type: 'solid',
                        color: '#999',
                        width: '1'
                    }
                },
                axisLabel: {
                    textStyle: {
                        color: '#999'
                    }
                },
                z: 10
            },
            yAxis: {
                name: '次/分钟',
                axisLine: {
                    show: true,
                    lineStyle: {
                        type: 'solid',
                        color: '#999',
                        width: '1'
                    }
                },
                axisTick: {
                    show: false
                },
                axisLabel: {
                    textStyle: {
                        color: '#999'
                    }
                }
            },
            dataZoom: [
                {
                    type: 'inside'
                }
            ],
            series: [
                {
                    name: 'YoungGC次数',
                    type: 'bar',
                    symbol: 'none',
                    itemStyle: {
                        normal: {
                            color: new echarts.graphic.LinearGradient(
                                    0, 0, 0, 1,
                                    [
                                        {offset: 0, color: '#83bff6'},
                                        {offset: 0.5, color: '#188df0'},
                                        {offset: 1, color: '#188df0'}
                                    ]
                            )
                        },
                        emphasis: {
                            color: new echarts.graphic.LinearGradient(
                                    0, 0, 0, 1,
                                    [
                                        {offset: 0, color: '#2378f7'},
                                        {offset: 0.7, color: '#2378f7'},
                                        {offset: 1, color: '#83bff6'}
                                    ]
                            )
                        }
                    },
                    data: workerYoungGCCountList
                }
            ]
        };

        if (myChart6 != null && myChart6 != "" && myChart6 != undefined) {
            myChart6.dispose();
        }
        myChart6 = echarts.init(document.getElementById('workerYoungGCCountStatistic'));
        myChart6.setOption(option6, true);
        // Enable data zoom when user click bar.
//        var zoomSize = 6;
        myChart6.on('click', function (params) {
            console.log(workerNameListGC[Math.max(params.dataIndex - zoomSize / 2, 0)]);
            console.log(workerNameListGC[Math.min(params.dataIndex + zoomSize / 2, workerNameListGC.length - 1)]);
            myChart6.dispatchAction({
                type: 'dataZoom',
                startValue: workerNameListGC[Math.max(params.dataIndex - zoomSize / 2, 0)],
                endValue: workerNameListGC[Math.min(params.dataIndex + zoomSize / 2, workerNameListGC.length - 1)]
            });
        });
    }


    /*require.config({
     paths: {
     'echarts': '${basePath}/assets/echarts2',
     'jquery': '${basePath}/assets/js/jquery-1.11.3.min'
     }
     });

     require(
     [
     'jquery',
     'echarts/echarts',
     'echarts/chart/bar'
     ],
     function ($, ec) {
     $.ajax({
     type: "post",
     url: "${basePath}/home/statis",
     dataType: "json",
     data: "",
     async: true,
     success: function (data) {
     showChart(data);
     }
     });

     function showChart(data) {

     var detailArr = new Array(data.groupDetail, data.workerDetail, data.msDetail, data.taskDetail, data.mappingDetail);
     var ecConfig = require('echarts/config');

     function eConsole(param) {
     if (param.dataIndex == 0) {
     var mes = '【' + '分组明细' + '】';
     mes += detailArr[0];
     }
     if (param.dataIndex == 1) {
     var mes = '【' + '机器明细' + '】';
     mes += detailArr[1];
     }
     if (param.dataIndex == 2) {
     var mes = '【' + '数据源明细' + '】';
     mes += detailArr[2];
     }
     if (param.dataIndex == 3) {
     var mes = '【' + '任务明细' + '】';
     mes += detailArr[3];
     }
     if (param.dataIndex == 4) {
     var mes = '【' + '映射明细' + '】';
     mes += detailArr[4];
     }
     if (param.type == 'click') {
     document.getElementById('console').innerHTML = mes;
     }
     console.log(param);
     }

     myChart.on(ecConfig.EVENT.CLICK, eConsole);
     }

     */

    jQuery(function ($) {
        $.fn.myScroll = function (options) {
            //默认配置
            var defaults = {
                speed: 40,  //滚动速度,值越大速度越慢
                rowHeight: 80 //每行的高度
            };

            var opts = $.extend({}, defaults, options), intId = [];

            function marquee(obj, step) {

                obj.find("table").animate({
                    marginTop: '-=1'
                }, 0, function () {
                    var s = Math.abs(parseInt($(this).css("margin-top")));
                    if (s >= step) {
                        $(this).find("tr").slice(0, 1).appendTo($(this));
                        $(this).css("margin-top", 0);
                    }
                });
            }

            this.each(function (i) {
                var sh = opts["rowHeight"], speed = opts["speed"], _this = $(this);
                intId[i] = setInterval(function () {
                    if (_this.find("table").height() <= _this.height()) {
                        clearInterval(intId[i]);
                    } else {
                        marquee(_this, sh);
                    }
                }, speed);

                _this.hover(function () {
                    clearInterval(intId[i]);
                }, function () {
                    intId[i] = setInterval(function () {
                        if (_this.find("table").height() <= _this.height()) {
                            clearInterval(intId[i]);
                        } else {
                            marquee(_this, sh);
                        }
                    }, speed);
                });
            });
        }

        $("div.list_lh").myScroll({
            speed: 40, //数值越大，速度越慢
            rowHeight: 80 //li的高度
        });
    })

</script>