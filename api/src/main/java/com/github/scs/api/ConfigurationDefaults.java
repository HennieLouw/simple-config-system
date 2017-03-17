package com.github.scs.api;

import com.github.scs.Configuration;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Set;

/**
 * An encapsulation of the default values which will be given when no configuration entry exists when calling any of the retrieve operations on a
 * {@link Configuration} instance.
 *
 * @author Hendrik Louw
 * @since 2017-03-17.
 */
@SuppressWarnings("WeakerAccess")
public class ConfigurationDefaults {

    /** default value to use when underlying sources returns null for {@link Configuration#retrieveInteger(String)}. */
    @Getter
    private final Integer defaultInteger;

    /** Default value to use when underlying sources returns null for {@link Configuration#retrieve(java.lang.String)}. */
    @Getter
    private final String defaultString;

    /** Default value to use when underlying source returns null for {@link Configuration#retrieveBoolean(java.lang.String)}. */
    @Getter
    private final Boolean defaultBoolean;

    /** Set of all Strings which represents a boolean, case insensitive. */
    private final Set<String> trueRepresentations;

    @Builder
    private ConfigurationDefaults(Integer defaultInteger, String defaultString, Boolean defaultBoolean, @Singular Set<String> trueRepresentations) {
        this.defaultInteger = defaultInteger;
        this.defaultString = defaultString;
        this.defaultBoolean = defaultBoolean;
        this.trueRepresentations = trueRepresentations;
    }

    /**
     * Ease of use operation for building default instances with the most common values.
     * <p>
     * This is equivalent to:
     * <pre>
     * {@code
     *
     * ConfigurationDefaults.builder()
     *                      .defaultString("")
     *                      .defaultBoolean(Boolean.FALSE)
     *                      .defaultInteger(-1)
     *                      .trueRepresentation("Y")
     *                      .trueRepresentation("YES")
     *                      .trueRepresentation("T")
     *                      .trueRepresentation("TRUE")
     *                      .trueRepresentation("1")
     *                      .build();
     * }
     * </pre>
     *
     * @return A Defaults instances which has been configured to the most common values.
     */
    public static ConfigurationDefaults buildCommon() {
        return ConfigurationDefaults.builder()
                                    .defaultString("")
                                    .defaultBoolean(Boolean.FALSE)
                                    .defaultInteger(-1)
                                    .trueRepresentation("Y")
                                    .trueRepresentation("YES")
                                    .trueRepresentation("T")
                                    .trueRepresentation("TRUE")
                                    .trueRepresentation("1")
                                    .build();
    }

    /**
     * Determines if the given string represents a boolean value by comparing it to all {@link #getTrueRepresentations() known 'true' string representations}.
     * This comparison is case-insensitive.
     *
     * @param value The value to be compared.
     * @return {@code true} if it represents a boolean, {@code false} otherwise.
     */
    public boolean representsTrueBoolean(String value) {
        if (StringUtils.isNotBlank(value)) {
            for (String representation : trueRepresentations) {
                if (StringUtils.equalsIgnoreCase(representation, value)) {
                    return true; // True representation.
                }
            }
        }
        return false; // Blank will always be considered. false.
    }

    public Set<String> getTrueRepresentations() {
        return Collections.unmodifiableSet(trueRepresentations);
    }
}
