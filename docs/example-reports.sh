#!/bin/bash
cd ..
java -jar jarhc/build/libs/jarhc-2.2.0-with-deps.jar --options docs/example-report-asm-7.0-options.txt
java -jar jarhc/build/libs/jarhc-2.2.0-with-deps.jar --options docs/example-report-asm-commons-7.0-options.txt
java -jar jarhc/build/libs/jarhc-2.2.0-with-deps.jar --options docs/example-report-asm-7.0-provided-options.txt
