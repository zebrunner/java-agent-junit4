package com.zebrunner.agent.junit.runner;

import com.zebrunner.agent.junit.listener.TestRunListener;
import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.util.ArrayList;
import java.util.List;

public class Zebrunner extends BlockJUnit4ClassRunner {

    private final TestRunListener testRunListener;

    public Zebrunner(Class<?> testClass) throws InitializationError {
        super(testClass);
        this.testRunListener = new TestRunListener();
    }

    @Override
    protected List<TestRule> classRules() {
        return super.classRules();
    }

    @Override
    protected List<MethodRule> rules(Object target) {
        return super.rules(target);
    }

    @Override
    protected List<TestRule> getTestRules(Object target) {
        List<TestRule> testRules = super.getTestRules(target);
        testRules.add(new TestRule() {
            @Override
            public Statement apply(Statement base, Description description) {
                return new Statement() {
                    @Override
                    public void evaluate() throws Throwable {
                        description.getMethodName();
                        base.evaluate();
                    }
                };
            }
        });
        return testRules;
    }

    @Override
    public void run(RunNotifier notifier) {
        testRunListener.setRunNotifier(notifier);
        notifier.addListener(testRunListener);
        super.run(notifier);
    }

    @Override
    protected List<FrameworkMethod> getChildren() {
        List<FrameworkMethod> frameworkMethods = super.getChildren();
        return frameworkMethods;
    }

    @Override
    protected Description describeChild(FrameworkMethod method) {
        return super.describeChild(method);
    }

    @Override
    public void filter(Filter filter) throws NoTestsRemainException {
        super.filter(filter);
    }

    @Override
    protected Object createTest() throws Exception {
        return super.createTest();
    }

    @Override
    protected Object createTest(FrameworkMethod method) throws Exception {
        return super.createTest(method);
    }

    @Override
    protected boolean isIgnored(FrameworkMethod child) {
        return false;//child.getName().contains("test");
    }
}
