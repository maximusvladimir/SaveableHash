package com.maximusvladimir.saveablehash.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;

import com.maximusvladimir.saveablehash.HashIO;
import com.maximusvladimir.saveablehash.IOSerialize;
import com.maximusvladimir.saveablehash.ParseFactory;

public class TestWithMultiType {
	public static void main(String[] args) {
		
		ParseFactory factory = new ParseFactory();
		
		class MyBlandClass {
			public String str;
			public int in;
			public MyBlandClass(String s, int i) {
				str = s;
				in = i;
			}
			
			public String toString() {
				return str + "+" + in;
			}
		}
		
		factory.add(MyBlandClass.class, new IOSerialize<MyBlandClass>() {
			@SuppressWarnings("unchecked")
			public <T> T load(ParseFactory factory, String data) {
				String[] splits = data.split("!!!");
				return (T) new MyBlandClass(splits[0],Integer.parseInt(splits[1]));
			}

			public <T> String save(ParseFactory factory, T obj) {
				MyBlandClass c = (MyBlandClass)obj;
				return c.str + "!!!" + c.in;
			}
		});
		
		HashMap<String, Object> myHash = new HashMap<String, Object>();
		for (int i = 0; i < 10000; i++) {
			if (Math.random() < 0.333333) {
				myHash.put("MYBLANDCLASSHASHKEY" + i, new MyBlandClass(genRandomString(),(int)(Math.random() * 2020020)));
			} else if (Math.random() < 0.666666666) {
				myHash.put("STRINGHASHKEY" + i, genRandomString());
			} else {
				myHash.put("INTEGERHASHKEY" + i, (int)(Math.random() * 438573859));
			}
		}

		try {
			FileWriter fr = new FileWriter(new File("test.hsh"));
			BufferedWriter writer = new BufferedWriter(fr);
			HashIO.save(factory, myHash, writer);
			writer.flush();
			writer.close();
			fr.close();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		
		HashMap<String, Object> myOtherHash = new HashMap<String, Object>();
		try {
			FileReader fr = new FileReader(new File("test.hsh"));
			BufferedReader reader = new BufferedReader(fr);
			HashIO.load(factory, myOtherHash, reader);
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
