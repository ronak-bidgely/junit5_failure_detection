#!/usr/bin/env python3
"""
Aggregate flaky test reports from all modules into a single summary.
Reads all junit5-retry-report-*.txt files and combines them.
"""

import json
import sys
from pathlib import Path
from datetime import datetime
from collections import defaultdict

def find_flaky_reports(root_dir="."):
    """Find all flaky test report files."""
    return list(Path(root_dir).rglob("junit5-retry-report-*.txt"))

def load_report(report_path):
    """Load a single flaky test report JSON file."""
    try:
        with open(report_path, 'r') as f:
            data = json.load(f)
            # Add module information
            module = report_path.parent.parent.name
            for test in data:
                test['module'] = module
                test['reportFile'] = str(report_path)
            return data
    except Exception as e:
        print(f"Warning: Failed to load {report_path}: {e}", file=sys.stderr)
        return []

def aggregate_reports(report_files):
    """Aggregate all flaky test reports."""
    all_tests = []
    for report_file in report_files:
        tests = load_report(report_file)
        all_tests.extend(tests)
    return all_tests

def generate_summary(flaky_tests):
    """Generate a summary of flaky tests."""
    if not flaky_tests:
        return {
            'totalFlakyTests': 0,
            'modules': {},
            'tests': []
        }
    
    # Group by module
    by_module = defaultdict(list)
    for test in flaky_tests:
        by_module[test['module']].append(test)
    
    # Calculate statistics
    module_stats = {}
    for module, tests in by_module.items():
        module_stats[module] = {
            'count': len(tests),
            'avgExecutionCount': sum(t['executionCount'] for t in tests) / len(tests),
            'tests': [t['testName'] for t in tests]
        }
    
    return {
        'timestamp': datetime.now().isoformat(),
        'totalFlakyTests': len(flaky_tests),
        'moduleCount': len(by_module),
        'modules': module_stats,
        'tests': flaky_tests
    }

def main():
    print("Searching for flaky test reports...")
    report_files = find_flaky_reports()
    
    if not report_files:
        print("✅ No flaky test reports found - no flaky tests detected!")
        # Write empty summary
        summary = generate_summary([])
        with open('flaky-tests-summary.json', 'w') as f:
            json.dump(summary, f, indent=2)
        return 0
    
    print(f"Found {len(report_files)} report file(s):")
    for rf in report_files:
        print(f"  - {rf}")
    
    print("\nAggregating reports...")
    flaky_tests = aggregate_reports(report_files)
    
    print(f"\n{'='*60}")
    print(f"FLAKY TEST SUMMARY")
    print(f"{'='*60}")
    print(f"Total flaky tests: {len(flaky_tests)}")
    
    if flaky_tests:
        by_module = defaultdict(list)
        for test in flaky_tests:
            by_module[test['module']].append(test)
        
        print(f"\nBy module:")
        for module, tests in sorted(by_module.items()):
            print(f"  {module}: {len(tests)} flaky test(s)")
            for test in tests:
                print(f"    - {test['testName']} (ran {test['executionCount']} times)")
    
    # Write aggregated summary
    summary = generate_summary(flaky_tests)
    output_file = 'flaky-tests-summary.json'
    with open(output_file, 'w') as f:
        json.dump(summary, f, indent=2)
    
    print(f"\n✅ Aggregated summary written to: {output_file}")
    
    # Exit with error code if flaky tests found (optional - can be used to fail CI)
    # return 1 if flaky_tests else 0
    return 0

if __name__ == '__main__':
    sys.exit(main())

