package com.zebrunner.agent.junit;

import com.nordstrom.automation.junit.AtomicTest;
import com.nordstrom.automation.junit.JUnitRetryAnalyzer;
import com.nordstrom.automation.junit.MethodWatcher;
import com.nordstrom.automation.junit.RunWatcher;
import com.nordstrom.automation.junit.RunnerWatcher;
import com.nordstrom.automation.junit.ShutdownListener;
import com.nordstrom.automation.junit.TestObjectWatcher;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.runner.Describable;
import org.junit.runner.Description;
import org.junit.runners.model.FrameworkMethod;

/**
 * Zebrunner Agent Listener implementation tracking JUnit test run events
 */
public class TestRunListener implements ShutdownListener, RunnerWatcher, TestObjectWatcher, RunWatcher<FrameworkMethod>, MethodWatcher<FrameworkMethod>, JUnitRetryAnalyzer {

    private final JUnitAdapter adapter;

    public TestRunListener() {
        this.adapter = new JUnitAdapter();
    }

    @Override
    public void runStarted(Object runner) {
        Describable describable = (Describable) runner;
        Description description = describable.getDescription();
        adapter.registerRunStart(description);
    }

    @Override
    public void runFinished(Object runner) {
        Describable describable = (Describable) runner;
        Description description = describable.getDescription();
        adapter.registerRunFinish(description);
    }

    @Override
    public void onShutdown() {
        //System.out.println("on shutdown");
    }

    @Override
    public void testStarted(AtomicTest<FrameworkMethod> atomicTest) {
        Description description = atomicTest.getDescription();
        FrameworkMethod frameworkMethod = atomicTest.getIdentity();
        adapter.registerTestStart(description, frameworkMethod);
    }

    @Override
    public void testFinished(AtomicTest<FrameworkMethod> atomicTest) {
        Description description = atomicTest.getDescription();
        adapter.registerTestFinish(description);
    }

    @Override
    public void testFailure(AtomicTest<FrameworkMethod> atomicTest, Throwable thrown) {
        Description description = atomicTest.getDescription();
        Throwable error = atomicTest.getThrowable();
        adapter.registerTestFailure(description, error.getMessage());
    }

    @Override
    public void testAssumptionFailure(AtomicTest<FrameworkMethod> atomicTest, AssumptionViolatedException thrown) {
        //System.out.println("assumption failure");
    }

    @Override
    public void testIgnored(AtomicTest<FrameworkMethod> atomicTest) {
        //System.out.println("ignored");
    }

    @Override
    public void beforeInvocation(Object runner, FrameworkMethod child, ReflectiveCallable callable) {
        //System.out.println();
    }

    @Override
    public void afterInvocation(Object runner, FrameworkMethod child, ReflectiveCallable callable, Throwable thrown) {
        //System.out.println("after invocation");
    }

    @Override
    public void testObjectCreated(Object testObj, Object runner) {
        //System.out.println("test object created");
    }

    @Override
    public Class<FrameworkMethod> supportedType() {
        return FrameworkMethod.class;
    }

    @Override
    public boolean retry(FrameworkMethod method, Throwable thrown) {
        return false;
    }

}
