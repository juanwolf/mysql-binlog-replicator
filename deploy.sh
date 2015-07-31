#!/bin/bash
VERSION=$( grep -oPm1 "(?<=<version>)[^-SNAPSHOT<]+" pom.xml | head -1)

TAG="mysql-binlog-replicator-$VERSION"

mvn  -B release:clean -Dtag=$TAG -DreleaseVersion=$VERSION release:prepare
mvn release:perform