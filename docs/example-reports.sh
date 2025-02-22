#!/bin/bash

# JarHC version
version=3.0.0-SNAPSHOT
jarhc=../jarhc/build/libs/jarhc-${version}-app.jar

# load environment variables
set -o allexport
source ../.env
set +o allexport

# use fixed timestamp to make reports reproducible
JDK_JAVA_OPTIONS="${JDK_JAVA_OPTIONS} -Djarhc.timestamp.override=1739992928000"

java -jar ${jarhc} --options example-report-asm-7.0-options.txt
java -jar ${jarhc} --options example-report-asm-commons-7.0-options.txt
java -jar ${jarhc} --options example-report-asm-7.0-provided-options.txt
java -jar ${jarhc} --options example-report-jakarta-ee-8-options.txt
java -jar ${jarhc} --options example-report-jakarta-ee-9-options.txt
java -jar ${jarhc} --options example-report-jakarta-ee-10-options.txt
java -jar ${jarhc} --options example-report-jarhc-1.7-options.txt
java -jar ${jarhc} --options example-report-jarhc-2.2.2-options.txt

java -jar ${jarhc} --diff                              \
     --input1 example-report-jarhc-1.7.json            \
     --input2 example-report-jarhc-2.2.2.json          \
     --output example-report-jarhc-diff-1.7-2.2.2.html \
     --title "JarHC 1.7 and 2.2.2"

java -jar ${jarhc} --diff                              \
     --input1 example-report-jakarta-ee-9.json         \
     --input2 example-report-jakarta-ee-10.json        \
     --output example-report-jakarta-ee-diff-9-10.html \
     --title "Jakarta EE 9 and 10"
