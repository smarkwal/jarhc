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
