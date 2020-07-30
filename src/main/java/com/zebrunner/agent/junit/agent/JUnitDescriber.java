package com.zebrunner.agent.junit.agent;

import com.nordstrom.automation.junit.LifecycleHooks;
import com.zebrunner.agent.core.agent.PremainInvocationListener;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.pool.TypePool;

import java.lang.instrument.Instrumentation;

import static com.zebrunner.agent.core.registrar.RerunContextHolder.isRerun;
import static net.bytebuddy.matcher.ElementMatchers.hasSuperType;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class JUnitDescriber implements PremainInvocationListener {

    private static final String CLASS_RUNNER_CLASS_NAME = "org.junit.runners.BlockJUnit4ClassRunner";
    private static final String IS_IGNORED_MTD_NAME = "isIgnored";

    @Override
    public void onPremain(String args, Instrumentation instrumentation) {
        if (isRerun()) {
            installZebrunnerTransformer(instrumentation);
        }

        installJUnitFoundationTransformer(instrumentation);
    }

    private static void installZebrunnerTransformer(Instrumentation instrumentation) {
        TypeDescription ignore = TypePool.Default.ofSystemLoader()
                                                 .describe(IsIgnoreProxy.class.getName())
                                                 .resolve();

        new AgentBuilder.Default()
                .type(hasSuperType(named(CLASS_RUNNER_CLASS_NAME)))
                .transform((builder, type, classloader, module) -> builder.method(named(IS_IGNORED_MTD_NAME))
                                                                          .intercept(MethodDelegation.to(ignore)))
                .installOn(instrumentation);
    }

    private static void installJUnitFoundationTransformer(Instrumentation instrumentation) {
        LifecycleHooks.installTransformer(instrumentation);
    }

}
