package com.github.scs.impl;

import com.github.scs.api.ConfigurationSource;
import com.github.scs.api.WritableConfigurationSource;
import com.github.scs.util.collections.PrioritisedSourceCastTransformer;
import com.github.scs.util.collections.PrioritisedSourcePredicate;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.functors.AndPredicate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

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
@SuppressWarnings("WeakerAccess")
public class PrioritisedSourcesWriter extends SimpleSourcesWriter {

    /** Instance of the prioritised writer for writing to the source with the highest priority only. */
    public static final PrioritisedSourcesWriter HIGHEST = new PrioritisedSourcesWriter(Order.ASCENDING);

    /** Instance of the prioritised writer for writing to the source with the lowest priority only. */
    public static final PrioritisedSourcesWriter LOWEST = new PrioritisedSourcesWriter(Order.DESCENDING);

    /** Enumeration of our supported order. */
    public enum Order {
        ASCENDING (1),
        DESCENDING(-1);

        public final int mutator;

        Order(int mutator) {
            this.mutator = mutator;
        }
    }

    /** Enum defining the order we will write in, Ascending or Descending. */
    private final Order order;

    /** Internal comparator we use for our ordering. */
    private final OrderComparator orderComparator = new OrderComparator();

    /**
     * Creates a new instance which will update the first configuration source, once they have been ordered
     * in the given order.
     * @param order The order in which the sources which will be updated will be placed.
     */
    private PrioritisedSourcesWriter(Order order) {
        this.order = order;
    }

    @Override
    protected Predicate<ConfigurationSource> definePredicate() {
        // Create a new FirstOnly Predicate for each call, as each call relates to a single set of sources being updated.
        return new AndPredicate<ConfigurationSource>(PrioritisedSourcePredicate.INSTANCE, new FirstOnlyPredicate());
    }

    @Override
    protected Collection<WritableConfigurationSource> collectWritable(Collection<? extends ConfigurationSource> sources) {
        // First we will need to filter and re-orderComparator only the prioritised items based on their priority.
        Collection<ConfigurationSource> filteredSources = CollectionUtils.select(sources, PrioritisedSourcePredicate.INSTANCE);

        // Re-orderComparator using our internal comparator.
        TreeSet<PrioritisedConfigurationSource> orderedSources = new TreeSet<PrioritisedConfigurationSource>(orderComparator);
        CollectionUtils.collect(filteredSources, PrioritisedSourceCastTransformer.INSTANCE, orderedSources);

        // Now we will select only the first item and return that as our set of writable entries.
        ArrayList<WritableConfigurationSource> writable = new ArrayList<WritableConfigurationSource>(1);
        PrioritisedConfigurationSource first = orderedSources.first();
        writable.add(first);
        return writable;
    }

    /** Predicate which will only provide the first item. */
    private class FirstOnlyPredicate implements Predicate<ConfigurationSource> {
        private boolean firstRun = true;

        public boolean evaluate(ConfigurationSource object) {
            if (firstRun) {
                firstRun = false;
                return true;
            }
            return false;
        }
    }

    /** Internal comparator to support orderComparator manipulation. */
    private class OrderComparator implements Comparator<PrioritisedConfigurationSource> {
        public int compare(PrioritisedConfigurationSource lhs, PrioritisedConfigurationSource rhs) {
            return (lhs.compareTo(rhs)) * order.mutator;
        }
    }


}
