package com.github.scs.util.collections;

import com.github.scs.api.ConfigurationSource;
import com.github.scs.impl.PrioritisedConfigurationSource;
import org.apache.commons.collections4.Predicate;

/**
 * Apache commons predicate implementation for sources which are {@link PrioritisedConfigurationSource prioritised}.
 *
 * @author Hendrik Louw
 * @since 2017-03-16.
 */
public class PrioritisedSourcePredicate implements Predicate<ConfigurationSource> {

    /** Gobal instance for ease of use. */
    public static final PrioritisedSourcePredicate INSTANCE = new PrioritisedSourcePredicate();

    public boolean evaluate(ConfigurationSource object) {
        return (object instanceof PrioritisedConfigurationSource);
    }
}
