package com.example.ecommerce;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;

public class MyTestWatcher implements TestWatcher, TestExecutionListener {

  // Session-level timestamp to ensure same filename across multiple test plan executions
  private static final String SESSION_TIMESTAMP = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));

  @Override
  public void testSuccessful(ExtensionContext context) {
    String testIdentifier = getFullyQualifiedTestName(context);
    Data data = getTracker().computeIfAbsent(testIdentifier, key -> new Data());
    data.increment();
    data.setLastStatus("PASSED");
  }

  @Override
  public void testFailed(ExtensionContext context, Throwable cause) {
    String testIdentifier = getFullyQualifiedTestName(context);
    Data data = getTracker().computeIfAbsent(testIdentifier, key -> new Data());
    data.increment();
    data.setThrowable(cause);
    data.setLastStatus("FAILED");
  }

  private String getFullyQualifiedTestName(ExtensionContext context) {
    // Get the test class name
    String className = context.getTestClass()
        .map(Class::getName)
        .orElse("UnknownClass");

    // Get the test method name
    String methodName = context.getTestMethod()
        .map(Method::getName)
        .orElse("unknownMethod");

    // Get display name which includes parameter values for parameterized tests
    String displayName = context.getDisplayName();

    // For parameterized tests, use the display name which includes parameters
    // For regular tests, use the method name
    // Display name examples: "[1] flaky@example.com", "[2] valid@example.com", "methodName()"
    if (displayName != null && !displayName.equals(methodName + "()")) {
      // This is a parameterized test - use display name with parameters
      return className + "#" + methodName + displayName;
    }

    return className + "#" + methodName;
  }

  private Map<String, Data> getTracker() {
    return DataStore.instance.getTracker();
  }

  @Override
  public void testPlanExecutionFinished(TestPlan testPlan) {
    // Filter for flaky tests: count > 1 AND last status is PASSED
    List<Entry<String, Data>> flakyTests = getTracker().entrySet().stream()
            .filter(it -> it.getValue().getCount() > 1)
            .filter(it -> "PASSED".equals(it.getValue().getLastStatus()))
            .filter(it -> it.getValue().getThrowable() != null)
            .collect(Collectors.toList());

    if (!flakyTests.isEmpty()) {
      // Print to console for this module
      System.err.println("================================================================");
      System.out.println(">>>>>>>JUnit5: FLAKY TESTS DETECTED in <" + testPlan.toString() + ">");
      flakyTests.forEach(it -> {
        System.out.println("Test " + it.getKey() + " executed " + it.getValue().getCount() + " time(s) - Status: " + it.getValue().getLastStatus());
        if (it.getValue().getThrowable() != null) {
          StringWriter sw = new StringWriter();
          it.getValue().getThrowable().printStackTrace(new PrintWriter(sw));
          System.out.println("Last failure: " + sw);
        }
      });
      System.err.println("================================================================\n");

      // Write JSON report for this module
      writeModuleReport(flakyTests);
    }

    // DO NOT clear the tracker - we need it to persist across retry test plans
    // Each retry is a new test plan execution, so we need to accumulate counts
  }

  private void writeModuleReport(List<Entry<String, Data>> results) {
    // Use maven.build.timestamp if available, otherwise use a session-based timestamp
    String timestamp = System.getProperty("maven.build.timestamp");
    if (timestamp == null || timestamp.isEmpty()) {
      // Fallback to a session-based timestamp (same for entire JVM session)
      timestamp = getSessionTimestamp();
    }

    String filename = String.format("junit5-retry-report-%s.txt", timestamp);
    Path targetDir = Paths.get(System.getProperty("user.dir"), "target");
    Path reportFile = targetDir.resolve(filename);

    try {
      // Create target directory if it doesn't exist
      Files.createDirectories(targetDir);

      // Convert results to a list of TestReport objects
      List<TestReport> reports = new ArrayList<>();
      for (Entry<String, Data> entry : results) {
        String lastFailure = null;
        if (entry.getValue().getThrowable() != null) {
          StringWriter sw = new StringWriter();
          entry.getValue().getThrowable().printStackTrace(new PrintWriter(sw));
          lastFailure = sw.toString();
        }

        reports.add(new TestReport(
            entry.getKey(),
            entry.getValue().getCount(),
            entry.getValue().getLastStatus(),
            lastFailure
        ));
      }

      // Use Gson to generate JSON
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      String json = gson.toJson(reports);

      Files.write(reportFile, json.getBytes(),
          StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

      System.out.println("Test retry report written to: " + reportFile.toAbsolutePath());
      System.out.println("Total retried tests in this module: " + results.size());
    } catch (IOException e) {
      System.err.println("Failed to write test retry report: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private static String getSessionTimestamp() {
    return SESSION_TIMESTAMP;
  }

  // DTO class for JSON serialization
  private static class TestReport {
    private final String testName;
    private final int executionCount;
    private final String status;
    private final String lastFailure;

    public TestReport(String testName, int executionCount, String status, String lastFailure) {
      this.testName = testName;
      this.executionCount = executionCount;
      this.status = status;
      this.lastFailure = lastFailure;
    }

    public String getTestName() {
      return testName;
    }

    public int getExecutionCount() {
      return executionCount;
    }

    public String getStatus() {
      return status;
    }

    public String getLastFailure() {
      return lastFailure;
    }
  }

  public static class Data {

    private final AtomicInteger counter = new AtomicInteger(0);
    private Throwable throwable;
    private String lastStatus;

    public void increment() {
      counter.incrementAndGet();
    }

    public int getCount() {
      return counter.get();
    }

    public void setThrowable(Throwable throwable) {
      this.throwable = throwable;
    }

    public Throwable getThrowable() {
      return throwable;
    }

    public void setLastStatus(String status) {
      this.lastStatus = status;
    }

    public String getLastStatus() {
      return lastStatus;
    }
  }
}
