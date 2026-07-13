package mezz.jei.gui.config.screen;

import mezz.jei.api.runtime.config.IJeiConfigCategory;
import mezz.jei.common.config.file.IConfigSchema;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

final class ConfigScreenModel {
	private final List<IJeiConfigCategory> categories;
	private final List<ConfigCategoryWidget> categoryWidgets = new ArrayList<>();
	private final List<ConfigNavItem> navItems = new ArrayList<>();

	private int activeCategoryIndex = 0;
	private String searchText = "";

	public ConfigScreenModel(IConfigSchema clientSchema) {
		this.categories = List.copyOf(clientSchema.getCategories());
	}

	public List<IJeiConfigCategory> getCategories() {
		return categories;
	}

	public void addCategoryWidget(ConfigCategoryWidget categoryWidget) {
		categoryWidgets.add(categoryWidget);
	}

	public List<ConfigCategoryWidget> getCategoryWidgets() {
		return categoryWidgets;
	}

	public void addNavItem(ConfigNavItem navItem) {
		navItems.add(navItem);
	}

	public List<ConfigNavItem> getNavItems() {
		return navItems;
	}

	public int getActiveCategoryIndex() {
		return activeCategoryIndex;
	}

	public void setActiveCategoryIndex(int activeCategoryIndex) {
		this.activeCategoryIndex = activeCategoryIndex;
	}

	public boolean hasActiveCategory() {
		return activeCategoryIndex >= 0 && activeCategoryIndex < categoryWidgets.size();
	}

	public ConfigCategoryWidget getActiveCategoryWidget() {
		return categoryWidgets.get(activeCategoryIndex);
	}

	public void setSearchText(String searchText) {
		this.searchText = searchText.toLowerCase(Locale.ROOT);
	}

	public String getSearchText() {
		return searchText;
	}

	public boolean isSearching() {
		return !searchText.isEmpty();
	}

	public boolean matchesSearch(ConfigEntryWidget<?> entry) {
		if (!isSearching()) {
			return true;
		}
		String name = entry.fullName.getString().toLowerCase(Locale.ROOT);
		return name.contains(searchText);
	}

	public List<ConfigEntryWidget<?>> getVisibleEntryWidgets() {
		if (isSearching()) {
			return getAllEntryWidgets()
				.filter(this::matchesSearch)
				.toList();
		}
		if (!hasActiveCategory()) {
			return List.of();
		}
		return List.copyOf(getActiveCategoryWidget().getEntryWidgets());
	}

	public Stream<ConfigEntryWidget<?>> getResetTargetEntries() {
		return getVisibleEntryWidgets().stream();
	}

	public Stream<ConfigEntryWidget<?>> getAllEntryWidgets() {
		return categoryWidgets.stream()
			.flatMap(widget -> widget.getEntryWidgets().stream());
	}
}
