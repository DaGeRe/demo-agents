package de.lancasterleipzig.agents.javassistAgent;

import java.lang.instrument.Instrumentation;


public class AgentMain {
	public static void premain(final String agentArgs, final Instrumentation inst) {
		System.out.println("Starting instrumentation...");

		final CallTransformer kiekerTransformer = new CallTransformer();
		inst.addTransformer(kiekerTransformer);
	}
}
