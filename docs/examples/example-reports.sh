#!/bin/bash

# JarHC version
version=3.0.1-SNAPSHOT
jarhc=$(realpath ../../jarhc/build/libs/jarhc-${version}-app.jar)

# load environment variables
set -o allexport
source ../../.env
set +o allexport

# use fixed timestamp to make reports reproducible
# and a fixed Java home to hide local installation details
JDK_JAVA_OPTIONS="${JDK_JAVA_OPTIONS} -Djarhc.timestamp.override=1739992928000 -Djarhc.javahome.override=/opt/java-17"

for report in asm-7.0 asm-commons-7.0 asm-7.0-provided \
             jarhc-1.7 jarhc-2.2.2 ; do

  cd "${report}"
  "$JAVA_HOME/bin/java" -jar "${jarhc}" --options options.txt
  cd ..

done

"$JAVA_HOME/bin/java" -jar "${jarhc}" --diff jarhc-1.7/report.json jarhc-2.2.2/report.html \
     --output jarhc-diff-1.7-2.2.2/report.html \
     --title "JarHC 1.7 and 2.2.2"
