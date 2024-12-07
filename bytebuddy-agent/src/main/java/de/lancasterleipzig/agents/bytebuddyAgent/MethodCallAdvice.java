package de.lancasterleipzig.agents.bytebuddyAgent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import net.bytebuddy.asm.Advice;

public class MethodCallAdvice {

	public static BufferedWriter writer;

	@Advice.OnMethodEnter
	public static void enter(@Advice.Origin final String operationSignature,
			@Advice.FieldValue(value = "_CALLS_", readOnly = false) Set<String> _CALLS_) {
		if (_CALLS_ == null) {
			_CALLS_ = new HashSet<String>();
		}

		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		String caller = stackTrace[stackTrace.length - 2].getClassName() + "." + stackTrace[stackTrace.length - 2].getMethodName();

		String callee = getCalleeMethod(operationSignature);
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

	private static String getCalleeMethod(final String operationSignature) {
		int parenthesisIndex = operationSignature.indexOf("(");
		int spaceIndex = operationSignature.lastIndexOf(' ') + 1;
		String callee;
		if (parenthesisIndex != -1 && parenthesisIndex > spaceIndex) {
			callee = operationSignature.substring(spaceIndex, parenthesisIndex);
		} else {
			callee = operationSignature.substring(spaceIndex);
		}
		return callee;
	}
}
