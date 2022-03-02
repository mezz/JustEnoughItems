package mezz.jei.collect;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

class InnerStackSet<T> {
	private final Set<String> uids;
	private final List<T> list;

	public InnerStackSet(T a, T b, String uidA, String uidB) {
		list = Arrays.asList(a, b);
		uids = new HashSet<>();
		uids.add(uidA);
		uids.add(uidB);
	}

	public boolean add(T stack, Function<T, String> uidGenerator) {
		String uid = uidGenerator.apply(stack);
		if (uids.add(uid)) {
			list.add(stack);
			return true;
		}
		return false;
	}

	public boolean remove(T stack, Function<T, String> uidGenerator) {
		String uid = uidGenerator.apply(stack);
		if (uids.remove(uid)) {
			list.removeIf(i -> uidGenerator.apply(i).equals(uid));
			return true;
		}
		return false;
	}

	public boolean contains(T stack, Function<T, String> uidGenerator) {
		String uid = uidGenerator.apply(stack);
		return uids.contains(uid);
	}

	public Optional<T> getByUid(String uid, Function<T, String> uidGenerator) {
		if (uids.contains(uid)) {
			return list.stream()
				.filter(i -> uidGenerator.apply(i).equals(uid))
				.findFirst();
		}
		return Optional.empty();
	}

	public T first() {
		return list.get(0);
	}

	public Stream<T> stream() {
		return list.stream();
	}

	public int size() {
		return list.size();
	}

	public boolean isEmpty() {
		return list.isEmpty();
	}
}
