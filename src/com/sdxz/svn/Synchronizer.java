package com.sdxz.svn;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.ISVNLogMessageChangePath;
import org.tigris.subversion.svnclientadapter.SVNRevision;

public class Synchronizer {

	static String srcPath = "";
	static String desPath = "";

	public static void syncFile(SvnDomain[] domains) {
		for (SvnDomain domain : domains) {
			SVN.SVNPATH = domain.getSvnUrl();
			SVN.USERNAME = domain.getUsername();
			SVN.PASSWORD = domain.getPassword();
			desPath = domain.getLocalDestLocation();
			srcPath = domain.getLocalRepository();
			int[] versions = domain.getSvnVersions();
			for (int i = 0; i < versions.length; i++) {
				int n = versions[i];
				start(n);
			}
		}
	}

	public static void run() {
		// 版本号为单个或多个
		int[] versions = new int[] { 219166 };
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

		for (int i = 0; i < logMessages.length; i++) {
			ISVNLogMessage logMessage = logMessages[i];
			ISVNLogMessageChangePath[] changedPaths = logMessage.getChangedPaths();
			System.out.println(logMessage.getRevision() + "\t" + logMessage.getAuthor() + "\t");
			for (int j = 0; j < changedPaths.length; j++) {
				String path = changedPaths[j].getPath();
				System.out.println("\t path： \t" + path);

				if (changedPaths[j].getAction() == 'D') {
					continue;
				}
				if (!isSameProject(path)) {
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

	/**
	 * @author:李小龙
	 * @createTime:2016年5月25日下午3:25:20
	 * @discription: 当前检出的文件是否属于目前的工程。
	 * @modify:
	 */
	static boolean isSameProject(String path) {
		String svnPath = SVN.SVNPATH;
		svnPath = svnPath.replaceAll("svn://10.110.1.24/svn/yaojian/", "/").replaceAll("/trunk", "/");
		String filePath = path.split("trunk")[0];
		return svnPath.equals(filePath);
	}

	private static void copy(FilePath file, String desPath) {
		String fileName = file.getContext() + FilePath.separator + file.getFileName();
		String sourcePath = file.getBase() + fileName;
		String destPath = desPath + fileName;
		if (new File(sourcePath).exists()) {
			System.out.println("\t file found. copy from :\t" + sourcePath + " \n");
			FileCopy.copy(sourcePath, destPath);
		} else {
			System.err.println("\t Not found: \t" + sourcePath + "\n");
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
		String spath = path;
		path = path.replaceFirst("/src.*/com/", "/webapp/WEB-INF/classes/com/");
		System.out.println("\t befor replace : " + spath + " \t after replace : " + path + "\t");
		filePath.setContext(path);
	}

	public static void main(String[] args) {
		String path = "/G3/dtd/dtd_food/samplingInspect/trunk/src/com/inspur/";
		// path = path.replaceFirst("/src.*com/", "+++++");
		path = path.replaceFirst("/src.*/com/", "/webapp/WEB-INF/classes/com/");
		System.out.println(" \t after replace : " + path + "\t");

	}
}
