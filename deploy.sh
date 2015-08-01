#!/bin/bash
VERSION=$( grep -oPm1 "(?<=<version>)[^-SNAPSHOT<]+" pom.xml | head -1)

TAG="mysql-binlog-replicator-$VERSION"

git config --global user.name "$GIT_NAME"
git config --global user.email "$GIT_EMAIL"

git checkout master

mvn -s ./.travis.settings.xml  --batch-mode -Dtag=$TAG -DreleaseVersion=$VERSION -Dgpg.passphrase=$GPG_PASSPHRASE release:prepare release:clean
mvn -s ./.travis.settings.xml release:perform -Dgpg.passphrase=$GPG_PASSPHRASE
