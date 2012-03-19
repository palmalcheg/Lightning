package com.github.lightning.internal.marshaller;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.github.lightning.base.AbstractMarshaller;

public class FloatMarshaller extends AbstractMarshaller {

	@Override
	public boolean acceptType(Class<?> type) {
		return float.class == type || Float.class == type;
	}

	@Override
	public void marshall(Object value, Class<?> type, DataOutput dataOutput) throws IOException {
		if (Float.class == type) {
			if (!writePossibleNull(value, dataOutput)) {
				return;
			}
		}

		dataOutput.writeFloat((Float) value);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <V> V unmarshall(Class<?> type, DataInput dataInput) throws IOException {
		if (Float.class == type) {
			if (isNull(dataInput)) {
				return null;
			}
		}

		return (V) Float.valueOf(dataInput.readFloat());
	}
}