#!/bin/bash
VERSION=$( grep -oPm1 "(?<=<version>)[^-SNAPSHOT<]+" pom.xml | head -1)

TAG="mysql-binlog-replicator-$VERSION"

git config --global user.name "Jean-Loup Adde [Sent by Travis]"
git config --global user.email "jean-loup.adde@juanwolf.fr"

mvn  -B release:clean -Dtag=$TAG -DreleaseVersion=$VERSION release:prepare
mvn release:perform