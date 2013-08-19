#!/usr/bin/env bash
#
# Wrapper for monitoring application

JAVA_HOME=/opt/sunjdk7
LOGBACK_CONFIG=logback.xml
CONEXT_DOMAIN=demo.openconext.org
ENGINEBLOCK_CRT=engine.demo.openconext.org.pem
TRUST_CRT=openconext_ca.pem

## Leave out the TRUST_CRT argument if not applicable (for officially signed certs)

$JAVA_HOME/bin/java \
  -Dlogback.configurationFile=$LOGBACK_CONFIG \
  -classpath "lib/*" \
  nl.surfnet.coin.monitoring.Monitor \
  $CONEXT_DOMAIN $ENGINEBLOCK_CRT $TRUST_CRT
