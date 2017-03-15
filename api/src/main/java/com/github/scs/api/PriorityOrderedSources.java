package com.github.scs.api;

/**
 * An abstraction of a collection of sources which have a ordered priority to them where a configuration entry will be retrieved from the source with the
 * highest priority before before attempting to retrieve it from lower priority sources.
 * <p>
 * Note: The behavior of the operation {@link ConfigurationSource#retrieve(String)} with two sources that have been given the same priority is considered
 * implementation specific and my be nondeterministic.
 * <p>
 * Additionally, the interface provides no contract for the behaviour of {@link WritableConfigurationSource#store(String, String)}.
 *
 * @author Hendrik Louw.
 * @since 2017-03-15.
 */
public interface PriorityOrderedSources extends WritableConfigurationSource {

    /**
     * Registers the given {@link ConfigurationSource configuration source} with the given priority in relation to other sources in this set.
     *
     * @param source   The source which must be registered.
     * @param priority The priority of the source, i.e. values for keys with a lower priority take precedence.
     */
    public void addSource(ConfigurationSource source, int priority);

    /**
     * Removes the given {@link ConfigurationSource configuration source} from this set of registered sources.
     * Note, Identity of the configuration source is determined by using the {@link ConfigurationSource#getUUID() UUID}.
     *
     * @param source The source instance which must be removed.
     */
    public void removeSource(ConfigurationSource source);

    /**
     * Removes all the underlying sources which have been configured for this priority ordered set of sources.
     */
    public void removeAllSources();

}

