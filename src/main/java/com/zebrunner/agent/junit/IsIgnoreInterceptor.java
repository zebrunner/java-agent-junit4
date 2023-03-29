package com.zebrunner.agent.junit;

import com.zebrunner.agent.core.listener.RerunListener;
import com.zebrunner.agent.core.registrar.domain.TestDTO;
import com.zebrunner.agent.junit.core.ArgumentsIndexResolver;
import com.zebrunner.agent.junit.core.RunContextService;
import com.zebrunner.agent.junit.core.TestCorrelationData;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;
import org.junit.runner.Description;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

@Slf4j
public class IsIgnoreInterceptor implements RerunListener {

    @Override
    public void onRerun(List<TestDTO> tests) {
        for (TestDTO test : tests) {
            TestCorrelationData testCorrelationData = TestCorrelationData.fromJsonString(test.getCorrelationData());

            RunContextService.addTestCorrelationData(testCorrelationData, test.getId());
        }
    }

    // BlockJUnit4ClassRunner -> protected boolean isIgnored(FrameworkMethod child)
    @RuntimeType
    public static Boolean isIgnore(@This final BlockJUnit4ClassRunner runner,
                                   @SuperCall final Callable<Boolean> proxy,
                                   @Argument(0) FrameworkMethod child) throws Exception {
        boolean isIgnored = proxy.call();
        if (isIgnored) {
            return true;
        }

        // BlockJUnit4ClassRunner -> protected Description describeChild(FrameworkMethod method)
        Description testDescription = invokeMethod(runner, "describeChild", child);
        if (testDescription == null) {
            log.warn(
                    "Unable to build test description from method: {}.\n" +
                    "Check that provided version of JUnit is compatible with Zebrunner agent.\n" +
                    "Test will be automatically ignored for rerun.",
                    child
            );
            return true;
        }

        int argumentsIndex = ArgumentsIndexResolver.resolve(testDescription);
        return !RunContextService.isEligibleForRerun(child, argumentsIndex);
    }

    /**
     * Invoked method by name from class or superclass hierarchy (if method is not exists).
     */
    private static <R> R invokeMethod(Object fromInstance, String methodName, Object... parameters) {
        R result = null;
        Method method = findMethodByName(fromInstance.getClass(), methodName, parameters);
        if (method != null) {
            result = invokeMethod(fromInstance, method, parameters);
        }
        return result;
    }

    private static <R> R invokeMethod(Object target, Method method, Object... parameters) {
        R result = null;
        try {
            method.setAccessible(true);
            result = (R) method.invoke(target, parameters);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * find declared method by name in class and superclasses.
     */
    private static Method findMethodByName(Class<?> klass, String methodName, Object... parameterTypes) {
        Class<?>[] parameterTypeClasses = Arrays.stream(parameterTypes)
                                                .map(Object::getClass)
                                                .toArray(Class[]::new);
        Method method;
        method = findDeclaredMethod(klass, methodName, parameterTypeClasses);
        while (method == null && klass.getSuperclass() != null) {
            klass = klass.getSuperclass();
            method = findDeclaredMethod(klass, methodName, parameterTypeClasses);
        }
        return method;
    }

    /**
     * Find declared method by name in class.
     */
    private static Method findDeclaredMethod(Class<?> klass, String methodName, Class<?>... parameterTypes) {
        Method[] methods = klass.getDeclaredMethods();
        return Arrays.stream(methods)
                     .filter(m -> m.getName().equals(methodName) && parametersFromTypes(m, parameterTypes))
                     .findFirst()
                     .orElse(null);
    }

    private static boolean parametersFromTypes(Method methodWithParameters, Class<?>... parametersToCompare) {
        for (int i = 0; i < methodWithParameters.getParameters().length; i++) {
            Class<?> parameterType = methodWithParameters.getParameters()[i].getType();
            if (!parameterType.isAssignableFrom(parametersToCompare[i])) {
                return false;
            }
        }
        return true;
    }

}
