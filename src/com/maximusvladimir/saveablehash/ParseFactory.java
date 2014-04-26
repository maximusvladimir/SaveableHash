package com.maximusvladimir.saveablehash;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
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
	 * 
	 * @param klass
	 *            The Class to assign the serializer to.
	 * @param ios
	 *            The Serializer to convert classes.
	 */
	public <T> void add(Class<?> klass, IOSerialize<T> ios) {
		hash.put(klass.getName(), ios);
	}

	/**
	 * Adds a new type deginition to be serialized. Do not expect this to work
	 * effectivly with classes that are large (or use classes that are large. Do
	 * not expect this to work at all on classes that use native code or native
	 * memory. This code may create a StackOverflowException if you use the same
	 * class within other classes.
	 * 
	 * @deprecated This method is experimental! Please use with caution!
	 * @param klass
	 */
	public <T> void add(final Class<?> klass) {
		IOSerialize<T> typedef = new IOSerialize<T>() {
			@SuppressWarnings("unchecked")
			public Object load(ParseFactory factory, String data) {
				return fromSerialize(klass, data);
			}

			public String save(ParseFactory factory, Object obj) {
				String tmp = toSerialize(obj, klass);
				// System.out.println(tmp);
				return tmp;
			}
		};
		add(klass, typedef);
	}

	/**
	 * Serializes a class and any primitive types it may use.
	 * 
	 * @param klass
	 *            The Class to serialize.
	 * @return A String containing all the data that is to be serialized.
	 */
	protected String toSerialize(Object obj, Class<?> klass) {
		Field[] fields = klass.getDeclaredFields();
		String res = "";
		int cntr = 0;
		for (int i = 0; i < fields.length; i++) {
			Field f = fields[i];
			boolean state = f.isAccessible();
			f.setAccessible(true);
			if (Modifier.isStatic(f.getModifiers()))
				continue;
			Object upper = null;
			try {
				upper = f.get(obj);
			} catch (Throwable t) {

			}
			f.setAccessible(state);
			if (upper == null)
				continue;
			Object lower = putPrimitive(upper);
			//res += "{";
			if (lower == null && upper != null) {
				// System.out.println(upper);
				String ser = toSerialize(upper, upper.getClass());
				ser = upper.getClass().getName() + ":" + f.getName() + ":" + putString(ser);
				// System.out.println(ser);
				res += ser;
			} else if (upper.getClass().isArray()) {
				res += "~ARRAY:" + f.getName() + ":" + lower;
			} else if (upper != null) {
				String yr = upper.getClass().getName() + ":" + f.getName()
						+ ":" + lower;
				//System.out.println(yr);
				res += yr;
			}
			res += "@";
			cntr++;
		}
		if (cntr >= 1)
			res = res.substring(0, res.length()-1);
		return res;
	}

	protected Object fromSerialize(Class<?> klass, String str) {
		Object o = createInstance(klass);
		String[] parts = str.split("@");
		Field[] fields = klass.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Field f = fields[i];
			boolean state = f.isAccessible();
			f.setAccessible(true);
			//System.out.println(f.getType().getName());
			if (Modifier.isStatic(f.getModifiers())) {
				f.setAccessible(state);
				continue;
			}
			String[] pars = null;
			for (int j = 0; j < parts.length; j++) {
				pars = parts[j].split(":");
				if (pars[1].equals(f.getName())) {
					break;
				}
			}
			String dataPart = pars[2];
			if (needsDecode(pars[0])) {
				dataPart = parseString(dataPart);
			}
			//System.out.println(pars[0] + "," + pars[1] + "," + pars[2]);
			Object obj = parseType(pars[0],dataPart);
			//System.out.println(obj);
			try {
				f.set(o, obj);
			} catch (Throwable t) {
				//t.printStackTrace();
			}
			f.setAccessible(state);
		}
		return o;
	}
	
	protected boolean needsDecode(String type) {
		if (type.equals("java.lang.String"))
			return true;
		else if (type.startsWith("java.lang."))
			return false;
		else
			return true;
	}
	
	/**
	 * Retrieves a class from a serialized string.
	 * 
	 * @param klass
	 *            The class type to deserialize.
	 * @param str
	 *            The string to retrieve the data from.
	 * @return Hopefully and object contiaining the right data.
	 */
	protected Object fromSerialize2(Class<?> klass, String str) {
		if (str == null || str.equals(""))
			return null;

		int indexer = 0;
		int depth = 0;
		int tmp0 = 0;
		Object o = createInstance(klass);
		Field[] fields = klass.getDeclaredFields();
		while (true) {
			if (indexer > str.length() - 1)
				break;

			if (str.charAt(indexer) == '{') {
				tmp0 = indexer;
				depth++;
			}
			if (str.charAt(indexer) == '}') {
				depth--;
				String inner = str.substring(tmp0, indexer);
				if (inner.indexOf(":") != -1 && inner.indexOf("{") == -1
						&& depth == 0) {
					try {
						String[] parts = inner.split(":");
						for (int i = 0; i < fields.length; i++) {
							Field cfield = fields[i];
							if (cfield.getName().equals(parts[1])) {
								boolean state = cfield.isAccessible();
								cfield.setAccessible(true);
								cfield.set(
										o,
										parseType(cfield.getType().getName(),
												parts[2]));
								cfield.setAccessible(state);
							}
						}
					} catch (Throwable e) {
						e.printStackTrace();
					}
				} else if (depth == 0) {
					
				}
			}
			indexer++;
		}
		return o;
	}

	/**
	 * Attempts to create an instance of a class.
	 * 
	 * @param klass
	 *            The class type to generate.
	 * @return An exception, or an instance of the object.
	 */
	protected Object createInstance(Class<?> klass) {
		Object o = null;
		try {
			Constructor<?> c = klass.getDeclaredConstructor();
			boolean sta = c.isAccessible();
			c.setAccessible(true);
			o = c.newInstance();
			c.setAccessible(sta);
		} catch (Throwable t) {

		}
		if (o == null) {
			Constructor<?>[] cnss = klass.getConstructors();
			for (int i = 0; i < cnss.length; i++) {
				Constructor<?> ctor = cnss[i];
				boolean state = ctor.isAccessible();
				ctor.setAccessible(true);
				int l = ctor.getParameterTypes().length;
				Object[] nulls = new Object[l];
				for (int m = 0; m < l; m++) {
					Class<?> cn = ctor.getParameterTypes()[m];
					if (cn.isPrimitive()) {
						if (cn == Boolean.class)
							nulls[m] = (Boolean)false;
						else
							nulls[m] = newPrimitive(cn);
					}
					if (nulls[m] == null){
						nulls[m] = new Object();
					}
				}
				Object res = null;
				try {
					if (l == 0)
						res = ctor.newInstance();
					else
						res = ctor.newInstance(nulls);
				} catch (Throwable t) {
					t.printStackTrace();
				}
				if (res != null) {
					o = res;
					break;
				}
				ctor.setAccessible(state);
			}
		}
		if (o == null) {
			throw new RuntimeException("Unable to create instance of "
					+ klass.getName());
		}
		return o;
	}
	
	protected <T> Object newPrimitive(Class<?> cn) {
		String c = cn.getName();
		if (c.equals("int"))
			return (Object)(int)0;
		if (c.equals("float"))
			return (Object)(float)0;
		if (c.equals("double"))
			return (Object)(double)0;
		if (c.equals("short"))
			return (Object)(short)0;
		if (c.equals("byte"))
			return (Object)(byte)0;
		if (c.equals("char"))
			return (Object)(char)'a';
		return null;
	}
	
	/**
	 * Gets a serializer for a particular class name.
	 * 
	 * @param className
	 *            The class name to lookup.
	 * @return A serializer or null if no serializer is found.
	 */
	public IOSerialize<?> get(String className) {
		return hash.get(className);
	}

	/**
	 * Converts the string to base 64.
	 * 
	 * @param data
	 *            The string to convert.
	 * @return A base64 version of the string.
	 */
	public String putString(String data) {
		return Base64.encode(data);
	}

	/**
	 * Converts the string from base 64.
	 * 
	 * @param data
	 *            The string to convert.
	 * @return A regular version of the base 64 string.
	 */
	public String parseString(String data) {
		return Base64.decode(data);
	}

	protected <T> String putPrimitive(T val) {
		// System.out.println(val.getClass().);
		if (val == null) {
			return "DATANULL";
		} else if (val.getClass().isArray()) {
			int length = Array.getLength(val);
			String builder = "";
			for (int i = 0; i < length; i++) {
				Object ob = Array.get(val, i);
				String n = putPrimitive(ob);
				if (n == null)
					throw new RuntimeException(
							"Type doesn't exist to convert to: "
									+ ob.getClass().getName());
				if (ob.getClass().isArray())
					builder += ",~ARRAY" + ":" + n;
				else
					builder += "," + ob.getClass().getName() + ":" + n;
			}
			if (builder.length() > 1)
				return putString(builder.substring(1));
			else
				return "DATANULL";
		} else if (val instanceof Character) {
			return ((int) ((Character) val)) + "";
		} else if (val instanceof Number) {
			return val + "";
		} else if (val instanceof String) {
			return putString((String) val);
		} else if (val instanceof BigInteger) {
			return ((BigInteger) val).toString();
		} else if (val instanceof BigDecimal) {
			return ((BigDecimal) val).toString();
		}
		return null;
	}

	/**
	 * Converts a type with in a hash into an outputable string.
	 * 
	 * @param set
	 *            The hash set to use.
	 * @param key
	 *            The key to use within the hashset.
	 * @return A string containing the data or a null value.
	 */
	public <T> String putType(HashMap<String, T> set, String key) {
		T value = set.get(key);
		String dataValue = putPrimitive(value);
		if (dataValue == null) {
			try {
				dataValue = putString(get(set.get(key).getClass().getName())
						.save(this, set.get(key)));
			} catch (Throwable t) {
				 t.printStackTrace();
				if (t instanceof StackOverflowError) {
					throw new RuntimeException(
							"If you would have read the documentation, you would have "
									+ "found that auto-serialization doesn't work on classes that"
									+ " contain instances of themselves.");
				} else
					throw new RuntimeException(
							"Cannot convert "
									+ set.get(key).getClass().getName()
									+ " to a value: Doesn't exist or there was an issue during conversion: "
									+ t.getMessage());
			}
		}
		String type = value.getClass().getName();
		if (value.getClass().isArray()) {
			type = "~ARRAY";
		}
		return type + ":" + key + ":" + dataValue;
	}

	/**
	 * Converts a string into a type of class.
	 * 
	 * @param type
	 *            The type of data.
	 * @param value
	 *            The string actually containing the data.
	 * @return A type that hopefully contains the data.
	 */
	@SuppressWarnings("unchecked")
	public <T> T parseType(String type, String value) {
		T val = null;
		if (value.equals("DATANULL"))
			return null;
		if (type.equals("~ARRAY") || type.startsWith("[")) {
			String data = parseString(value);
			String[] elements = data.split(",");
			T[] r = null;
			if (elements.length == 0)
				return (T) r;
			try {
				String typer = (elements[0].split(":")[0]);
				if (typer.equals("~ARRAY")) {
					Object[] swp = new Object[elements.length];
					for (int i = 0; i < elements.length; i++) {
						String[] arrData = elements[i].split(":");
						Object res = parseType(arrData[0], arrData[1]);
						swp[i] = res;
					}
					return (T) (Object) swp;
				}
				r = (T[]) Array.newInstance(Class.forName(typer),
						elements.length);
			} catch (Throwable t) {
				throw new RuntimeException("Unable to create array: "
						+ t.getMessage());
			}
			// System.out.println(r.getClass().getName());
			for (int i = 0; i < elements.length; i++) {
				String[] typevalue = elements[i].split(":");
				T t = (T) parseType(typevalue[0], typevalue[1]);
				r[i] = t;
			}
			return (T) r;
		}

		try {
			type = primitiveTypeToBox(type);
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
					IOSerialize<?> ios = get(type);
					if (ios == null) {
						val = (T) fromSerialize(Class.forName(type), value);
					} else {
						val = (T) ios.load(this, parseString(value));
					}
				} catch (Throwable t) {
					t.printStackTrace();
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

	/**
	 * Converts a primitive type to a wrapped primitive type.
	 * 
	 * @param type
	 *            The primitive type to convert.
	 * @return A wrapped primitive type.
	 */
	protected String primitiveTypeToBox(String type) {
		final String[] prims = new String[] { "long", "float", "double",
				"short", "byte", "boolean", "char" };
		for (int i = 0; i < prims.length; i++) {
			if (type.equals(prims[i])) {
				type = "java.lang." + Character.toUpperCase(prims[i].charAt(0))
						+ prims[i].substring(1);
			}
		}
		if (type.equals("int"))
			type = "java.lang.Integer";
		// System.out.println(type+"DROP");
		return type;
	}

	/**
	 * Converts a wrapped primitive into a primitive type.
	 * 
	 * @param type
	 *            The wrapped primitive to convert.
	 * @return A primitive type.
	 */
	protected String boxToPrimitiveType(String type) {
		if (type.indexOf("java.lang.") != -1)
			type = type.replace("java.lang.", "");
		else
			return type;
		final String[] boxes = new String[] { "Integer", "Long", "Float",
				"Double", "Short", "Byte", "Boolean", "Character" };
		boolean contains = false;
		for (int i = 0; i < boxes.length; i++) {
			if (boxes[i].equals(type)) {
				contains = true;
				i = boxes.length + 5;
			}
		}
		if (!contains) {
			return "java.lang." + type;
		}
		type = type.toLowerCase();
		if (type.equals("integer"))
			type = "int";
		if (type.equals("character"))
			type = "char";
		return type;
	}
}
