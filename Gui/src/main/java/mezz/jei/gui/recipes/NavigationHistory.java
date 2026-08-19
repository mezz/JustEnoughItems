package mezz.jei.gui.recipes;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

final class NavigationHistory<T> {
	private final Deque<T> backwardHistory = new ArrayDeque<>();
	private final Deque<T> forwardHistory = new ArrayDeque<>();

	public void record(T value) {
		backwardHistory.push(value);
		forwardHistory.clear();
	}

	public Optional<T> goBack(T currentValue) {
		T previousValue = backwardHistory.poll();
		if (previousValue == null) {
			return Optional.empty();
		}
		forwardHistory.push(currentValue);
		return Optional.of(previousValue);
	}

	public Optional<T> goForward(T currentValue) {
		T nextValue = forwardHistory.poll();
		if (nextValue == null) {
			return Optional.empty();
		}
		backwardHistory.push(currentValue);
		return Optional.of(nextValue);
	}

	public void clear() {
		backwardHistory.clear();
		forwardHistory.clear();
	}
}
