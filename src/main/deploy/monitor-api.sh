#!/usr/bin/env bash
#
# Wrapper for monitoring application


DIRECTORY=`dirname $0`

LOGBACK_CONFIG=$DIRECTORY/logback.xml
PROPERTIES_FILE=$DIRECTORY/monitor.properties

java \
  -Dlogback.configurationFile=$LOGBACK_CONFIG \
  -Dmonitor.propertiesFile=$PROPERTIES_FILE \
    -classpath "$DIRECTORY/lib/*:$DIRECTORY/../keys" \
  nl.surfnet.coin.monitoring.ApiMonitor