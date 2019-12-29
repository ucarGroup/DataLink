<%@ page language="java" pageEncoding="utf-8" contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="basePath" value="${pageContext.servletContext.contextPath }"/>

<div id="taskWriter" class="col-sm-12">
    <div class="col-sm-12">
        <span class="help-inline col-sm-8">
            <label class="middle col-sm-2">
                <input class="ace" type="checkbox" id="rdbms-checkbox"
                        <c:if test="${taskModel.currentWriters['writer-rdbms']==1}">
                            checked
                        </c:if>
                       onchange="rdbmsClicked();"/>
                <span class="lbl">Rdbms</span>
            </label>
            <label class="middle col-sm-2">
                <input class="ace" type="checkbox" id="es-checkbox"
                        <c:if test="${taskModel.currentWriters['writer-es']==1}">
                            checked
                        </c:if>
                       onchange="esClicked();"/>
                <span class="lbl">ElasticSearch</span>
            </label>
            <label class="middle col-sm-2">
                <input class="ace" type="checkbox" id="hdfs-checkbox"
                        <c:if test="${taskModel.currentWriters['writer-hdfs']==1}">
                            checked
                        </c:if>
                       onchange="hdfsClicked();"/>
                <span class="lbl">Hdfs</span>
            </label>
            <label class="middle col-sm-2">
                <input class="ace" type="checkbox" id="dummy-checkbox" disabled/>
                <span class="lbl">Dummy</span>
            </label>
            <label class="middle col-sm-2">
                <input class="ace" type="checkbox" id="hbase-checkbox"
                        <c:if test="${taskModel.currentWriters['writer-hbase']==1}">
                            checked
                        </c:if>
                       onchange="hbaseClicked();"/>
                <span class="lbl">HBase</span>
            </label>
            

             <label class="middle col-sm-2">
                 <input class="ace" type="checkbox" id="kudu-checkbox"
                         <c:if test="${taskModel.currentWriters['writer-kudu']==1}">
                             checked
                         </c:if>
                        onchange="kuduClicked();" disabled/>
                 <span class="lbl">Kudu</span>
             </label>

              <label class="middle col-sm-2">
                  <input class="ace" type="checkbox" id="kafka-checkbox"
                          <c:if test="${taskModel.currentWriters['writer-kafka']==1}">
                              checked
                          </c:if>
                         onchange="kafkaClicked();"/>
                  <span class="lbl">Kafka</span>
              </label>


        </span>
    </div>

    <jsp:include page="taskWriterTabs.jsp"/>

