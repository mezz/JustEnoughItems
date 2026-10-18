package mezz.jei.common.search;

import mezz.jei.api.search.ISearchStorage;
import mezz.jei.api.search.ISearchStorageBuilder;
import mezz.jei.api.search.ISearchStorageBuilderFactory;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Filters removed values when a custom backend cannot delete its entries.
 * Requests an index rebuild when too many obsolete entries accumulate.
 */
public class RemovalFallbackSearchStorage<T> implements ISearchStorage<T> {
	private static final int MIN_STALE_STRINGS_BEFORE_REBUILD = 1024;

	private final ISearchStorage<Entry<T>> delegate;
	private final Map<T, Entry<T>> entries;
	private long liveStrings;
	private long staleStrings;

	private RemovalFallbackSearchStorage(ISearchStorage<Entry<T>> delegate, Map<T, Entry<T>> entries, long liveStrings) {
		this.delegate = delegate;
		this.entries = entries;
		this.liveStrings = liveStrings;
	}

	public static ISearchStorageBuilderFactory wrap(ISearchStorageBuilderFactory factory) {
		if (factory.supportsDeletion()) {
			return factory;
		}
		return new ISearchStorageBuilderFactory() {
			@Override
			public <T> ISearchStorageBuilder<T> create() {
				return wrapIfNeeded(factory.create());
			}

			@Override
			public <T> ISearchStorageBuilder<T> create(String id) {
				return wrapIfNeeded(factory.create(id));
			}

			@Override
			public boolean supportsDeletion() {
				return true;
			}
		};
	}

	private static <T> ISearchStorageBuilder<T> wrapIfNeeded(ISearchStorageBuilder<T> builder) {
		if (builder instanceof SearchStorageBuilderAdapter<T> adapter && adapter.getSearchStorage().supportsDeletion()) {
			return builder;
		}
		// A fresh generic builder has no values yet, so it can index entries instead.
		@SuppressWarnings("unchecked")
		ISearchStorageBuilder<Entry<T>> entryBuilder = (ISearchStorageBuilder<Entry<T>>) (ISearchStorageBuilder<?>) builder;
		return new Builder<>(entryBuilder);
	}

	@Override
	public boolean supportsDeletion() {
		return true;
	}

	@Override
	public void put(String key, T value) {
		Entry<T> entry = entries.computeIfAbsent(value, Entry::new);
		delegate.put(key, entry);
		entry.strings++;
		liveStrings++;
	}

	@Override
	public boolean removeAll(Collection<T> values) {
		Set<Entry<T>> removed = Collections.newSetFromMap(new IdentityHashMap<>());
		for (T value : values) {
			Entry<T> entry = entries.get(value);
			if (entry != null) {
				removed.add(entry);
			}
		}
		if (removed.isEmpty()) {
			return true;
		}
		long removedStrings = 0;
		for (Entry<T> entry : removed) {
			removedStrings += entry.strings;
		}
		if (delegate.supportsDeletion()) {
			if (!delegate.removeAll(removed)) {
				return false;
			}
		} else {
			// Allow 25% obsolete strings, with a small floor to avoid frequent rebuilds of tiny indexes.
			long limit = Math.max(MIN_STALE_STRINGS_BEFORE_REBUILD, (liveStrings - removedStrings) / 4);
			if (staleStrings + removedStrings > limit) {
				return false;
			}
			staleStrings += removedStrings;
		}
		for (Entry<T> entry : removed) {
			entries.remove(entry.value);
			entry.value = null;
		}
		liveStrings -= removedStrings;
		return true;
	}

	@Override
	public void getSearchResults(String token, Consumer<Collection<T>> resultsConsumer) {
		List<T> results = new ArrayList<>();
		delegate.getSearchResults(token, matches -> collectActive(matches, results));
		resultsConsumer.accept(results);
	}

	@Override
	public void getAllElements(Consumer<Collection<T>> resultsConsumer) {
		List<T> results = new ArrayList<>();
		delegate.getAllElements(matches -> collectActive(matches, results));
		resultsConsumer.accept(results);
	}

	private static <T> void collectActive(Collection<Entry<T>> matches, List<T> results) {
		for (Entry<T> entry : matches) {
			T value = entry.value;
			if (value != null) {
				results.add(value);
			}
		}
	}

	@Override
	public String statistics() {
		return "RemovalFallbackSearchStorage: " + staleStrings + " obsolete strings, " + delegate.statistics();
	}

	private static class Entry<T> {
		private @Nullable T value;
		private long strings;

		private Entry(T value) {
			this.value = value;
		}
	}

	private static class Builder<T> implements ISearchStorageBuilder<T> {
		private final ISearchStorageBuilder<Entry<T>> delegate;
		private final Map<T, Entry<T>> entries = new IdentityHashMap<>();
		private long liveStrings;
		private boolean built;

		private Builder(ISearchStorageBuilder<Entry<T>> delegate) {
			this.delegate = delegate;
		}

		@Override
		public void put(String key, T value) {
			checkNotBuilt();
			Entry<T> entry = entries.computeIfAbsent(value, Entry::new);
			delegate.put(key, entry);
			entry.strings++;
			liveStrings++;
		}

		@Override
		public ISearchStorage<T> build() {
			checkNotBuilt();
			built = true;
			return new RemovalFallbackSearchStorage<>(delegate.build(), entries, liveStrings);
		}

		private void checkNotBuilt() {
			if (built) {
				throw new IllegalStateException("Search storage has already been built");
			}
		}
	}
}
