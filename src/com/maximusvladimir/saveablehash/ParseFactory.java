package com.maximusvladimir.saveablehash;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;

public class ParseFactory {
	@SuppressWarnings("rawtypes")
	private HashMap<String, IOSerialize> hash = new HashMap<String, IOSerialize>();

	/**
	 * Creates a new ParseFactory.
	 */
	public ParseFactory() {

	}

	/**
	 * Adds a new type definition to be serialized.
	 * @param klass The Class to assign the serializer to.
	 * @param ios The Serializer to convert classes.
	 */
	public <T> void add(Class<?> klass, IOSerialize<T> ios) {
		hash.put(klass.getName(), ios);
	}

	/**
	 * Gets a serializer for a particular class name.
	 * @param className The class name to lookup.
	 * @return A serializer or null if no serializer is found.
	 */
	public IOSerialize<?> get(String className) {
		return hash.get(className);
	}

	/**
	 * Converts the string to base 64.
	 * @param data The string to convert.
	 * @return A base64 version of the string.
	 */
	public String putString(String data) {
		return Base64.encode(data);
	}

	/**
	 * Converts the string from base 64.
	 * @param data The string to convert.
	 * @return A regular version of the base 64 string.
	 */
	public String parseString(String data) {
		return Base64.decode(data);
	}

	/**
	 * Converts a type with in a hash into an outputable string.
	 * @param set The hash set to use.
	 * @param key The key to use within the hashset.
	 * @return A string containing the data or a null value.
	 */
	public <T> String putType(HashMap<String, T> set, String key) {
		T value = set.get(key);
		String dataValue = "DATANULL";
		if (value == null) {

		} else if (value instanceof Character) {
			dataValue = ((int) ((Character) value)) + "";
		} else if (value instanceof Number) {
			dataValue = value + "";
		} else if (value instanceof String) {
			dataValue = putString((String) value);
		} else if (value instanceof BigInteger) {
			dataValue = ((BigInteger) value).toString();
		} else if (value instanceof BigDecimal) {
			dataValue = ((BigDecimal) value).toString();
		} else {
			try {
				dataValue = putString(get(set.get(key).getClass().getName())
						.save(this, set.get(key)));
			} catch (Throwable t) {
				throw new RuntimeException(
						"Cannot convert "
								+ set.get(key).getClass().getName()
								+ " to a value: Doesn't exist or there was an issue during conversion: "
								+ t.getMessage());
			}
		}
		return value.getClass().getName() + ":" + key + ":" + dataValue;
	}

	/**
	 * Converts a string into a type of class.
	 * @param type The type of data.
	 * @param value The string actually containing the data.
	 * @return A type that hopefully contains the data.
	 */
	@SuppressWarnings("unchecked")
	public <T> T parseType(String type, String value) {
		T val = null;
		if (value.equals("DATANULL"))
			return null;
		try {
			if (type.startsWith("java.lang.")) {
				type = type.replace("java.lang.", "");
				if (type.equals("Integer"))
					val = (T) (Integer) Integer.parseInt(value);
				else if (type.equals("Double"))
					val = (T) (Double) Double.parseDouble(value);
				else if (type.equals("Float"))
					val = (T) (Float) Float.parseFloat(value);
				else if (type.equals("Long"))
					val = (T) (Long) Long.parseLong(value);
				else if (type.equals("Short"))
					val = (T) (Short) Short.parseShort(value);
				else if (type.equals("Byte"))
					val = (T) (Byte) Byte.parseByte(value);
				else if (type.equals("Boolean"))
					val = (T) (Boolean) Boolean.parseBoolean(value);
				else if (type.equals("Character"))
					val = (T) (Character) (char) (Integer.parseInt(value));
				else if (type.equals("String"))
					val = (T) parseString(value);
			} else if (type.startsWith("java.math.")) {
				type = type.replace("java.math.", "");
				if (type.equals("BigInteger"))
					val = (T) new BigInteger(parseString(value));
				else if (type.equals("BigDecimal"))
					val = (T) new BigDecimal(parseString(value));
			} else {
				try {
					val = (T) get(type).load(this, parseString(value));
				} catch (Throwable t) {
					throw new RuntimeException(
							"Cannot convert "
									+ type
									+ " to a value: Doesn't exist or there was an issue during conversion: "
									+ t.getMessage());
				}
			}
		} catch (ClassCastException cce) {

		}
		return val;
	}
}
