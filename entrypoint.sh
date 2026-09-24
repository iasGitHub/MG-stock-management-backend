#!/bin/sh
# Railway expose DATABASE_URL au format postgres://user:pass@host:port/db?params.
# L'application attend DB_URL au format JDBC (jdbc:postgresql://...) avec
# DB_USERNAME/DB_PASSWORD. Conversion automatique si DB_URL n'est pas fourni
# explicitement (les valeurs definies manuellement passent telles quelles).
set -eu

if [ -n "${DATABASE_URL:-}" ] && [ -z "${DB_URL:-}" ]; then
  rest="${DATABASE_URL#*://}"
  userinfo="${rest%%@*}"
  hostpart="${rest#*@}"
  hostport="${hostpart%%/*}"
  dbquery="${hostpart#*/}"
  db="${dbquery%%\?*}"
  query=""
  case "$dbquery" in
    *\?*) query="?${dbquery#*\?}" ;;
  esac
  user="${userinfo%%:*}"
  pass="${userinfo#*:}"
  host="${hostport%%:*}"
  port="${hostport##*:}"
  if [ "$port" = "$hostport" ]; then
    port=5432
  fi
  DB_URL="jdbc:postgresql://${host}:${port}/${db}${query}"
  DB_USERNAME="$user"
  DB_PASSWORD="$pass"
  export DB_URL DB_USERNAME DB_PASSWORD
  echo "DATABASE_URL converti -> jdbc:postgresql://${host}:${port}/${db}"
fi

exec java -jar /app/app.jar
