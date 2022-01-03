#!/bin/bash

#
# Copyright 2022 Stephan Markwalder
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

# include helper functions
source include/utils.sh
source include/assertions.sh

# get project directories
projectDir=$(get_abs_filename "../../../")
buildDir="$projectDir/build"
libsDir="$buildDir/libs"
resultsDir="$projectDir/src/test/sh/results"

if [[ ! -d "$libsDir" ]]; then
  echo "Directory not found: $libsDir"
  echo "Run a full build with './gradlew clean build' first."
  exit 1
fi

# get JarHC version from VERSION file in build dir
version=$(cat "$buildDir/resources/main/VERSION")

echo "Test JarHC $version"

# for each Java version
for javaVersion in 8 11 17; do

  # crete Docker image name
  image="eclipse-temurin:$javaVersion-jre"

  printf "====================================================================\n"
  printf "Java: %s | %s\n\n" "$javaVersion" "$image"

  # prepare Docker run command
  DOCKER="docker run --rm -v $libsDir/jarhc-$version-with-deps.jar:/jarhc/jarhc.jar -v $resultsDir:/jarhc/results -e JARHC_DATA=/jarhc/data -w /jarhc $image"

  # prepare JarHC command
  JARHC="$DOCKER java -Djarhc.version.override=0.0.1 -jar jarhc.jar"

  # run JarHC on JarHC
  $JARHC jarhc.jar --output "results/jarhc-$javaVersion.txt"

done

printf "====================================================================\n"
