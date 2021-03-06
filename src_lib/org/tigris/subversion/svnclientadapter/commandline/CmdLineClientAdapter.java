/*
 *  Copyright(c) 2003-2004 by the authors indicated in the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.tigris.subversion.svnclientadapter.commandline;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.tigris.subversion.svnclientadapter.AbstractClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNAnnotations;
import org.tigris.subversion.svnclientadapter.ISVNDirEntry;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;
import org.tigris.subversion.svnclientadapter.ISVNProperty;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNBaseDir;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNConstants;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNStatusUnversioned;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import org.tigris.subversion.svnclientadapter.StringUtils;

/**
 * <p>
 * Implements a <tt>ISVNClientAdapter</tt> using the
 * Command line client. This expects the <tt>svn</tt>
 * executible to be in the path.</p>
 * 
 * @author Philip Schatz (schatz at tigris)
 * @author C~dric Chabanois (cchabanois at no-log.org)
 */
public class CmdLineClientAdapter extends AbstractClientAdapter {

	//Fields
    private CmdLineNotificationHandler notificationHandler = new CmdLineNotificationHandler();
	private SvnCommandLine _cmd = new SvnCommandLine("svn",notificationHandler);
	private SvnMultiArgCommandLine _cmdMulti = new SvnMultiArgCommandLine("svn",notificationHandler);
	private SvnAdminCommandLine svnAdminCmd = new SvnAdminCommandLine("svnadmin",notificationHandler);
    private String version = null;

    private static boolean availabilityCached = false;
    private static boolean available;
    
	//Methods
	public static boolean isAvailable() {
		// availabilityCached flag must be reset if location of client changes
		if (!availabilityCached) {
			// this will need to be fixed when path to svn will be customizable
			SvnCommandLine cmd = new SvnCommandLine("svn", new CmdLineNotificationHandler());
			try {
				cmd.version();
	    		available = true;
			} catch (Exception e) {
				available = false;
			}
			availabilityCached = true;
		}
		return available;
	}
    
