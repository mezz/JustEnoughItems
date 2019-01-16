package mezz.jei.gui.overlay;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

import net.minecraft.item.ItemStack;

import com.google.common.collect.ImmutableList;
import mezz.jei.api.IIngredientFilter;
import mezz.jei.api.IItemListOverlay;
import mezz.jei.config.Config;
import mezz.jei.util.ErrorUtil;

@Deprecated
public class ItemListOverlay implements IItemListOverlay {

	private final IngredientListOverlay ingredientListOverlay;
	private final IIngredientFilter ingredientFilter;

	public ItemListOverlay(IngredientListOverlay ingredientListOverlay, IIngredientFilter ingredientFilter) {
		this.ingredientListOverlay = ingredientListOverlay;
		this.ingredientFilter = ingredientFilter;
	}

	@Override
	public boolean hasKeyboardFocus() {
		return this.ingredientListOverlay.hasKeyboardFocus();
	}

	@Nullable
	@Override
	public ItemStack getStackUnderMouse() {
		Object ingredient = this.ingredientListOverlay.getIngredientUnderMouse();
		if (ingredient instanceof ItemStack) {
			return (ItemStack) ingredient;
		}
		return null;
	}

	@Override
	public void setFilterText(String filterText) {
		ErrorUtil.checkNotNull(filterText, "filterText");
		if (Config.setFilterText(filterText)) {
			this.ingredientListOverlay.onSetFilterText(filterText);
		}
	}

	@Override
	public String getFilterText() {
		return Config.getFilterText();
	}

	@Override
	public ImmutableList<ItemStack> getFilteredStacks() {
		List<Object> ingredients = ingredientFilter.getFilteredIngredients();
		ImmutableList.Builder<ItemStack> builder = ImmutableList.builder();
		for (Object ingredient : ingredients) {
			if (ingredient instanceof ItemStack) {
				builder.add((ItemStack) ingredient);
			}
		}
		return builder.build();
	}

	@Override
	public ImmutableList<ItemStack> getVisibleStacks() {
		if (this.ingredientListOverlay.isListDisplayed()) {
			ImmutableList.Builder<ItemStack> visibleStacks = ImmutableList.builder();
			List<Object> visibleIngredients = this.ingredientListOverlay.getVisibleIngredients();
			for (Object ingredient : visibleIngredients) {
				if (ingredient instanceof ItemStack) {
					visibleStacks.add((ItemStack) ingredient);
				}
			}

			return visibleStacks.build();
		}
		return ImmutableList.of();
	}

	@Override
	public void highlightStacks(Collection<ItemStack> stacks) {

	}
}
