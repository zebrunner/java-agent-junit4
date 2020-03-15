package com.zebrunner.agent.junit.listener;

import com.nordstrom.automation.junit.AtomicTest;
import com.nordstrom.automation.junit.JUnitRetryAnalyzer;
import com.nordstrom.automation.junit.LifecycleHooks;
import com.nordstrom.automation.junit.MethodWatcher;
import com.nordstrom.automation.junit.RunWatcher;
import com.nordstrom.automation.junit.RunnerWatcher;
import com.nordstrom.automation.junit.ShutdownListener;
import com.nordstrom.automation.junit.TestObjectWatcher;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.runners.model.FrameworkMethod;

public class TestRunListenerV2 implements ShutdownListener, RunnerWatcher, TestObjectWatcher, RunWatcher<FrameworkMethod>, MethodWatcher<FrameworkMethod>, JUnitRetryAnalyzer {

    @Override
    public void beforeInvocation(Object runner, FrameworkMethod child, ReflectiveCallable callable) {
        System.out.println("before invocation");
        try {
            callable.run();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Override
    public void afterInvocation(Object runner, FrameworkMethod child, ReflectiveCallable callable, Throwable thrown) {
        System.out.println("after invocation");
    }

    @Override
    public void testStarted(AtomicTest<FrameworkMethod> atomicTest) {
        System.out.println("test started");
    }

    @Override
    public void testFinished(AtomicTest<FrameworkMethod> atomicTest) {
        System.out.println("test finished");
    }

    @Override
    public void testFailure(AtomicTest<FrameworkMethod> atomicTest, Throwable thrown) {
        System.out.println("test failure");
    }

    @Override
    public void testAssumptionFailure(AtomicTest<FrameworkMethod> atomicTest, AssumptionViolatedException thrown) {
        System.out.println("assumption failure");
    }

    @Override
    public void testIgnored(AtomicTest<FrameworkMethod> atomicTest) {
        System.out.println("ignored");
    }

    @Override
    public void runStarted(Object runner) {
        System.out.println("run started");
    }

    @Override
    public void runFinished(Object runner) {
        System.out.println("run finished");
    }

    @Override
    public void onShutdown() {
        System.out.println("on shutdown");
    }

    @Override
    public void testObjectCreated(Object testObj, Object runner) {
        System.out.println("test object created");
    }

    @Override
    public Class<FrameworkMethod> supportedType() {
        return FrameworkMethod.class;
    }

    @Override
    public boolean retry(FrameworkMethod method, Throwable thrown) {
        System.out.println("retry");
        return false;
    }
}
