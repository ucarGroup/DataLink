<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-content-inner">
    <div class="page-content">
        <div class="row">
            <form id="task_statistic" class="form-horizontal" role="form">

                <input type="hidden" class="col-sm-12" value="${taskInfo.id}" id="taskIdForStatistic"/>

                <div class="form-group">
                    <label class="col-sm-3 control-label no-padding-right" id="taskName"
                           for="taskName" style="font-size:18px;">任务ID=${taskInfo.id} : ${taskInfo.taskName}</label>
                </div>

                <div class="form-group">
                    <div class="col-md-1"></div>
                    <input class="col-sm-2" type='text' id='startTime' name="startTime"
                           value="${taskStatisticInfo.startTime}"/>

                    <div class="col-sm-1" style="align-content: center">
                        <table align="center">
                            <tr>
                                <td>至</td>
                            </tr>
                        </table>
                    </div>

                    <input class="col-sm-2" type='text' id='endTime' name="endTime"
                           value="${taskStatisticInfo.endTime}"/>
                    &nbsp; &nbsp; &nbsp;
                    <button type="button" id="search" class="btn btn-sm btn-purple" onclick="doSearch()">查询</button>
                </div>

                <div class="form-group">
                    <div class="col-md-10">
                        <div style="padding-left: 20px;">
                            <div id="taskDelayTime" style="width: 1100px;height: 300px;"></div>
                        </div>
                    </div>

                    <div class="col-md-10">
                        <div style="padding-left: 20px;">
                            <div id="timeForPerRecord" style="width: 1100px;height: 300px;"></div>
                        </div>
                    </div>

                    <div class="col-md-10">
                        <div style="padding-left: 20px;">
                            <div id="recordTPS" style="width: 1100px;height: 300px;"></div>
                        </div>
                    </div>

                    <div class="col-md-10">
                        <div style="padding-left: 20px;">
                            <div id="recordSize" style="width: 1100px;height: 300px;"></div>
                        </div>
                    </div>

                    <div class="col-md-10">
                        <div style="padding-left: 20px;">
                            <div id="exceptionsPerMinute" style="width: 1100px;height: 300px;"></div>
                        </div>
                    </div>

                </div>

            </form>
        </div>

        <div class="clearfix form-actions">
            <div class="col-md-offset-5 col-md-7">
                <button class="btn" type="reset" onclick="back2Main();">
                    返回
                    <i class="ace-icon fa fa-undo bigger-110"></i>
                </button>
            </div>
        </div>
    </div>
</div>


