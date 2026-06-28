#!/bin/bash

# Generate the example reports published under website/docs/examples/.
# This script lives in website/scripts/ but operates on the examples directory;
# it resolves all paths from the repository root, so it can be run from anywhere.
repo_root="$(git rev-parse --show-toplevel)"
cd "${repo_root}/website/docs/examples"

# JarHC version (read from gradle.properties)
version=$(sed -n -E 's/^version[[:space:]]*=[[:space:]]*(.+)$/\1/p' "${repo_root}/gradle.properties")
jarhc=$(realpath "${repo_root}/jarhc/build/libs/jarhc-${version}-app.jar")

# load environment variables
set -o allexport
source "${repo_root}/.env"
set +o allexport

# use fixed timestamp to make reports reproducible
# and a fixed Java home to hide local installation details
JDK_JAVA_OPTIONS="${JDK_JAVA_OPTIONS} -Djarhc.timestamp.override=1739992928000 -Djarhc.javahome.override=/opt/java-17"

for report in asm asm-commons asm-provided \
              jarhc-1.7 jarhc-2.2.2 ; do

  cd "${report}"
  "$JAVA_HOME/bin/java" -jar "${jarhc}" --options options.txt
  cd ..

done

"$JAVA_HOME/bin/java" -jar "${jarhc}" --diff jarhc-1.7/report.json jarhc-2.2.2/report.html \
     --output jarhc-diff-1.7-2.2.2/report.html \
     --title "JarHC 1.7 and 2.2.2"
