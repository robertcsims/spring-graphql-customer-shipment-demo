#!/usr/bin/env bash
# One-line launcher for Sr. Leadership demo (clean, no auth friction)
exec mvn spring-boot:run -Dspring-boot.run.profiles=demo "$@"
