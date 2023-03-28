package com.zebrunner.agent.junit.core;

import com.google.gson.Gson;
import lombok.Builder;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Value
@Builder
public class TestCorrelationData {

    private static final Gson GSON = new Gson();

    String thread = Thread.currentThread().getName();

    String className;
    String methodName;
    List<String> parameterClassNames;
    int argumentsIndex;

    String displayName;

    @Override
    public String toString() {
        StringBuilder builderPattern = new StringBuilder("[%s]: %s.%s(%s)");

        List<Object> buildParameters = new ArrayList<>(5);
        buildParameters.add(Thread.currentThread().getName());
        buildParameters.add(className);
        buildParameters.add(methodName);
        buildParameters.add(String.join(", ", parameterClassNames));

        if (argumentsIndex != ArgumentsIndexResolver.ARGUMENTS_INDEX_DEFAULT_VALUE) {
            builderPattern.append("[%d]");
            buildParameters.add(argumentsIndex);
        }

        return String.format(builderPattern.toString(), buildParameters.toArray());
    }

    public String asJsonString() {
        return GSON.toJson(this);
    }

    public static TestCorrelationData fromJsonString(String json) {
        return GSON.fromJson(json, TestCorrelationData.class);
    }

}
