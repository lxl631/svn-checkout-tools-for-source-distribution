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
		//设置用户名和密码
		svnClient.setUsername("lixlrj");
		svnClient.setPassword("lixlrj");
		// svnClient.setUsername("baizheng");
		// svnClient.setPassword("9719048");
		SVNUrl url = null;
		SVNRevision beginRevision = beginNumber;
		SVNRevision endRevision = endNumber;
		try {
			url = new SVNUrl("svn://10.110.1.24/svn/yaojian/G3/dtd/dtd_food/samplingInspect/trunk");
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
