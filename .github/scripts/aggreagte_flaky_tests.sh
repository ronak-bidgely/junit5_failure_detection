#!/usr/bin/env bash
set -euo pipefail

OUTPUT_FILE="flaky-tests-summary.json"

# Find ALL junit5 retry report files (portable way)
files=()
while IFS= read -r -d '' file; do
  files+=("$file")
done < <(find . -path '*target/junit5-retry-report-*.txt' -print0 2>/dev/null || true)

if [[ ${#files[@]} -eq 0 ]]; then
  printf '%s\n' \
    '{"timestamp":null,"totalFlakyTests":0,"moduleCount":0,"modules":{},"tests":[]}' \
    > "${OUTPUT_FILE}"
  exit 0
fi

echo "Found ${#files[@]} flaky test report file(s):"
for f in "${files[@]}"; do
  echo "  - $f"
done

# Aggregate all reports into a single JSON structure
jq -n --argjson timestamp "\"$(date -u +"%Y-%m-%dT%H:%M:%SZ")\"" '
{
  timestamp: $timestamp,
  totalFlakyTests: 0,
  moduleCount: 0,
  modules: {},
  tests: []
}' > "${OUTPUT_FILE}.tmp"

# Process each report file
for file in "${files[@]}"; do
  module="$(basename "$(dirname "$(dirname "${file}")")")"

  # Read the report and add module/file info to each test
  jq --arg module "${module}" --arg file "${file}" '
    map(. + {module: $module, reportFile: $file})
  ' "${file}" > "${OUTPUT_FILE}.${module}.tmp"
done

# Merge all module reports into final summary
jq -s '
  # Flatten all test arrays from all modules
  [.[] | select(type == "array") | .[]] as $allTests |

  # Group tests by module
  ($allTests | group_by(.module)) as $groupedByModule |

  # Build module statistics
  ($groupedByModule | map({
    key: .[0].module,
    value: {
      count: length,
      avgExecutionCount: (map(.executionCount) | add / length),
      tests: map(.testName)
    }
  }) | from_entries) as $moduleStats |

  {
    timestamp: (now | todateiso8601),
    totalFlakyTests: ($allTests | length),
    moduleCount: ($moduleStats | keys | length),
    modules: $moduleStats,
    tests: $allTests
  }
' "${OUTPUT_FILE}".*.tmp > "${OUTPUT_FILE}"

# Clean up temporary files
rm -f "${OUTPUT_FILE}".*.tmp

echo ""
echo "✅ Aggregated flaky test summary written to: ${OUTPUT_FILE}"
echo "   Total flaky tests: $(jq '.totalFlakyTests' "${OUTPUT_FILE}")"
echo "   Modules affected: $(jq '.moduleCount' "${OUTPUT_FILE}")"

