package com.sdxz.svn;

public class InvokeInit {
	static String USERNAME = "lixlrj"; // svn用户名
	static String PASSWORD = "lixlrj"; // svn密码
	static String LOCALDESTLOCATION = "E:/Package/SVNTarget/sample"; // 本地打包后的路径。

	public static void main(String[] args) {

		SVN.setup();
		int[] svnVersions = new int[] { 219166 };
		SvnDomain domain1 = new SvnDomain();
		domain1.setLocalRepository("E:/workspace/dtdCommon4Sd");
		domain1.setSvnUrl("svn://10.110.1.24/svn/yaojian/G3/dtd/dtd_food/commonForSdFood/trunk");
		domain1.setSvnVersions(svnVersions);
		domain1.setUsername(USERNAME);
		domain1.setPassword(PASSWORD);
		domain1.setLocalDestLocation(LOCALDESTLOCATION);

		SvnDomain domain2 = new SvnDomain();
		domain2.setLocalRepository("E:/workspace/samplingInspect");
		domain2.setSvnUrl("svn://10.110.1.24/svn/yaojian/G3/dtd/dtd_food/samplingInspect/trunk");
		domain2.setSvnVersions(svnVersions);
		domain2.setUsername(USERNAME);
		domain2.setPassword(PASSWORD);
		domain2.setLocalDestLocation(LOCALDESTLOCATION);

		SvnDomain[] domains = new SvnDomain[] { domain1, domain2 };
		Synchronizer.syncFile(domains);
	}
}
