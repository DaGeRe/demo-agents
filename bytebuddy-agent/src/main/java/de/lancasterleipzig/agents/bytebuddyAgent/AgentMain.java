package de.lancasterleipzig.agents.bytebuddyAgent;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.Set;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.Identified.Extendable;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.dynamic.DynamicType.Builder.FieldDefinition.Optional.Valuable;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.utility.JavaModule;

public class AgentMain {
	public static void premain(final String agentArgs, final Instrumentation inst) {
		System.out.println("Starting instrumentation...");

		final Extendable basicAgentBuilder = new AgentBuilder.Default()
				.with(ONLY_ERROR_LOGGER)
				// .with(AgentBuilder.LambdaInstrumentationStrategy.ENABLED)
				// Instrumenting Lambdas results in strange errors as soon as java.util.regex is needed, but not loaded yet
				.type(new ElementMatcher<TypeDescription>() {
					@Override
					public boolean matches(final TypeDescription target) {
						if (target.getClassFileVersion().getMajorVersion() < 52) {
							return false;
						}
						if (target.isInterface()) {
							return false;
						}
						if (target.getName().startsWith("scala.collection") 
								|| target.getName().startsWith("scala.util")
								|| target.getName().startsWith("scala.math")
								|| target.getName().startsWith("scala.Predef")
								|| target.getName().startsWith("scala.Option")
								|| target.getName().startsWith("scala.runtime")
								|| target.getName().startsWith("kafka.log.LogManager")
								|| target.getName().contains("kafka.utils.VerifiableProperties")) {
							return false;
						}
						return true;
					}
				}).transform(new AgentBuilder.Transformer.ForAdvice().advice(new ElementMatcher<MethodDescription>() {

					@Override
					public boolean matches(final MethodDescription target) {
						return true;
					}

				}, MethodCallAdvice.class.getName()));
		basicAgentBuilder.transform(new AgentBuilder.Transformer() {

			@Override
			public Builder<?> transform(final Builder<?> builder, final TypeDescription typeDescription,
					final ClassLoader classLoader, final JavaModule module,
					final ProtectionDomain protectionDomain) {
				final Valuable<?> definedField = builder.defineField("_CALLS_", Set.class,
						Modifier.STATIC | Modifier.FINAL | Modifier.PRIVATE);
				return definedField;
			}
		}).installOn(inst);
	}

	private static final AgentBuilder.Listener ONLY_ERROR_LOGGER = new AgentBuilder.Listener() {
		@Override
		public void onDiscovery(final String typeName, final ClassLoader classLoader, final JavaModule module,
				final boolean loaded) {}

		@Override
		public void onTransformation(final TypeDescription typeDescription, final ClassLoader classLoader,
				final JavaModule module, final boolean loaded, final DynamicType dynamicType) {}

		@Override
		public void onIgnored(final TypeDescription typeDescription, final ClassLoader classLoader,
				final JavaModule module, final boolean loaded) {}

		@Override
		public void onError(final String typeName, final ClassLoader classLoader, final JavaModule module,
				final boolean loaded, final Throwable throwable) {
			throwable.printStackTrace();
		}

		@Override
		public void onComplete(final String typeName, final ClassLoader classLoader, final JavaModule module,
				final boolean loaded) {

		}
	};
}
