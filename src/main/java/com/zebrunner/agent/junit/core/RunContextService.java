package com.zebrunner.agent.junit.core;

import com.nordstrom.automation.junit.AtomicTest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.junit.runners.model.FrameworkMethod;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RunContextService {

    private static final Map<TestCorrelationData, Long> TEST_CORRELATION_DATA_TO_TEST_ID = new ConcurrentHashMap<>();

    public static void addTestCorrelationData(TestCorrelationData testCorrelationData, Long testId) {
        if (testCorrelationData != null) {
            TEST_CORRELATION_DATA_TO_TEST_ID.putIfAbsent(testCorrelationData, testId);
        }
    }

    public static Optional<Long> getZebrunnerIdOnRerun(AtomicTest test) {
        int testArgumentsIndex = ArgumentsIndexResolver.resolve(test.getDescription());

        return TEST_CORRELATION_DATA_TO_TEST_ID.keySet()
                                               .stream()
                                               .filter(correlationData -> belongsToMethod(correlationData, test.getIdentity()))
                                               .filter(correlationData -> correlationData.getArgumentsIndex() == testArgumentsIndex)
                                               .map(TEST_CORRELATION_DATA_TO_TEST_ID::get)
                                               .findFirst();
    }

    public static boolean isEligibleForRerun(FrameworkMethod method, int argumentsIndex) {
        return TEST_CORRELATION_DATA_TO_TEST_ID.keySet()
                                               .stream()
                                               .anyMatch(correlationData -> belongsToMethod(correlationData, method) && correlationData.getArgumentsIndex() == argumentsIndex);
    }

    private static boolean belongsToMethod(TestCorrelationData testCorrelationData, FrameworkMethod method) {
        String contextParameters = String.join(", ", testCorrelationData.getParameterClassNames());
        String methodParameters = String.join(", ", getMethodParameterNames(method.getMethod()));

        return testCorrelationData.getClassName().equals(method.getDeclaringClass().getName())
               && testCorrelationData.getMethodName().equals(method.getMethod().getName())
               && contextParameters.equals(methodParameters);
    }

    private static List<String> getMethodParameterNames(Method method) {
        return Arrays.stream(method.getParameterTypes())
                     .map(Class::getName)
                     .collect(Collectors.toList());
    }

}
