#!/bin/sh

# yarn run server:builddev
# yarn run server:server

#tail -f /dev/null
sbt -Dsbt.global.base=.sbt -Dsbt.boot.directory=.sbt -Dsbt.ivy.home=.ivy2 run