<script src="${basePath}/assets/echarts2/echarts.js"></script>
<script type="text/javascript">
    $("#startTime").datetimepicker(
            {
                format: 'YYYY-MM-DD HH:mm:ss'
            }
    );
    $("#endTime").datetimepicker(
            {
                format: 'YYYY-MM-DD HH:mm:ss'
            }
    );

    require.config({
        paths: {
            'echarts': '${basePath}/assets/echarts2'
        }
    });

    require(
            [
                'echarts/echarts',
                'echarts/chart/line',
                'echarts/chart/bar'
            ],
            function (ec) {
                var delayTime = ${delayTimeList};
                var delayCreateTime = ${delayCreateTimeList};
                var recordTPS = ${recordTPSList};
                var writeTimePerRecord = ${writeTimePerRecordList};
                var recordSize = ${recordSizeList};
                var exceptionsPerMinute = ${exceptionsPerMinuteList};
                var readWriteCountPerMinute = ${readWriteCountPerMinuteList};
                var createTime = ${createTimeList};

                var option = buildOption();
                var mydivChart = document.getElementById("taskDelayTime");
                var myChart = ec.init(mydivChart);
                myChart.setOption(option, true);

                var option1 = buildOption1();
                var mydivChart1 = document.getElementById("recordTPS");
                var myChart1 = ec.init(mydivChart1);
                myChart1.setOption(option1, true);

                var option2 = buildOption2();
                var mydivChart2 = document.getElementById("timeForPerRecord");
                var myChart2 = ec.init(mydivChart2);
                myChart2.setOption(option2, true);

                var option3 = buildOption3();
                var mydivChart3 = document.getElementById("recordSize");
                var myChart3 = ec.init(mydivChart3);
                myChart3.setOption(option3, true);

                var option4 = buildOption4();
                var mydivChart4 = document.getElementById("exceptionsPerMinute");
                var myChart4 = ec.init(mydivChart4);
                myChart4.setOption(option4, true);

                function buildOption() {

                    option = {
                        title: {
                            text: '同步任务延迟时间'
                        },
                        tooltip: {
                            trigger: 'axis'//tooltip触发方式:axis以X轴线触发,item以每一个数据项触发
                        },
                        legend: {
                            data: ['delayTime', '单位：毫秒']
                        },
                        grid: {
                            left: '3%',
                            right: '4%',
                            bottom: '3%',
                            x: 100,//表格左边的宽度
                            x2: 100,//表格右边的宽度
                            containLabel: true
                        },
                        dataZoom: {
                            show: true,
                            start: 0
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
                            type: 'category',//X轴均为category，Y轴均为value
                            boundaryGap: false,//数值轴两端的空白策略
                            data: delayCreateTime
                        },
                        yAxis: {
                            type: 'value'
                        },
                        series: [
                            {
                                name: 'delayTime',
                                type: 'line',
//                                stack: '总量',//折线图堆叠的重要参数stack。只要将stack的值设置不相同，就不会堆叠了
                                symbol: 'none',
                                data: delayTime
                            }
                        ]
                    };

                    return option;
                }

                function buildOption1() {

                    option1 = {
                        title: {
                            text: '每分钟同步条数'
                        },
                        tooltip: {
                            trigger: 'axis'
                        },
                        legend: {
                            data: ['recordsPerMinute', 'readWriteCount', '单位：条/分钟;次/分钟']
                        },
                        grid: {
                            left: '3%',
                            right: '4%',
                            bottom: '3%',
                            x: 100,
                            x2: 100,
                            containLabel: true
                        },
                        dataZoom: {
                            show: true,
                            start: 0
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
                            type: 'category',
                            boundaryGap: false,
                            data: createTime
                        },
                        yAxis: {
                            type: 'value'
                        },
                        series: [
                            {
                                name: 'recordsPerMinute',
                                type: 'line',
                                symbol: 'none',
                                data: recordTPS
                            },
                            {
                                name: 'readWriteCount',
                                type: 'line',
                                symbol: 'none',
                                data: readWriteCountPerMinute
                            }
                        ]
                    };

                    return option1;
                }

                function buildOption2() {

                    option2 = {
                        title: {
                            text: '每条记录写入耗时'
                        },
                        tooltip: {
                            trigger: 'axis'
                        },
                        legend: {
                            data: ['writeTime', '单位：毫秒']
                        },
                        grid: {
                            left: '3%',
                            right: '4%',
                            bottom: '3%',
                            x: 100,
                            x2: 100,
                            containLabel: true
                        },
                        dataZoom: {
                            show: true,
                            start: 0
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
                            type: 'category',
                            boundaryGap: false,
                            data: createTime
                        },
                        yAxis: {
                            type: 'value'
                        },
                        series: [
                            {
                                name: 'writeTime',
                                type: 'line',
                                symbol: 'none',
                                data: writeTimePerRecord
                            }
                        ]
                    };

                    return option2;
                }

                function buildOption3() {

                    option3 = {
                        title: {
                            text: '每分钟同步流量'
                        },
                        tooltip: {
                            trigger: 'axis'
                        },
                        legend: {
                            data: ['recordSize', 'readWriteCount', '单位：字节/分钟;次/分钟']
                        },
                        grid: {
                            left: '3%',
                            right: '4%',
                            bottom: '3%',
                            x: 100,
                            x2: 100,
                            containLabel: true
                        },
                        dataZoom: {
                            show: true,
                            start: 0
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
                            type: 'category',
                            boundaryGap: false,
                            data: createTime
                        },
                        yAxis: {
                            type: 'value'
                        },
                        series: [
                            {
                                name: 'recordSize',
                                type: 'line',
                                symbol: 'none',
                                data: recordSize
                            },
                            {
                                name: 'readWriteCount',
                                type: 'line',
                                symbol: 'none',
                                data: readWriteCountPerMinute
                            }
                        ]
                    };

                    return option3;
                }

                function buildOption4() {

                    option4 = {
                        title: {
                            text: '每分钟异常个数'
                        },
                        tooltip: {
                            trigger: 'axis'
                        },
                        legend: {
                            data: ['exceptionsPerMinute', '单位：个/分钟']
                        },
                        grid: {
                            left: '3%',
                            right: '4%',
                            bottom: '3%',
                            x: 100,
                            x2: 100,
                            containLabel: true
                        },
                        dataZoom: {
                            show: true,
                            start: 0
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
                            type: 'category',
                            boundaryGap: false,
                            data: createTime
                        },
                        yAxis: {
                            type: 'value'
                        },
                        series: [
                            {
                                name: 'exceptionsPerMinute',
                                type: 'line',
                                symbol: 'none',
                                data: exceptionsPerMinute
                            }
                        ]
                    };

                    return option4;
                }
            }
    );


    function doSearch() {
        var start = $("#startTime").val();
        var end = $("#endTime").val();

        var obj = {
            taskId: $("#taskIdForStatistic").val(),
            startTime: Date.parse(new Date(start)),
            endTime: Date.parse(new Date(end))
        };

        $.ajax({
            type: "POST",
            url: "${basePath}/taskMonitor/doSearchTaskStatistic",
            contentType: "application/json",
            dataType: "json",
            data: JSON.stringify(obj),
            async: false,
            error: function (xhr, status, err) {
                alert(err);
            },
            success: function (data) {
                if (data.failMessage != null) {
                    alert(data.failMessage);
                } else {

                    require(
                            [
                                'echarts/echarts',
                                'echarts/chart/line',
                                'echarts/chart/bar'
                            ],
                            function (ec) {
                                var option = buildOption(data);
                                var mydivChart = document.getElementById("taskDelayTime");
                                var myChart = ec.init(mydivChart);
                                myChart.setOption(option, true);

                                var option1 = buildOption1(data);
                                var mydivChart1 = document.getElementById("recordTPS");
                                var myChart1 = ec.init(mydivChart1);
                                myChart1.setOption(option1, true);

                                var option2 = buildOption2(data);
                                var mydivChart2 = document.getElementById("timeForPerRecord");
                                var myChart2 = ec.init(mydivChart2);
                                myChart2.setOption(option2, true);

                                var option3 = buildOption3(data);
                                var mydivChart3 = document.getElementById("recordSize");
                                var myChart3 = ec.init(mydivChart3);
                                myChart3.setOption(option3, true);

                                var option4 = buildOption4(data);
                                var mydivChart4 = document.getElementById("exceptionsPerMinute");
                                var myChart4 = ec.init(mydivChart4);
                                myChart4.setOption(option4, true);
                            }
                    );
                }
            }
        });

    }

    function buildOption(data) {
        var delayTime = data.delayTimeList;
        var delayCreateTime = data.delayCreateTimeList;

        option = {
            title: {
                text: '同步任务延迟时间'
            },
            tooltip: {
                trigger: 'axis'//tooltip触发方式:axis以X轴线触发,item以每一个数据项触发
            },
            legend: {
                data: ['delayTime', '单位：毫秒']
            },
            grid: {
                left: '3%',
                right: '4%',
                bottom: '3%',
                x: 100,
                x2: 100,
                containLabel: true
            },
            dataZoom: {
                show: true,
                start: 0
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
                type: 'category',//X轴均为category，Y轴均为value
                boundaryGap: false,//数值轴两端的空白策略
                data: delayCreateTime
            },
            yAxis: {
                type: 'value'
            },
            series: [
                {
                    name: 'delayTime',
                    type: 'line',
//                                stack: '总量',//折线图堆叠的重要参数stack。只要将stack的值设置不相同，就不会堆叠了
                    symbol: 'none',
                    data: delayTime
                }
            ]
        };

        return option;
    }

    function buildOption1(data) {
        var recordTPS = data.recordTPSList;
        var readWriteCountPerMinute = data.readWriteCountPerMinuteList;
        var createTime = data.createTimeList;
        var option1 = {
            title: {
                text: '每分钟同步条数'
            },
            tooltip: {
                trigger: 'axis'
            },
            legend: {
                data: ['recordsPerMinute', 'readWriteCount', '单位：条/分钟;次/分钟']
            },
            grid: {
                left: '3%',
                right: '4%',
                bottom: '3%',
                x: 100,
                x2: 100,
                containLabel: true
            },
            dataZoom: {
                show: true,
                start: 0
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
                type: 'category',
                boundaryGap: false,
                data: createTime
            },
            yAxis: {
                type: 'value'
            },
            series: [
                {
                    name: 'recordsPerMinute',
                    type: 'line',
                    symbol: 'none',
                    data: recordTPS
                },
                {
                    name: 'readWriteCount',
                    type: 'line',
                    symbol: 'none',
                    data: readWriteCountPerMinute
                }
            ]
        };

        return option1;
    }

    function buildOption2(data) {
        var writeTimePerRecord = data.writeTimePerRecordList;
        var createTime = data.createTimeList;
        var option2 = {
            title: {
                text: '每条记录写入耗时'
            },
            tooltip: {
                trigger: 'axis'
            },
            legend: {
                data: ['writeTime', '单位：毫秒']
            },
            grid: {
                left: '3%',
                right: '4%',
                bottom: '3%',
                x: 100,
                x2: 100,
                containLabel: true
            },
            dataZoom: {
                show: true,
                start: 0
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
                type: 'category',
                boundaryGap: false,
                data: createTime
            },
            yAxis: {
                type: 'value'
            },
            series: [
                {
                    name: 'writeTime',
                    type: 'line',
                    symbol: 'none',
                    data: writeTimePerRecord
                }
            ]
        };

        return option2;
    }

    function buildOption3(data) {
        var recordSize = data.recordSizeList;
        var readWriteCountPerMinute = data.readWriteCountPerMinuteList;
        var createTime = data.createTimeList;

        var option3 = {
            title: {
                text: '每分钟同步流量'
            },
            tooltip: {
                trigger: 'axis'
            },
            legend: {
                data: ['recordSize', 'readWriteCount', '单位：字节/分钟;次/分钟']
            },
            grid: {
                left: '3%',
                right: '4%',
                bottom: '3%',
                x: 100,
                x2: 100,
                containLabel: true
            },
            dataZoom: {
                show: true,
                start: 0
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
                type: 'category',
                boundaryGap: false,
                data: createTime
            },
            yAxis: {
                type: 'value'
            },
            series: [
                {
                    name: 'recordSize',
                    type: 'line',
                    symbol: 'none',
                    data: recordSize
                },
                {
                    name: 'readWriteCount',
                    type: 'line',
                    symbol: 'none',
                    data: readWriteCountPerMinute
                }
            ]
        };

        return option3;
    }

    function buildOption4(data) {
        var exceptionsPerMinute = data.exceptionsPerMinuteList;
        var createTime = data.createTimeList;

        var option4 = {
            title: {
                text: '每分钟异常个数'
            },
            tooltip: {
                trigger: 'axis'
            },
            legend: {
                data: ['exceptionsPerMinute', '单位：个/分钟']
            },
            grid: {
                left: '3%',
                right: '4%',
                bottom: '3%',
                x: 100,
                x2: 100,
                containLabel: true
            },
            dataZoom: {
                show: true,
                start: 0
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
                type: 'category',
                boundaryGap: false,
                data: createTime
            },
            yAxis: {
                type: 'value'
            },
            series: [
                {
                    name: 'exceptionsPerMinute',
                    type: 'line',
                    symbol: 'none',
                    data: exceptionsPerMinute
                }
            ]
        };

        return option4;
    }

    function back2Main() {
        $("#taskStatistic").hide();
        $("#mainContentInner").show();
    }

</script>