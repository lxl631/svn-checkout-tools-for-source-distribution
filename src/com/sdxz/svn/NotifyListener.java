/**
 * 
 */
package com.sdxz.svn;

import java.io.File;

import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;

public class NotifyListener implements ISVNNotifyListener {
	public void setCommand(int cmd) {
		// the command that is being executed. See
		// ISVNNotifyListener.Command
		// ISVNNotifyListener.Command.ADD for example
	}

	public void logMessage(String message) {
		System.out.println(message);
	}

	public void logCommandLine(String message) {
		// the command line used
		System.out.println(message);
	}

	public void logError(String message) {
		// when an error occurs
		System.out.println("error :" + message);
	}

	public void logRevision(long revision, String path) {
		// when command completes against revision
		System.out.println("revision :" + revision);
	}

	public void logCompleted(String message) {
		// when command completed
		System.out.println(message);
	}

	public void onNotify(File path, SVNNodeKind nodeKind) {
		// each time the status of a file or directory changes (file added,
		// reverted ...)
		// nodeKind is SVNNodeKind.FILE or SVNNodeKind.DIR

		// this is the function we use in subclipse to know which files need
		// to be refreshed

		System.out.println("Status of " + path.toString() + " has changed");
	}
}