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


/**
 * <p>
 * Base class for enumerating the possible types for a <code>Status</code>.
 * </p>
 */
public class SVNStatusKind {
    private final int kind;

    private static final int none = 0;
    private static final int normal = 1;
    private static final int modified = 2;
    private static final int added = 3;
    private static final int deleted = 4;
    private static final int unversioned = 5;
    private static final int missing = 6;
    private static final int replaced = 7;
    private static final int merged = 8;
    private static final int conflicted = 9;
    private static final int obstructed = 10;
    private static final int ignored = 11;
    private static final int incomplete = 12;
    private static final int external = 13;
    private static final int locked = 14;
    
    /** does not exist */
    public static final SVNStatusKind NONE = new SVNStatusKind(none);
    
    /** exists, but uninteresting. */
    public static final SVNStatusKind NORMAL = new SVNStatusKind(normal);
    
    /** is scheduled for addition */
    public static final SVNStatusKind ADDED = new SVNStatusKind(added);
    
    /** under v.c., but is missing */
    public static final SVNStatusKind MISSING = new SVNStatusKind(missing);
    
    /** a directory doesn't contain a complete entries list  */
    public static final SVNStatusKind INCOMPLETE = new SVNStatusKind(incomplete);
    
    /** scheduled for deletion */
    public static final SVNStatusKind DELETED = new SVNStatusKind(deleted);
    
    /** was deleted and then re-added */
    public static final SVNStatusKind REPLACED = new SVNStatusKind(replaced);
    
    /** text or props have been modified */
    public static final SVNStatusKind MODIFIED = new SVNStatusKind(modified);
    
    /** local mods received repos mods */
    public static final SVNStatusKind MERGED = new SVNStatusKind(merged);
    
    /** local mods received conflicting repos mods */
    public static final SVNStatusKind CONFLICTED = new SVNStatusKind(conflicted);
    
    /** an unversioned resource is in the way of the versioned resource */
    public static final SVNStatusKind OBSTRUCTED = new SVNStatusKind(obstructed);
    
    /** a resource marked as ignored */
    public static final SVNStatusKind IGNORED = new SVNStatusKind(ignored);
    
    /** an unversioned path populated by an svn:external property */
    public static final SVNStatusKind EXTERNAL = new SVNStatusKind(external);
    
    /** is not a versioned thing in this wc */
    public static final SVNStatusKind UNVERSIONED = new SVNStatusKind(unversioned);
    
    /** a resource marked as locked */
    public static final SVNStatusKind LOCKED = new SVNStatusKind(locked);
    
    //Constructors
    /**
     * <p>
     * Constructs a <code>Type</code> for the given a type name.</p>
     *
     *
     * @param type Name of the type.
     * @throws IllegalArgumentException If the parameter is invalid.
     */
    private SVNStatusKind(int kind) throws IllegalArgumentException {
        this.kind = kind;
    }
    
    public int toInt() {
    	return kind;
    }
    
    public static SVNStatusKind fromInt(int kind) {
        switch (kind)
        {
        case none:
            return NONE;
        case normal:
            return NORMAL;
        case added:
            return ADDED;
        case missing:
            return MISSING;
        case deleted:
            return DELETED;
        case replaced:
            return REPLACED;
        case modified:
            return MODIFIED;
        case merged:
            return MERGED;
        case conflicted:
            return CONFLICTED;
        case ignored:
            return IGNORED;
        case incomplete:
            return INCOMPLETE;
        case external:
            return EXTERNAL;
        case unversioned:
            return UNVERSIONED;
        case locked:
            return LOCKED;
        case obstructed:
            return OBSTRUCTED;
        default:
            return null;
        }
    }
        
    public String toString() {
        switch (kind)
        {
        case none:
            return "non-svn";
        case normal:
            return "normal";
        case added:
            return "added";
        case missing:
            return "missing";
        case deleted:
            return "deleted";
        case replaced:
            return "replaced";
        case modified:
            return "modified";
        case merged:
            return "merged";
        case conflicted:
            return "conflicted";
        case ignored:
            return "ignored";
        case incomplete:
            return "incomplete";
        case external:
            return "external";
        case locked:
            return "locked";
        case obstructed:
            return "obstructed";
        case unversioned:
        default:
            return "unversioned";
        }
    }
    
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof SVNStatusKind)) {
            return false;
        }
        return ((SVNStatusKind)obj).kind == kind;
	}
    
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return new Integer(kind).hashCode();
	}
}