package com.github.scs.util.collections;

import com.github.scs.api.ConfigurationSource;
import com.github.scs.api.WritableConfigurationSource;
import org.apache.commons.collections4.Transformer;

/**
 * Implementation of the Apache common collections {@link Transformer} to cast {@link ConfigurationSource} instances into {@link WritableConfigurationSource}
 * instances.
 * <p>
 * Note, this transformer does not check if the given instances can be casted, and will throw a {@link ClassCastException} if given a non writable source.
 *
 * @author Hendrik Louw
 * @since 2017-03-16.
 */
public class WritableSourceCastTransformer implements Transformer<ConfigurationSource, WritableConfigurationSource> {

    /** Global Instance for ease of use. */
    public static final WritableSourceCastTransformer INSTANCE = new WritableSourceCastTransformer();

    public WritableConfigurationSource transform(ConfigurationSource input) {
        return (WritableConfigurationSource) input;
    }
}
