package com.zebrunner.agent.junit.listener;

import com.zebrunner.agent.junit.adapter.JUnitAdapter;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;

/**
 * Zebrunner Agent Listener implementation tracking JUnit test run events
 */
public class TestRunListener extends RunListener {

    private final JUnitAdapter adapter;
    private RunNotifier runNotifier;

    public TestRunListener() {
        this.adapter = new JUnitAdapter();
    }

    @Override
    public void testSuiteStarted(Description description) {
        adapter.registerRunStart(description);
    }

    @Override
    public void testSuiteFinished(Description description) {
        adapter.registerRunFinish(description);
    }

    @Override
    public void testStarted(Description description) {
        adapter.registerTestStart(description);
    }

    @Override
    public void testFinished(Description description) {
        adapter.registerTestFinish(description);
    }

    @Override
    public void testFailure(Failure failure) {
        adapter.registerTestFailure(failure);
    }

    @Override
    public void testIgnored(Description description) {
//        String ignoredReason = test.getAnnotation(Ignore.class).value();
//        System.out.println("Execution of test case ignored : " + test.getMethodName() + ". Reason: " + ignoredReason);
        // do nothing
    }

    @Override
    public void testRunStarted(Description description) {
//        System.out.println("Test run started and contains " + description.testCount() + " tests");
    }

    @Override
    public void testRunFinished(Result result) {
//        System.out.println("Test run finished. Total: " + result.getRunCount() + ". Failed: " + result.getFailureCount() + ". Ignored: " + result.getIgnoreCount());
    }

    public void setRunNotifier(RunNotifier runNotifier) {
        this.runNotifier = runNotifier;
    }
}
