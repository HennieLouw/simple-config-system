package com.github.scs.impl;

import com.github.scs.api.ConfigurationSource;

import java.util.HashMap;
import java.util.UUID;

/**
 * Basic implementation of a {@link ConfigurationSource} which is backed by a {@link HashMap} and thread safe.
 *
 * @author Hendrik Louw
 * @since 2017-03-17.
 */
public class MemoryConfigurationSource extends AbstractThreadSafeConfigurationSource {

    /** Our UUID */
    private final UUID internalID = UUID.randomUUID();

    /** Our internal memory store. */
    private final HashMap<String, String> store = new HashMap<String, String>();

    public UUID getUUID() {
        return internalID;
    }

    protected void storeInternal(String key, String value) {
        store.put(key, value);
    }

    protected String retrieveInternal(String key) {
        return store.get(key);
    }
}
