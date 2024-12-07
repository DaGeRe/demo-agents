package de.lancasterleipzig.agents.javassistAgent;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;

public class CallTransformer implements ClassFileTransformer {
	@Override
	public byte[] transform(final ClassLoader loader, final String className, final Class<?> classBeingRedefined,
			final ProtectionDomain protectionDomain, final byte[] classfileBuffer) throws IllegalClassFormatException {
		final String realClassName = className.replaceAll(File.separator, ".");

		if (realClassName.startsWith("scala.collection")
				|| realClassName.startsWith("scala.util")
				|| realClassName.startsWith("scala.math")
				|| realClassName.startsWith("scala.Predef")
				|| realClassName.startsWith("scala.Option")
				|| realClassName.startsWith("scala.runtime")
				|| realClassName.startsWith("kafka.log.LogManager")
				|| realClassName.contains("kafka.utils.VerifiableProperties")
				|| realClassName.startsWith("sun.reflect")
				|| realClassName.startsWith("jdk.internal")
				|| realClassName.startsWith("com.sun")
				|| realClassName.startsWith("sun.")
				|| realClassName.startsWith("java.io")
				|| realClassName.startsWith("java.lang")
				|| realClassName.startsWith("jdk.")
				|| realClassName.startsWith("java.")
				|| realClassName.startsWith("javax.")
				|| realClassName.startsWith("de.lancasterleipzig.agents.javassistAgent")) {
			return null;
		}

		final ClassPool cp = ClassPool.getDefault();
		CtClass cc;
		try {
			cc = cp.get(realClassName);

			if (!cc.isInterface()) {
				for (CtMethod method : cc.getDeclaredMethods()) {
					if (!method.isEmpty()) {
						String fqn = cc.getName() + "." + method.getName();
						method.insertBefore("de.lancasterleipzig.agents.javassistAgent.CallWritingManager.call(\"" + fqn + "\");");
					}
				}
			}
			final byte[] byteCode = cc.toBytecode();
			cc.detach();

			return byteCode;
		} catch (NotFoundException | IOException | CannotCompileException e) {
			System.err.println("Error in: " + realClassName);
			e.printStackTrace();
		}
		return null;
	}
}
