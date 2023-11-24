package net.neoforged.testframework.collector;

import com.google.common.collect.Multimap;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.neoforged.fml.ModContainer;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface Collector<Z> {
    void collect(ModContainer container, Consumer<Z> acceptor);

    default Collector<Z> and(Collector<Z> other) {
        return (container, acceptor) -> {
            this.collect(container, acceptor);
            other.collect(container, acceptor);
        };
    }

    default <C extends Collection<Z>> C toCollection(ModContainer container, Supplier<? extends C> collectionFactory) {
        final C col = collectionFactory.get();
        collect(container, col::add);
        return col;
    }

    default <K, V, M extends Multimap<K, V>> M toMultimap(ModContainer container, M multimap, Function<Z, K> keyCollector, Function<Z, V> valueCollector) {
        collect(container, z -> multimap.put(keyCollector.apply(z), valueCollector.apply(z)));
        return multimap;
    }
}
