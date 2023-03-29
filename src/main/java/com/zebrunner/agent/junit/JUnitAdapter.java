package com.zebrunner.agent.junit;

import com.nordstrom.automation.junit.AtomicTest;
import com.zebrunner.agent.core.registrar.RunContextHolder;
import com.zebrunner.agent.core.registrar.TestRunRegistrar;
import com.zebrunner.agent.core.registrar.descriptor.Status;
import com.zebrunner.agent.core.registrar.descriptor.TestFinishDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestRunFinishDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestRunStartDescriptor;
import com.zebrunner.agent.core.registrar.descriptor.TestStartDescriptor;
import com.zebrunner.agent.junit.core.ArgumentsIndexResolver;
import com.zebrunner.agent.junit.core.RunContextService;
import com.zebrunner.agent.junit.core.TestCorrelationData;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.runner.Description;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Adapter used to convert JUnit test domain to Zebrunner Agent domain
 */
public class JUnitAdapter {

    private static final TestRunRegistrar registrar = TestRunRegistrar.getInstance();

    private static Description rootSuiteDescription;
    private static final Set<String> testIds = ConcurrentHashMap.newKeySet();

    public void registerRunStart(Description description) {
        if (rootSuiteDescription == null) {
            rootSuiteDescription = description;

            TestRunStartDescriptor testRunStartDescriptor = new TestRunStartDescriptor(
                    description.getDisplayName(), "junit4", OffsetDateTime.now(), null
            );

            registrar.registerStart(testRunStartDescriptor);
        }
    }

    public void registerRunFinish(Description description) {
        if (Objects.equals(rootSuiteDescription.getTestClass(), description.getTestClass())) {
            registrar.registerFinish(new TestRunFinishDescriptor(OffsetDateTime.now()));
        }
    }

    public void registerTestStart(AtomicTest test) {
        TestCorrelationData testCorrelationData = this.buildTestCorrelationData(test);
        TestStartDescriptor testStartDescriptor = new TestStartDescriptor(
                testCorrelationData.asJsonString(),
                test.getDescription().getDisplayName(),
                test.getIdentity().getDeclaringClass(),
                test.getIdentity().getMethod(),
                testCorrelationData.getArgumentsIndex()
        );

        if (RunContextHolder.isRerun()) {
            RunContextService.getZebrunnerIdOnRerun(test)
                             .ifPresent(testStartDescriptor::setZebrunnerId);
        }

        String currentTestId = testCorrelationData.toString();
        testIds.add(currentTestId);
        registrar.registerTestStart(currentTestId, testStartDescriptor);
    }

    public void registerTestFinish(AtomicTest test) {
        TestCorrelationData testCorrelationData = this.buildTestCorrelationData(test);
        String currentTestId = testCorrelationData.toString();

        if (testIds.contains(currentTestId)) {
            TestFinishDescriptor testFinishDescriptor = new TestFinishDescriptor(Status.PASSED);

            testIds.remove(currentTestId);
            registrar.registerTestFinish(testCorrelationData.toString(), testFinishDescriptor);
        }
    }

    public void registerTestFailure(AtomicTest test, Throwable thrown) {
        TestCorrelationData testCorrelationData = this.buildTestCorrelationData(test);
        String currentTestId = testCorrelationData.toString();
        TestFinishDescriptor result = new TestFinishDescriptor(Status.FAILED, OffsetDateTime.now(), ExceptionUtils.getStackTrace(thrown));

        testIds.remove(currentTestId);
        registrar.registerTestFinish(currentTestId, result);
    }

    public void registerTestSkipped(AtomicTest test, Throwable thrown) {
        TestCorrelationData testCorrelationData = this.buildTestCorrelationData(test);
        String currentTestId = testCorrelationData.toString();
        TestFinishDescriptor result = new TestFinishDescriptor(Status.SKIPPED, OffsetDateTime.now(), ExceptionUtils.getStackTrace(thrown));

        testIds.remove(currentTestId);
        registrar.registerTestFinish(currentTestId, result);
    }

    private TestCorrelationData buildTestCorrelationData(AtomicTest test) {
        return TestCorrelationData.builder()
                                  .className(test.getIdentity().getDeclaringClass().getName())
                                  .methodName(test.getIdentity().getMethod().getName())
                                  .parameterClassNames(
                                          Arrays.stream(test.getIdentity().getMethod().getParameterTypes())
                                                .map(Class::getName)
                                                .collect(Collectors.toList())
                                  )
                                  .argumentsIndex(ArgumentsIndexResolver.resolve(test.getDescription()))
                                  .displayName(test.getDescription().getDisplayName())
                                  .build();
    }

}
