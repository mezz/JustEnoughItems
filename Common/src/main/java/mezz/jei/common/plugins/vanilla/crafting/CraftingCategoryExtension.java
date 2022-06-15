package mezz.jei.common.plugins.vanilla.crafting;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import mezz.jei.common.platform.IPlatformRecipeHelper;
import mezz.jei.common.platform.Services;
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
		List<List<ItemStack>> inputs = recipe.getIngredients().stream()
			.map(ingredient -> List.of(ingredient.getItems()))
			.toList();
		ItemStack resultItem = recipe.getResultItem();

		int width = getWidth();
		int height = getHeight();
		craftingGridHelper.setOutputs(builder, VanillaTypes.ITEM_STACK, List.of(resultItem));
		craftingGridHelper.setInputs(builder, VanillaTypes.ITEM_STACK, inputs, width, height);
	}

	@Nullable
	@Override
	public ResourceLocation getRegistryName() {
		return recipe.getId();
	}

	@Override
	public int getWidth() {
		IPlatformRecipeHelper recipeHelper = Services.PLATFORM.getRecipeHelper();
		return recipeHelper.getWidth(recipe);
	}

	@Override
	public int getHeight() {
		IPlatformRecipeHelper recipeHelper = Services.PLATFORM.getRecipeHelper();
		return recipeHelper.getHeight(recipe);
	}
}
