package com.zebrunner.agent.junit.core;

import lombok.extern.slf4j.Slf4j;
import org.junit.runner.Description;

@Slf4j
public class ArgumentsIndexResolver {

    public static final int ARGUMENTS_INDEX_DEFAULT_VALUE = -1;

    public static int resolve(Description description) {
        String methodName = description.getMethodName();
        if (methodName != null && methodName.contains("[") && methodName.endsWith("]")) {
            String argumentsIndexAsString = methodName.substring(methodName.lastIndexOf("[") + 1, methodName.length() - 1);

            return parseIntOrElse(argumentsIndexAsString, ARGUMENTS_INDEX_DEFAULT_VALUE);
        }

        return ARGUMENTS_INDEX_DEFAULT_VALUE;
    }

    private static int parseIntOrElse(String string, Integer defaultValue) {
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException e) {
            log.debug("Unable to parse int from string: {}", string);
            return defaultValue;
        }
    }

}
