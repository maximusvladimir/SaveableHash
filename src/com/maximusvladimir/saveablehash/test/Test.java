package com.maximusvladimir.saveablehash.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;

import com.maximusvladimir.saveablehash.HashIO;

public class Test {
	public static void main(String[] args) {
		HashMap<String, String> myHash = new HashMap<String, String>();
		for (int i = 0; i < 10000; i++) {
			myHash.put("hash" + i, genRandomString());
		}

		try {
			FileWriter fr = new FileWriter(new File("test.hsh"));
			BufferedWriter writer = new BufferedWriter(fr);
			HashIO.save(myHash, writer);
			writer.flush();
			writer.close();
			fr.close();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		
		HashMap<String, String> myOtherHash = new HashMap<String, String>();
		try {
			FileReader fr = new FileReader(new File("test.hsh"));
			BufferedReader reader = new BufferedReader(fr);
			HashIO.load(myOtherHash, reader);
			reader.close();
			fr.close();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		
		String[] keys = new String[myOtherHash.keySet().size()];
		keys = myOtherHash.keySet().toArray(keys);
		for (int i = 0; i < keys.length; i++) {
			System.out.println(keys[i] + "=" + myOtherHash.get(keys[i]));
		}
	}
	
	private final static char[] abc123 = "abcdefghijklmnopqstuvwxyz0123456789".toCharArray();
	
	public static String genRandomString() {
		String builder = "";
		for (int i = 0; i < (int)(Math.random() * 20) + 5; i++) {
			char c = abc123[(int)(Math.random() * abc123.length)];
			if (Math.random() < 0.5)
				c = Character.toUpperCase(c);
			builder += c;
		}
		return builder;
	}
}
