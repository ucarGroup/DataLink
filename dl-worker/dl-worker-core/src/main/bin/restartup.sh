#!/bin/bash

case "`uname`" in
    Linux)
		bin_abs_path=$(readlink -f $(dirname $0))
		;;
	*)
		bin_abs_path=`cd $(dirname $0); pwd`
		;;
esac

echo "restartup.sh is running..."
echo "workerStop: start"
sh ${bin_abs_path}/stop.sh
echo "workerStop: end"

echo "workerStartup: start"
sh ${bin_abs_path}/startup.sh
echo "workerStartup: end"