#!/bin/bash

#
# Copyright 2021 Stephan Markwalder
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# change to directory with this script
scriptDir=$(dirname "$0")
cd "$scriptDir" || exit 1

buildDir="../../../build"
libsDir="$buildDir/libs"

if [[ ! -d "$libsDir" ]]; then
  echo "Directory not found: $libsDir"
  echo "Run a full build with './gradlew clean build' first."
  exit 1
fi

# get JarHC version from VERSION file in build dir
version=$(cat "$buildDir/resources/main/VERSION")

echo "Test JarHC $version"

# include helper functions
source include/assertions.sh

# for each Java home in java.txt
while read -r javaHome; do

  printf "====================================================================\n"

  if [[ ! -d "$javaHome" ]]; then
    error "Java home not found: $javaHome"
    continue
  fi

  # print information about Java runtime
  info=$("$javaHome/bin/java" -cp app Version)
  printf "Java: %s\n" "$info"

  # delete JarHC data/cache directory
  # TODO: replace with option --data=temp
  rm -rf ~/.jarhc

  # prepare JarHC command
  jarFile="$libsDir/jarhc-$version-with-deps.jar"
  JARHC="$javaHome/bin/java -jar $jarFile"

  # run JarHC --version
  actual=$($JARHC --version)
  expected="JarHC - JAR Health Check $version"
  assertEquals "JarHC --version" "$expected" "$actual"

  # run JarHC --help
  actual=$($JARHC --help)
  # echo "$actual" > results/help.txt
  expected=$(cat results/help.txt)
  assertEquals "JarHC --help" "$expected" "$actual"

  # redefine JarHC command with version override
  JARHC="$javaHome/bin/java -Djarhc.version.override=0.0.1 -jar $jarFile"

  # run JarHC for ASM
  actual=$($JARHC "-s" "-jr" "org.ow2.asm:asm:9.2")
  # echo "$actual" > results/asm.txt
  expected=$(cat results/asm.txt)
  assertEquals "JarHC for ASM" "$expected" "$actual"

done <java.txt

printf "====================================================================\n"

# print test summary
printTestSummary
