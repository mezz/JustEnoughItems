package mezz.jei.core.util;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class WeakList<T> {
	@Nullable
	private List<WeakReference<T>> list;

	public void add(T item) {
		if (list == null) {
			list = new ArrayList<>();
		}
		list.add(new WeakReference<>(item));
	}

	public void forEach(Consumer<T> consumer) {
		if (list == null) {
			return;
		}
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
		if (list.isEmpty()) {
			list = null;
		}
	}

	public boolean isEmpty() {
		if (list == null) {
			return true;
		}
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
