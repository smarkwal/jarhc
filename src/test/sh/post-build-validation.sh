#!/bin/bash

buildDir="../../../build"
libsDir="$buildDir/libs"

# get JarHC version from VERSION file in build dir
version=$(cat "$buildDir/resources/main/VERSION")

echo "Test JarHC $version"

# include helper functions
source include/assertions.sh

# for each Java home in java.txt
# shellcheck disable=SC2002
cat java.txt | while read -r javaHome; do

  printf "====================================================================\n"

  # print information about Java runtime
  info=$("$javaHome/bin/java" -cp app Version)
  printf "Java: %s\n" "$info"

  # delete JarHC data/cache directory
  # TODO: replace with option --data=temp
  rm -rf ~/.jarhc

  # prepare JarHC command
  JARHC="$javaHome/bin/java -jar $libsDir/jarhc-$version-with-deps.jar"

  # run JarHC --version
  actual=$($JARHC --version)
  expected="JarHC - JAR Health Check $version"
  assertEquals "JarHC --version" "$expected" "$actual"

  # run JarHC --help
  actual=$($JARHC --help)
  # echo "$actual" > help.txt
  expected=$(cat results/help.txt)
  assertEquals "JarHC --help" "$expected" "$actual"

  # run JarHC for ASM
  actual=$($JARHC "-s" "-jr" "org.ow2.asm:asm:9.2")
  # echo "$actual" > asm.txt
  expected=$(cat results/asm.txt)
  assertEquals "JarHC for ASM" "${expected//VERSION/$version}" "$actual"

done

printf "====================================================================\n"

# print test summary
printTestSummary
