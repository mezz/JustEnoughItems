package mezz.jei.api.gui;

import java.util.Collection;
import java.util.List;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IStackHelper;
import net.minecraft.item.ItemStack;

/**
 * IGuiItemStackGroup displays ItemStacks in a gui.
 *
 * If multiple ItemStacks are set, they will be displayed in rotation.
 * ItemStacks with subtypes and wildcard metadata will be displayed as multiple ItemStacks.
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
	void init(int slotIndex, boolean input, int xPosition, int yPosition);

	/**
	 * Takes a list of ingredients from IRecipeWrapper getInputs or getOutputs
	 * @deprecated since JEI 3.11.2.
	 * Use {@link IStackHelper#toItemStackList(Object)} to convert to a List<ItemStack> if necessary
	 */
	@Deprecated
	void setFromRecipe(int slotIndex, List ingredients);

	/**
	 * Takes an Object from IRecipeWrapper getInputs or getOutputs
	 * @deprecated since JEI 3.11.0.
	 * Use {@link IStackHelper#toItemStackList(Object)} to convert to a List<ItemStack> if necessary
	 */
	@Deprecated
	void setFromRecipe(int slotIndex, Object ingredients);

	@Override
	void set(int slotIndex, Collection<ItemStack> itemStacks);

	@Override
	void set(int slotIndex, ItemStack itemStack);

	@Override
	void addTooltipCallback(ITooltipCallback<ItemStack> tooltipCallback);
}
