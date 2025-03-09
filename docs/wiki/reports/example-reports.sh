#!/bin/bash

# JarHC version
version=3.0.0-SNAPSHOT
jarhc=`realpath ../../../jarhc/build/libs/jarhc-${version}-app.jar`

# load environment variables
set -o allexport
source ../../../.env
set +o allexport

# use fixed timestamp to make reports reproducible
JDK_JAVA_OPTIONS="${JDK_JAVA_OPTIONS} -Djarhc.timestamp.override=1739992928000"

for report in asm-7.0 asm-commons-7.0 asm-7.0-provided \
             jakarta-ee-8 jakarta-ee-9 jakarta-ee-10 \
             jarhc-1.7 jarhc-2.2.2 ; do

  cd ${report}
  java -jar ${jarhc} --options options.txt
  cd ..

done

java -jar ${jarhc} --diff                      \
     --input1 jarhc-1.7/report.json            \
     --input2 jarhc-2.2.2/report.html          \
     --output jarhc-diff-1.7-2.2.2/report.html \
     --title "JarHC 1.7 and 2.2.2"

java -jar ${jarhc} --diff                      \
     --input1 jakarta-ee-9/report.html         \
     --input2 jakarta-ee-10/report.json        \
     --output jakarta-ee-diff-9-10/report.html \
     --title "Jakarta EE 9 and 10"
