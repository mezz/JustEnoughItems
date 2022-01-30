package mezz.jei.api.gui.ingredient;

import org.jetbrains.annotations.Nullable;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.world.item.ItemStack;

import mezz.jei.api.gui.IRecipeLayout;

import java.util.List;

/**
 * IGuiItemStackGroup displays ItemStacks in a gui.
 *
 * If multiple ItemStacks are set, they will be displayed in rotation.
 *
 * Get an instance from {@link IRecipeLayout#getItemStacks()}.
 *
 * @deprecated since JEI 9.3.0.
 * Update to using {@link IRecipeCategory#setRecipe(IRecipeLayoutBuilder, Object, List)}
 */
@Deprecated
public interface IGuiItemStackGroup extends IGuiIngredientGroup<ItemStack> {

	/**
	 * Initialize the itemStack at slotIndex.
	 *
	 * Note that for legacy reasons, this method adds a padding and offset of 1 pixel on all sides, so that an 18x18 slot texture will center a 16x16 item.
	 * If you do not want this behavior, use the full init method defined in
	 * {@link IGuiIngredientGroup#init(int, boolean, IIngredientRenderer, int, int, int, int, int, int)} with padding set to 0.
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
}
