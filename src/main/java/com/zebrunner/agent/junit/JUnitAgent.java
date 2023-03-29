package com.zebrunner.agent.junit;

import com.zebrunner.agent.core.registrar.RunContextHolder;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.pool.TypePool;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.lang.instrument.Instrumentation;

import static net.bytebuddy.matcher.ElementMatchers.hasSuperType;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class JUnitAgent {

    private static final String IS_IGNORED_METHOD_NAME = "isIgnored";

    public static void premain(String args, Instrumentation instrumentation) {
        if (RunContextHolder.isRerun()) {
            installZebrunnerTransformer(instrumentation);
        }

        installJUnitFoundationTransformer(instrumentation);
    }

    private static void installZebrunnerTransformer(Instrumentation instrumentation) {
        TypeDescription ignore = TypePool.Default.ofSystemLoader()
                                                 .describe(IsIgnoreInterceptor.class.getName())
                                                 .resolve();

        new AgentBuilder.Default()
                .type(hasSuperType(named(BlockJUnit4ClassRunner.class.getName())))
                .transform((builder, type, classloader, module) -> builder.method(named(IS_IGNORED_METHOD_NAME))
                                                                          .intercept(MethodDelegation.to(ignore)))
                .installOn(instrumentation);
    }

    private static void installJUnitFoundationTransformer(Instrumentation instrumentation) {
        com.nordstrom.automation.junit.JUnitAgent.installTransformer(instrumentation);
    }

}
