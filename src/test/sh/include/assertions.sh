GREEN="\033[0;32m"
RED="\033[0;31m"
NONE="\033[0m"

((passed = 0))
((failed = 0))

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

printTestSummary() {
  local total=$((passed + failed))
  if ((failed == 0)); then
    printf "${GREEN}PASSED${NONE} - Total: %s, Passed: %s, Failed: %s\n" "$total" "$passed" "$failed"
    exit 0
  else
    printf "${RED}PASSED${NONE} - Total: %s, Passed: %s, Failed: %s\n" "$total" "$passed" "$failed"
    exit 1
  fi
}
