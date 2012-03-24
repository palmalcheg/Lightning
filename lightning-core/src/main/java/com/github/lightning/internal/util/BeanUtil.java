/**
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.lightning.internal.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.objectweb.asm.Type;

import com.github.lightning.metadata.PropertyAccessor;
import com.github.lightning.metadata.PropertyDescriptor;

public final class BeanUtil {

	private BeanUtil() {
	}

	public static Field getFieldByPropertyName(String propertyName, Class<?> type) {
		try {
			return type.getDeclaredField(propertyName);
		}
		catch (NoSuchFieldException e) {
			return null;
		}
	}

	public static Method findSetterMethod(Method method) {
		if (method.getName().startsWith("set")) {
			return method;
		}

		String propertyName = StringUtil.toUpperCamelCase(extractPropertyName(method.getName()));

		Class<?> type = method.getReturnType();
		Class<?> clazz = method.getDeclaringClass();
		String setterName = "set" + propertyName;

		try {
			return clazz.getDeclaredMethod(setterName, type);
		}
		catch (Exception e) {
			// Seems there's no setter, so ignore all exceptions
			return null;
		}
	}

	public static Method findGetterMethod(Method method) {
		if (method.getName().startsWith("get") || method.getName().startsWith("is")) {
			return method;
		}

		String propertyName = StringUtil.toUpperCamelCase(extractPropertyName(method.getName()));

		Class<?> type = method.getParameterTypes()[0];
		Class<?> clazz = method.getDeclaringClass();
		String getterObjectName = "get" + propertyName;
		String getterBooleanName = "is" + propertyName;

		try {
			return clazz.getDeclaredMethod(getterObjectName, type);
		}
		catch (Exception e) {
			if (type == boolean.class) {
				try {
					return clazz.getDeclaredMethod(getterBooleanName, type);
				}
				catch (Exception ex) {
					// Intentionally left blank - just fall through
				}
			}

			// Seems there's no setter, so ignore all exceptions
			return null;
		}
	}

	public static String buildPropertyName(Method method) {
		return buildPropertyName(method.getName());
	}

	public static String buildPropertyName(String methodName) {
		return StringUtil.toLowerCamelCase(extractPropertyName(methodName));
	}

	public static String buildInternalSignature(Iterable<PropertyDescriptor> propertyDescriptors) {
		StringBuilder internalSignature = new StringBuilder();
		for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
			internalSignature.append(propertyDescriptor.getInternalSignature());
		}
		return internalSignature.toString();
	}

	public static <T> String buildInternalSignature(String propertyName, PropertyAccessor propertyAccessor) {
		String type = Type.getDescriptor(propertyAccessor.getType());
		return new StringBuilder("{").append(propertyName).append("}").append(type).toString();
	}

	private static String extractPropertyName(String methodName) {
		if (methodName.toUpperCase().startsWith("GET") || methodName.toUpperCase().startsWith("IS")
				|| methodName.toUpperCase().startsWith("SET")) {

			char[] characters = methodName.toCharArray();
			for (int i = 1; i < characters.length; i++) {
				if (Character.isUpperCase(characters[i])) {
					return StringUtil.toLowerCamelCase(methodName.substring(i));
				}
			}
		}
		return StringUtil.toLowerCamelCase(methodName);
	}

}
