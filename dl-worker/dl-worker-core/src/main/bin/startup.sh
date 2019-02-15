#!/bin/bash

if [ -f "/etc/profile" ];then
    source /etc/profile
fi
current_path=`pwd`
case "`uname`" in
    Linux)
		bin_abs_path=$(readlink -f $(dirname $0))
		;;
	*)
		bin_abs_path=`cd $(dirname $0); pwd`
		;;
esac
base=${bin_abs_path}/..
worker_conf=$base/conf/worker.properties
logback_configurationFile=$base/conf/logback.xml
java_opts_file=$base/conf/javaopts
export LANG=en_US.UTF-8
export BASE=$base

if [ -e $java_opts_file ]; then
	source $java_opts_file
fi

if [ -f $base/bin/worker.pid ] ; then
    ps -fe | grep datalink-worker | grep -v grep > /dev/null 2>&1
    if [ $? -ne 0 ];then
    	echo "datalink worker is hung up"
    	rm -f worker.pid
    else
	    echo "found worker.pid , Please run stop.sh first ,then startup.sh" 2>&2
        exit 1
    fi
fi

if [ ! -d $base/logs/worker ] ; then
	mkdir -p $base/logs/worker
fi

if [ ! -d $base/logs/gc ] ; then
	mkdir -p $base/logs/gc
fi

## set java path
if [ -z "$JAVA" ] ; then
  JAVA=$(which java)
fi

if [ -z "$JAVA" ]; then
  	echo "Cannot find a Java JDK. Please set either set JAVA or put java (>=1.5) in your PATH." 2>&2
    exit 1
fi

case "$#"
in
0 )
	;;
1 )
	var=$*
	if [ -f $var ] ; then
		worker_conf=$var
	else
		echo "THE PARAMETER IS NOT CORRECT.PLEASE CHECK AGAIN."
        exit
	fi;;
2 )
	var=$1
	if [ -f $var ] ; then
		worker_conf=$var
	else
		if [ "$1" = "debug" ]; then
			DEBUG_PORT=$2
			DEBUG_SUSPEND="y"
			JAVA_DEBUG_OPT="-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=$DEBUG_PORT,server=y,suspend=$DEBUG_SUSPEND"
		fi
     fi;;
* )
	echo "THE PARAMETERS MUST BE TWO OR LESS.PLEASE CHECK AGAIN."
	exit;;
esac

str=`file -L $JAVA | grep 64-bit`
if [ -n "$JAVA_OPTS_CONF" ]; then
    JAVA_OPTS=$JAVA_OPTS_CONF
elif [ -n "$str" ]; then
	JAVA_OPTS="-server -Xms2048m -Xmx3072m -Xmn2048m -XX:SurvivorRatio=2 -Xss256k -XX:-UseAdaptiveSizePolicy -XX:MaxTenuringThreshold=15 -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+UseCMSCompactAtFullCollection -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:+HeapDumpOnOutOfMemoryError -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:${BASE}/logs/gc/gc-worker-%t.log -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=20 -XX:GCLogFileSize=1024K"
else
	JAVA_OPTS="-server -Xms1024m -Xmx1024m -XX:NewSize=256m -XX:MaxNewSize=256m -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:${BASE}/logs/gc/gc-worker-%t.log -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=20 -XX:GCLogFileSize=1024K"
fi

JAVA_OPTS=" $JAVA_OPTS -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true -Dfile.encoding=UTF-8"
WORKER_OPTS="-DappName=datalink-worker -Dlogback.configurationFile=$logback_configurationFile -Djava.opts.file=$java_opts_file -Dworker.conf=$worker_conf -Dworker.home=$base -Dcom.sun.management.jmxremote.port=9933 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false"

if [ -e $worker_conf -a -e $logback_configurationFile ]
then

	for i in $base/lib/*;
		do CLASSPATH=$i:"$CLASSPATH";
	done
 	CLASSPATH="$base/conf:$CLASSPATH";

 	echo "cd to $bin_abs_path for workaround relative path"
  	cd $bin_abs_path

	echo LOG CONFIGURATION : $logback_configurationFile
	echo worker conf : $worker_conf
	echo CLASSPATH :$CLASSPATH
	echo JAVA_OPTS :$JAVA_OPTS
	$JAVA $JAVA_OPTS $JAVA_DEBUG_OPT $WORKER_OPTS -classpath .:$CLASSPATH com.ucar.datalink.worker.core.boot.WorkerBootStrap 1>>$base/logs/worker/worker.log 2>&1 &
	echo $! > $base/bin/worker.pid

	echo "cd to $current_path for continue"
  	cd $current_path
else
	echo "worker conf("$worker_conf") OR log configration file($logback_configurationFile) is not exist,please create then first!"
fi

echo "append worker cron script"
if [ ! -f "/var/spool/cron/root" ];then
    touch /var/spool/cron/root
fi
grep "startup.sh" /var/spool/cron/root
if [ $? -ne 0 ];then
	cron_asterisk="* * * * *"
    cron_content="$base/bin/startup.sh"
    echo "$cron_asterisk sh $cron_content" >> /var/spool/cron/root
    if [ -f "/etc/init.d/crond" ];then
    	/etc/init.d/crond restart
    fi
fi
