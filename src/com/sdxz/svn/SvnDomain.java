package com.sdxz.svn;

public class SvnDomain {

	private String username;

	private int[] svnVersions;

	private String localDestLocation;

	public String getLocalDestLocation() {
		return localDestLocation;
	}

	public void setLocalDestLocation(String localDestLocation) {
		this.localDestLocation = localDestLocation;
	}

	public int[] getSvnVersions() {
		return svnVersions;
	}

	public void setSvnVersions(int[] svnVersions) {
		this.svnVersions = svnVersions;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getLocalRepository() {
		return localRepository;
	}

	public void setLocalRepository(String localRepository) {
		this.localRepository = localRepository;
	}

	public String getSvnUrl() {
		return svnUrl;
	}

	public void setSvnUrl(String svnUrl) {
		this.svnUrl = svnUrl;
	}

	private String password;
	private String localRepository;
	private String svnUrl;
}
