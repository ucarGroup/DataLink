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
                    <input class="col-sm-2" type='text' id='startTime2' name="startTime"
                           value="${workerJvmStateInfo.startTime}"/>

                    <div class="col-sm-1" style="align-content: center">
                        <table align="center">
                            <tr>
                                <td>至</td>
                            </tr>
                        </table>
                    </div>

                    <input class="col-sm-2" type='text' id='endTime2' name="endTime"
                           value="${workerJvmStateInfo.endTime}"/>
                    &nbsp; &nbsp; &nbsp;
                    <button type="button" id="search" class="btn btn-sm btn-purple" onclick="doSearchSystemMonitor()">查询</button>
                </div>

                <div class="form-group">

                    <div class="col-md-10">
                        <div style="padding-left: 10px;">
                            <div id="loadAverageInfo" style="width: 1100px;height: 300px;"></div>
                        </div>
                    </div>

                    <div class="col-md-10">
                        <div style="padding-left: 10px;">
                            <div id="CPUUtilization" style="width: 1100px;height: 300px;"></div>
                        </div>
                    </div>

                    <div class="col-md-10">
                        <div style="padding-left: 10px;">
                            <div id="networkTraffic" style="width: 1100px;height: 300px;"></div>
                        </div>
                    </div>

                    <div class="col-md-10">
                        <div style="padding-left: 10px;">
                            <div id="tcpCurrentEstab" style="width: 1100px;height: 300px;"></div>
                        </div>
                    </div>

                </div>

            </form>
        </div>

    </div>
</div>


