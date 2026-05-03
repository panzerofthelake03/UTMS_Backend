#!/bin/sh
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

# Apache Maven Wrapper startup script, version 3.3.2
#
# Required ENV vars:
#   JAVA_HOME - location of a JDK home dir
#
# Optional ENV vars
#   MAVEN_OPTS - parameters passed to the Java VM when running Maven
#   MAVEN_SKIP_RC - flag to disable loading of mavenrc files

if [ -z "$MAVEN_SKIP_RC" ]; then
  if [ -f /usr/local/etc/mavenrc ]; then
    . /usr/local/etc/mavenrc
  fi
  if [ -f /etc/mavenrc ]; then
    . /etc/mavenrc
  fi
  if [ -f "$HOME/.mavenrc" ]; then
    . "$HOME/.mavenrc"
  fi
fi

# OS specific support
cygwin=false
mingw=false
case "$(uname)" in
  CYGWIN*) cygwin=true ;;
  MINGW*) mingw=true ;;
esac

if [ -z "$JAVA_HOME" ]; then
  javaExecutable="$(which javac)"
  if [ -n "$javaExecutable" ] && ! [ "$(expr "$javaExecutable" : '\([^ ]*\)')" = "no" ]; then
    javaHome="$(dirname "$javaExecutable")"
    javaHome=$(expr "$javaHome" : '\(.*\)/bin')
    JAVA_HOME="$javaHome"
  fi
fi

if [ ! -x "$JAVA_HOME/bin/java" ]; then
  echo "Error: JAVA_HOME is not defined correctly." >&2
  echo "  We cannot execute $JAVA_HOME/bin/java" >&2
  exit 1
fi

find_maven_basedir() {
  if [ -z "$1" ]; then
    echo "Path not specified to find_maven_basedir" >&2
    return 1
  fi
  basedir="$1"
  wdir="$1"
  while [ "$wdir" != '/' ]; do
    if [ -d "$wdir"/.mvn ]; then
      basedir=$wdir
      break
    fi
    wdir=$(cd "$wdir/.." || exit 1; pwd)
  done
  printf '%s' "$(cd "$basedir" || exit 1; pwd)"
}

BASE_DIR=$(find_maven_basedir "$(dirname "$0")")
if [ -z "$BASE_DIR" ]; then
  exit 1
fi

MAVEN_PROJECTBASEDIR=${MAVEN_BASEDIR:-"$BASE_DIR"}
export MAVEN_PROJECTBASEDIR

wrapperJarPath="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
if [ -r "$wrapperJarPath" ]; then
  echo "Found $wrapperJarPath"
else
  echo "Couldn't find $wrapperJarPath, downloading it ..."
  wrapperUrl="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar"
  while IFS="=" read -r key value; do
    case "$key" in wrapperUrl) wrapperUrl="$value" ; break ;;
    esac
  done < "$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"
  echo "Downloading from: $wrapperUrl"
  if command -v wget > /dev/null; then
    wget "$wrapperUrl" -O "$wrapperJarPath" || exit 1
  elif command -v curl > /dev/null; then
    curl -o "$wrapperJarPath" "$wrapperUrl" || exit 1
  fi
fi

MAVEN_OPTS="$(cat "$MAVEN_PROJECTBASEDIR/.mvn/jvm.config" 2>/dev/null) $MAVEN_OPTS"

MAVEN_CMD_LINE_ARGS="$MAVEN_CONFIG $@"
export MAVEN_CMD_LINE_ARGS

exec "$JAVA_HOME/bin/java" \
  $MAVEN_OPTS \
  -classpath "$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar" \
  "-Dmaven.multiModuleProjectDirectory=${MAVEN_PROJECTBASEDIR}" \
  org.apache.maven.wrapper.MavenWrapperMain "$@"
