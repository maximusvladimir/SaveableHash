package com.maximusvladimir.saveablehash.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;

import com.maximusvladimir.saveablehash.HashIO;

public class TestWithArrays {
	public static void main(String[] args) {
		HashMap<String, int[][]> myHash = new HashMap<String, int[][]>();
		for (int i = 0; i < 10; i++) {
			int[][] malloc = new int[(int)(Math.random()* 20)+1][];
			for (int a = 0; a < malloc.length; a++) {
				int[] beta = new int[(int)(Math.random()* 20)+1];
				for (int s = 0; s < beta.length; s++) {
					beta[s] = (int)(Integer.MAX_VALUE * Math.random());
				}
				malloc[a] = beta;
			}
			myHash.put("hash" + i, malloc);
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
		
		HashMap<String, int[][]> readingHash = new HashMap<String, int[][]>();
		try {
			FileReader fr = new FileReader(new File("test.hsh"));
			BufferedReader reader = new BufferedReader(fr);
			HashIO.load(readingHash, reader);
			reader.close();
			fr.close();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		
		HashMap<String,int[][]> myOtherHash = new HashMap<String, int[][]>();
		
		// It is somewhat difficult to convert the types over... (stupid Java type erasure)
		String[] keys = new String[readingHash.keySet().size()];
		keys = readingHash.keySet().toArray(keys);
		for (int i = 0; i < keys.length; i++) {
			Object[] aarr = (Object[])(Object)readingHash.get(keys[i]);
			int[][] ndat = new int[aarr.length][];
			for (int j = 0; j < aarr.length; j++) {
				Object[] inter = (Object[])aarr[j];
				int[] rdat = new int[inter.length];
				for (int k = 0; k < inter.length; k++) {
					rdat[k] = (int) inter[k];
				}
				ndat[j] = rdat;
			}
			myOtherHash.put(keys[i], ndat);
		}
		
		boolean matched = true;
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];
			if (!check2DArrayEqual(myOtherHash.get(key),myHash.get(key))) {
				matched = false;
				break;
			}
		}
		System.out.println("myOtherHash == myHash: " + matched);
	}
	
	private static boolean check2DArrayEqual(int[][] a1, int[][] a2) {
		if (a1.length != a2.length)
			return false;
		for (int q = 0; q < a1.length; q++) {
			if (a1[q].length != a2[q].length)
				return false;
			for (int w = 0; w < a2[q].length; w++) {
				if (a1[q][w] != a2[q][w])
					return false;
			}
		}
		return true;
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
