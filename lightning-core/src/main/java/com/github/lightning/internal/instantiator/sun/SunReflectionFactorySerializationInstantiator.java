/**
 * Copyright 2006-2009 the original author or authors.
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
package com.github.lightning.internal.instantiator.sun;

import java.io.NotSerializableException;
import java.lang.reflect.Constructor;

import sun.reflect.ReflectionFactory;

import com.github.lightning.instantiator.ObjectInstantiator;
import com.github.lightning.internal.instantiator.ObjenesisException;
import com.github.lightning.internal.instantiator.SerializationInstantiatorHelper;

/**
 * Instantiates an object using internal sun.reflect.ReflectionFactory - a class
 * only available on
 * JDK's that use Sun's 1.4 (or later) Java implementation. This instantiator
 * will create classes in
 * a way compatible with serialization, calling the first non-serializable
 * superclass' no-arg
 * constructor. This is the best way to instantiate an object without any side
 * effects caused by the
 * constructor - however it is not available on every platform.
 * 
 * @author Leonardo Mesquita
 * @see ObjectInstantiator
 */
@SuppressWarnings("restriction")
public class SunReflectionFactorySerializationInstantiator implements ObjectInstantiator {

	private final Constructor<?> mungedConstructor;

	public SunReflectionFactorySerializationInstantiator(Class<?> type) {

		Class<?> nonSerializableAncestor = SerializationInstantiatorHelper.getNonSerializableSuperClass(type);
		ReflectionFactory reflectionFactory = ReflectionFactory.getReflectionFactory();
		Constructor<?> nonSerializableAncestorConstructor;
		try {
			nonSerializableAncestorConstructor = nonSerializableAncestor.getConstructor((Class[]) null);
		}
		catch (NoSuchMethodException e) {
			/**
			 * @todo (Henri) I think we should throw a NotSerializableException
			 *       just to put the same
			 *       message a ObjectInputStream. Otherwise, the user won't know
			 *       if the null returned
			 *       if a "Not serializable", a
			 *       "No default constructor on ancestor" or a "Exception in
			 *       constructor"
			 */
			throw new ObjenesisException(new NotSerializableException(type + " has no suitable superclass constructor"));
		}

		mungedConstructor = reflectionFactory.newConstructorForSerialization(type, nonSerializableAncestorConstructor);
		mungedConstructor.setAccessible(true);
	}

	@Override
	public Object newInstance() {
		try {
			return mungedConstructor.newInstance((Object[]) null);
		}
		catch (Exception e) {
			throw new ObjenesisException(e);
		}
	}
}
