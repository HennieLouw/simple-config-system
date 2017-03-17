package com.github.scs.impl;

import com.github.scs.api.ConfigurationSource;
import com.github.scs.api.PriorityOrderedSources;
import com.github.scs.api.WritableConfigurationSource;

import java.util.UUID;


/**
 * Implementation of a {@link WritableConfigurationSource} which ensures thread safety before calling it's underlying delegate.
 * <p>
 * Note on equality usage for {@link PriorityOrderedSources#removeSource(ConfigurationSource)}:
 * This implementation will provide the {@link ConfigurationSource#getUUID() UUID} of it's delegate, allowing for this wrapper or it's delegating instance to
 * be passed when removal is required.
 *
 * @author Hendrik Louw
 * @since 2017-03-17.
 */
public class ThreadSafeConfigurationSource extends AbstractThreadSafeConfigurationSource {

    /** The configuration source we are providing thread safety for. */
    private final WritableConfigurationSource delegate;

    public ThreadSafeConfigurationSource(WritableConfigurationSource delegate) {
        if (delegate == null) {
            throw new IllegalArgumentException("Delegate cannot be null.");
        }
        this.delegate = delegate;
    }

    public UUID getUUID() {
        return delegate.getUUID();
    }

    protected void storeInternal(String key, String value) {
        delegate.store(key, value);
    }

    protected String retrieveInternal(String key) {
        return delegate.retrieve(key);
    }
}
