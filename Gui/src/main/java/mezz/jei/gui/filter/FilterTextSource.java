package mezz.jei.gui.filter;

import java.util.ArrayList;
import java.util.List;

public class FilterTextSource implements IFilterTextSource {
	private final List<Listener> listeners = new ArrayList<>();
	private String filterText = "";

	@Override
	public String getFilterText() {
		return filterText;
	}

	@Override
	public boolean setFilterText(String filterText) {
		if (this.filterText.equals(filterText)) {
			return false;
		}
		this.filterText = filterText;
		for (Listener listener : this.listeners) {
			listener.onChange(filterText);
		}
		return true;
	}

	@Override
	public void addListener(Listener listener) {
		this.listeners.add(listener);
	}
}