<script src="${basePath}/assets/echarts2/echarts.js"></script>
<script type="text/javascript">
    $("#startTime2").datetimepicker(
            {
                format: 'YYYY-MM-DD HH:mm:ss'
            }
    );
    $("#endTime2").datetimepicker(
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

                var loadAverageList = ${loadAverageList};
                var userCPUUtilizationList = ${userCPUUtilizationList};
                var sysCPUUtilizationList = ${sysCPUUtilizationList};
                var incomingNetworkTrafficList = ${incomingNetworkTrafficList};
                var outgoingNetworkTrafficList = ${outgoingNetworkTrafficList};
                var tcpCurrentEstabList = ${tcpCurrentEstabList};
                var createTimeSysList = ${createTimeSysList};

                var option6 = buildOption6();
                var mydivChart6 = document.getElementById("loadAverageInfo");
                var myChart6 = ec.init(mydivChart6);
                myChart6.setOption(option6, true);

                var option7 = buildOption7();
                var mydivChart7 = document.getElementById("CPUUtilization");
                var myChart7 = ec.init(mydivChart7);
                myChart7.setOption(option7, true);

                var option8 = buildOption8();
                var mydivChart8 = document.getElementById("networkTraffic");
                var myChart8 = ec.init(mydivChart8);
                myChart8.setOption(option8, true);

                var option9 = buildOption9();
                var mydivChart9 = document.getElementById("tcpCurrentEstab");
                var myChart9 = ec.init(mydivChart9);
                myChart9.setOption(option9, true);



                function buildOption6() {
                    option6 = {
                        title: {
                            text: '平均负载'
                        },
                        tooltip: {
                            trigger: 'axis'
                        },
                        legend: {
                            data: ['平均负载']
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
                            data: createTimeSysList
                        },
                        yAxis: {
                            type: 'value'
                        },
                        series: [
                            {
                                name: '平均负载',
                                type: 'line',
                                symbol: 'none',
                                data: loadAverageList
                            }
                        ]
                    };
                    return option6;
                }

                function buildOption7() {
                    option7 = {
                        title: {
                            text: 'CPU使用率'
                        },
                        tooltip: {
                            trigger: 'axis'
                        },
                        legend: {
                            data: ['用户CPU使用率', '系统CPU使用率']
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
                            data: createTimeSysList
                        },
                        yAxis: {
                            name: '%',
                            type: 'value'
                        },
                        series: [
                            {
                                name: '用户CPU使用率',
                                type: 'line',
                                symbol: 'none',
                                data: userCPUUtilizationList
                            },
                            {
                                name: '系统CPU使用率',
                                type: 'line',
                                symbol: 'none',
                                data: sysCPUUtilizationList
                            }
                        ]
                    };
                    return option7;
                }

                function buildOption8() {
                    option8 = {
                        title: {
                            text: '网卡流量'
                        },
                        tooltip: {
                            trigger: 'axis'
                        },
                        legend: {
                            data: ['接收网卡流量', '发送网卡流量']
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
                            data: createTimeSysList
                        },
                        yAxis: {
                            name: 'Mbps',
                            type: 'value'
                        },
                        series: [
                            {
                                name: '接收网卡流量',
                                type: 'line',
                                symbol: 'none',
                                data: incomingNetworkTrafficList
                            },
                            {
                                name: '发送网卡流量',
                                type: 'line',
                                symbol: 'none',
                                data: outgoingNetworkTrafficList
                            }
                        ]
                    };
                    return option8;
                }

                function buildOption9() {
                    option9 = {
                        title: {
                            text: 'TCP连接数'
                        },
                        tooltip: {
                            trigger: 'axis'
                        },
                        legend: {
                            data: ['TCP连接数']
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
                            data: createTimeSysList
                        },
                        yAxis: {
                            type: 'value'
                        },
                        series: [
                            {
                                name: 'TCP连接数',
                                type: 'line',
                                symbol: 'none',
                                data: tcpCurrentEstabList
                            }
                        ]
                    };
                    return option9;
                }

            }
    );

    function doSearchSystemMonitor() {
        var start = $("#startTime2").val();
        var end = $("#endTime2").val();

        var obj = {
            workerId: $("#id").val(),
            startTime: Date.parse(new Date(start)),
            endTime: Date.parse(new Date(end))
        };

        $.ajax({
            type: "POST",
            url: "${basePath}/worker/doSearchSystemMonitor",
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

                                var option6 = buildOption6(data);
                                var mydivChart6 = document.getElementById("loadAverageInfo");
                                var myChart6 = ec.init(mydivChart6);
                                myChart6.setOption(option6, true);

                                var option7 = buildOption7(data);
                                var mydivChart7 = document.getElementById("CPUUtilization");
                                var myChart7 = ec.init(mydivChart7);
                                myChart7.setOption(option7, true);

                                var option8 = buildOption8(data);
                                var mydivChart8 = document.getElementById("networkTraffic");
                                var myChart8 = ec.init(mydivChart8);
                                myChart8.setOption(option8, true);

                                var option9 = buildOption9(data);
                                var mydivChart9 = document.getElementById("tcpCurrentEstab");
                                var myChart9 = ec.init(mydivChart9);
                                myChart9.setOption(option9, true);
                            }
                    );
                }
            }
        });

    }



    function buildOption6(data) {
        var loadAverageList = data.loadAverageList;
        var createTimeSysList = data.createTimeSysList;

        var option6 = {
            title: {
                text: '平均负载'
            },
            tooltip: {
                trigger: 'axis'
            },
            legend: {
                data: ['平均负载']
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
                data: createTimeSysList
            },
            yAxis: {
                type: 'value'
            },
            series: [
                {
                    name: '平均负载',
                    type: 'line',
                    symbol: 'none',
                    data: loadAverageList
                }
            ]
        };
        return option6;
    }

    function buildOption7(data) {
        var userCPUUtilizationList = data.userCPUUtilizationList;
        var sysCPUUtilizationList = data.sysCPUUtilizationList;
        var createTimeSysList = data.createTimeSysList;

        var option7 = {
            title: {
                text: 'CPU使用率'
            },
            tooltip: {
                trigger: 'axis'
            },
            legend: {
                data: ['用户CPU使用率', '系统CPU使用率']
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
                data: createTimeSysList
            },
            yAxis: {
                name: '%',
                type: 'value'
            },
            series: [
                {
                    name: '用户CPU使用率',
                    type: 'line',
                    symbol: 'none',
                    data: userCPUUtilizationList
                },
                {
                    name: '系统CPU使用率',
                    type: 'line',
                    symbol: 'none',
                    data: sysCPUUtilizationList
                }
            ]
        };

        return option7;
    }

    function buildOption8(data) {
        var incomingNetworkTrafficList = data.incomingNetworkTrafficList;
        var outgoingNetworkTrafficList = data.outgoingNetworkTrafficList;
        var createTimeSysList = data.createTimeSysList;

        var option8 = {
            title: {
                text: '网卡流量'
            },
            tooltip: {
                trigger: 'axis'
            },
            legend: {
                data: ['接收网卡流量', '发送网卡流量']
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
                data: createTimeSysList
            },
            yAxis: {
                name: 'Mbps',
                type: 'value'
            },
            series: [
                {
                    name: '接收网卡流量',
                    type: 'line',
                    symbol: 'none',
                    data: incomingNetworkTrafficList
                },
                {
                    name: '发送网卡流量',
                    type: 'line',
                    symbol: 'none',
                    data: outgoingNetworkTrafficList
                }
            ]
        };

        return option8;
    }

    function buildOption9(data) {
        var tcpCurrentEstabList = data.tcpCurrentEstabList;
        var createTimeSysList = data.createTimeSysList;

        var option9 = {
            title: {
                text: 'TCP连接数'
            },
            tooltip: {
                trigger: 'axis'
            },
            legend: {
                data: ['TCP连接数']
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
                data: createTimeSysList
            },
            yAxis: {
                type: 'value'
            },
            series: [
                {
                    name: 'TCP连接数',
                    type: 'line',
                    symbol: 'none',
                    data: tcpCurrentEstabList
                }
            ]
        };
        return option9;
    }

</script>