#!/bin/bash 

source /etc/profile
current_path=`pwd`
case "`uname`" in
    Linux)
		bin_abs_path=$(readlink -f $(dirname $0))
		;;
	*)
		bin_abs_path=`cd $(dirname $0); pwd`
		;;
esac

get_pid() {
    pid=$1
    java_pid=""
    if [ -z "$pid" ] ; then
    	java_pid=`ps aux | grep java | grep "appName=datax-admin" | grep -v grep | awk '{print $2}' `
    else
    	java_pid=`ps aux | grep java | grep "$java_pid" | grep -v grep | awk '{print $2}' `
    fi
    if [ -z "$java_pid" ] ; then
        echo ""; return
    fi

    java_pid_port=`netstat -anpt | grep "$pid" | grep "LISTEN" | grep -v grep `
    if [ -z "$java_pid_port" ] ; then
        echco ""; return
    fi
    echo $java_pid
}

base=${bin_abs_path}/..
logback_configurationFile=$base/conf/logback.xml
export LANG=en_US.UTF-8
export BASE=$base


check_start_pid=`get_pid`
if [ ! -z "$check_start_pid" ] ; then
 	echo "found datax-admin running , Please run admin-stop.sh first ,then startup.sh" 2>&2
 	exit 1
fi

if [ ! -d $base/log_admin ] ; then 
	mkdir -p $base/log_admin
fi

## set java path
if [ -z "$JAVA" ] ; then
  JAVA=$(which java)
fi

JAVA_OPTS="-server -Xms512m -Xmx1024m -XX:NewSize=256m -XX:MaxNewSize=256m -XX:MaxPermSize=128m "
JAVA_OPTS=" $JAVA_OPTS -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true -Dfile.encoding=UTF-8"
ADMIN_OPTS="-DappName=datax-admin -Dlogback.configurationFile=$logback_configurationFile -Ddatax.home=$base" 

if [ -e $logback_configurationFile ]
then 
	
	for i in $base/lib/*;
		do CLASSPATH=$i:"$CLASSPATH";
	done
 	CLASSPATH="$base/conf:$CLASSPATH";
 	
 	echo "cd to $bin_abs_path for workaround relative path"
  	cd $bin_abs_path
 	
	echo LOG CONFIGURATION : $logback_configurationFile
	echo CLASSPATH :$CLASSPATH
	mkdir -p $base/../logs/log_admin/
	$JAVA $JAVA_OPTS $JAVA_DEBUG_OPT $ADMIN_OPTS -classpath .:$CLASSPATH com.ucar.datalink.flinker.core.admin.DataxAdminLauncher 1>>$base/log_admin/admin.log 2>&1 &
	echo "cd to $current_path for continue"
  	cd $current_path

else
	echo "log configration file($logback_configurationFile) is not exist,please create then first!"
fi

    echo "install cron"
    grep "startup.sh" /var/spool/cron/root > /dev/null 2>&1
    if [ $? -ne 0 ];then
        cron_asterisk="* * * * *"
        cron_content="$base/bin/startup.sh"
        echo "$cron_asterisk sh $cron_content" >> /var/spool/cron/root
        if [ ! -f "/etc/init.d/crond" ];then
            /etc/init.d/crond restart
        fi
   fi



