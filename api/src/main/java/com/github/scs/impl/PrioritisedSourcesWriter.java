package com.github.scs.impl;

import com.github.scs.api.ConfigurationSource;
import com.github.scs.api.PriorityOrderedSources;
import com.github.scs.api.WritableConfigurationSource;
import com.github.scs.util.collections.PrioritisedSourcePredicate;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.functors.AndPredicate;

import java.util.Collection;
import java.util.Comparator;

/**
 * Extension of the {@link SimpleSourcesWriter} which will ensure the following.
 * <ol>
 *     <li>The Writer will only update writable instances which are also {@link PrioritisedConfigurationSource prioritised}. </li>
 *     <li>The Writer will only update one instance, namely the first discovered instance based on the defined ordering.</li>
 * </ol>
 *
 * @author Hendrik Louw
 * @since 2017-03-16.
 * @see PrioritisedConfigurationSource
 * @see PrioritisedConfigurationSource#compareTo(PrioritisedConfigurationSource)
 */
public class PrioritisedSourcesWriter extends SimpleSourcesWriter {

    /** Mutator value for ascending. */
    private static final int MUTATOR_ASC = 1;

    /** Mutator value for descending. */
    private static final int MUTATOR_DESC = -1;

    /** Value which is used to order ascending or descending on the natual ordering of a prioritised configuration source. */
    private final int orderMutator;

    /** Internal comparator we use for our ordering. */
    private final Comparator<PrioritisedConfigurationSource> order = new Order();

    @Override
    protected Predicate<ConfigurationSource> definePredicate() {
        return new AndPredicate<ConfigurationSource>(PrioritisedSourcePredicate.INSTANCE, );
        return super.definePredicate();
    }

    @Override
    protected Collection<WritableConfigurationSource> collectWritable(Collection<ConfigurationSource> sources) {
        return super.collectWritable(sources);
    }


    private class

    /** Internal comparator to support order manipulation. */
    private class Order implements Comparator<PrioritisedConfigurationSource> {
        public int compare(PrioritisedConfigurationSource lhs, PrioritisedConfigurationSource rhs) {
            return (lhs.compareTo(rhs)) * orderMutator;
        }
    }


}
