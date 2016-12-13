package mezz.jei.gui;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import mezz.jei.ItemFilter;
import mezz.jei.api.IItemListOverlay;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.config.Config;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;

public class ItemListOverlay implements IItemListOverlay {
	private final ItemFilter itemFilter;
	private final List<IAdvancedGuiHandler<?>> advancedGuiHandlers;
	private final IIngredientRegistry ingredientRegistry;
	private final Set<ItemStack> highlightedStacks = new HashSet<ItemStack>();

	@Nullable
	private ItemListOverlayInternal internal;

	public ItemListOverlay(ItemFilter itemFilter, List<IAdvancedGuiHandler<?>> advancedGuiHandlers, IIngredientRegistry ingredientRegistry) {
		this.itemFilter = itemFilter;
		this.advancedGuiHandlers = advancedGuiHandlers;
		this.ingredientRegistry = ingredientRegistry;
	}

	@Nullable
	public ItemListOverlayInternal create(GuiScreen guiScreen) {
		close();

		if (Config.isOverlayEnabled()) {
			GuiProperties guiProperties = GuiProperties.create(guiScreen);
			if (guiProperties != null) {
				final int columns = ItemListOverlayInternal.getColumns(guiProperties);
				if (columns >= 4) {
					internal = new ItemListOverlayInternal(this, ingredientRegistry, guiScreen, guiProperties);
					return internal;
				}
			}
		}

		return null;
	}

	@Nullable
	public ItemListOverlayInternal getInternal() {
		return internal;
	}

	@Nullable
	@Override
	public ItemStack getStackUnderMouse() {
		if (internal != null) {
			return internal.getStackUnderMouse();
		}
		return null;
	}

	@Override
	public void setFilterText(@Nullable String filterText) {
		Preconditions.checkNotNull(filterText, "filterText cannot be null");

		Config.setFilterText(filterText);

		if (internal != null) {
			internal.setFilterText(filterText);
		}
	}

	public void rebuildItemFilter() {
		ItemFilter itemFilter = getItemFilter();
		itemFilter.rebuild();
		ItemListOverlayInternal.setToFirstPage();
		if (internal != null) {
			internal.updateLayout();
		}
	}

	@Override
	public String getFilterText() {
		return Config.getFilterText();
	}

	@Override
	public ImmutableList<ItemStack> getVisibleStacks() {
		if (internal == null) {
			return ImmutableList.of();
		}
		return internal.getVisibleStacks();
	}

	@Override
	public ImmutableList<ItemStack> getFilteredStacks() {
		return itemFilter.getItemStacks();
	}

	@Override
	public void highlightStacks(Collection<ItemStack> stacks) {
		highlightedStacks.clear();
		highlightedStacks.addAll(stacks);
	}

	public Set<ItemStack> getHighlightedStacks() {
		return highlightedStacks;
	}

	public ItemFilter getItemFilter() {
		return itemFilter;
	}

	public List<IAdvancedGuiHandler<?>> getAdvancedGuiHandlers() {
		return advancedGuiHandlers;
	}

	public boolean isOpen() {
		return internal != null;
	}

	public void close() {
		if (internal != null) {
			internal.close();
		}
		internal = null;
	}
}
