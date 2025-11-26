#!/usr/bin/env bash
set -euo pipefail

OUTPUT_FILE="flaky-tests-summary.json"

# Find the first junit5 retry report file
file="$(find . -path '*target/junit5-retry-report-*.txt' -print | head -n 1 || true)"

if [[ -z "${file:-}" ]]; then
  printf '%s\n' \
    '{"timestamp":null,"totalFlakyTests":0,"moduleCount":0,"modules":{},"tests":[]}' \
    > "${OUTPUT_FILE}"
  exit 0
fi

module="$(basename "$(dirname "$(dirname "${file}")")")"

jq --arg module "${module}" --arg file "${file}" '
  . as $tests
  | {
      timestamp: (now | todateiso8601),
      totalFlakyTests: ($tests | length),
      moduleCount: (if ($tests | length) > 0 then 1 else 0 end),
      modules: {
        ($module): {
          count: ($tests | length),
          avgExecutionCount:
            (if ($tests | length) == 0
             then 0
             else ($tests | map(.executionCount) | add / length)
             end),
          tests: ($tests | map(.testName))
        }
      },
      tests: ($tests | map(. + {module: $module, reportFile: $file}))
    }
' "${file}" > "${OUTPUT_FILE}"

