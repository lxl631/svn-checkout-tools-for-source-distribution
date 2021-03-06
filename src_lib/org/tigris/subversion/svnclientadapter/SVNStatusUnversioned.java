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
package org.tigris.subversion.svnclientadapter;

import java.io.File;
import java.util.Date;

/**
 * <p>
 * A special status class that is used if a File/Folder is not versioned.</p>
 * 
 * @author Philip Schatz (schatz at tigris)
 * @author C�dric Chabanois (cchabanois at no-log.org)
 */
public class SVNStatusUnversioned implements ISVNStatus {
    private File file;
    private boolean isIgnored = false;
	
    public SVNStatusUnversioned(File file, boolean isIgnored) {
        this.file = file;
        // A file can be both unversioned and ignored.
        this.isIgnored = isIgnored;
    }
    
	public SVNStatusUnversioned(File file) {
		this.file = file;
	}
    
	public SVNUrl getUrl() {
		return null;
	}

	public SVNRevision.Number getLastChangedRevision() {
		return null;
	}

	public Date getLastChangedDate() {
		return null;
	}

	public String getLastCommitAuthor() {
		return null;
	}

	public SVNStatusKind getTextStatus() {
        if (isIgnored) {
        	return SVNStatusKind.IGNORED;
        }
    	return SVNStatusKind.UNVERSIONED;
	}
	
    /**
     * @return As this status does not describe a managed resource, we
     * cannot pretend that there is property status, and thus always
     * return {@link SVNStatusKind#NONE}.
     */
	public SVNStatusKind getPropStatus() {
		return SVNStatusKind.NONE;
	}
	
    public SVNStatusKind getRepositoryTextStatus() {
        return SVNStatusKind.UNVERSIONED;
    }

    public SVNStatusKind getRepositoryPropStatus() {
        return SVNStatusKind.UNVERSIONED;
    }

	public SVNRevision.Number getRevision() {
		return SVNRevision.INVALID_REVISION;
	}

	public boolean isCopied() {
		return false;
	}

	public String getPath() {
		return file.getPath();
	}

    public File getFile() {
        return file.getAbsoluteFile();
    }
    
    /**
     * @return As this status does not describe a managed resource, we
     * cannot pretend to know the node kind, and thus always return
     * {@link SVNNodeKind#UNKNOWN}.
     */
	public SVNNodeKind getNodeKind() {
        return SVNNodeKind.UNKNOWN;
	}

	public SVNUrl getUrlCopiedFrom() {
		return null;
	}

    /**
     * @see org.tigris.subversion.svnclientadapter.ISVNStatus#getConflictNew()
     */
    public File getConflictNew() {
        return null;
    }

    /**
     * @see org.tigris.subversion.svnclientadapter.ISVNStatus#getConflictOld()
     */
    public File getConflictOld() {
        return null;
    }

    /**
     * @see org.tigris.subversion.svnclientadapter.ISVNStatus#getConflictWorking()
     */
    public File getConflictWorking() {
        return null;
    }

    /**
     * @see org.tigris.subversion.svnclientadapter.ISVNStatus#getLockComment()
     */
    public String getLockComment() {
        return null;
    }

    /**
     * @see org.tigris.subversion.svnclientadapter.ISVNStatus#getLockCreationDate()
     */
    public Date getLockCreationDate() {
        return null;
    }

    /**
     * @see org.tigris.subversion.svnclientadapter.ISVNStatus#getLockOwner()
     */
    public String getLockOwner() {
        return null;
    }

    /**
     * @see org.tigris.subversion.svnclientadapter.ISVNStatus#getLockToken()
     */
    public String getLockToken() {
        return null;
    }

    /**
     * @see org.tigris.subversion.svnclientadapter.ISVNStatus#getReposLock()
     */
    public ISVNLock getReposLock() {
        return null;
    }
}
