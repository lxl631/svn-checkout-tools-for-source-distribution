package com.sdxz.svn;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileCopy {

	public static boolean copy(String file1, String file2) {
		try {
			java.io.File file_in = new java.io.File(file1);
			java.io.File file_out = new java.io.File(file2);

			if (file_in.isDirectory())
				return false;

			if (!createPath(file_out.getParent()))
				throw new RuntimeException("系统不能创建指定路径：" + file2);

			if (copy_(file_in, file_out))
				return true; // if success then return true
			else
				return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false; // if fail then return false
		}
	}

	public static boolean copy_(java.io.File file_in, java.io.File file_out) {
		FileInputStream in1 = null;
		FileOutputStream out1 = null;

		try {
			in1 = new FileInputStream(file_in);
			out1 = new FileOutputStream(file_out);

			byte[] bytes = new byte[1024];
			int c;
			while ((c = in1.read(bytes)) != -1)
				out1.write(bytes, 0, c);
		} catch (Exception e) {
			return false;
		} finally {
			if (in1 != null) {
				try {
					in1.close();
				} catch (IOException e) {
				}
			}
			if (out1 != null) {
				try {
					out1.close();
				} catch (IOException e) {
				}
			}
		}
		return true;
	}

	public static boolean createPath(String path) {
		File file = new File(path);
		if (file.exists()) {
			if (file.isDirectory())
				return true;
			else
				return false;
		}

		if (createPath(file.getParent())) {
			if (!file.mkdir())
				return false;
		} else
			return false;
		return true;
	}

	public static File[] findFile(final String path, final String fileName) {
		File dir = new File(path);
		return dir.listFiles(new FileFilter() {

			public boolean accept(File file) {
				String name = file.getName();
				if (name.matches(fileName))
					return true;
				else
					return false;
			}
		});
	}

}
