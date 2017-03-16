package com.github.scs.util.collections;

import com.github.scs.api.ConfigurationSource;
import com.github.scs.api.WritableConfigurationSource;
import org.apache.commons.collections4.Predicate;

/**
 * Apache Commons Predicate Implementation to predicate {@link ConfigurationSource } instances which is also writable.
 *
 * @author Hendrik Louw
 * @since 2017-03-16.
 */
public class WritableSourcePredicate implements Predicate<ConfigurationSource> {

    /** Global Instance for ease of use. */
    public static final WritableSourcePredicate INSTANCE = new WritableSourcePredicate();

    public boolean evaluate(ConfigurationSource object) {
        return (object instanceof WritableConfigurationSource);
    }
}
