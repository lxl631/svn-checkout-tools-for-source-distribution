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

import java.util.Date;

/**
 * Interface to describe a lock. It is return by the lock operation
 */
public interface ISVNLock {

    /**
     * @return the owner of the lock
     */
    public String getOwner();

    /**
     * @return the path of the locked item
     */
    public String getPath();

    /**
     * @return the token provided during the lock operation
     */
    public String getToken();

    /**
     * @return the comment provided during the lock operation
     */
    public String getComment();
 
    /**
     * @return the date the lock was created
     */
    public Date getCreationDate();
 
    /**
     * @return the date when the lock will expire
     */
    public Date getExpirationDate();
 
}
