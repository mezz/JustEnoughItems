package mezz.jei.collect;

import mezz.jei.api.ingredients.IExtractableIngredientHelper;
import mezz.jei.api.ingredients.subtypes.UidContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class StackSet<S, T> extends AbstractIngredientSet<T> {
	private final IExtractableIngredientHelper<T, S> ingredientHelper;
	private final Map<S, Object> ingredients;

	public StackSet(IExtractableIngredientHelper<T, S> ingredientHelper) {
		this.ingredientHelper = ingredientHelper;
		this.ingredients = new HashMap<>();
	}

	private String getUid(T v) {
		return this.ingredientHelper.getUniqueId(v, UidContext.Ingredient);
	}

	@Override
	public Optional<T> getByUid(String uid) {
		return this.ingredients.values()
			.stream()
			.<Optional<T>>map(stored -> {
				if (stored instanceof InnerStackSet) {
					@SuppressWarnings("unchecked")
					InnerStackSet<T> innerStackSet = (InnerStackSet<T>) stored;
					return innerStackSet.getByUid(uid, this::getUid);
				}
				@SuppressWarnings("unchecked")
				T value = (T) stored;
				if (getUid(value).equals(uid)) {
					return Optional.of(value);
				}
				return Optional.empty();
			})
			.flatMap(Optional::stream)
			.findFirst();
	}

	@Override
	public boolean add(T v) {
		S item = ingredientHelper.extractImmutablePart(v);
		Object stored = ingredients.get(item);
		if (stored == null) {
			ingredients.put(item, v);
			return true;
		}
		if (stored instanceof InnerStackSet) {
			@SuppressWarnings("unchecked")
			InnerStackSet<T> innerStackSet = (InnerStackSet<T>) stored;
			return innerStackSet.add(v, this::getUid);
		}
		@SuppressWarnings("unchecked")
		T storedValue = (T) stored;
		String storedUid = getUid(storedValue);
		String vUid = getUid(v);
		if (!storedUid.equals(vUid)) {
			ingredients.put(item, new InnerStackSet<>(storedValue, v, storedUid, vUid));
			return true;
		}
		return false;
	}

	@Override
	public boolean remove(Object o) {
		@SuppressWarnings("unchecked")
		T stack = (T) o;
		S item = ingredientHelper.extractImmutablePart(stack);
		Object stored = ingredients.get(item);
		if (stored == null) {
			return false;
		}
		if (stored instanceof InnerStackSet<?>) {
			@SuppressWarnings("unchecked")
			InnerStackSet<T> innerStackSet = (InnerStackSet<T>) stored;
			if (innerStackSet.remove(stack, this::getUid)) {
				if (innerStackSet.size() == 0) {
					ingredients.remove(item);
				} else if (innerStackSet.size() == 1) {
					ingredients.put(item, stack);
				}
				return true;
			}
			return false;
		}
		@SuppressWarnings("unchecked")
		T storedValue = (T) stored;
		String storedUid = getUid(storedValue);
		String stackUid = getUid(stack);
		if (storedUid.equals(stackUid)) {
			ingredients.remove(item);
			return true;
		}
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		if (c instanceof StackSet) {
			return super.removeAll(c);
		}
		Objects.requireNonNull(c);
		boolean modified = false;
		for (Object aC : c) {
			modified |= remove(aC);
		}
		return modified;
	}

	@Override
	public boolean contains(Object o) {
		@SuppressWarnings("unchecked")
		T stack = (T) o;
		S single = ingredientHelper.extractImmutablePart(stack);
		Object stored = ingredients.get(single);
		if (stored == null) {
			return false;
		}
		if (stored instanceof InnerStackSet<?>) {
			@SuppressWarnings("unchecked")
			InnerStackSet<T> innerStackSet = (InnerStackSet<T>) stored;
			return innerStackSet.contains(stack, this::getUid);
		}
		@SuppressWarnings("unchecked")
		T storedValue = (T) stored;
		String storedUid = getUid(storedValue);
		String stackUid = getUid(stack);
		return storedUid.equals(stackUid);
	}

	@Override
	public void clear() {
		ingredients.clear();
	}

	@Override
	public Iterator<T> iterator() {
		return ingredients.values()
			.stream()
			.flatMap(stored -> {
				if (stored instanceof InnerStackSet) {
					@SuppressWarnings("unchecked")
					InnerStackSet<T> s = (InnerStackSet<T>) stored;
					return s.stream();
				}
				@SuppressWarnings("unchecked")
				T value = (T) stored;
				return Stream.of(value);
			})
			.iterator();
	}

	@Override
	public int size() {
		return ingredients.values()
			.stream()
			.mapToInt(stored -> {
				if (stored instanceof InnerStackSet s) {
					return s.size();
				}
				return 1;
			})
			.sum();
	}
}
