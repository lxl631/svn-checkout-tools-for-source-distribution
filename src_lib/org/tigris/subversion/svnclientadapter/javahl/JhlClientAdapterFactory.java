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
package org.tigris.subversion.svnclientadapter.javahl;

import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.SVNClientException;

/**
 * Concrete implementation of SVNClientAdapterFactory for javahl interface.
 * To register this factory, just call {@link JhlClientAdapterFactory#setup()} 
 */
public class JhlClientAdapterFactory extends SVNClientAdapterFactory {
	
    public static final String JAVAHL_CLIENT = "javahl";
    
	/* (non-Javadoc)
	 * @see org.tigris.subversion.svnclientadapter.SVNClientAdapterFactory#createSVNClientImpl()
	 */
	protected ISVNClientAdapter createSVNClientImpl() {
		return new JhlClientAdapter();
	}

    private JhlClientAdapterFactory() {
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.SVNClientAdapterFactory#getClientType()
     */
    protected String getClientType() {
        return JAVAHL_CLIENT;
    }
    
    public static void setup() throws SVNClientException {
        if (!JhlClientAdapter.isAvailable()) {
        	throw new SVNClientException("Javahl client adapter is not available");
        }
        
    	SVNClientAdapterFactory.registerAdapterFactory(new JhlClientAdapterFactory());
    }
  
}
