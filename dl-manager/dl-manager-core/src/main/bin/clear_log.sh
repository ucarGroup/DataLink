#!/bin/bash 

current_date=`date +%Y%m01`
three_months_ago=`date -d "${current_date} last month last month last month" +%Y-%m`
#echo $three_months_ago"*"

logs_path="/usr/local/frame/datax/logs"
log_admin_path="/usr/local/frame/datax/log_admin"
rm -rf $logs_path"/"$three_months_ago"*"
rm -rf $log_admin_path"/"$three_months_ago"*"

