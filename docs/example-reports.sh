#!/bin/bash
cd ..
java -jar jarhc/build/libs/jarhc-3.0.0-app.jar --options docs/example-report-asm-7.0-options.txt
java -jar jarhc/build/libs/jarhc-3.0.0-app.jar --options docs/example-report-asm-commons-7.0-options.txt
java -jar jarhc/build/libs/jarhc-3.0.0-app.jar --options docs/example-report-asm-7.0-provided-options.txt
