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
package org.tigris.subversion.svnclientadapter.javasvn;

import org.tigris.subversion.svnclientadapter.ISVNPromptUserPassword;
import org.tmatesoft.svn.core.javahl.PromptUserPassword4;

public class JavaSvnPromptUserPassword implements PromptUserPassword4 {

    private ISVNPromptUserPassword worker;
    
    public JavaSvnPromptUserPassword(ISVNPromptUserPassword arg0) {
        super();
        this.worker = arg0;
    }
    
    /* (non-Javadoc)
     * @see org.tigris.subversion.javahl.PromptUserPassword3#prompt(java.lang.String, java.lang.String, boolean)
     */
    public boolean prompt(String realm, String username, boolean maySave) {
        return this.worker.prompt(realm, username, maySave);
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.javahl.PromptUserPassword3#askQuestion(java.lang.String, java.lang.String, boolean, boolean)
     */
    public String askQuestion(String realm, String question, boolean showAnswer, boolean maySave) {
        return this.worker.askQuestion(realm, question, showAnswer, maySave);
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.javahl.PromptUserPassword3#userAllowedSave()
     */
    public boolean userAllowedSave() {
        return this.worker.userAllowedSave();
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.javahl.PromptUserPassword2#askTrustSSLServer(java.lang.String, boolean)
     */
    public int askTrustSSLServer(String info, boolean allowPermanently) {
        return this.worker.askTrustSSLServer(info, allowPermanently);
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.javahl.PromptUserPassword#prompt(java.lang.String, java.lang.String)
     */
    public boolean prompt(String realm, String username) {
        return this.prompt(realm, username, true);
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.javahl.PromptUserPassword#askYesNo(java.lang.String, java.lang.String, boolean)
     */
    public boolean askYesNo(String realm, String question, boolean yesIsDefault) {
        return this.worker.askYesNo(realm, question, yesIsDefault);
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.javahl.PromptUserPassword#askQuestion(java.lang.String, java.lang.String, boolean)
     */
    public String askQuestion(String realm, String question, boolean showAnswer) {
        return this.askQuestion(realm, question, showAnswer, true);
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.javahl.PromptUserPassword#getUsername()
     */
    public String getUsername() {
        return this.worker.getUsername();
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.javahl.PromptUserPassword#getPassword()
     */
    public String getPassword() {
        return this.worker.getPassword();
    }

    public int getSSHPort() {
        return this.worker.getSSHPort();
    }
    public String getSSHPrivateKeyPassphrase() {
        return this.worker.getSSHPrivateKeyPassphrase();
    }
    public String getSSHPrivateKeyPath() {
        return this.worker.getSSHPrivateKeyPath();
    }
    public boolean promptSSH(String realm, String username, int sshPort,
            boolean maySave) {
        return this.worker.promptSSH(realm, username, sshPort, maySave);
    }
}
