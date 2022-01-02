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

GREEN="\033[0;32m"
RED="\033[0;31m"
NONE="\033[0m"

((passed = 0))
((failed = 0))
((errors = 0))

assertEquals() {
  local message=$1
  local expected=$2
  local actual=$3
  if [ "$actual" == "$expected" ]; then
    printf "${GREEN}PASSED${NONE} - %s\n" "$message"
    ((passed += 1))
  else
    printf "${RED}FAILED${NONE} - %s\n" "$message"
    printf "%s\n" "$(diff <(echo "$expected") <(echo "$actual"))"
    ((failed += 1))
  fi
}

error() {
  local message=$1
  printf "${RED}ERROR${NONE} - %s\n" "$message"
  ((errors += 1))
}

printTestSummary() {
  local total=$((passed + failed))
  if ((failed + errors == 0)); then
    printf "${GREEN}PASSED${NONE} - Total: %s, Passed: %s, Failed: %s, Errors: %s\n" "$total" "$passed" "$failed" "$errors"
    exit 0
  else
    printf "${RED}FAILED${NONE} - Total: %s, Passed: %s, Failed: %s, Errors: %s\n" "$total" "$passed" "$failed" "$errors"
    exit 1
  fi
}
