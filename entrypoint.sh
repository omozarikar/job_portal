#!/bin/sh
# ============================================================
# Render gives us SPRING_DATASOURCE_URL as a raw connection string like:
#   postgres://user:pass@host:5432/dbname
# Spring's JDBC driver needs:
#   jdbc:postgresql://host:5432/dbname  (with user/pass passed separately)
# This script rewrites the URL at container startup, before launching the JAR.
# ============================================================

if [ -n "$SPRING_DATASOURCE_URL" ]; then
  case "$SPRING_DATASOURCE_URL" in
    postgres://*|postgresql://*)
      # Strip the postgres:// or postgresql:// prefix, then strip any user:pass@ section
      STRIPPED=$(echo "$SPRING_DATASOURCE_URL" | sed -E 's#^postgres(ql)?://##')
      HOSTPART=$(echo "$STRIPPED" | sed -E 's#^[^@]*@##')
      export SPRING_DATASOURCE_URL="jdbc:postgresql://${HOSTPART}"
      ;;
  esac
fi

exec java -jar app.jar
