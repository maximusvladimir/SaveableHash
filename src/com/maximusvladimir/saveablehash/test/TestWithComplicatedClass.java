package com.maximusvladimir.saveablehash.test;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Random;

import com.maximusvladimir.saveablehash.HashIO;
import com.maximusvladimir.saveablehash.ParseFactory;

public class TestWithComplicatedClass {
	public static void main(String[] args) {
		HashMap<String, Triangle> trianglesAreEverywhere = new HashMap<String, Triangle>();
		Random random = new Random();
		System.out.println("Generated data:");
		for (int i = 0; i < 10; i++) {
			Triangle tri = new Triangle(new MyPoint(random.nextInt(20) - 10,
					random.nextInt(20) - 10), new MyPoint(
					random.nextInt(20) - 10, random.nextInt(20) - 10),
					new MyPoint(random.nextInt(20) - 10,
							random.nextInt(20) - 10), new Color(
							random.nextInt()));
			// Perform a method call.
			tri.calculateArea();
			System.out.println(tri);
			trianglesAreEverywhere.put("theTriangleNumber" + (i + 1), tri);
		}
		
		// Lets create a factory so that we can auto-generate serializations.
		ParseFactory factory = new ParseFactory();
		// This is under-construction, but it kinda works.
		// As you can tell, there is no methods within the class Triangle
		// that generate code or serialize. All the magic takes place within
		// ParseFactory.
		factory.add(Triangle.class);
		
		File file = new File("triangles.txt");
		
		System.out.println("Now to save it to a file.");
		try {
			FileWriter fr = new FileWriter(file);
			BufferedWriter writer = new BufferedWriter(fr);
			HashIO.save(factory, trianglesAreEverywhere, writer, false); // false = do not compress.
			writer.flush();
			writer.close();
			fr.close();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		
		System.out.println("File size: " + file.length() + " bytes");
		
		// Now we load it up again.
		System.out.println("And the imported data from the file:");
		HashMap<String, Triangle> myOtherHash = new HashMap<String, Triangle>();
		try {
			FileReader fr = new FileReader(file);
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
			System.out.println(myOtherHash.get(keys[i]));
		}
	}
}

class Triangle {
	private MyPoint firstPoint;
	private MyPoint secondPoint;
	private MyPoint thirdPoint;
	private Color color;
	private float cachedArea = -1;

	public Triangle() {
		firstPoint = new MyPoint(4, 9);
		secondPoint = new MyPoint(6, 10);
		thirdPoint = new MyPoint(5, 5);
	}

	public Triangle(MyPoint fp, MyPoint sp, MyPoint tp, Color col) {
		firstPoint = fp;
		secondPoint = sp;
		thirdPoint = tp;
		color = col;
	}

	public void calculateArea() {
		// Uses Heron's Formula
		float a = (float) Math.sqrt(Math.pow(firstPoint.x - secondPoint.x, 2)
				+ Math.pow(firstPoint.y - secondPoint.y, 2));
		float b = (float) Math.sqrt(Math.pow(secondPoint.x - thirdPoint.x, 2)
				+ Math.pow(secondPoint.y - thirdPoint.y, 2));
		float c = (float) Math.sqrt(Math.pow(thirdPoint.x - firstPoint.x, 2)
				+ Math.pow(thirdPoint.y - firstPoint.y, 2));
		float s = (a + b + c) * 0.5f;
		cachedArea = (float) Math.sqrt(s * (s - a) * (s - b) * (s - c));
	}

	public boolean doesAreaExist() {
		return cachedArea >= 0;
	}

	public String toString() {
		return "First point: " + firstPoint + " Second point: " + secondPoint
				+ " Third point: " + thirdPoint + " Area: " + cachedArea
				+ " Color: " + color;
	}

	public void setColor(Color c) {
		color = c;
	}

	public Color getColor() {
		return color;
	}

	public void setFirstPoint(MyPoint p) {
		firstPoint = p;
	}

	public MyPoint getFirstPoint() {
		return firstPoint;
	}

	public void setSecondPoint(MyPoint p) {
		secondPoint = p;
	}

	public MyPoint getSecondPoint() {
		return secondPoint;
	}

	public void setThirdPoint(MyPoint p) {
		firstPoint = p;
	}

	public MyPoint getThirdPoint() {
		return thirdPoint;
	}
}

class MyPoint {
	public float x, y;

	public MyPoint() {

	}

	public MyPoint(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public String toString() {
		return "(" + x + ", " + y + ")";
	}
}