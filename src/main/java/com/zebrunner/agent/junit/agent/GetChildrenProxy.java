package com.zebrunner.agent.junit.agent;

import com.zebrunner.agent.core.registrar.RerunContextHolder;
import com.zebrunner.agent.core.rest.domain.TestDTO;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;
import org.junit.runners.model.FrameworkMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class GetChildrenProxy {

    @RuntimeType
    public static <T> List<T> intercept(@This final Object runner, @SuperCall final Callable<List<T>> proxy) throws Exception {
        List<T> children = new ArrayList<>(proxy.call());
        if (RerunContextHolder.isRerun()) {
            children = filterChildrenToRerun(children);
        }
        return children;
    }

    private static <T> List<T> filterChildrenToRerun(List<T> children) {
        return children.stream()
                       .filter(child -> child instanceof FrameworkMethod)
                       .filter(child -> isMethodToRerun((FrameworkMethod) child))
                       .collect(Collectors.toList());
    }

    private static boolean isMethodToRerun(FrameworkMethod method) {
        List<TestDTO> tests = RerunContextHolder.getTests();
        return tests.stream()
                    .anyMatch(t -> equals(t, method));
    }

    private static boolean equals(TestDTO test, FrameworkMethod method) {
        String displayName = method.getName() + "(" + method.getDeclaringClass().getName() + ")";
        return test.getName().equals(displayName);
    }
}
