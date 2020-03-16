package com.zebrunner.agent.junit.agent;

import com.zebrunner.agent.core.registrar.RerunContextHolder;
import com.zebrunner.agent.core.rest.domain.TestDTO;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;
import org.junit.runner.Description;
import org.junit.runners.ParentRunner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class GetChildrenProxy {

    @RuntimeType
    public static <T> List<T> intercept(@This final Object runner, @SuperCall final Callable<List<T>> proxy) throws Exception {
        List<T> children = new ArrayList<>(proxy.call());
        if (RerunContextHolder.isRerun() && !RerunContextHolder.getTests().isEmpty()) {
            children = filterChildrenToRerun(runner, children);
        }
        return children;
    }

    private static <T> List<T> filterChildrenToRerun(Object runner, List<T> children) {
        return children.stream()
                       .filter(child -> isItemForRerun(runner, child))
                       .collect(Collectors.toList());
    }

    private static <T> boolean isItemForRerun(Object runner, T child) {
        if (runner instanceof ParentRunner) {
            return isRunnerToRerun((ParentRunner<T>) runner, child);
        }
        return true;
    }

    /**
     * Reruns test if it`s present in rerun scope or it`s not describable
     * @param runner - current runner
     * @param child - child of the runner
     * @param <T> - child type
     * @return value which indicate that child can be executed on rerun
     */
    private static <T> boolean isRunnerToRerun(ParentRunner<T> runner, T child) {
        Description description = invokeMethod(runner, "describeChild", child);
        List<TestDTO> tests = RerunContextHolder.getTests();
        return description != null && runnerToRerun(tests, description);
    }

    private static boolean runnerToRerun(List<TestDTO> tests, Description description) {
        boolean isRunnerToRerun = doesDescriptionToRerun(description, tests);
        if (!isRunnerToRerun && !description.getChildren().isEmpty()) {
            isRunnerToRerun = description.getChildren().stream()
                                         .anyMatch(childDescription -> doesDescriptionToRerun(childDescription, tests));
        }
        return isRunnerToRerun;
    }

    private static boolean doesDescriptionToRerun(Description description, List<TestDTO> tests) {
        return tests.stream()
                    .anyMatch(t -> t.getUid().equals(description.getDisplayName()));
    }

    /**
     * Invoked method by name from class or superclass hierarchy (if method is not exists)
     * @param fromInstance
     * @param methodName
     * @param parameterTypes
     * @param <R>
     * @return
     */
    @SuppressWarnings("unchecked")
    private static <R> R invokeMethod(Object fromInstance, String methodName, Object... parameterTypes) {
        R result = null;
        try {
            Method method = findMethodByName(fromInstance.getClass(), methodName, parameterTypes);
            if (method != null) {
                method.setAccessible(true);
                result = (R) method.invoke(fromInstance, parameterTypes);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * find declared method by name in class and superclasses
     * @param klass
     * @param methodName
     * @param parameterTypes
     * @return
     */
    private static Method findMethodByName(Class<?> klass, String methodName, Object... parameterTypes) {
        Class<?>[] parameterTypeClasses = Arrays.stream(parameterTypes)
                                                .map(Object::getClass)
                                                .toArray(Class[]::new);
        Method method;
        method = getDeclaredMethod(klass, methodName, parameterTypeClasses);
        while (method == null && klass.getSuperclass() != null) {
            klass = klass.getSuperclass();
            method = getDeclaredMethod(klass, methodName, parameterTypeClasses);
        }
        return method;
    }

    /**
     * Find declared method by name in class
     * @param klass
     * @param methodName
     * @param parameterTypes
     * @return
     */
    private static Method getDeclaredMethod(Class<?> klass, String methodName, Class<?>... parameterTypes) {
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
