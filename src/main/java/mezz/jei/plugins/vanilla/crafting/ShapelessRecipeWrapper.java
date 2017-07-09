package mezz.jei.plugins.vanilla.crafting;

import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IStackHelper;
import mezz.jei.api.recipe.wrapper.ICraftingRecipeWrapper;
import mezz.jei.gui.recipes.RecipeInfoIcon;
import mezz.jei.recipes.BrokenCraftingRecipeException;
import mezz.jei.util.ErrorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.List;

public class ShapelessRecipeWrapper<T extends IRecipe> implements ICraftingRecipeWrapper {
	private final IJeiHelpers jeiHelpers;
	private final RecipeInfoIcon recipeInfoIcon;
	protected final T recipe;

	public ShapelessRecipeWrapper(IJeiHelpers jeiHelpers, T recipe) {
		this.jeiHelpers = jeiHelpers;
		this.recipe = recipe;
		this.recipeInfoIcon = new RecipeInfoIcon();
	}

	@Override
	public void getIngredients(IIngredients ingredients) {
		ItemStack recipeOutput = recipe.getRecipeOutput();
		IStackHelper stackHelper = jeiHelpers.getStackHelper();

		try {
			List<List<ItemStack>> inputLists = stackHelper.expandRecipeItemStackInputs(recipe.getIngredients());
			ingredients.setInputLists(ItemStack.class, inputLists);
			ingredients.setOutput(ItemStack.class, recipeOutput);
		} catch (RuntimeException e) {
			String info = ErrorUtil.getInfoFromBrokenCraftingRecipe(recipe, recipe.getIngredients(), recipeOutput);
			throw new BrokenCraftingRecipeException(info, e);
		}
	}

	@Override
	public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
		ResourceLocation registryName = recipe.getRegistryName();
		if (registryName != null) {
			recipeInfoIcon.draw(minecraft);
		}
	}

	@Override
	public List<String> getTooltipStrings(int mouseX, int mouseY) {
		ResourceLocation registryName = recipe.getRegistryName();
		if (registryName != null) {
			return recipeInfoIcon.getTooltipStrings(registryName, mouseX, mouseY);
		}
		return Collections.emptyList();
	}
}
