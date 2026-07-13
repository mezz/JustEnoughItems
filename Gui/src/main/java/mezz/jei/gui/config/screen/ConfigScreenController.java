package mezz.jei.gui.config.screen;

import mezz.jei.common.util.ImmutableRect2i;

import java.util.List;

final class ConfigScreenController {
	private final ConfigScreenModel model;
	private final ConfigScreenLayout layout;
	private final Runnable clearSearchInput;

	public ConfigScreenController(ConfigScreenModel model, ConfigScreenLayout layout, Runnable clearSearchInput) {
		this.model = model;
		this.layout = layout;
		this.clearSearchInput = clearSearchInput;
	}

	public void setSearchText(String searchText) {
		model.setSearchText(searchText);
		layout.resetContentScroll();
		updateContentLayout();
	}

	public void setActiveCategory(int index) {
		if (index < 0 || index >= model.getCategoryWidgets().size()) {
			return;
		}

		model.setActiveCategoryIndex(index);
		model.setSearchText("");
		layout.resetContentScroll();
		clearSearchInput.run();

		for (int i = 0; i < model.getCategoryWidgets().size(); i++) {
			ConfigCategoryWidget widget = model.getCategoryWidgets().get(i);
			if (i == index) {
				widget.expanded = true;
			} else {
				widget.expanded = false;
				widget.resetBounds();
			}
		}

		updateContentLayout();
	}

	public boolean hasResettableEntries() {
		return model.getResetTargetEntries()
			.anyMatch(ConfigEntryWidget::isModified);
	}

	public void resetTargetEntries() {
		List<ConfigEntryWidget<?>> entries = model.getResetTargetEntries().toList();
		for (ConfigEntryWidget<?> entry : entries) {
			entry.resetToDefault();
		}
		updateContentLayout();
	}

	public List<ConfigEntryWidget<?>> getVisibleEntryWidgets() {
		return model.getVisibleEntryWidgets();
	}

	public boolean scroll(double mouseX, double mouseY, double scrollY) {
		return layout.scroll(mouseX, mouseY, scrollY);
	}

	public boolean startContentScrollDrag(double mouseX, double mouseY) {
		if (layout.startContentScrollDrag(mouseX, mouseY)) {
			updateContentLayout();
			return true;
		}
		return false;
	}

	public boolean dragContentScroll(double mouseY) {
		if (layout.dragContentScroll(mouseY)) {
			updateContentLayout();
			return true;
		}
		return false;
	}

	public boolean stopContentScrollDrag() {
		return layout.stopContentScrollDrag();
	}

	public void stepScrollPositions() {
		if (layout.stepContentScroll()) {
			updateContentLayout();
		}
		if (layout.stepNavScroll()) {
			updateNavLayout();
		}
	}

	public void calculateNavItemHeights() {
		ImmutableRect2i navArea = layout.getNavArea();
		for (ConfigNavItem navItem : model.getNavItems()) {
			navItem.calculateHeight(navArea.getWidth());
		}
	}

	public void updateContentLayout() {
		if (updateContentLayoutInternal()) {
			updateContentLayoutInternal();
		}
	}

	private boolean updateContentLayoutInternal() {
		ImmutableRect2i contentArea = layout.getContentArea();
		int currentY = contentArea.getY() - (int) layout.getCurrentScrollY();
		int totalContentHeight = 0;

		if (model.isSearching()) {
			for (ConfigCategoryWidget widget : model.getCategoryWidgets()) {
				widget.resetBounds();
				for (ConfigEntryWidget<?> entryWidget : widget.getEntryWidgets()) {
					if (model.matchesSearch(entryWidget)) {
						int height = updateEntryBounds(entryWidget, currentY);
						currentY += height;
						totalContentHeight += height;
					}
				}
			}
		} else if (model.hasActiveCategory()) {
			for (ConfigCategoryWidget widget : model.getCategoryWidgets()) {
				if (widget != model.getActiveCategoryWidget()) {
					widget.resetBounds();
				}
			}

			ConfigCategoryWidget activeWidget = model.getActiveCategoryWidget();
			activeWidget.area = ImmutableRect2i.EMPTY;

			for (ConfigEntryWidget<?> entryWidget : activeWidget.getEntryWidgets()) {
				int height = updateEntryBounds(entryWidget, currentY);
				currentY += height;
				totalContentHeight += height;
			}
		}

		return layout.setTotalContentHeight(totalContentHeight);
	}

	private int updateEntryBounds(ConfigEntryWidget<?> entryWidget, int y) {
		ImmutableRect2i contentArea = layout.getContentArea();
		int entryWidth = contentArea.getWidth() - 4;
		entryWidget.updateBounds(new ImmutableRect2i(contentArea.getX() + 2, y, entryWidth, ConfigScreenLayout.ENTRY_HEIGHT));
		int height = entryWidget.getHeight();
		entryWidget.updateBounds(new ImmutableRect2i(contentArea.getX() + 2, y, entryWidth, height));
		return height;
	}

	public void updateNavLayout() {
		if (updateNavLayoutInternal()) {
			updateNavLayoutInternal();
		}
	}

	private boolean updateNavLayoutInternal() {
		ImmutableRect2i navArea = layout.getNavArea();
		int navY = navArea.getY() - (int) layout.getNavCurrentScrollY();
		int totalNavHeight = 0;
		for (ConfigNavItem navItem : model.getNavItems()) {
			int itemHeight = navItem.getCachedHeight();
			navItem.updateBounds(new ImmutableRect2i(
				navArea.getX(),
				navY,
				navArea.getWidth(),
				itemHeight
			));
			navY += itemHeight + ConfigScreenLayout.NAV_ITEM_GAP;
			totalNavHeight += itemHeight + ConfigScreenLayout.NAV_ITEM_GAP;
		}
		if (!model.getNavItems().isEmpty()) {
			totalNavHeight -= ConfigScreenLayout.NAV_ITEM_GAP;
		}
		return layout.setTotalNavHeight(totalNavHeight);
	}
}
