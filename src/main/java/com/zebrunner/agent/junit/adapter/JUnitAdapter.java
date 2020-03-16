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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        TestStartDescriptor testStartDescriptor = new TestStartDescriptor(description.getDisplayName(), description.getDisplayName(), startedAt, description.getTestClass(), method);
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

    public void registerTestFailure(Failure failure) {
        OffsetDateTime endedAt = OffsetDateTime.now();
        TestFinishDescriptor result = new TestFinishDescriptor(Status.FAILED, endedAt, failure.getMessage());
        String currentTestId = generateTestId(failure.getDescription());
        testsInExecution.remove(currentTestId);
        registrar.finishTest(currentTestId, result);
    }

    // TODO by nsidorevich on 2/27/20: ??? parametrized tests?
    private String generateTestId(Description description) {
        return description.getDisplayName();
    }

    // TODO by nsidorevich on 2/28/20: what if we had overriden test method?
    private Method retrieveTestMethod(Description description) {
        try {
            String methodName = description.getMethodName();
            String simpleMethodName = retrieveMethodNameFromNameWithInstance(methodName);
            return description.getTestClass().getDeclaredMethod(simpleMethodName);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private String retrieveMethodNameFromNameWithInstance(String nameWithInstance) {
        Matcher matcher = Pattern.compile("([\\s\\S]*)\\[(.*)\\]").matcher(nameWithInstance);
        return matcher.matches() ? matcher.group(1) : nameWithInstance;
    }

}
