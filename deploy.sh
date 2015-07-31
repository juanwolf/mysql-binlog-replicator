#!/bin/bash
VERSION=$( grep -oPm1 "(?<=<version>)[^-SNAPSHOT<]+" pom.xml | head -1)

TAG="mysql-binlog-replicator-$VERSION"

git config --global user.name "$GIT_NAME"
git config --global user.email "$GIT_EMAIL"

mvn -s ./.travis.settings.xml  --batch-mode -Dtag=$TAG -DreleaseVersion=$VERSION release:prepare release:clean
mvn -s ./.travis.settings.xml release:perform