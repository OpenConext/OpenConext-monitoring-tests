#!/usr/bin/env bash
#
# Wrapper for monitoring application

DIRECTORY=`dirname $0`

LOGBACK_CONFIG=$DIRECTORY/logback.xml
CONEXT_DOMAIN=surfconext.nl
ENGINEBLOCK_CRT=engine.surfconext.nl.pem
TRUST_CRT=

## Leave out the TRUST_CRT argument if not applicable (for officially signed certs)

java \
  -Dlogback.configurationFile=$LOGBACK_CONFIG \
  -classpath "$DIRECTORY/lib/*" \
  nl.surfnet.coin.monitoring.Monitor \
  $CONEXT_DOMAIN $ENGINEBLOCK_CRT $TRUST_CRT