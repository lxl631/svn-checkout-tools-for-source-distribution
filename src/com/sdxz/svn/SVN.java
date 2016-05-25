package com.sdxz.svn;

import java.net.MalformedURLException;

import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.SVNClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import org.tigris.subversion.svnclientadapter.javahl.JhlClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.javasvn.JavaSvnClientAdapterFactory;

public class SVN {

	static SVNUrl url = null; 
	static final String SVNPATH = "svn://10.110.1.24/svn/yaojian/G3/dtd/dtd_food/commonForSdFood/trunk";
	static final String USERNAME = "lixlrj";
	static final String PASSWORD = "lixlrj";

	public static void setup() {
		try {
			JhlClientAdapterFactory.setup();
		} catch (SVNClientException e) {
			System.out.println(e.getLocalizedMessage());
		}
		try {
			JavaSvnClientAdapterFactory.setup();
		} catch (SVNClientException e) {
			e.printStackTrace();
		}
	}

	public static ISVNLogMessage[] getLogMessages(SVNRevision beginNumber, SVNRevision endNumber) {
		ISVNClientAdapter svnClient = null;
		try {
			String bestClientType = SVNClientAdapterFactory.getPreferredSVNClientType();
			svnClient = SVNClientAdapterFactory.createSVNClient(bestClientType);
		} catch (SVNClientException e) {
			e.printStackTrace();
		}
		// 设置用户名和密码
		svnClient.setUsername(USERNAME);
		svnClient.setPassword(PASSWORD);

		SVNRevision beginRevision = beginNumber;
		SVNRevision endRevision = endNumber;
		try {
			url = new SVNUrl(SVNPATH);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		ISVNLogMessage[] logMessages = null;
		try {
			logMessages = svnClient.getLogMessages(url, beginRevision, endRevision);
		} catch (SVNClientException e) {
			e.printStackTrace();
		}
		return logMessages;
	}
}
