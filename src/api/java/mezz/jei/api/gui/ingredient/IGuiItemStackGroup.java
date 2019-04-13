package mezz.jei.api.gui.ingredient;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;

import mezz.jei.api.gui.IRecipeLayout;

/**
 * IGuiItemStackGroup displays ItemStacks in a gui.
 *
 * If multiple ItemStacks are set, they will be displayed in rotation.
 *
 * Get an instance from {@link IRecipeLayout#getItemStacks()}.
 */
public interface IGuiItemStackGroup extends IGuiIngredientGroup<ItemStack> {

	/**
	 * Initialize the itemStack at slotIndex.
	 *
	 * @param slotIndex the slot index of this itemStack
	 * @param input     whether this slot is an input. Used for the recipe-fill feature.
	 * @param xPosition x position of the slot relative to the recipe background
	 * @param yPosition y position of the slot relative to the recipe background
	 */
	@Override
	void init(int slotIndex, boolean input, int xPosition, int yPosition);

	@Override
	void set(int slotIndex, @Nullable ItemStack itemStack);

	@Override
	void addTooltipCallback(ITooltipCallback<ItemStack> tooltipCallback);
}
