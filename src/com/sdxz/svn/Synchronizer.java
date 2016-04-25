package com.sdxz.svn;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.ISVNLogMessageChangePath;
import org.tigris.subversion.svnclientadapter.SVNRevision;

public class Synchronizer {

	public static void main(String[] args) {
		SVN.setup();
		// 版本号为单个或多个
		int[] versions = new int[] { 161936, 160989, 190215 };
		for (int i = 0; i < versions.length; i++) {
			int n = versions[i];
			start(n);
		}

		// 版本号范围 如：10000--20000
		/*
		 * for (int i=400; i<=450; i++){ start(i); }
		 */
	}

	private static void start(int number) {
		SVNRevision beginNumber = new SVNRevision.Number(number);
		SVNRevision endNumber = new SVNRevision.Number(number);
		ISVNLogMessage[] logMessages = SVN.getLogMessages(beginNumber, endNumber);
		String srcPath = "E:/workspace/samplingInspect/";
		String desPath = "E:/DevelopEnvironment/SVNTarget/sample";
		for (int i = 0; i < logMessages.length; i++) {
			ISVNLogMessage logMessage = logMessages[i];
			ISVNLogMessageChangePath[] changedPaths = logMessage.getChangedPaths();
			System.out.println(logMessage.getRevision() + "\t" + logMessage.getAuthor() + "\t");
			for (int j = 0; j < changedPaths.length; j++) {
				String path = changedPaths[j].getPath();
				System.out.println("\t path \t" + path);
				if (changedPaths[j].getAction() == 'D') {
					continue;
				}
				FilePath filePath = new FilePath();
				filePath.setBase(srcPath);

				String npath = path.split("trunk")[1];
				filePath.setContext(npath.substring(0, npath.lastIndexOf(FilePath.separator)));
				filePath.setFileName(path.substring(path.lastIndexOf(FilePath.separator) + 1));
				// System.out.print("\t\r\n filePath:\r\n" +filePath+"\r\n");
				replaceSrcWebRoot(filePath);
				if (filePath.getExtension() != null && filePath.getExtension().equals("java")) {
					filePath.setExtension("class");
					// System.out.println("\t" + filePath.getContextPath());
					copy(filePath, desPath);
					for (Iterator iter = getJavaRelativeFiles(filePath).iterator(); iter.hasNext();) {
						FilePath file = (FilePath) iter.next();
						// System.out.println("\t\t" + file.getContextPath());
						copy(file, desPath);
					}
				} else {
					// System.out.println("\t" + filePath.getContextPath());
					copy(filePath, desPath);
				}
			}
		}
	}

	private static void copy(FilePath file, String desPath) {
		String fileName = file.getContext() + FilePath.separator + file.getFileName();
		String sourcePath = file.getBase() + fileName;
		String destPath = desPath + fileName;
		if (new File(sourcePath).exists()) {
			System.out.println("\t file found. copy from :\t" + sourcePath);
			FileCopy.copy(sourcePath, destPath);
		} else {
			System.err.println("\t Not found: \t" + sourcePath);
		}
	}

	private static List getNameTemplates(FilePath filePath) {
		List nameTemplates = new ArrayList();
		nameTemplates.add("^" + filePath.getName() + "[$][^.]+[.]class$");
		return nameTemplates;
	}

	private static List getJavaRelativeFiles(FilePath filePath1) {
		List nameTemplates = getNameTemplates(filePath1);
		List allFiles = new ArrayList();
		if (nameTemplates.size() > 0) {
			for (Iterator iter = nameTemplates.iterator(); iter.hasNext();) {
				String fileName = (String) iter.next();
				File[] files = FileCopy.findFile(filePath1.getBase() + filePath1.getContext(), fileName);
				if (files != null && files.length > 0) {
					for (int k = 0; k < files.length; k++) {
						File file = files[k];
						FilePath filePath2 = new FilePath(filePath1);
						filePath2.setFileName(file.getName());
						allFiles.add(filePath2);
					}
				}
			}
		}
		return allFiles;
	}

	/*
	 * private static void replaceSrcWebRoot(FilePath filePath) { String path =
	 * filePath.getContext(); if ((path).matches(
	 * "^/((src_lib/[^/]+/.*$)|(src((/.*$)|$))|(WebRoot((/.*)|$)))")) { path =
	 * path.replaceFirst("^/((src_lib/[^/]+/)|(src/))", "/WEB-INF/classes/");
	 * path = path.replaceFirst("^/src$", "/WEB-INF/classes/"); path =
	 * path.replaceFirst("^/WebRoot", "/"); } filePath.setContext(path); }
	 */

	private static void replaceSrcWebRoot(FilePath filePath) {
		String path = filePath.getContext();
		path = path.replaceFirst("/src/.*/com/", "/webapp/WEB-INF/classes/com/");
		System.err.println(path);
		filePath.setContext(path);
	}
}
