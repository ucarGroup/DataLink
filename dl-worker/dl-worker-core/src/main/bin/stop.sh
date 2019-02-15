#!/bin/bash

    echo "uninstall cron"
    grep "startup.sh" /var/spool/cron/root
    if [ $? -eq 0 ];then
        sed -i "/startup.sh/d" /var/spool/cron/root
        if [ ! -f "/etc/init.d/crond" ];then
        	/etc/init.d/crond restart
        fi
    fi

cygwin=false;
linux=false;
case "`uname`" in
    CYGWIN*)
        cygwin=true
        ;;
    Linux*)
    	linux=true
    	;;
esac

get_pid() {
	STR=$1
	PID=$2
    if $cygwin; then
        JAVA_CMD="$JAVA_HOME\bin\java"
        JAVA_CMD=`cygpath --path --unix $JAVA_CMD`
        JAVA_PID=`ps |grep $JAVA_CMD |awk '{print $1}'`
    else
    	if $linux; then
	        if [ ! -z "$PID" ]; then
	        	JAVA_PID=`ps -C java -f --width 1000|grep "$STR"|grep "$PID"|grep -v grep|awk '{print $2}'`
		    else
		        JAVA_PID=`ps -C java -f --width 1000|grep "$STR"|grep -v grep|awk '{print $2}'`
	        fi
	    else
	    	if [ ! -z "$PID" ]; then
	        	JAVA_PID=`ps aux |grep "$STR"|grep "$PID"|grep -v grep|awk '{print $2}'`
		    else
		        JAVA_PID=`ps aux |grep "$STR"|grep -v grep|awk '{print $2}'`
	        fi
	    fi
    fi
    echo $JAVA_PID;
}

base=`dirname $0`/..
pidfile=$base/bin/worker.pid
if [ ! -f "$pidfile" ];then
	echo "worker is not running. exists"
	exit
fi

pid=`cat $pidfile`
if [ "$pid" == "" ] ; then
	pid=`get_pid "appName=datalink-worker"`
fi

echo -e "`hostname`: stopping worker $pid ... "
kill $pid

LOOPS=0
while (true);
do
	gpid=`get_pid "appName=datalink-worker" "$pid"`
    if [ "$gpid" == "" ] ; then
    	echo "Oook! cost:$LOOPS"
    	if [ -f "$pidfile" ];then
    		`rm $pidfile`
    	fi
    	break;
    fi
    let LOOPS=LOOPS+1
    sleep 1
done
