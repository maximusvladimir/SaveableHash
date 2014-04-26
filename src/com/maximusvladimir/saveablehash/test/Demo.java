package com.maximusvladimir.saveablehash.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Random;

import com.maximusvladimir.saveablehash.HashIO;
import com.maximusvladimir.saveablehash.IOSerialize;
import com.maximusvladimir.saveablehash.ParseFactory;

public class Demo {
	public static void main(String[] args) {
		
		class Timestamp {
			public long start;
			public long end;
			public String buf;
			public Timestamp() {
				
			}
			
			public void setStart(long s) {
				start = s;
			}
			
			public long getStart() {
				return start;
			}
			
			public void setEnd(long e) {
				end = e;
			}
			
			public long getEnd() {
				return end;
			}
			
			public String toString() {
				return "start:" + start + " end:" + end + " buffer:" + buf;
			}
		}
		
		ParseFactory factory = new ParseFactory();
		factory.add(Timestamp.class);
		/*factory.add(Timestamp.class, new IOSerialize<Timestamp>() {
			public <T> T load(ParseFactory factory, String data) {
				Timestamp stamp = new Timestamp();
				String[] parts = data.split(",");
				stamp.setStart(Long.parseLong(parts[0]));
				stamp.setEnd(Long.parseLong(parts[1]));
				return (T)stamp;
			}

			@Override
			public <T> String save(ParseFactory factory, T obj) {
				return ((Timestamp)obj).getStart() + "," + ((Timestamp)obj).getEnd();
			}
			
		});*/
		
		HashMap<String, Timestamp> millisecondTimeFrames = new HashMap<String, Timestamp>();
		for (int i = 0; i < 20; i++) {
			Timestamp stamp = new Timestamp();
			long sl = (long)(Math.random() * Long.MAX_VALUE);
			if (Math.random() < 0.5)
				sl = -sl;
			long el = (long)(Math.random() * Long.MAX_VALUE);
			if (Math.random() < 0.5)
				el = -el;
			stamp.setStart(sl);
			stamp.setEnd(el);
			stamp.buf = "jigj" + -new Random(i*200 + (int)(Math.random() * 300)).nextLong();
			millisecondTimeFrames.put("i"+i, stamp);
		}
		
		try {
			FileWriter fr = new FileWriter(new File("timeframes.txt"));
			BufferedWriter writer = new BufferedWriter(fr);
			HashIO.save(factory, millisecondTimeFrames, writer);
			writer.flush();
		} catch (Throwable t) {

		}

		millisecondTimeFrames.clear();

		try {
			FileReader fr = new FileReader(new File("timeframes.txt"));
			BufferedReader reader = new BufferedReader(fr);
			HashIO.load(factory, millisecondTimeFrames, reader);
		} catch (Throwable t) {

		}
		
		String[] keys = new String[millisecondTimeFrames.keySet().size()];
		keys = millisecondTimeFrames.keySet().toArray(keys);
		for (int i = 0; i < keys.length; i++) {
			System.out.println(keys[i] + "=" + millisecondTimeFrames.get(keys[i]));
		}
		// Don't forget to close your stuff here. :wink:
	}
}
