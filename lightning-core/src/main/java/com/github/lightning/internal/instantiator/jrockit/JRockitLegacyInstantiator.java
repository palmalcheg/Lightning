/**
 * COPYRIGHT & LICENSE
 *
 * This code is Copyright (c) 2006 BEA Systems, inc. It is provided free, as-is and without any warranties for the purpose of
 * inclusion in Objenesis or any other open source project with a FSF approved license, as long as this notice is not
 * removed. There are no limitations on modifying or repackaging the code apart from this. 
 *
 * BEA does not guarantee that the code works, and provides no support for it. Use at your own risk.
 *
 * Originally developed by Leonardo Mesquita. Copyright notice added by Henrik St�hl, BEA JRockit Product Manager.
 *  
 */

package com.github.lightning.internal.instantiator.jrockit;

import java.lang.reflect.Method;

import com.github.lightning.instantiator.ObjectInstantiator;
import com.github.lightning.internal.instantiator.ObjenesisException;

/**
 * Instantiates a class by making a call to internal JRockit private methods. It
 * is only supposed to
 * work on JRockit 1.4.2 JVMs prior to release R25.1. From release R25.1 on,
 * JRockit supports
 * sun.reflect.ReflectionFactory, making this "trick" unnecessary. This
 * instantiator will not call
 * any constructors.
 * 
 * @author Leonardo Mesquita
 * @see com.github.lightning.instantiator.ObjectInstantiator
 * @see com.github.lightning.internal.instantiator.sun.SunReflectionFactoryInstantiator
 */
public class JRockitLegacyInstantiator implements ObjectInstantiator {

	private static Method safeAllocObjectMethod = null;

	private static void initialize() {
		if (safeAllocObjectMethod == null) {
			Class memSystem;
			try {
				memSystem = Class.forName("jrockit.vm.MemSystem");
				safeAllocObjectMethod = memSystem.getDeclaredMethod("safeAllocObject",
						new Class[] { Class.class });
				safeAllocObjectMethod.setAccessible(true);
			}
			catch (RuntimeException e) {
				throw new ObjenesisException(e);
			}
			catch (ClassNotFoundException e) {
				throw new ObjenesisException(e);
			}
			catch (NoSuchMethodException e) {
				throw new ObjenesisException(e);
			}
		}
	}

	private final Class type;

	public JRockitLegacyInstantiator(Class type) {
		initialize();
		this.type = type;
	}

	@Override
	public Object newInstance() {
		try {
			return safeAllocObjectMethod.invoke(null, new Object[] { type });
		}
		catch (Exception e) {
			throw new ObjenesisException(e);
		}
	}
}
