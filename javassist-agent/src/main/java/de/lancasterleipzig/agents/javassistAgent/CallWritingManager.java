package de.lancasterleipzig.agents.javassistAgent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class CallWritingManager {

	private static final Set<String> _CALLS_ = new HashSet<>();

	private static BufferedWriter writer;

	public static void call(String callee) {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		StackTraceElement stackTraceElement = stackTrace[stackTrace.length - 3];
		String caller = stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName();

		String callRelationship = caller + "->" + callee;
		if (!_CALLS_.contains(callRelationship)) {
			writeCall(callRelationship);
			_CALLS_.add(callRelationship);
		}
	}

	private static void writeCall(String callRelationship) {
		try {
			if (writer == null) {
				writer = new BufferedWriter(new FileWriter(new File("calls.txt")));
			}
			writer.write(callRelationship + "\n");
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
