/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.commons.collections.bidimap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.Unmodifiable;
import org.apache.commons.collections.collection.UnmodifiableCollection;
import org.apache.commons.collections.iterators.UnmodifiableMapIterator;
import org.apache.commons.collections.map.UnmodifiableEntrySet;
import org.apache.commons.collections.set.UnmodifiableSet;

/**
 * Decorates another <code>BidiMap</code> to ensure it can't be altered.
 *
 * @since Commons Collections 3.0
 * @version $Revision: 646777 $ $Date: 2008-04-10 13:33:15 +0100 (Thu, 10 Apr 2008) $
 * 
 * @author Stephen Colebourne
 */
public final class UnmodifiableBidiMap
        extends AbstractBidiMapDecorator implements Unmodifiable {
    
    /** The inverse unmodifiable map */
    private UnmodifiableBidiMap inverse;

    /**
     * Factory method to create an unmodifiable map.
     * <p>
     * If the map passed in is already unmodifiable, it is returned.
     * 
     * @param map  the map to decorate, must not be null
     * @return an unmodifiable BidiMap
     * @throws IllegalArgumentException if map is null
     */
    public static BidiMap decorate(BidiMap map) {
        if (map instanceof Unmodifiable) {
            return map;
        }
        return new UnmodifiableBidiMap(map);
    }

    //-----------------------------------------------------------------------
    /**
     * Constructor that wraps (not copies).
     * 
     * @param map  the map to decorate, must not be null
     * @throws IllegalArgumentException if map is null
     */
    private UnmodifiableBidiMap(BidiMap map) {
        super(map);
    }

    //-----------------------------------------------------------------------
    @Override
	public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
	public Object put(Object key, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
	public void putAll(Map mapToCopy) {
        throw new UnsupportedOperationException();
    }

    @Override
	public Object remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
	public Set entrySet() {
        Set set = super.entrySet();
        return UnmodifiableEntrySet.decorate(set);
    }

    @Override
	public Set keySet() {
        Set set = super.keySet();
        return UnmodifiableSet.decorate(set);
    }

    @Override
	public Collection values() {
        Collection coll = super.values();
        return UnmodifiableCollection.decorate(coll);
    }

    //-----------------------------------------------------------------------
    @Override
	public Object removeValue(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
	public MapIterator mapIterator() {
        MapIterator it = getBidiMap().mapIterator();
        return UnmodifiableMapIterator.decorate(it);
    }

    @Override
	public BidiMap inverseBidiMap() {
        if (inverse == null) {
            inverse = new UnmodifiableBidiMap(getBidiMap().inverseBidiMap());
            inverse.inverse = this;
        }
        return inverse;
    }
    
}
