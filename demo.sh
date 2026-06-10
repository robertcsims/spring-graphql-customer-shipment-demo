#!/usr/bin/env bash
# One-line launcher for Sr. Leadership demo (clean, no auth friction).
# Prefers the packaged executable jar (if present from `mvn package -DskipTests`)
# for faster startup and better resilience in constrained environments / agent harnesses.
# Falls back to mvn spring-boot:run for development before packaging.
set -e

# Make repeated starts reliable: if something is already listening on 8080 (common
# when harnesses or scripts re-invoke demo.sh), cleanly terminate it first.
if command -v lsof >/dev/null 2>&1; then
  PIDS=$(lsof -ti :8080 2>/dev/null || true)
  if [ -n "$PIDS" ]; then
    echo "demo.sh: killing previous listener(s) on 8080: $PIDS"
    kill -9 $PIDS 2>/dev/null || true
    sleep 2
  fi
elif command -v ss >/dev/null 2>&1; then
  # Fallback using ss (parse pid=NNNN)
  PIDS=$(ss -tlnp 2>/dev/null | awk '/:8080/ {gsub(/.*pid=/,"",$NF); gsub(/[),].*/,"",$NF); print $NF}' | sort -u | tr '\n' ' ')
  if [ -n "$PIDS" ]; then
    echo "demo.sh: killing previous listener(s) on 8080: $PIDS"
    kill -9 $PIDS 2>/dev/null || true
    sleep 2
  fi
fi

# Prefer the executable (fat) JAR produced by spring-boot-maven-plugin with <classifier>exec</classifier>.
# This is the one with the proper Main-Class manifest and embedded dependencies.
if [ -f target/customer-shipment-graphql-0.1.0-SNAPSHOT-exec.jar ]; then
  echo "demo.sh: launching executable JAR (fat jar with embedded deps)"
  exec java -jar target/customer-shipment-graphql-0.1.0-SNAPSHOT-exec.jar --spring.profiles.active=demo "$@"
elif [ -f target/customer-shipment-graphql-0.1.0-SNAPSHOT.jar ]; then
  echo "demo.sh: WARNING: only thin JAR found, may lack manifest. Prefer 'mvn package' output."
  exec java -jar target/customer-shipment-graphql-0.1.0-SNAPSHOT.jar --spring.profiles.active=demo "$@"
else
  echo "demo.sh: no packaged JAR found, falling back to mvn spring-boot:run"
  exec mvn spring-boot:run -Dspring-boot.run.profiles=demo "$@"
fi
