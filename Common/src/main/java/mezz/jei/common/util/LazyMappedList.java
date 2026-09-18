package mezz.jei.common.util;

import java.util.AbstractList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.function.Function;

/**
 * A read-only view that maps entries on demand and retains only the most recently accessed results.
 * The source list must not change while this view is in use.
 */
public final class LazyMappedList<T, R> extends AbstractList<R> implements RandomAccess {
	private final List<T> source;
	private final Function<T, R> mapper;
	private final int cacheSize;
	private final LinkedHashMap<Integer, R> cache = new LinkedHashMap<>(16, 0.75f, true);

	public LazyMappedList(List<T> source, Function<T, R> mapper, int cacheSize) {
		if (cacheSize <= 0) {
			throw new IllegalArgumentException("cacheSize must be positive");
		}
		this.source = source;
		this.mapper = mapper;
		this.cacheSize = cacheSize;
	}

	@Override
	public R get(int index) {
		Objects.checkIndex(index, source.size());
		R result = cache.get(index);
		if (result == null) {
			result = Objects.requireNonNull(mapper.apply(source.get(index)));
			cache.put(index, result);
			if (cache.size() > cacheSize) {
				cache.pollFirstEntry();
			}
		}
		return result;
	}

	@Override
	public int size() {
		return source.size();
	}
}
