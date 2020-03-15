package com.zebrunner.agent.junit.adapter;

import com.zebrunner.agent.core.registrar.Status;
import com.zebrunner.agent.core.registrar.TestFinishDescriptor;
import com.zebrunner.agent.core.registrar.TestRunFinishDescriptor;
import com.zebrunner.agent.core.registrar.TestRunRegistrar;
import com.zebrunner.agent.core.registrar.TestRunStartDescriptor;
import com.zebrunner.agent.core.registrar.TestStartDescriptor;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;

import java.lang.reflect.Method;
import java.time.OffsetDateTime;

/**
 * Adapter used to convert JUnit test domain to Zebrunner Agent domain
 */
public class JUnitAdapter {

    private static final TestRunRegistrar registrar = TestRunRegistrar.registrar();

    private String currentTestId;
    // static is required !
    private static Description rootSuiteDescription;

    public void registerRunStart(Description description) {
        if (rootSuiteDescription == null) {
            rootSuiteDescription = description;

            String name = description.getClassName();
            TestRunStartDescriptor testRunStartDescriptor = new TestRunStartDescriptor(name, "junit", OffsetDateTime.now(), name);

            registrar.start(testRunStartDescriptor);
        }
    }

    public void registerRunFinish(Description description) {
        if (rootSuiteDescription.getTestClass().equals(description.getTestClass())) {
            registrar.finish(new TestRunFinishDescriptor(OffsetDateTime.now()));
        }
    }

    public void registerTestStart(Description description) {
        OffsetDateTime startedAt = OffsetDateTime.now();
        Method method = retrieveTestMethod(description);

        TestStartDescriptor testStartDescriptor = new TestStartDescriptor(description.getDisplayName(), startedAt, description.getTestClass(), method);
        this.currentTestId = generateTestId();

        registrar.startTest(this.currentTestId, testStartDescriptor);
    }

    public void registerTestFinish(Description description) {
        OffsetDateTime endedAt = OffsetDateTime.now();
        TestFinishDescriptor testFinishDescriptor = new TestFinishDescriptor(Status.PASSED, endedAt);
        registrar.finishTest(this.currentTestId, testFinishDescriptor);
    }

    public void registerTestFailure(Failure failure) {
        OffsetDateTime endedAt = OffsetDateTime.now();
        TestFinishDescriptor result = new TestFinishDescriptor(Status.FAILED, endedAt, failure.getMessage());
        registrar.finishTest(this.currentTestId, result);
    }

    private String generateTestId() {
        return Long.toString(System.currentTimeMillis());
    }

    // TODO by nsidorevich on 2/27/20: ??? parametrized tests?
    private String generateTestId(Description description) {
        return "[" + description.getClassName() + "]/[" + description.getMethodName() + "]";
    }

    // TODO by nsidorevich on 2/28/20: what if we had overriden test method?
    private Method retrieveTestMethod(Description description) {
        try {
            return description.getTestClass().getDeclaredMethod(description.getMethodName());
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

}
