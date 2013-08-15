#!/usr/bin/env bash
#
# Wrapper for monitoring application

JAVA_HOME=/opt/sunjdk7
LOGBACK_CONFIG=logback.xml
CONEXT_DOMAIN=surfconext.nl
ENGINEBLOCK_CRT=engineblock-prod.crt



$JAVA_HOME/bin/java \
  -Dlogback.configurationFile=$LOGBACK_CONFIG \
  -classpath "lib/*" \
  nl.surfnet.coin.monitoring.Monitor \
  $CONEXT_DOMAIN $ENGINEBLOCK_CRT
