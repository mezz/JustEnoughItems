package mezz.jei.input;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class TextHistory {
	private static final int MAX_HISTORY = 100;

	public enum Direction {
		NEXT, PREVIOUS
	}

	private final List<String> history = new LinkedList<>();

	public boolean add(String currentText) {
		if (currentText.length() > 0) {
			history.remove(currentText);
			history.add(currentText);
			if (history.size() > MAX_HISTORY) {
				history.remove(0);
			}
			return true;
		}
		return false;
	}

	public Optional<String> get(Direction direction, String currentText) {
		return switch (direction) {
			case NEXT -> getNext(currentText);
			case PREVIOUS -> getPrevious(currentText);
		};
	}

	public Optional<String> getPrevious(String currentText) {
		int historyIndex = history.indexOf(currentText);
		if (historyIndex < 0) {
			if (add(currentText)) {
				historyIndex = history.size() - 1;
			} else {
				historyIndex = history.size();
			}
		}
		if (historyIndex <= 0) {
			return Optional.empty();
		}
		String value = history.get(historyIndex - 1);
		return Optional.of(value);
	}

	public Optional<String> getNext(String currentText) {
		int historyIndex = history.indexOf(currentText);
		if (historyIndex < 0) {
			return Optional.empty();
		}
		final String historyString;
		if (historyIndex + 1 < history.size()) {
			historyString = history.get(historyIndex + 1);
		} else {
			historyString = "";
		}
		return Optional.of(historyString);
	}
}
