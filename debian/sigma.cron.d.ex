#
# Regular cron jobs for the sigma package
#
0 4	* * *	root	[ -x /usr/bin/sigma_maintenance ] && /usr/bin/sigma_maintenance
