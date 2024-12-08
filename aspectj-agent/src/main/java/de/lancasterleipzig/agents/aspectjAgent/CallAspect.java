package de.lancasterleipzig.agents.aspectjAgent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class CallAspect {

	private static final Set<String> _CALLS_ = new HashSet<String>();

	public static BufferedWriter writer;

	@Pointcut("!within(de.lancasterleipzig..*)")
	public void notWithinKieker() {}

	// Use the following pointcut to also instrument constructors
	// @Pointcut("execution(* *(..)) || execution(new(..))")
	@Pointcut("execution(* *(..))")
	public void monitoredOperation() {

	}

	@Before("monitoredOperation() && notWithinKieker()")
	public void beforeOperation(final JoinPoint thisJoinPoint) throws Throwable {
		String operationSignature = thisJoinPoint.getSignature().getDeclaringTypeName() + "." + thisJoinPoint.getSignature().getName();

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
