package mezz.jei.config;

/** Mutable search text for one filter context. */
public class FilterTextSource implements IFilterTextSource {
	private final ConfigValues values;

	public FilterTextSource(ConfigValues values) {
		this.values = values;
	}

	@Override
	public String getFilterText() {
		return values.filterText;
	}

	@Override
	public boolean setFilterText(String filterText) {
		if (values.filterText.equals(filterText)) {
			return false;
		}
		values.filterText = filterText;
		return true;
	}
}