    /**
     * @return something like "svn, version 0.35.1 (r8050)"
     */
    public String getVersion() throws SVNClientException {
        if (version != null)
            return version;
        try {
            // we don't want to log this ...
            notificationHandler.disableLog();
            version = _cmd.version();
            int i = version.indexOf("\n\r");
            version = version.substring(0,i);
            return version;
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e); 
        } finally {
            notificationHandler.enableLog();
        }
    }
 
	/* (non-Javadoc)
	 * @see org.tigris.subversion.subclipse.client.ISVNClientAdapter#addNotifyListener(org.tigris.subversion.subclipse.client.ISVNClientNotifyListener)
	 */
	public void addNotifyListener(ISVNNotifyListener listener) {
        notificationHandler.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.subclipse.client.ISVNClientAdapter#removeNotifyListener(org.tigris.subversion.subclipse.client.ISVNClientNotifyListener)
	 */
	public void removeNotifyListener(ISVNNotifyListener listener) {
        notificationHandler.remove(listener);
	}

    private boolean isManagedDir(File dir) {
        File entries = new File(dir, SVNConstants.SVN_DIRNAME + "/entries");
        return entries.exists();
    }

	/* (non-Javadoc)
	 * @see org.tigris.subversion.subclipse.client.ISVNClientAdapter#getStatus(java.io.File[])
	 */
	public ISVNStatus[] getStatus(File[] files) throws SVNClientException {
        
        ISVNStatus[] statuses = new ISVNStatus[files.length]; 
        
        // all files that are in nonmanaged dirs are unversioned
        // all directories that do not have a .svn dir are not versioned
        ArrayList pathsList = new ArrayList();
        for (int i = 0; i < files.length;i++) {
            File file = files[i];
            File dir;
            if (file.isDirectory()) {
                dir = file;
            } else
            {
                dir = file.getParentFile();
            }
            if (isManagedDir(dir)) {
                pathsList.add(toString(file));
            } else {
                statuses[i] = new SVNStatusUnversioned(file,false);
            }
        }
        String[] paths = (String[])pathsList.toArray(new String[pathsList.size()]);
        
        // we must do a svn status and svn info only on resources that are in versioned dirs 
        // because otherwise svn will stop after the first "svn: 'resource' is not a working copy" 
        CmdLineStatuses cmdLineStatuses;
        try {
            String cmdLineInfoStrings = _cmd.info(paths);
            String cmdLineStatusStrings = _cmd.status(paths, false, true, false);
            cmdLineStatuses = new CmdLineStatuses(cmdLineInfoStrings,cmdLineStatusStrings);
		} catch (CmdLineException e) {
			throw SVNClientException.wrapException(e);
		}
        
        for (int i = 0; i < cmdLineStatuses.size();i++) {
            ISVNStatus status = cmdLineStatuses.get(i);
            for (int j=0; j < files.length;j++) {
                if (files[j].getAbsoluteFile().equals(status.getFile())) {
                    statuses[j] = status;
                }
            }
        }
        
        return statuses;        
	}

    /*
     * (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#getSingleStatus(java.io.File)
     */
    public ISVNStatus getSingleStatus(File path) 
             throws SVNClientException {
        return getStatus(new File[] {path})[0];
    }

    private ISVNDirEntry[] getList(String target, SVNRevision rev, boolean recursive)
		throws SVNClientException {
	
		byte[] listXml;
		try {
			listXml = _cmd.list(target, toString(rev), recursive);	
			return CmdLineRemoteDirEntry.createDirEntries(listXml);
		} catch (CmdLineException e) {
			throw SVNClientException.wrapException(e);
		}
	}    
    
	/* (non-Javadoc)
	 * @see org.tigris.subversion.subclipse.client.ISVNClientAdapter#getList(java.net.URL, org.tigris.subversion.subclipse.client.ISVNRevision, boolean)
	 */
	public ISVNDirEntry[] getList(SVNUrl svnUrl, SVNRevision revision, boolean recurse)
		throws SVNClientException {
		return getList(toString(svnUrl), revision, recurse);
	}
	
	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#getList(java.io.File, org.tigris.subversion.svnclientadapter.SVNRevision, boolean)
	 */
	public ISVNDirEntry[] getList(File path, SVNRevision revision,
			boolean recurse) throws SVNClientException {
		return getList(toString(path), revision, recurse);
	}

	/*
	 * (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#getDirEntry(org.tigris.subversion.svnclientadapter.SVNUrl, org.tigris.subversion.svnclientadapter.SVNRevision)
	 */
	public ISVNDirEntry getDirEntry(SVNUrl url, SVNRevision revision)
		throws SVNClientException {
		
		// list give the DirEntrys of the elements of a directory or the DirEntry
		// of a file
		ISVNDirEntry[] entries = getList(url.getParent(),revision,false);
		
		String expectedPath = url.getLastPathSegment();
		for (int i = 0; i < entries.length;i++) {
			if (entries[i].getPath().equals(expectedPath)) {
				return entries[i];
			}
		}
		return null; // not found
	}
	
	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#getDirEntry(java.io.File, org.tigris.subversion.svnclientadapter.SVNRevision)
	 */
	public ISVNDirEntry getDirEntry(File path, SVNRevision revision)
			throws SVNClientException {
		// list give the DirEntrys of the elements of a directory or the DirEntry
		// of a file
		ISVNDirEntry[] entries = getList(path.getParentFile(),revision,false);
		
		String expectedPath = path.getName();
		for (int i = 0; i < entries.length;i++) {
			if (entries[i].getPath().equals(expectedPath)) {
				return entries[i];
			}
		}
		return null; // not found
	}
	
	/* (non-Javadoc)
	 * @see org.tigris.subversion.subclipse.client.ISVNClientAdapter#remove(java.io.File[], boolean)
	 */
	public void remove(File[] files, boolean force) throws SVNClientException {
		String[] paths = new String[files.length];
		try {
			for (int i = 0; i < files.length; i++) {
				paths[i] = files[i].toString();
			}
			_cmd.delete(paths, null,force);
		} catch (CmdLineException e) {
			throw SVNClientException.wrapException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.subclipse.client.ISVNClientAdapter#revert(java.io.File, boolean)
	 */
	public void revert(File file, boolean recursive) throws SVNClientException {
		try {
			notificationHandler.setBaseDir(SVNBaseDir.getBaseDir(file));
			_cmd.revert(new String[] { toString(file) }, recursive);
		} catch (CmdLineException e) {
			throw SVNClientException.wrapException(e);
		}

	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.subclipse.client.ISVNClientAdapter#getContent(java.net.SVNUrl, org.tigris.subversion.subclipse.client.ISVNRevision)
	 */
	public InputStream getContent(SVNUrl arg0, SVNRevision arg1) throws SVNClientException {

		try {
			InputStream content = _cmd.cat(toString(arg0), toString(arg1));

			//read byte-by-byte and put it in a vector.
			//then take the vector and fill a byteArray.
			byte[] byteArray;
			byteArray = streamToByteArray(content);
			return new ByteArrayInputStream(byteArray);
		} catch (IOException e) {
			throw SVNClientException.wrapException(e);
		} catch (CmdLineException e) {
			throw SVNClientException.wrapException(e);
		}

	}

	/*
	 * (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#getContent(java.io.File, org.tigris.subversion.svnclientadapter.SVNRevision)
	 */
	public InputStream getContent(File path, SVNRevision revision) throws SVNClientException {

		try {
			InputStream content = _cmd.cat(toString(path), toString(revision));

			//read byte-by-byte and put it in a vector.
			//then take the vector and fill a byteArray.
			byte[] byteArray;
			byteArray = streamToByteArray(content);
			return new ByteArrayInputStream(byteArray);
		} catch (IOException e) {
			throw SVNClientException.wrapException(e);
		} catch (CmdLineException e) {
			throw SVNClientException.wrapException(e);
		}

	}


	/* (non-Javadoc)
	 * @see org.tigris.subversion.subclipse.client.ISVNClientAdapter#mkdir(java.net.URL, java.lang.String)
	 */
	public void mkdir(SVNUrl arg0, String arg1) throws SVNClientException {
		try {
			_cmd.mkdir(toString(arg0), arg1);
		} catch (CmdLineException e) {
			throw SVNClientException.wrapException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.subclipse.client.ISVNClientAdapter#getLogMessages(java.net.URL, org.tigris.subversion.subclipse.client.ISVNRevision, org.tigris.subversion.subclipse.client.ISVNRevision, boolean)
	 */
	public ISVNLogMessage[] getLogMessages(SVNUrl url, SVNRevision revStart, SVNRevision revEnd, boolean fetchChangePath)
		throws SVNClientException {
        return getLogMessages((Object) url, revStart, revEnd, fetchChangePath);
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.subclipse.client.ISVNClientAdapter#remove(java.net.URL[], java.lang.String)
	 */
	public void remove(SVNUrl[] urls, String message) throws SVNClientException {
		String[] urlsStrings = new String[urls.length];
		for (int i = 0; i < urls.length; i++) {
			urlsStrings[i] = urls[i].toString();
		}
		try {
			_cmd.delete(urlsStrings, message,false);
		} catch (CmdLineException e) {
			throw SVNClientException.wrapException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.subclipse.client.ISVNClientAdapter#copy(java.net.URL, java.net.URL, java.lang.String, org.tigris.subversion.subclipse.client.ISVNRevision)
	 */
	public void copy(SVNUrl src, SVNUrl dest, String message, SVNRevision rev)
		throws SVNClientException {
		try {
			_cmd.copy(toString(src), toString(dest), message, toString(rev));
		} catch (CmdLineException e) {
			throw SVNClientException.wrapException(e);
		}
	}

    /*
     * (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#copy(java.io.File, java.io.File)
     */
	public void copy(File srcPath, File destPath) throws SVNClientException {
		try {
			_cmd.copy(toString(srcPath), toString(destPath));
		} catch (CmdLineException e) {
			throw SVNClientException.wrapException(e);
		}
		//sometimes the dir has not yet been created.
		//wait up to 5 sec for the dir to be created.
		for (int i = 0; i < 50 && !destPath.exists(); i++) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e2) {
				//do nothing if interrupted
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.subclipse.client.ISVNClientAdapter#move(java.net.URL, java.net.URL, java.lang.String, org.tigris.subversion.subclipse.client.ISVNRevision)
	 */
	public void move(SVNUrl url, SVNUrl destUrl, String message, SVNRevision revision)
		throws SVNClientException {
		try {
			notificationHandler.setBaseDir(new File("."));
			_cmd.move(toString(url), toString(destUrl), message, toString(revision), false);
		} catch (CmdLineException e) {
			throw SVNClientException.wrapException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.subclipse.client.ISVNClientAdapter#move(java.io.File, java.io.File, boolean)
	 */
	public void move(File file, File file2, boolean force) throws SVNClientException {
		try {
			notificationHandler.setBaseDir(SVNBaseDir.getBaseDir(new File[] {file,file2}));
			_cmd.move(toString(file), toString(file2), null, null, force);
		} catch (CmdLineException e) {
			throw SVNClientException.wrapException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.subclipse.client.ISVNClientAdapter#setUsername(java.lang.String)
	 */
	public void setUsername(String string) {
		if (string == null || string.length() == 0)
			return;
		_cmd.setUsername(string);
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.subclipse.client.ISVNClientAdapter#setPassword(java.lang.String)
	 */
	public void setPassword(String password) {
		if (password == null)
			return;

		_cmd.setPassword(password);
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.subclipse.client.ISVNClientAdapter#addDirectory(java.io.File, boolean)
	 */
	public void addDirectory(File file, boolean recurse) throws SVNClientException {
		try {
			notificationHandler.setBaseDir(SVNBaseDir.getBaseDir(file));
			_cmd.add(toString(file), recurse);
		} catch (CmdLineException e) {
			//if something is already in svn and we
			//try to add it, we get a warning.
			//ignore it.\
			if (e.getMessage().startsWith("svn: warning: "))
				return;
			throw SVNClientException.wrapException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.subclipse.client.ISVNClientAdapter#addFile(java.io.File)
	 */
	public void addFile(File file) throws SVNClientException {
		try {
			notificationHandler.setBaseDir(SVNBaseDir.getBaseDir(file));
			_cmd.add(toString(file), false);
		} catch (CmdLineException e) {
			//if something is already in svn and we
			//try to add it, we get a warning.
			//ignore it.\
			if (e.getMessage().startsWith("svn: warning: "))
				return;
			throw SVNClientException.wrapException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.subclipse.client.ISVNClientAdapter#commit(java.io.File[], java.lang.String, boolean)
	 */
	public long commit(File[] parents, String comment, boolean recurse) throws SVNClientException {
		return commit(parents, comment, recurse , false);
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.subclipse.client.ISVNClientAdapter#commit(java.io.File[], java.lang.String, boolean, boolean)
	 */
	public long commit(File[] parents, String comment, boolean recurse, boolean keepLocks) throws SVNClientException {
		String[] paths = new String[parents.length];
		for (int i = 0; i < parents.length; i++) {
			paths[i] = toString(parents[i]);
		}
		try {
			notificationHandler.setBaseDir(SVNBaseDir.getBaseDir(parents));
			_cmd.checkin(paths, comment, keepLocks);
			return _cmd.getRevision();
		} catch (CmdLineException e) {
			if ("".equals(e.getMessage()))
				return SVNRevision.SVN_INVALID_REVNUM;
			if (e.getMessage().startsWith("svn: Attempted to lock an already-locked dir")) {
				//PHIL is this the best way to handle pending locks? (ie caused by "svn cp")
				//loop through up to 5 sec, waiting for locks
				//to be removed.
				for (int i = 0; i < 50; i++) {
					try {
						notificationHandler.setBaseDir(SVNBaseDir.getBaseDir(parents));
						_cmd.checkin(paths, comment, keepLocks);
						return _cmd.getRevision();
					} catch (CmdLineException e1) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e2) {
							//do nothing if interrupted
						}
					}
				}
			}
			throw SVNClientException.wrapException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.subclipse.client.ISVNClientAdapter#update(java.io.File, org.tigris.subversion.subclipse.client.ISVNRevision, boolean)
	 */
	public long update(File file, SVNRevision revision, boolean b) throws SVNClientException {
		try {
			notificationHandler.setBaseDir(SVNBaseDir.getBaseDir(file));
			_cmd.update(toString(file), toString(revision));
            return _cmd.getRevision();
		} catch (CmdLineException e) {
			throw SVNClientException.wrapException(e);
		}
	}

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#update(java.io.File[], org.tigris.subversion.svnclientadapter.SVNRevision, boolean, boolean)
     */
    public long[] update(File[] files, SVNRevision revision, boolean recurse, boolean ignoreExternals) throws SVNClientException
    {
		try {
			notificationHandler.setBaseDir(SVNBaseDir.getBaseDir(files[0]));
			_cmdMulti.update(toString(files), toString(revision));
            return _cmdMulti.getRevisions();
		} catch (CmdLineException e) {
			throw SVNClientException.wrapException(e);
		}    	
    }

	/* (non-Javadoc)
	 * @see org.tigris.subversion.subclipse.client.ISVNClientAdapter#checkout(java.net.URL, java.io.File, org.tigris.subversion.subclipse.client.ISVNRevision, boolean)
	 */
	public void checkout(SVNUrl url, File destPath, SVNRevision revision, boolean b)
		throws SVNClientException {
		try {
			notificationHandler.setBaseDir(SVNBaseDir.getBaseDir(destPath));
			_cmd.checkout(toString(url), toString(destPath), toString(revision), b);
		} catch (CmdLineException e) {
			throw SVNClientException.wrapException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.subclipse.client.ISVNClientAdapter#getStatusRecursively(java.io.File,boolean)
	 */
    public ISVNStatus[] getStatus(File path, boolean descend, boolean getAll)     
	   throws SVNClientException {
		try {
			// first we get the status of the files
            String statusLinesString = _cmd.status(new String[] { toString(path) },descend,getAll, false);

            String[] parts = StringUtils.split(statusLinesString,Helper.NEWLINE);
            CmdLineStatusPart[] cmdLineStatusParts = new CmdLineStatusPart[parts.length];
            String[] targetsInfo = new String[parts.length];
            for (int i = 0; i < parts.length;i++) {
                cmdLineStatusParts[i] = new CmdLineStatusPart(parts[i]);
                targetsInfo[i] = cmdLineStatusParts[i].getFile().toString();
            }

            // this is not enough, so we get info from the files
            String infoLinesString = _cmd.info(targetsInfo);
                 
            parts = CmdLineInfoPart.parseInfoParts(infoLinesString);
            CmdLineInfoPart[] cmdLineInfoParts = new CmdLineInfoPart[parts.length];
            for (int i = 0; i < parts.length;i++) {
                cmdLineInfoParts[i] = new CmdLineInfoPart(parts[i]);
            }                 

            CmdLineStatuses cmdLineStatuses = new CmdLineStatuses(cmdLineInfoParts, cmdLineStatusParts);
            
            return cmdLineStatuses.toArray();

		} catch (CmdLineException e) {
			if (e.getMessage().trim().matches("svn:.*is not a working copy.*")) {
				return new ISVNStatus[] {new SVNStatusUnversioned(path)};
			}
			throw SVNClientException.wrapException(e);
		}
	}

	private void diff(
		String oldPath,
		SVNRevision oldPathRevision,
		String newPath,
		SVNRevision newPathRevision,
		File outFile,
		boolean recurse) throws SVNClientException {
		if (newPath == null)
			newPath = oldPath;
		if (oldPathRevision == null)
			oldPathRevision = SVNRevision.BASE;
		if (newPathRevision == null)
			newPathRevision = SVNRevision.WORKING;

		try {
			InputStream is =
				_cmd.diff(
					oldPath,
					toString(oldPathRevision),
					newPath,
					toString(newPathRevision),
					recurse);

			streamToFile(is, outFile);
		} catch (IOException e) {
			//this should never happen
		} catch (CmdLineException e) {
			throw SVNClientException.wrapException(e);
		}
	}

    /*
     * (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#diff(java.io.File, org.tigris.subversion.svnclientadapter.SVNRevision, java.io.File, org.tigris.subversion.svnclientadapter.SVNRevision, java.io.File, boolean)
     */
	public void diff(
		File oldPath,
		SVNRevision oldPathRevision,
		File newPath,
		SVNRevision newPathRevision,
		File outFile,
		boolean recurse)
		throws SVNClientException {
		if (oldPath == null)
			oldPath = new File(".");
		diff(
			toString(oldPath),
			oldPathRevision,
			toString(newPath),
			newPathRevision,
			outFile,
			recurse);
	}

    /*
     * (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#diff(java.io.File, java.io.File, boolean)
     */
	public void diff(File path, File outFile, boolean recurse) throws SVNClientException {
		diff(path, null, null, null, outFile, recurse);
	}

    /*
     * (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#diff(org.tigris.subversion.svnclientadapter.SVNUrl, org.tigris.subversion.svnclientadapter.SVNRevision, org.tigris.subversion.svnclientadapter.SVNUrl, org.tigris.subversion.svnclientadapter.SVNRevision, java.io.File, boolean)
     */
	public void diff(
		SVNUrl oldUrl,
		SVNRevision oldUrlRevision,
		SVNUrl newUrl,
		SVNRevision newUrlRevision,
		File outFile,
		boolean recurse)
		throws SVNClientException {
		diff(toString(oldUrl), oldUrlRevision, toString(newUrl), newUrlRevision, outFile, recurse);
	}

    /*
     * (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#diff(org.tigris.subversion.svnclientadapter.SVNUrl, org.tigris.subversion.svnclientadapter.SVNRevision, org.tigris.subversion.svnclientadapter.SVNRevision, java.io.File, boolean)
     */
	public void diff(
		SVNUrl url,
		SVNRevision oldUrlRevision,
		SVNRevision newUrlRevision,
		File outFile,
		boolean recurse)
		throws SVNClientException {
		diff(url, oldUrlRevision, url, newUrlRevision, outFile, recurse);
	}

    /*
     * (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#propertyGet(java.io.File, java.lang.String)
     */
	public ISVNProperty propertyGet(File path, String propertyName) throws SVNClientException {
		try {
			InputStream valueAndData = _cmd.propget(toString(path), propertyName);
            
			byte[] bytes = streamToByteArray(valueAndData);
            if (bytes.length == 0) {
                return null; // the property does not exist
            }
            
			return new CmdLineProperty(propertyName, new String(bytes), path, bytes);
		} catch (CmdLineException e) {
			throw SVNClientException.wrapException(e);
		} catch (IOException e) {
			throw SVNClientException.wrapException(e);
		}
	}

    /*
     * (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#propertySet(java.io.File, java.lang.String, java.io.File, boolean)
     */
	public void propertySet(File path, String propertyName, File propertyFile, boolean recurse)
		throws SVNClientException, IOException {
		try {
			_cmd.propsetFile(propertyName, toString(propertyFile), toString(path), recurse);

			// there is no notification (Notify.notify is not called) when we set a property
			// so we will do notification ourselves
			ISVNStatus[] statuses = getStatus(path,recurse,false);
			for (int i = 0; i < statuses.length;i++) {
				notificationHandler.notifyListenersOfChange(statuses[i].getFile().getAbsolutePath());	
			}

		} catch (CmdLineException e) {
			throw SVNClientException.wrapException(e);
		}
	}

    /*
     * (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#propertyDel(java.io.File, java.lang.String, boolean)
     */
	public void propertyDel(File path, String propertyName, boolean recurse)
		throws SVNClientException {
            try {
                _cmd.propdel(propertyName, toString(path), recurse);

				// there is no notification (Notify.notify is not called) when we delete a property
				// so we will do notification ourselves
				ISVNStatus[] statuses = getStatus(path,recurse,false);
				for (int i = 0; i < statuses.length;i++) {
					notificationHandler.notifyListenersOfChange(statuses[i].getFile().getAbsolutePath());	
				}
                
            } catch (CmdLineException e) {
                throw SVNClientException.wrapException(e);
            }        
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#setRevProperty(org.tigris.subversion.svnclientadapter.SVNUrl, org.tigris.subversion.svnclientadapter.SVNRevision.Number, java.lang.String, java.lang.String, boolean)
	 */
	public void setRevProperty(SVNUrl path, SVNRevision.Number revisionNo, String propName, String propertyData, boolean force) throws SVNClientException {
		try {
			_cmd.revpropset(propName, propertyData, toString(path), Long.toString(revisionNo.getNumber()), force);
			// there is no notification to send

		} catch (CmdLineException e) {
			throw SVNClientException.wrapException(e);
		}
     }
    /*
     * (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#mkdir(java.io.File)
     */
	public void mkdir(File file) throws SVNClientException {
		try {
			_cmd.mkdir(toString(file));
		} catch (CmdLineException e) {
			throw SVNClientException.wrapException(e);
		}
		//sometimes the dir has not yet been created.
		//wait up to 5 sec for the dir to be created.
		for (int i = 0; i < 50 && !file.exists(); i++) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e2) {
				//do nothing if interrupted
			}
		}
	}

    /*
     * (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#doImport(java.io.File, org.tigris.subversion.svnclientadapter.SVNUrl, java.lang.String, boolean)
     */
	public void doImport(File path, SVNUrl url, String message, boolean recurse)
		throws SVNClientException {
        try {
            notificationHandler.setBaseDir(SVNBaseDir.getBaseDir(path));
            _cmd.importFiles(toString(path), toString(url), message, recurse);
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        }
	}

    /*
     * (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#doExport(org.tigris.subversion.svnclientadapter.SVNUrl, java.io.File, org.tigris.subversion.svnclientadapter.SVNRevision, boolean)
     */
	public void doExport(SVNUrl srcUrl, File destPath, SVNRevision revision, boolean force)
		throws SVNClientException {
		try {
			_cmd.export(toString(srcUrl), toString(destPath), toString(revision), force);
		} catch (CmdLineException e) {
			throw SVNClientException.wrapException(e);
		}
	}

    /*
     * (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#doExport(java.io.File, java.io.File, boolean)
     */
	public void doExport(File srcPath, File destPath, boolean force) throws SVNClientException {
		try {
			_cmd.export(toString(srcPath), toString(destPath), null, force);
		} catch (CmdLineException e) {
			throw SVNClientException.wrapException(e);
		}
	}

    /*
     * (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#copy(java.io.File, org.tigris.subversion.svnclientadapter.SVNUrl, java.lang.String)
     */
	public void copy(File srcPath, SVNUrl destUrl, String message) throws SVNClientException {
		try {
			_cmd.copy(toString(srcPath), toString(destUrl), message, null);
		} catch (CmdLineException e) {
			throw SVNClientException.wrapException(e);
		}
	}

    /*
     * (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#getLogMessages(java.io.File, org.tigris.subversion.svnclientadapter.SVNRevision, org.tigris.subversion.svnclientadapter.SVNRevision, boolean)
     */
	public ISVNLogMessage[] getLogMessages(
		File path,
		SVNRevision revStart,
		SVNRevision revEnd,
		boolean fetchChangePath)
		throws SVNClientException {
        return getLogMessages((Object) path, revStart, revEnd, fetchChangePath);
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#copy(org.tigris.subversion.svnclientadapter.SVNUrl, java.io.File, org.tigris.subversion.svnclientadapter.SVNRevision)
	 */
	public void copy(SVNUrl srcUrl, File destPath, SVNRevision revision)
		throws SVNClientException {
		try {
			_cmd.copy(toString(srcUrl), toString(destPath), null, toString(revision));
		} catch (CmdLineException e) {
			throw SVNClientException.wrapException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#propertySet(java.io.File, java.lang.String, java.lang.String, boolean)
	 */
	public void propertySet(File path, String propertyName, String propertyValue, boolean recurse)
		throws SVNClientException {
		try {
			_cmd.propset(propertyName, propertyValue, toString(path), recurse);

			// there is no notification (Notify.notify is not called) when we set a property
			// so we will do notification ourselves
			ISVNStatus[] statuses = getStatus(path,recurse,false);
			for (int i = 0; i < statuses.length;i++) {
				notificationHandler.notifyListenersOfChange(statuses[i].getFile().getAbsolutePath());	
			}
			
		} catch (CmdLineException e) {
			throw SVNClientException.wrapException(e);
		}
	}

    /**
     * A safe <code>toString()</code> implementation which implements
     * <code>null</code> checking on <code>obj</code>.
     */
	private static String toString(Object obj) {
		return (obj == null) ? null : obj.toString();
	}

    /**
     * Implementation used by overloads of <code>getLogMessages()</code>.
     *
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#getLogMessages(org.tigris.subversion.svnclientadapter.SVNUrl, org.tigris.subversion.svnclientadapter.SVNRevision, org.tigris.subversion.svnclientadapter.SVNRevision, boolean)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#getLogMessages(java.io.File, org.tigris.subversion.svnclientadapter.SVNRevision, org.tigris.subversion.svnclientadapter.SVNRevision, boolean)
     */
	private ISVNLogMessage[] getLogMessages(
		Object pathOrUrl,
		SVNRevision revisionStart,
		SVNRevision revisionEnd,
		boolean fetchChangePath)
		throws SVNClientException {
		String revRange = toString(revisionStart) + ":" +
            toString(revisionEnd);
		try {
            byte[] messages;

            // To acquire the paths associated with each delta, we'd
            // have to include the --verbose argument.
			if (fetchChangePath) {
                messages = _cmd.logVerbose(toString(pathOrUrl), revRange);
			} else {
                messages = _cmd.log(toString(pathOrUrl), revRange);
			}
			return CmdLineLogMessage.createLogMessages(messages);
        } catch (CmdLineException e) {
			throw SVNClientException.wrapException(e);
		}
	}

	private static void streamToFile(InputStream stream, File outFile) throws IOException {
		int tempByte;
		try {
			FileOutputStream os = new FileOutputStream(outFile);
			while ((tempByte = stream.read()) != -1) {
				os.write(tempByte);
			}
			os.close();
			stream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static byte[] streamToByteArray(InputStream stream)
		throws IOException {
		//read byte-by-byte and put it in a vector.
		//then take the vector and fill a byteArray.
		Vector buffer = new Vector(1024);
		int tempByte;
		while ((tempByte = stream.read()) != -1) {
			buffer.add(new Byte((byte) tempByte));
		}

		byte[] byteArray = new byte[buffer.size()];
		for (int i = 0; i < byteArray.length; i++) {
			Byte b = (Byte) buffer.get(i);
			byteArray[i] = b.byteValue();
		}
		return byteArray;
	}

	private ISVNAnnotations annotate(String target, SVNRevision revisionStart, SVNRevision revisionEnd) throws SVNClientException {
        try {
            notificationHandler.setCommand(ISVNNotifyListener.Command.ANNOTATE);
            if(revisionStart == null)
                revisionStart = new SVNRevision.Number(1);
            if(revisionEnd == null)
                revisionEnd = SVNRevision.HEAD;

            String annotations = _cmd.annotate(target,toString(revisionStart),toString(revisionEnd));
            
            return new CmdLineAnnotations(annotations,Helper.NEWLINE);
		} catch (CmdLineException e) {
			throw SVNClientException.wrapException(e);
		}
	}
	
    /*
     * (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#blame(org.tigris.subversion.svnclientadapter.SVNUrl, org.tigris.subversion.svnclientadapter.SVNRevision, org.tigris.subversion.svnclientadapter.SVNRevision)
     */
    public ISVNAnnotations annotate(SVNUrl url, SVNRevision revisionStart, SVNRevision revisionEnd)
        throws SVNClientException
    {
    	return annotate(toString(url), revisionStart, revisionEnd);
    }

    /*
     * (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#annotate(java.io.File, org.tigris.subversion.svnclientadapter.SVNRevision, org.tigris.subversion.svnclientadapter.SVNRevision)
     */
    public ISVNAnnotations annotate(File file, SVNRevision revisionStart, SVNRevision revisionEnd)
        throws SVNClientException
    {
        return annotate(toString(file), revisionStart, revisionEnd);
    }
    
	/*
	 * (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#getProperties(java.io.File)
	 */
	public ISVNProperty[] getProperties(File path) throws SVNClientException {
		try {
			String propertiesString = _cmd.proplist(toString(path), false);
			String propertyName;
			List properties = new LinkedList();
			
			StringTokenizer st = new StringTokenizer(propertiesString, Helper.NEWLINE);
			while (st.hasMoreTokens()) {
				String propertyLine = st.nextToken();
				if (propertyLine.startsWith("Properties on '")) {
				} else {
					propertyName = propertyLine.substring(2);
					properties.add(propertyGet(path,propertyName));
				}
			}
			return (ISVNProperty[]) properties.toArray(new ISVNProperty[0]);
			
		} catch (CmdLineException e) {
			throw SVNClientException.wrapException(e);
		}
	}

	/**
	 * Remove 'conflicted' state on working copy files or directories
	 * @param path
	 * @throws SVNClientException
	 */    
	public void resolved(File path) 
		throws SVNClientException
	{
		try {
			notificationHandler.setBaseDir(SVNBaseDir.getBaseDir(path));
			_cmd.resolved(new String[] { toString(path) }, false);
			
			// there is no notification when we do svn resolve, we will do notification ourselves
			notificationHandler.notifyListenersOfChange(path.getAbsolutePath());	
		} catch (CmdLineException e) {
			throw SVNClientException.wrapException(e);
		}
		
	}

	
	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#createRepository(java.io.File)
	 */
	public void createRepository(File path, String repositoryType) throws SVNClientException {
		try {
			svnAdminCmd.create(toString(path), repositoryType);
		} catch (CmdLineException e) {
			throw SVNClientException.wrapException(e);
		}		
	}

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#getStatus(java.io.File, boolean, boolean, boolean)
     */
    public ISVNStatus[] getStatus(File path, boolean descend, boolean getAll, boolean contactServer) throws SVNClientException {
        notImplementedYet();
        return null;
    }

	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#cancelOperation()
	 */
	public void cancelOperation() throws SVNClientException {
        notImplementedYet();
	}
	
	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#getInfoFromWorkingCopy(java.io.File)
	 */
	public ISVNInfo getInfoFromWorkingCopy(File path) throws SVNClientException {
        try {
            notificationHandler.setBaseDir(SVNBaseDir.getBaseDir(path));
            String cmdLineInfoStrings = _cmd.info(new String[] { toString(path) });
            return new CmdLineInfoPart(cmdLineInfoStrings);
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        }        
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#getInfo(java.io.File)
	 */
	public ISVNInfo getInfo(File path) throws SVNClientException {
        try {
            notificationHandler.setBaseDir(SVNBaseDir.getBaseDir(path));
            String cmdLineInfoStrings = _cmd.info(new String[] { toString(path) });
            return new CmdLineInfoPart(cmdLineInfoStrings);
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        }        
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#getInfo(org.tigris.subversion.svnclientadapter.SVNUrl)
	 */
	public ISVNInfo getInfo(SVNUrl url) throws SVNClientException {
		return getInfo(new SVNUrl[] { url });
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#getInfo(org.tigris.subversion.svnclientadapter.SVNUrl[])
	 */
	public ISVNInfo getInfo(SVNUrl[] urls) throws SVNClientException {
        try {
    		String[] urlStrings = new String[urls.length];
    		for (int i = 0; i < urls.length; i++) {
    			urlStrings[i] = toString(urls[i]);
    		}
			//notificationHandler.setBaseDir(SVNBaseDir.getBaseDir(urls));
            String cmdLineInfoStrings = _cmd.info(urlStrings);
            return new CmdLineInfoPart(cmdLineInfoStrings);
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        }        
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#getRepositoryRoot(org.tigris.subversion.svnclientadapter.SVNUrl, org.tigris.subversion.svnclientadapter.SVNRevision)
	 */
	public SVNUrl getRepositoryRoot(SVNUrl url) throws SVNClientException {
        notImplementedYet();
        return null;
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#switchUrl(org.tigris.subversion.svnclientadapter.SVNUrl, java.io.File, org.tigris.subversion.svnclientadapter.SVNRevision, boolean)
	 */
	public void switchToUrl(File path, SVNUrl url, SVNRevision revision, boolean recurse) throws SVNClientException {
		try {
			notificationHandler.setBaseDir(SVNBaseDir.getBaseDir(path));
            _cmd.switchUrl(toString(path), toString(url), toString(revision));
        } catch (CmdLineException e) {
        	throw SVNClientException.wrapException(e);
        }
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#setConfigDirectory(java.io.File)
	 */
	public void setConfigDirectory(File dir) throws SVNClientException {
		_cmd.setConfigDirectory(toString(dir));
	}

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#cleanup(java.io.File)
     */
    public void cleanup(File path) throws SVNClientException {
        try {
            notificationHandler.setBaseDir(SVNBaseDir.getBaseDir(path));
            _cmd.cleanup(toString(path));
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        }
    }
    
	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#merge(org.tigris.subversion.svnclientadapter.SVNUrl, org.tigris.subversion.svnclientadapter.SVNRevision, org.tigris.subversion.svnclientadapter.SVNUrl, org.tigris.subversion.svnclientadapter.SVNRevision, java.io.File, boolean, boolean)
	 */
	public void merge(SVNUrl path1, SVNRevision revision1, SVNUrl path2,
			SVNRevision revision2, File localPath, boolean force,
			boolean recurse) throws SVNClientException {
		try {
			notificationHandler.setBaseDir(SVNBaseDir.getBaseDir(localPath));
            _cmd.merge(toString(path1), toString(revision1), toString(path2), toString(revision2), toString(localPath), force, recurse, false);
        } catch (CmdLineException e) {
        	throw SVNClientException.wrapException(e);
        }
	}

	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#merge(org.tigris.subversion.svnclientadapter.SVNUrl, org.tigris.subversion.svnclientadapter.SVNRevision, org.tigris.subversion.svnclientadapter.SVNUrl, org.tigris.subversion.svnclientadapter.SVNRevision, java.io.File, boolean, boolean, boolean)
	 */
	public void merge(SVNUrl path1, SVNRevision revision1, SVNUrl path2,
			SVNRevision revision2, File localPath, boolean force,
			boolean recurse, boolean dryRun) throws SVNClientException {
		try {
			notificationHandler.setBaseDir(SVNBaseDir.getBaseDir(localPath));
            _cmd.merge(toString(path1), toString(revision1), toString(path2), toString(revision2), toString(localPath), force, recurse, dryRun);
        } catch (CmdLineException e) {
        	throw SVNClientException.wrapException(e);
        }
	}
    
	/* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#lock(java.lang.String[], java.lang.String, boolean)
     */
    public void lock(File[] paths, String comment, boolean force)
            throws SVNClientException {
		String[] files = new String[paths.length];
		for (int i = 0; i < paths.length; i++) {
			files[i] = toString(paths[i]);
		}
		try {
			notificationHandler.setBaseDir(SVNBaseDir.getBaseDir(paths));
            _cmd.lock(files, comment, force);
        } catch (CmdLineException e) {
        	throw SVNClientException.wrapException(e);
        }
        finally {
            for (int i = 0; i < files.length; i++) {
                notificationHandler.notifyListenersOfChange(files[i]);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#unlock(java.lang.String[], boolean)
     */
    public void unlock(File[] paths, boolean force) throws SVNClientException {
		String[] files = new String[paths.length];
		for (int i = 0; i < paths.length; i++) {
			files[i] = toString(paths[i]);
		}
		try {
			notificationHandler.setBaseDir(SVNBaseDir.getBaseDir(paths));
            _cmd.unlock(files, force);
        } catch (CmdLineException e) {
        	throw SVNClientException.wrapException(e);
        }
        finally {
            for (int i = 0; i < files.length; i++) {
                notificationHandler.notifyListenersOfChange(files[i]);
            }
        }
   }
}
