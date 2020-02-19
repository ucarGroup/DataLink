#!/bin/bash

    echo "uninstall cron"
    grep "startup.sh" /var/spool/cron/root > /dev/null 2>&1
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
# compatible with previous logic, not deleted.
pidfile=$base/bin/datax-admin.pid
pid=`get_pid "appName=datax-admin"`
echo -e "`hostname`: stopping datax-admin $pid ... "
kill $pid

LOOPS=0
while (true); 
do 
	gpid=`get_pid "appName=datax-admin" "$pid"`
    if [ "$gpid" == "" ] ; then
    	echo "Oook! cost:$LOOPS"
    	if [ -f "$pidfile" ];then
    	    `rm $pidfile` > /dev/null 2>&1
    	fi
    	break;
    fi
    let LOOPS=LOOPS+1
    sleep 1
done



