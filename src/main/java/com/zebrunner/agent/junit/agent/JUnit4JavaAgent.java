package com.zebrunner.agent.junit.agent;

import com.nordstrom.automation.junit.Hooked;
import com.nordstrom.automation.junit.LifecycleHooks;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.pool.TypePool;
import org.junit.runners.ParentRunner;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;

import static net.bytebuddy.matcher.ElementMatchers.hasSuperType;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class JUnit4JavaAgent {

    public static void premain(String args, Instrumentation inst) {

        installClassFileTransformer(inst);

        LifecycleHooks.installTransformer(inst);
    }

    private static ClassFileTransformer installClassFileTransformer(Instrumentation inst) {
        final TypeDescription getChildren = TypePool.Default.ofSystemLoader().describe(GetChildrenProxy.class.getName()).resolve();

        return new AgentBuilder.Default()
                .with(AgentBuilder.TypeStrategy.Default.REDEFINE)
                .type(hasSuperType(named(ParentRunner.class.getName())))
                .transform((builder, type, classloader, module) -> builder.method(named("getChildren")).intercept(MethodDelegation.to(getChildren))
                                                                          .implement(Hooked.class))
                .installOn(inst);
    }
}
