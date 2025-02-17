#!/bin/bash

version=3.0.0-SNAPSHOT

cd ..
java -jar jarhc/build/libs/jarhc-${version}-app.jar --options docs/example-report-asm-7.0-options.txt
java -jar jarhc/build/libs/jarhc-${version}-app.jar --options docs/example-report-asm-commons-7.0-options.txt
java -jar jarhc/build/libs/jarhc-${version}-app.jar --options docs/example-report-asm-7.0-provided-options.txt
java -jar jarhc/build/libs/jarhc-${version}-app.jar --options docs/example-report-jakarta-ee-8-options.txt
java -jar jarhc/build/libs/jarhc-${version}-app.jar --options docs/example-report-jakarta-ee-9-options.txt
java -jar jarhc/build/libs/jarhc-${version}-app.jar --options docs/example-report-jakarta-ee-10-options.txt
java -jar jarhc/build/libs/jarhc-${version}-app.jar --options docs/example-report-jarhc-1.7-options.txt
java -jar jarhc/build/libs/jarhc-${version}-app.jar --options docs/example-report-jarhc-2.2.2-options.txt

java -jar jarhc/build/libs/jarhc-${version}-app.jar         \
     --diff                                                 \
     --input1 docs/example-report-jarhc-1.7.json            \
     --input2 docs/example-report-jarhc-2.2.2.json          \
     --output docs/example-report-jarhc-diff-1.7-2.2.2.html \
     --title "JarHC 1.7 and 2.2.2"

java -jar jarhc/build/libs/jarhc-${version}-app.jar         \
     --diff                                                 \
     --input1 docs/example-report-jakarta-ee-9.json         \
     --input2 docs/example-report-jakarta-ee-10.json        \
     --output docs/example-report-jakarta-ee-diff-9-10.html \
     --title "Jakarta EE 9 and 10"
