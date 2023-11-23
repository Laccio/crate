docker run -d --rm \
--net=host \
--privileged \
--pid=host \
-v /sys:/sys \
-v /var/lib/docker/containers:/var/lib/docker/containers:ro \
-v /tmp/powerapi-sensor-reporting:/reporting \
-v $(pwd):/srv \
powerapi/hwpc-sensor \
-n "$(hostname -f)" \
-r "mongodb" -U "mongodb://127.0.0.1" -D "test" -C "prep" \
-s "rapl" -o -e "RAPL_ENERGY_PKG" \
-s "msr" -e "TSC" -e "APERF" -e "MPERF" \
-c "core" -e "CPU_CLK_UNHALTED:REF_P" -e "CPU_CLK_UNHALTED:THREAD_P" -e "LLC_MISSES" -e "INSTRUCTIONS_RETIRED"

