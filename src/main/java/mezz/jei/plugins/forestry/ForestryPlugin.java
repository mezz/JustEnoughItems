package mezz.jei.plugins.forestry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cpw.mods.fml.common.Loader;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.plugins.forestry.centrifuge.CentrifugeRecipeCategory;
import mezz.jei.plugins.forestry.centrifuge.CentrifugeRecipeHandler;
import mezz.jei.plugins.forestry.centrifuge.CentrifugeRecipeMaker;
import mezz.jei.plugins.forestry.crafting.ForestryShapedRecipeHandler;
import mezz.jei.plugins.forestry.fabricator.FabricatorCraftingRecipeHandler;
import mezz.jei.plugins.forestry.fabricator.FabricatorRecipeCategory;
import mezz.jei.plugins.forestry.fabricator.FabricatorRecipeMaker;
import mezz.jei.plugins.forestry.fabricator.FabricatorSmeltingRecipeHandler;

public class ForestryPlugin implements IModPlugin {

	@Override
	public boolean isModLoaded() {
		return Loader.isModLoaded("Forestry");
	}

	@Override
	public Iterable<? extends IRecipeCategory> getRecipeCategories() {
		return Arrays.asList(
				new CentrifugeRecipeCategory(),
				new FabricatorRecipeCategory()
		);
	}

	@Override
	public Iterable<? extends IRecipeHandler> getRecipeHandlers() {
		return Arrays.asList(
				new ForestryShapedRecipeHandler(),
				new CentrifugeRecipeHandler(),
				new FabricatorCraftingRecipeHandler(),
				new FabricatorSmeltingRecipeHandler()
		);
	}

	@Override
	public Iterable<Object> getRecipes() {
		List<Object> recipes = new ArrayList<Object>();

		recipes.addAll(CentrifugeRecipeMaker.getCentrifugeRecipes());
		recipes.addAll(FabricatorRecipeMaker.getCraftingRecipes());
		recipes.addAll(FabricatorRecipeMaker.getSmeltingRecipes());

		return recipes;
	}
}
