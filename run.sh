#!/bin/bash

# load environment variables
set -o allexport
source .env
set +o allexport

usage()
{
  echo "Usage: $0 <command>"
  echo "Commands:"
  echo "  generate-reports : Build application and generate example reports."
  echo "  prepare-commit   : Build application, run tests, update test resources, and generate example reports."
  exit 1
}

# check if any arguments are passed
if [ "$#" -eq 0 ]; then
  usage
fi

# check if command is valid
if( [ "$1" != "generate-reports" ] && [ "$1" != "prepare-commit" ] ); then
  echo "Invalid command: $1"
  usage
fi

# check if JAVA_HOME is set
if [[ -z "${JAVA_HOME}" ]]; then
  echo "JAVA_HOME is not set."
  exit 2
fi

# build application
./gradlew clean :jarhc:build -Pskip.tests

if [ $? -ne 0 ]; then
  echo "Build failed."
  exit 3
fi

if( [ "$1" == "prepare-commit" ] ); then

  # run tests
  ./gradlew :jarhc:test -Pjarhc.test.resources.generate

  if [ $? -ne 0 ]; then
    echo "Tests failed."
    open jarhc/build/reports/tests/test/index.html
    exit 4
  fi

  # run release tests
  ./gradlew :jarhc-release-tests:test -Pjarhc.test.resources.generate

  if [ $? -ne 0 ]; then
    echo "Release tests failed."
    open jarhc-release-tests/build/reports/tests/test/index.html
    exit 5
  fi

fi

# generate example reports
cd docs/wiki/reports
./example-reports.sh
