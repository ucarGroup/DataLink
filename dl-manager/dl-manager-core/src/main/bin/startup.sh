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
manager_conf=$base/conf/manager.properties
logback_configurationFile=$base/conf/logback.xml
export LANG=en_US.UTF-8
export BASE=$base

if [ -f $base/bin/manager.pid ] ; then
    ps -fe | grep datalink-manager | grep -v grep > /dev/null 2>&1
    if [ $? -ne 0 ];then
    	echo "datalink manager is hung up"
    	rm -f manager.pid
    else
	    echo "found manager.pid , Please run stop.sh first ,then startup.sh" 2>&2
        exit 1
    fi
fi

if [ ! -d $base/logs/manager ] ; then
	mkdir -p $base/logs/manager
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
		manager_conf=$var
	else
		echo "THE PARAMETER IS NOT CORRECT.PLEASE CHECK AGAIN."
        exit
	fi;;
2 )
	var=$1
	if [ -f $var ] ; then
		manager_conf=$var
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
if [ -n "$str" ]; then
	JAVA_OPTS="-server -Xms2048m -Xmx3072m -Xmn1024m -XX:SurvivorRatio=2 -Xss256k -XX:-UseAdaptiveSizePolicy -XX:MaxTenuringThreshold=15 -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+UseCMSCompactAtFullCollection -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:+HeapDumpOnOutOfMemoryError -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:${BASE}/logs/gc/gc-manager-%t.log -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=20 -XX:GCLogFileSize=1024K"
else
	JAVA_OPTS="-server -Xms1024m -Xmx1024m -XX:NewSize=256m -XX:MaxNewSize=256m -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:${BASE}/logs/gc/gc-manager-%t.log -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=20 -XX:GCLogFileSize=1024K"
fi

JAVA_OPTS=" $JAVA_OPTS -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true -Dfile.encoding=UTF-8"
WEBAPP_CONF=${base}/webapp
MANAGER_OPTS="-DappName=datalink-manager -Dlogback.configurationFile=$logback_configurationFile -Dmanager.conf=$manager_conf -Dwebapp.conf=$WEBAPP_CONF -Dcom.sun.management.jmxremote.port=9933 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false"

if [ -e $manager_conf -a -e $logback_configurationFile ]
then

	for i in $base/lib/*;
		do CLASSPATH=$i:"$CLASSPATH";
	done
 	CLASSPATH="$base/conf:$CLASSPATH";

 	echo "cd to $bin_abs_path for workaround relative path"
  	cd $bin_abs_path

	echo LOG CONFIGURATION : $logback_configurationFile
	echo manager conf : $manager_conf
	echo CLASSPATH :$CLASSPATH
	$JAVA $JAVA_OPTS $JAVA_DEBUG_OPT $MANAGER_OPTS -classpath .:$CLASSPATH com.ucar.datalink.manager.core.boot.ManagerBootStrap 1>>$base/logs/manager/manager.log 2>&1 &
	echo $! > $base/bin/manager.pid

	echo "cd to $current_path for continue"
  	cd $current_path
else
	echo "manager conf("$manager_conf") OR log configration file($logback_configurationFile) is not exist,please create then first!"
fi


echo "append manager cron script"
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
