package mezz.jei.library.plugins.vanilla.crafting;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import mezz.jei.common.platform.IPlatformRecipeHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.library.util.RecipeUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CraftingCategoryExtension<T extends CraftingRecipe> implements ICraftingCategoryExtension {
	protected final T recipe;

	public CraftingCategoryExtension(T recipe) {
		this.recipe = recipe;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
		ItemStack resultItem = RecipeUtil.getResultItem(recipe);

		int width = getWidth();
		int height = getHeight();
		craftingGridHelper.createAndSetOutputs(builder, List.of(resultItem));
		craftingGridHelper.createAndSetIngredients(builder, recipe.getIngredients(), width, height);
	}

	@Nullable
	@Override
	public ResourceLocation getRegistryName() {
		return recipe.getId();
	}

	@Override
	public int getWidth() {
		if (recipe instanceof JeiShapedRecipe shapedRecipe) {
			return shapedRecipe.getWidth();
		}
		IPlatformRecipeHelper recipeHelper = Services.PLATFORM.getRecipeHelper();
		return recipeHelper.getWidth(recipe);
	}

	@Override
	public int getHeight() {
		if (recipe instanceof JeiShapedRecipe shapedRecipe) {
			return shapedRecipe.getHeight();
		}
		IPlatformRecipeHelper recipeHelper = Services.PLATFORM.getRecipeHelper();
		return recipeHelper.getHeight(recipe);
	}
}
