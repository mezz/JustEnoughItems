package mezz.jei.api.gui;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

import net.minecraft.item.ItemStack;

/**
 * IGuiItemStackGroup displays ItemStacks in a gui.
 *
 * If multiple ItemStacks are set, they will be displayed in rotation.
 * ItemStacks with subtypes and wildcard metadata will be displayed as multiple ItemStacks.
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
	void init(int slotIndex, boolean input, int xPosition, int yPosition);

	/**
	 * Takes a list of ingredients from IRecipeWrapper getInputs or getOutputs
	 */
	void setFromRecipe(int slotIndex, @Nonnull List ingredients);

	/**
	 * Takes an Object from IRecipeWrapper getInputs or getOutputs
	 */
	void setFromRecipe(int slotIndex, @Nonnull Object ingredients);

	@Override
	void set(int slotIndex, @Nonnull Collection<ItemStack> itemStacks);

	@Override
	void set(int slotIndex, @Nonnull ItemStack itemStack);

	@Override
	void addTooltipCallback(@Nonnull ITooltipCallback<ItemStack> tooltipCallback);
}
