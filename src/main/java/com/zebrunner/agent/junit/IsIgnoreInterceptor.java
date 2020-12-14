package com.zebrunner.agent.junit;

import com.zebrunner.agent.core.registrar.RerunContextHolder;
import com.zebrunner.agent.core.rest.domain.TestDTO;
import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.FrameworkMethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

public class IsIgnoreInterceptor {

    @RuntimeType
    public static Boolean isIgnore(@This final Runner runner, @SuperCall final Callable<Boolean> proxy, @Argument(0) Object child) throws Exception {
        boolean isIgnored = proxy.call();
        if (!isIgnored) {
            if (child instanceof FrameworkMethod) {
                isIgnored = !isItemForRerun(runner, child);
            }
        }
        return isIgnored;
    }

    @SuppressWarnings("unchecked")
    private static <T> boolean isItemForRerun(Runner runner, T child) {
        if (runner instanceof ParentRunner) {
            return isChildForRerun((ParentRunner<T>) runner, child);
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
    private static <T> boolean isChildForRerun(ParentRunner<T> runner, T child) {
        Description description = invokeMethod(runner, "describeChild", child);
        List<TestDTO> tests = RerunContextHolder.getTests();
        return description != null && isDescriptionForRerun(description, tests);
    }

    private static boolean isDescriptionForRerun(Description description, List<TestDTO> tests) {
        return tests.stream()
                    .anyMatch(t -> t.getUuid().equals(description.getDisplayName()));
    }

    /**
     * Invoked method by name from class or superclass hierarchy (if method is not exists)
     * @param fromInstance
     * @param methodName
     * @param parameters
     * @param <R>
     * @return
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
        method = findDeclaredMethod(klass, methodName, parameterTypeClasses);
        while (method == null && klass.getSuperclass() != null) {
            klass = klass.getSuperclass();
            method = findDeclaredMethod(klass, methodName, parameterTypeClasses);
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