</div>
<script type="text/javascript">
    $(document).ready(function () {
        rdbmsSyncModeChange();

        if (currentPageName == "mysql") {
            $('#kudu-checkbox').attr("disabled", false);
        }

    });

    $("#taskWriter").ready(function () {
        rdbmsClicked();
        esClicked();
        hdfsClicked();
        hbaseClicked();
        kuduClicked();
        kafkaClicked();
    });

    function rdbmsSyncModeChange() {
        var syncMode = $('#rdbms-syncMode').val();
        if (syncMode == 'TablePartlyOrdered') {
            $('#rdbms-merging').attr("disabled", false);
        } else {
            $('#rdbms-merging').attr("disabled", true);
            $('#rdbms-merging').val('false');
        }

        rdbmsMergingChange();
    }

    function rdbmsMergingChange() {
        var merging = $('#rdbms-merging').val();
        if (merging == 'true') {
            $('#rdbms-useBatch').attr("disabled", false);
        } else {
            $('#rdbms-useBatch').attr("disabled", true);
            $('#rdbms-useBatch').val('false');
        }
    }

    function rdbmsClicked() {
        if ($('#rdbms-checkbox').prop("checked") == true) {
            $('#div-rdbms').show();
        } else {
            $('#div-rdbms').hide();
        }
    }

    function esClicked() {
        if ($('#es-checkbox').prop("checked") == true) {
            $('#div-es').show();
        } else {
            $('#div-es').hide();
        }
    }

    function hdfsClicked() {
        if ($('#hdfs-checkbox').prop("checked") == true) {
            $('#div-hdfs').show();
        } else {
            $('#div-hdfs').hide();
        }
    }

    function hbaseClicked() {
        if ($('#hbase-checkbox').prop("checked") == true) {
            $('#div-hbase').show();
        } else {
            $('#div-hbase').hide();
        }
    }


    function kuduClicked() {
        if ($('#kudu-checkbox').prop("checked") == true) {
            $('#div-kudu').show();
        } else {
            $('#div-kudu').hide();
        }
    }

    function kafkaClicked() {
        if ($('#kafka-checkbox').prop("checked") == true) {
            $('#div-kafka').show();
        } else {
            $('#div-kafka').hide();
        }
    }


    function getWritersObj() {
        var obj = {};
        if ($('#rdbms-checkbox').prop("checked") == true) {
            obj['writer-rdbms'] = {
                "@type": "com.ucar.datalink.domain.plugin.writer.rdbms.RdbmsWriterParameter",
                poolSize: $("#rdbms-poolSize").val(),
                dryRun: $("#rdbms-dryRun").val(),
                useBatch: $("#rdbms-useBatch").val(),
                batchSize: $("#rdbms-batchSize").val(),
                merging: $("#rdbms-merging").val(),
                maxRetryTimes: $("#rdbms-maxRetryTimes").val(),
                retryMode: $("#rdbms-retryMode").val(),
                perfStatistic: $("#rdbms-perfStatistic").val(),
                syncMode: $("#rdbms-syncMode").val(),
                useUpsert: $("#rdbms-useUpsert").val()
            };
        }
        if ($('#es-checkbox').prop("checked") == true) {
            obj['writer-es'] = {
                "@type": "com.ucar.datalink.domain.plugin.writer.es.EsWriterParameter",
                poolSize: $("#es-poolSize").val(),
                dryRun: $("#es-dryRun").val(),
                useBatch: $("#es-useBatch").val(),
                batchSize: $("#es-batchSize").val(),
                merging: $("#es-merging").val(),
                maxRetryTimes: $("#es-maxRetryTimes").val(),
                retryMode: $("#es-retryMode").val(),
                perfStatistic: $("#es-perfStatistic").val()
            };
        }
        if ($('#hdfs-checkbox').prop("checked") == true) {
            obj['writer-hdfs'] = {
                "@type": "com.ucar.datalink.domain.plugin.writer.hdfs.HdfsWriterParameter",
                poolSize: $("#hdfs-poolSize").val(),
                dryRun: $("#hdfs-dryRun").val(),
                useBatch: $("#hdfs-useBatch").val(),
                batchSize: $("#hdfs-batchSize").val(),
                merging: $("#hdfs-merging").val(),
                maxRetryTimes: $("#hdfs-maxRetryTimes").val(),
                retryMode: $("#hdfs-retryMode").val(),
                commitMode: $("#hdfs-commitMode").val(),
                streamLeisureLimit: $("#hdfs-streamLeisureLimit").val(),
                hdfsPacketSize: $("#hdfs-hdfsPacketSize").val(),
                perfStatistic: $("#hdfs-perfStatistic").val(),
                hbasePath: $("#hdfs-hbasePath").val(),
                mysqlBinlogPath: $("#hdfs-mysqlBinlogPath").val(),
                binlogPathPrefix: $("#hdfs-binlogPathPrefix").val(),
                hsyncInterval: $("#hdfs-hsyncInterval").val(),
                socketTimeout: $("#hdfs-socketTimeout").val()
            };
        }
        if ($('#hbase-checkbox').prop("checked") == true) {
            obj['writer-hbase'] = {
                "@type": "com.ucar.datalink.domain.plugin.writer.hbase.HBaseWriterParameter",
                poolSize: $("#hbase-poolSize").val(),
                dryRun: $("#hbase-dryRun").val(),
                useBatch: $("#hbase-useBatch").val(),
                batchSize: $("#hbase-batchSize").val(),
                merging: $("#hbase-merging").val(),
                maxRetryTimes: $("#hbase-maxRetryTimes").val(),
                retryMode: $("#hbase-retryMode").val(),
                perfStatistic: $("#hbase-perfStatistic").val()
            };
        }

        if ($('#kudu-checkbox').prop("checked") == true) {
            obj['writer-kudu'] = {
                "@type": "com.ucar.datalink.domain.plugin.writer.kudu.KuduWriterParameter",
                poolSize: $("#kudu-poolSize").val(),
                batchSize: $("#kudu-batchSize").val()
            };
        }
        if ($('#kafka-checkbox').prop("checked") == true) {
            obj['writer-kafka'] = {
                "@type": "com.ucar.datalink.domain.plugin.writer.kafka.KafkaWriterParameter",
                poolSize: $("#kafka-poolSize").val(),
                dryRun: $("#kafka-dryRun").val(),
                useBatch: $("#kafka-useBatch").val(),
                batchSize: $("#kafka-batchSize").val(),
                merging: $("#kafka-merging").val(),
                maxRetryTimes: $("#kafka-maxRetryTimes").val(),
                retryMode: $("#kafka-retryMode").val(),
                perfStatistic: $("#kafka-perfStatistic").val(),
                serializeMode: $("#kafka-serializeMode").val(),
                partitionMode: $("#kafka-partitionMode").val()
            };
        }
        ;


        return obj;
    }
</script>