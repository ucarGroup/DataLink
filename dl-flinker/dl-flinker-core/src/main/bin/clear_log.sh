#!/bin/bash 

current_date=`date +%Y%m01`
three_months_ago=`date -d "${current_date} last month last month last month" +%Y-%m`

logs_path="/usr/local/frame/dl-manager/logs/manager"
rm -rf $logs_path"/"$three_months_ago"*"

