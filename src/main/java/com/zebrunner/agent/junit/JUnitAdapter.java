package com.zebrunner.agent.junit;

import com.zebrunner.agent.core.registrar.Status;
import com.zebrunner.agent.core.registrar.TestFinishDescriptor;
import com.zebrunner.agent.core.registrar.TestRunFinishDescriptor;
import com.zebrunner.agent.core.registrar.TestRunRegistrar;
import com.zebrunner.agent.core.registrar.TestRunStartDescriptor;
import com.zebrunner.agent.core.registrar.TestStartDescriptor;
import org.junit.runner.Description;
import org.junit.runners.model.FrameworkMethod;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Adapter used to convert JUnit test domain to Zebrunner Agent domain
 */
public class JUnitAdapter {

    private static final TestRunRegistrar registrar = TestRunRegistrar.registrar();

    // static is required !
    private static Description rootSuiteDescription;
    private static List<String> testsInExecution = Collections.synchronizedList(new ArrayList<>());

    public void registerRunStart(Description description) {
        if (rootSuiteDescription == null) {
            rootSuiteDescription = description;

            String name = description.getClassName();
            TestRunStartDescriptor testRunStartDescriptor = new TestRunStartDescriptor(
                    name,
                    "junit",
                    OffsetDateTime.now(),
                    name
            );

            registrar.start(testRunStartDescriptor);
        }
    }

    public void registerRunFinish(Description description) {
        if (Objects.equals(rootSuiteDescription.getTestClass(), description.getTestClass())) {
            registrar.finish(new TestRunFinishDescriptor(OffsetDateTime.now()));
        }
    }

    public void registerTestStart(Description description, FrameworkMethod frameworkMethod) {
        TestStartDescriptor testStartDescriptor = new TestStartDescriptor(
                String.valueOf(description.getDisplayName()),
                String.valueOf(description.getDisplayName()),
                OffsetDateTime.now(),
                description.getTestClass(),
                frameworkMethod.getMethod()
        );
        String currentTestId = generateTestId(description);
        testsInExecution.add(currentTestId);
        registrar.startTest(currentTestId, testStartDescriptor);
    }

    public void registerTestFinish(Description description) {
        String currentTestId = generateTestId(description);
        if (testsInExecution.contains(currentTestId)) {
            OffsetDateTime endedAt = OffsetDateTime.now();
            TestFinishDescriptor testFinishDescriptor = new TestFinishDescriptor(Status.PASSED, endedAt);
            testsInExecution.remove(currentTestId);
            registrar.finishTest(currentTestId, testFinishDescriptor);
        }
    }

    public void registerTestFailure(Description description, String failureMessage) {
        OffsetDateTime endedAt = OffsetDateTime.now();
        TestFinishDescriptor result = new TestFinishDescriptor(Status.FAILED, endedAt, failureMessage);
        String currentTestId = generateTestId(description);
        testsInExecution.remove(currentTestId);
        registrar.finishTest(currentTestId, result);
    }

    // TODO by nsidorevich on 2/27/20: ??? parametrized tests?
    private String generateTestId(Description description) {
        return description.getDisplayName();
    }

}
