package mezz.jei.input;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

public class TextHistory {
	private static final int MAX_HISTORY = 100;
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

	@Nullable
	public String getPrevious(String currentText) {
		int historyIndex = history.indexOf(currentText);
		if (historyIndex < 0) {
			if (add(currentText)) {
				historyIndex = history.size() - 1;
			} else {
				historyIndex = history.size();
			}
		}
		if (historyIndex <= 0) {
			return null;
		}
		return history.get(historyIndex - 1);
	}

	@Nullable
	public String getNext(String currentText) {
		int historyIndex = history.indexOf(currentText);
		if (historyIndex < 0) {
			return null;
		}
		String historyString;
		if (historyIndex + 1 < history.size()) {
			historyString = history.get(historyIndex + 1);
		} else {
			historyString = "";
		}
		return historyString;
	}
}
