#!/bin/bash

current_path=`pwd`
case "`uname`" in
    Linux)
		bin_abs_path=$(readlink -f $(dirname $0))
		;;
	*)
		bin_abs_path=`cd $(dirname $0); pwd`
		;;
esac
base=${bin_abs_path}
java_opts_file=$base/javaopts.properties
export LANG=en_US.UTF-8
export BASE=$base


## set java path
if [ -z "$JAVA" ] ; then
  JAVA=$(which java)
fi

if [ -z "$JAVA" ]; then
  	echo "Cannot find a Java JDK. Please set either set JAVA or put java (>=1.5) in your PATH." 2>&2
    exit 1
fi

str=`file -L $JAVA | grep 64-bit`
if [ -e $java_opts_file ]; then
    JAVA_OPTS=`cat $java_opts_file`
elif [ -n "$str" ]; then
	JAVA_OPTS="-server -Xms5120m -Xmx6144m -Xmn2048m -XX:SurvivorRatio=2 -Xss256k -XX:-UseAdaptiveSizePolicy -XX:MaxTenuringThreshold=15 -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+UseCMSCompactAtFullCollection -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:+HeapDumpOnOutOfMemoryError -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:${BASE}/logs/gc/gc-worker-%t.log -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=20 -XX:GCLogFileSize=1024K"
else
	JAVA_OPTS="-server -Xms1024m -Xmx1024m -XX:NewSize=256m -XX:MaxNewSize=256m -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:${BASE}/logs/gc/gc-worker-%t.log -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=20 -XX:GCLogFileSize=1024K"
fi

JAVA_OPTS=" $JAVA_OPTS -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true -Dfile.encoding=UTF-8"

if [ -e $java_opts_file ]
then

	for i in $base/lib/*;
		do CLASSPATH=$i:"$CLASSPATH";
	done
 	CLASSPATH="$base/conf:$CLASSPATH";

 	echo "cd to $bin_abs_path for workaround relative path"
  	cd $bin_abs_path

	echo CLASSPATH :$CLASSPATH
	echo JAVA_OPTS :$JAVA_OPTS

	echo $! > $base/bin/worker.pid

	echo "cd to $current_path for continue"
  	cd $current_path
else
	echo "worker conf("$worker_conf") OR log configration file($logback_configurationFile) is not exist,please create then first!"
fi
