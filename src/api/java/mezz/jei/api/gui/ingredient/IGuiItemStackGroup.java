package mezz.jei.api.gui.ingredient;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.world.item.ItemStack;

import mezz.jei.api.gui.IRecipeLayout;

import org.jetbrains.annotations.Nullable;
import java.util.List;

/**
 * IGuiItemStackGroup displays ItemStacks in a gui.
 *
 * If multiple ItemStacks are set, they will be displayed in rotation.
 *
 * Get an instance from {@link IRecipeLayout#getItemStacks()}.
 *
 * @deprecated Update to using {@link IRecipeCategory#setRecipe(IRecipeLayoutBuilder, Object, List)}
 */
@Deprecated(forRemoval = true, since = "9.3.0")
public interface IGuiItemStackGroup extends IGuiIngredientGroup<ItemStack> {

	/**
	 * Initialize the itemStack at slotIndex.
	 *
	 * @deprecated Update to using {@link IRecipeCategory#setRecipe(IRecipeLayoutBuilder, Object, List)}
	 * and {@link IRecipeLayoutBuilder#addSlot(RecipeIngredientRole, int, int)}
	 *
	 * @apiNote for legacy reasons, this method adds a padding and offset of 1 pixel on all sides, so that an 18x18 slot texture will center a 16x16 item.
	 * The new methods do not have this legacy 1 pixel offset.
	 */
	@Override
	@Deprecated(forRemoval = true, since = "9.3.0")
	void init(int ingredientIndex, boolean input, int xPosition, int yPosition);


	@Override
	@Deprecated(forRemoval = true, since = "9.3.0")
	void set(int ingredientIndex, @Nullable ItemStack itemStack);

	@Override
	@Deprecated(forRemoval = true, since = "9.3.0")
	void addTooltipCallback(ITooltipCallback<ItemStack> tooltipCallback);
}
