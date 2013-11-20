#!/usr/bin/env bash
#
# Wrapper for monitoring application

DIRECTORY=`dirname $0`

LOGBACK_CONFIG=$DIRECTORY/logback.xml
API_BASEURL=https://api.surfconext.nl/v1
CLIENT_ID=https://monitor.surfconext.nl
CLIENT_SECRET=??
PERSON_ID=urn:collab:person:surf.net:niels

## Leave out the TRUST_CRT argument if not applicable (for officially signed certs)
java \
  -Dlogback.configurationFile=$LOGBACK_CONFIG \
  -classpath "$DIRECTORY/lib/*" \
  nl.surfnet.coin.monitoring.ApiMonitor \
   $API_BASEURL $CLIENT_ID $CLIENT_SECRET $PERSON_ID