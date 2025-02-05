/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.camel.resume;

/**
 * This provides an interface for resumable objects. Such objects allow its users to address them at a specific offset.
 * For example, when reading large files, it may be possible to inform the last offset that was read, thus allowing
 * users of this interface to skip to that offset. This can potentially improve resumable operations by allowing
 * reprocessing of data.
 *
 * @param <Y> the type of the key, name or object that can be addressed by the given offset
 * @param <T> the type of the addressable value for the resumable object (for example, a file would use a Long value)
 */
public interface Resumable<Y, T> {

    /**
     * Updates the last offset as appropriate for the user of the interface
     *
     * @param offset the offset value
     */
    void updateLastOffset(T offset);

    /**
     * Gets the last offset value
     * 
     * @return the last offset value according to the interface and type implemented
     */
    Offset<T> getLastOffset();

    /**
     * Gets the addressable part (key) of the resumable
     * 
     * @return the addressable part of the resumable
     */
    Y getAddressable();
}
