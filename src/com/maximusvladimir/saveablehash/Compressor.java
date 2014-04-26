package com.maximusvladimir.saveablehash;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import de.bwaldvogel.base91.Base91;

public class Compressor {
	public static String decompress(String data) {
		try {
			ByteArrayOutputStream baos;
			ByteArrayInputStream bais = new ByteArrayInputStream(Base91.decodeByte(data));
			GZIPInputStream zis = null;
			byte[] buffer = new byte[8192];
			baos = new ByteArrayOutputStream();
			buffer = new byte[1024];
			zis = new GZIPInputStream(bais);
			for (int len; (len = zis.read(buffer, 0, 1024)) != -1;) {
				baos.write(buffer, 0, len);
			}
			zis.close();
			bais.close();
			baos.close();
			byte[] bytes = baos.toByteArray();
			return new String(bytes);
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}
	}

	public static String compress(String data) {
		try {
			byte[] baFileContent = data.getBytes();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			GZIPOutputStream zos;
			zos = new GZIPOutputStream(baos);
			zos.write(baFileContent);
			zos.close();
			baos.close();
			byte[] baFileContentCompressed = baos.toByteArray();
			return Base91.encodeByte(baFileContentCompressed);
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}
	}
}
