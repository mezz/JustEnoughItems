package mezz.jei.gui.overlay;

public interface IFilterTextSource {
	String getFilterText();

	boolean setFilterText(String filterText);

	void addListener(Listener listener);

	interface Listener {
		void onChange(String filterText);
	}
}
