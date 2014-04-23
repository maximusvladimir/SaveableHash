package com.maximusvladimir.saveablehash;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;

public class HashIO {
	/**
	 * Loads a HashMap from a BufferedReader.
	 * @param factory The factory to use to read the data.
	 * @param set The HashMap to load the data into.
	 * @param reader The BufferedReader to read the data from.
	 */
	@SuppressWarnings("unchecked")
	public static <T> void load(ParseFactory factory, HashMap<String,T> set, BufferedReader reader) {
		boolean flag = reader == null;
		try {
			if (!flag)
				flag = reader.ready();
		} catch (Throwable t) {
		}
		if (!flag)
			throw new IllegalArgumentException("Invalid buffered reader.");

		String line = null;
		long lineNum = 0;
		try {
			while ((line = reader.readLine()) != null) {
				lineNum++;
				if (!line.equals("com.maximusvladimir.saveablehash")) {
					String[] parts = line.split(":");
					if (parts.length < 2)
						throw new RuntimeException(
								"Invalid or corrupt file (at line #" + lineNum
										+ ").");
					String type = parts[0];
					String key = parts[1];
					String value = parts[2];
					try {
						T par = (T)factory.parseType(type, value);
						if (par == null)
							throw new Exception();
						set.put(key, par);
					} catch (RuntimeException re) {
						throw re;
					} catch (Throwable t) {
						throw new RuntimeException(
								"Invalid or corrupt file (at line #" + lineNum
										+ ").");
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Saves a HashMap to a BufferedWriter.
	 * @param factory The factory to use to save the data.
	 * @param set The HashMap to load the data from.
	 * @param writer The BufferedWriter to write the data to.
	 */
	public static void save(ParseFactory factory, HashMap<String,?> set, BufferedWriter writer) {
		if (writer == null)
			throw new IllegalArgumentException("Invalid buffered writer.");
		
		try {
			String end = System.lineSeparator();
			writer.write("com.maximusvladimir.saveablehash" + end);
			
			String[] strs = new String[set.size()];
			strs = set.keySet().toArray(strs);
			for (int i = 0; i < strs.length; i++) {
				writer.write(factory.putType(set, strs[i]) + end);
			}
		} catch (IOException t) {
			throw new RuntimeException("There was an issue writing to the buffer.");
		}
	}

	/**
	 * Loads a HashMap from a BufferedReader, using the default ParseFactory.
	 * @param set The HashMap to load the data into.
	 * @param reader The BufferedReader to read the data from.
	 */
	public static <T> void load(HashMap<String, T> set, BufferedReader reader) {
		load(new ParseFactory(),set,reader);
	}
	
	/**
	 * Saves a HashMap to a BufferedWriter, using the default ParseFactory.
	 * @param set The HashMap to load the data from.
	 * @param writer The BufferedWriter to write the data to.
	 */
	public static void save(HashMap<String, ?> set, BufferedWriter writer) {
		save(new ParseFactory(),set,writer);
	}
}
