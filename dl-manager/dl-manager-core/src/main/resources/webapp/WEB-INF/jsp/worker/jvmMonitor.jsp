<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div class="main-content-inner">
    <div class="page-content">
        <div class="row">
            <form id="jvm_form" class="form-horizontal" role="form">

                <input type="hidden" name="id" id="id" value="${workerInfo.id}">

                <div class="form-group">
                    <label class="col-sm-2 control-label no-padding-right" id="workerAddress"
                           for="workerAddress" style="font-size:18px;">机器IP : ${workerInfo.workerAddress}</label>
                </div>

                <div class="form-group">
                    <div class="col-md-1"></div>
                    <input class="col-sm-2" type='text' id='startTime' name="startTime"
                           value="${workerJvmStateInfo.startTime}"/>

                    <div class="col-sm-1" style="align-content: center">
                        <table align="center">
                            <tr>
                                <td>至</td>
                            </tr>
                        </table>
                    </div>

                    <input class="col-sm-2" type='text' id='endTime' name="endTime"
                           value="${workerJvmStateInfo.endTime}"/>
                    &nbsp; &nbsp; &nbsp;
                    <button type="button" id="search" class="btn btn-sm btn-purple" onclick="doSearchJvmMonitor()">查询</button>
                </div>

                <div class="form-group">
                    <div class="col-md-10">
                        <div style="padding-left: 10px;">
                            <div id="youngMemInfo" style="width: 1100px;height: 300px;"></div>
                        </div>
                    </div>

                    <div class="col-md-10">
                        <div style="padding-left: 10px;">
                            <div id="oldMemInfo" style="width: 1100px;height: 300px;"></div>
                        </div>
                    </div>

                    <div class="col-md-10">
                        <div style="padding-left: 10px;">
                            <div id="GCCountInfo" style="width: 1100px;height: 300px;"></div>
                        </div>
                    </div>

                    <div class="col-md-10">
                        <div style="padding-left: 10px;">
                            <div id="GCTimeInfo" style="width: 1100px;height: 300px;"></div>
                        </div>
                    </div>

                    <div class="col-md-10">
                        <div style="padding-left: 10px;">
                            <div id="threadCountInfo" style="width: 1100px;height: 300px;"></div>
                        </div>
                    </div>

                </div>

            </form>
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

                var youngUsed = ${youngUsedList};
                var youngMax = ${youngMaxList};
                var oldUsed = ${oldUsedList};
                var oldMax = ${oldMaxList};
                var youngGCCountList = ${youngGCCountList};
                var oldGCCountList = ${oldGCCountList};
                var youngGCTimeList = ${youngGCTimeList};
                var oldGCTimeList = ${oldGCTimeList};
                var threadCountList = ${threadCountList};
                var createTime = ${createTimeList};

                var option = buildOption();
                var mydivChart = document.getElementById("youngMemInfo");
                var myChart = ec.init(mydivChart);
                myChart.setOption(option, true);

                var option2 = buildOption2();
                var mydivChart2 = document.getElementById("oldMemInfo");
                var myChart2 = ec.init(mydivChart2);
                myChart2.setOption(option2, true);

                var option3 = buildOption3();
                var mydivChart3 = document.getElementById("GCCountInfo");
                var myChart3 = ec.init(mydivChart3);
                myChart3.setOption(option3, true);

                var option4 = buildOption4();
                var mydivChart4 = document.getElementById("GCTimeInfo");
                var myChart4 = ec.init(mydivChart4);
                myChart4.setOption(option4, true);

                var option5 = buildOption5();
                var mydivChart5 = document.getElementById("threadCountInfo");
                var myChart5 = ec.init(mydivChart5);
                myChart5.setOption(option5, true);

                function buildOption() {
                    option = {
                        title: {
                            text: '新生代'
                        },
                        tooltip: {
                            trigger: 'axis'//tooltip触发方式:axis以X轴线触发,item以每一个数据项触发
                        },
                        legend: {
                            data: ['已用内存', '最大内存']
                        },
                        grid: {
                            left: '3%',
                            right: '4%',
                            bottom: '3%',
                            x: 50,//表格左边的宽度
                            x2: 50,//表格右边的宽度
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
                            data: createTime
                        },
                        yAxis: {
                            name: 'MB',
                            type: 'value'
                        },
                        series: [
                            {
                                name: '最大内存',
                                type: 'line',
//                                stack: '总量',//折线图堆叠的重要参数stack。只要将stack的值设置不相同，就不会堆叠了
                                symbol: 'none',
                                data: youngMax
                            },
                            {
                                name: '已用内存',
                                type: 'line',
//                                stack: '总量',
                                symbol: 'none',
                                data: youngUsed
                            }
                        ]
                    };
                    return option;
                }

                function buildOption2() {
                    option2 = {
                        title: {
                            text: '老年代'
                        },
                        tooltip: {
                            trigger: 'axis'
                        },
                        legend: {
                            data: ['已用内存', '最大内存']
                        },
                        grid: {
                            left: '3%',
                            right: '4%',
                            bottom: '3%',
                            x: 50,
                            x2: 50,
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
                            name: 'MB',
                            type: 'value'
                        },
                        series: [
                            {
                                name: '最大内存',
                                type: 'line',
                                symbol: 'none',
                                data: oldMax
                            },
                            {
                                name: '已用内存',
                                type: 'line',
                                symbol: 'none',
                                data: oldUsed
                            }
                        ]
                    };
                    return option2;
                }

                function buildOption3() {
                    option3 = {
                        title: {
                            text: 'GC次数'
                        },
                        tooltip: {
                            trigger: 'axis'
                        },
                        legend: {
                            data: ['新生代', '老年代']
                        },
                        grid: {
                            left: '3%',
                            right: '4%',
                            bottom: '3%',
                            x: 50,
                            x2: 50,
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
                            name: '次/分钟',
                            type: 'value'
                        },
                        series: [
                            {
                                name: '新生代',
                                type: 'line',
                                symbol: 'none',
                                data: youngGCCountList
                            },
                            {
                                name: '老年代',
                                type: 'line',
                                symbol: 'none',
                                data: oldGCCountList
                            }
                        ]
                    };
                    return option3;
                }

                function buildOption4() {
                    option4 = {
                        title: {
                            text: 'GC时间'
                        },
                        tooltip: {
                            trigger: 'axis'
                        },
                        legend: {
                            data: ['新生代', '老年代']
                        },
                        grid: {
                            left: '3%',
                            right: '4%',
                            bottom: '3%',
                            x: 50,
                            x2: 50,
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
                            name: '毫秒/分钟',
                            type: 'value'
                        },
                        series: [
                            {
                                name: '新生代',
                                type: 'line',
                                symbol: 'none',
                                data: youngGCTimeList
                            },
                            {
                                name: '老年代',
                                type: 'line',
                                symbol: 'none',
                                data: oldGCTimeList
                            }
                        ]
                    };
                    return option4;
                }

                function buildOption5() {
                    option5 = {
                        title: {
                            text: '当前线程数'
                        },
                        tooltip: {
                            trigger: 'axis'
                        },
                        legend: {
                            data: ['线程数']
                        },
                        grid: {
                            left: '3%',
                            right: '4%',
                            bottom: '3%',
                            x: 50,
                            x2: 50,
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
                                name: '线程数',
                                type: 'line',
                                symbol: 'none',
                                data: threadCountList
                            }
                        ]
                    };
                    return option5;
                }



            }
    );


    function doSearchJvmMonitor() {

        var start = $("#startTime").val();
        var end = $("#endTime").val();

        var obj = {
            workerId: $("#id").val(),
            startTime: Date.parse(new Date(start)),
            endTime: Date.parse(new Date(end))
        };

        $.ajax({
            type: "POST",
            url: "${basePath}/worker/doSearchJvmMonitor",
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
                                var mydivChart = document.getElementById("youngMemInfo");
                                var myChart = ec.init(mydivChart);
                                myChart.setOption(option, true);

                                var option2 = buildOption2(data);
                                var mydivChart2 = document.getElementById("oldMemInfo");
                                var myChart2 = ec.init(mydivChart2);
                                myChart2.setOption(option2, true);

                                var option3 = buildOption3(data);
                                var mydivChart3 = document.getElementById("GCCountInfo");
                                var myChart3 = ec.init(mydivChart3);
                                myChart3.setOption(option3, true);

                                var option4 = buildOption4(data);
                                var mydivChart4 = document.getElementById("GCTimeInfo");
                                var myChart4 = ec.init(mydivChart4);
                                myChart4.setOption(option4, true);

                                var option5 = buildOption5(data);
                                var mydivChart5 = document.getElementById("threadCountInfo");
                                var myChart5 = ec.init(mydivChart5);
                                myChart5.setOption(option5, true);

                            }
                    );
                }
            }
        });

    }

    function buildOption(data) {
        var youngUsed = data.youngUsedList;
        var youngMax = data.youngMaxList;
        var createTime = data.createTimeList;
        var option = {
            title: {
                text: '新生代'
            },
            tooltip: {
                trigger: 'axis'
            },
            legend: {
                data: ['已用内存', '最大内存']
            },
            grid: {
                left: '3%',
                right: '4%',
                bottom: '3%',
                x: 50,
                x2: 50,
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
                name: 'MB',
                type: 'value'
            },
            series: [
                {
                    name: '最大内存',
                    type: 'line',
                    symbol: 'none',
                    data: youngMax
                },
                {
                    name: '已用内存',
                    type: 'line',
                    symbol: 'none',
                    data: youngUsed
                }
            ]
        };

        return option;
    }

    function buildOption2(data) {
        var oldUsed = data.oldUsedList;
        var oldMax = data.oldMaxList;
        var createTime = data.createTimeList;
        var option2 = {
            title: {
                text: '老年代'
            },
            tooltip: {
                trigger: 'axis'
            },
            legend: {
                data: ['已用内存', '最大内存']
            },
            grid: {
                left: '3%',
                right: '4%',
                bottom: '3%',
                x: 50,
                x2: 50,
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
                name: 'MB',
                type: 'value'
            },
            series: [
                {
                    name: '最大内存',
                    type: 'line',
                    symbol: 'none',
                    data: oldMax
                },
                {
                    name: '已用内存',
                    type: 'line',
                    symbol: 'none',
                    data: oldUsed
                }
            ]
        };

        return option2;
    }

    function buildOption3(data) {
        var youngGCCountList = data.youngGCCountList;
        var oldGCCountList = data.oldGCCountList;
        var createTime = data.createTimeList;

        var option3 = {
            title: {
                text: 'GC次数'
            },
            tooltip: {
                trigger: 'axis'
            },
            legend: {
                data: ['新生代', '老年代']
            },
            grid: {
                left: '3%',
                right: '4%',
                bottom: '3%',
                x: 50,
                x2: 50,
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
                name: '次/分钟',
                type: 'value'
            },
            series: [
                {
                    name: '新生代',
                    type: 'line',
                    symbol: 'none',
                    data: youngGCCountList
                },
                {
                    name: '老年代',
                    type: 'line',
                    symbol: 'none',
                    data: oldGCCountList
                }
            ]
        };

        return option3;
    }

    function buildOption4(data) {
        var youngGCTimeList = data.youngGCTimeList;
        var oldGCTimeList = data.oldGCTimeList;
        var createTime = data.createTimeList;

        var option4 = {
            title: {
                text: 'GC时间'
            },
            tooltip: {
                trigger: 'axis'
            },
            legend: {
                data: ['新生代', '老年代']
            },
            grid: {
                left: '3%',
                right: '4%',
                bottom: '3%',
                x: 50,
                x2: 50,
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
                name: '毫秒/分钟',
                type: 'value'
            },
            series: [
                {
                    name: '新生代',
                    type: 'line',
                    symbol: 'none',
                    data: youngGCTimeList
                },
                {
                    name: '老年代',
                    type: 'line',
                    symbol: 'none',
                    data: oldGCTimeList
                }
            ]
        };

        return option4;
    }

    function buildOption5(data) {
        var threadCountList = data.threadCountList;
        var createTime = data.createTimeList;

        var option5 = {
            title: {
                text: '当前线程数'
            },
            tooltip: {
                trigger: 'axis'
            },
            legend: {
                data: ['线程数']
            },
            grid: {
                left: '3%',
                right: '4%',
                bottom: '3%',
                x: 50,
                x2: 50,
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
                    name: '线程数',
                    type: 'line',
                    symbol: 'none',
                    data: threadCountList
                }
            ]
        };
        return option5;
    }

</script>