package mezz.jei.core.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class WeakList<T> {
	private final List<WeakReference<T>> list = new ArrayList<>();

	public void add(T item) {
		list.add(new WeakReference<>(item));
	}

	public void forEach(Consumer<T> consumer) {
		Iterator<WeakReference<T>> iterator = list.iterator();
		while (iterator.hasNext()) {
			WeakReference<T> reference = iterator.next();
			T item = reference.get();
			if (item == null) {
				iterator.remove();
			} else {
				consumer.accept(item);
			}
		}
	}

	public boolean isEmpty() {
		Iterator<WeakReference<T>> iterator = list.iterator();
		while (iterator.hasNext()) {
			WeakReference<T> reference = iterator.next();
			T item = reference.get();
			if (item == null) {
				iterator.remove();
			} else {
				return false;
			}
		}
		return true;
	}
}
