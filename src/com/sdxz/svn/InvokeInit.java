package com.sdxz.svn;

public class InvokeInit {

	public static void main(String[] args) {

		SVN.setup();
		int[] svnVersions = new int[] { 219166 };
		SvnDomain domain1 = new SvnDomain();
		domain1.setLocalRepository("E:/workspace/dtdCommon4Sd");
		domain1.setSvnUrl("svn://10.110.1.24/svn/yaojian/G3/dtd/dtd_food/commonForSdFood/trunk");
		domain1.setSvnVersions(svnVersions);

		SvnDomain domain2 = new SvnDomain();
		domain2.setLocalRepository("E:/workspace/samplingInspect");
		domain2.setSvnUrl("svn://10.110.1.24/svn/yaojian/G3/dtd/dtd_food/samplingInspect/trunk");
		domain2.setSvnVersions(svnVersions);

		SvnDomain[] domains = new SvnDomain[] { domain1, domain2 };
		Synchronizer.syncFile(domains);
	}
}
