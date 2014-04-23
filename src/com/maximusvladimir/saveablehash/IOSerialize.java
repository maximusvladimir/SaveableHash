package com.maximusvladimir.saveablehash;

public interface IOSerialize<T> {
	/**
	 * Loads the Class from the data.
	 * @param factory The factory that is used.
	 * @param data The data to load.
	 * @return A class that was created from the string.
	 */
	public <T> T load(ParseFactory factory, String data);
	
	/**
	 * Saves a class to a string.
	 * @param factory The factory that is used.
	 * @param obj The class to save.
	 * @return A string containing information about the class.
	 */
	public <T> String save(ParseFactory factory, T obj);
}
