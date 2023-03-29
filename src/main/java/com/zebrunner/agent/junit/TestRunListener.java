package com.zebrunner.agent.junit;

import com.nordstrom.automation.junit.AtomicTest;
import com.nordstrom.automation.junit.RunWatcher;
import com.nordstrom.automation.junit.RunnerWatcher;
import lombok.extern.slf4j.Slf4j;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.Describable;

/**
 * Zebrunner Agent Listener implementation tracking JUnit test run events
 */
@Slf4j
public class TestRunListener implements RunnerWatcher, RunWatcher {

    private final JUnitAdapter adapter;

    public TestRunListener() {
        this.adapter = new JUnitAdapter();
    }

    @Override
    public void runStarted(Object runner) {
        log.debug("Beginning TestRunListener -> runStarted(Object runner)");

        Describable describable = (Describable) runner;
        adapter.registerRunStart(describable.getDescription());

        log.debug("Finishing TestRunListener -> runStarted(Object runner)");
    }

    @Override
    public void runFinished(Object runner) {
        log.debug("Beginning TestRunListener -> runFinished(Object runner)");

        Describable describable = (Describable) runner;
        adapter.registerRunFinish(describable.getDescription());

        log.debug("Finishing TestRunListener -> runFinished(Object runner)");
    }

    @Override
    public void testStarted(AtomicTest atomicTest) {
        log.debug("Beginning TestRunListener -> testStarted(AtomicTest atomicTest)");

        adapter.registerTestStart(atomicTest);

        log.debug("Finishing TestRunListener -> testStarted(AtomicTest atomicTest)");
    }

    @Override
    public void testFinished(AtomicTest atomicTest) {
        log.debug("Beginning TestRunListener -> testFinished(AtomicTest atomicTest)");

        adapter.registerTestFinish(atomicTest);

        log.debug("Finishing TestRunListener -> testFinished(AtomicTest atomicTest)");
    }

    @Override
    public void testFailure(AtomicTest atomicTest, Throwable thrown) {
        log.debug("Beginning TestRunListener -> testFailure(AtomicTest atomicTest, Throwable thrown)");

        adapter.registerTestFailure(atomicTest, thrown);

        log.debug("Finishing TestRunListener -> testFailure(AtomicTest atomicTest, Throwable thrown)");
    }

    @Override
    public void testAssumptionFailure(AtomicTest atomicTest, AssumptionViolatedException thrown) {
        log.debug("Beginning TestRunListener -> testAssumptionFailure(AtomicTest atomicTest, AssumptionViolatedException thrown)");

        adapter.registerTestSkipped(atomicTest, thrown);

        log.debug("Finishing TestRunListener -> testAssumptionFailure(AtomicTest atomicTest, AssumptionViolatedException thrown)");
    }

    @Override
    public void testIgnored(AtomicTest atomicTest) {
        log.debug("Beginning TestRunListener -> testIgnored(AtomicTest atomicTest)");
        log.debug("Finishing TestRunListener -> testIgnored(AtomicTest atomicTest)");
    }

}
