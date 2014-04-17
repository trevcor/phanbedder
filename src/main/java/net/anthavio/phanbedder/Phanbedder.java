package net.anthavio.phanbedder;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * @author martin.vanek
 *
 */
public class Phanbedder {

	public static final String PHANTOMJS_VERSION = "1.9.7";

	public static File unpack() {
		String javaIoTmpdir = System.getProperty("java.io.tmpdir");
		//multiple versions can coexist
		return unpack(new File(javaIoTmpdir, "phantomjs-" + PHANTOMJS_VERSION));
	}

	public static String unpack(String directory) {
		File file = unpack(new File(directory));
		return file.getAbsolutePath();
	}

	public static File unpack(File directory) {
		if (!directory.exists()) {
			if (!directory.mkdirs()) {
				throw new IllegalArgumentException("Failed to make target directory: " + directory);
			}
		}

		File file;
		boolean chmodx;
		String osname = System.getProperty("os.name").toLowerCase();
		if (osname.contains("win")) {
			file = new File(directory, "phantomjs.exe");
			unpack("windows/phantomjs.exe", file);
			chmodx = false;
		} else if (osname.contains("mac os")) {
			file = new File(directory, "phantomjs");
			unpack("macosx/phantomjs", file);
			chmodx = true;

		} else if (osname.contains("linux")) {
			file = new File(directory, "phantomjs");
			//Linux has i386 or amd64
			String osarch = System.getProperty("os.arch");
			if (osarch.equals("i386")) {
				unpack("linux86/phantomjs", file);
			} else {
				unpack("linux64/phantomjs", file);
			}
			chmodx = true;

		} else {
			throw new IllegalArgumentException("Unsupported OS " + osname);
		}

		if (chmodx) {
			if (!file.setExecutable(true)) {
				throw new IllegalArgumentException("Failed to make executable " + file);
			}
		}

		return file;

		/*
		if (chmodx) {
			String cmd = "chmod +x " + path;
			Runtime.getRuntime().exec(cmd);
		}
		*/
	}

	private static void unpack(String resource, File target) {
		ClassLoader classLoader = Phanbedder.class.getClassLoader(); //same jarfile -> same classloader
		InputStream ras = classLoader.getResourceAsStream(resource);
		if (ras == null) {
			throw new IllegalStateException("Resource not found " + resource + " using ClassLoader " + classLoader);
		}
		BufferedInputStream input = new BufferedInputStream(ras);

		BufferedOutputStream output = null;
		try {
			output = new BufferedOutputStream(new FileOutputStream(target));

			while (input.available() > 0) {
				byte[] buffer = new byte[input.available()];
				input.read(buffer);
				output.write(buffer);
			}
			output.flush();

		} catch (Exception e) {
			throw new IllegalStateException("Failed to unpack resource: " + resource + " into: " + target);
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException iox) {
					//ignore
				}
			}
			try {
				input.close();
			} catch (IOException iox) {
				//ignore
			}
		}
	}

}