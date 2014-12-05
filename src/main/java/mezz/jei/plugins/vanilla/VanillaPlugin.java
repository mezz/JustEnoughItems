package mezz.jei.plugins.vanilla;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeType;
import mezz.jei.plugins.vanilla.crafting.CraftingRecipeMaker;
import mezz.jei.plugins.vanilla.crafting.CraftingRecipeType;
import mezz.jei.plugins.vanilla.crafting.ShapedOreRecipeHandler;
import mezz.jei.plugins.vanilla.crafting.ShapedRecipesHandler;
import mezz.jei.plugins.vanilla.crafting.ShapelessOreRecipeHandler;
import mezz.jei.plugins.vanilla.crafting.ShapelessRecipesHandler;
import mezz.jei.plugins.vanilla.furnace.FurnaceRecipeType;
import mezz.jei.plugins.vanilla.furnace.fuel.FuelRecipeHandler;
import mezz.jei.plugins.vanilla.furnace.fuel.FuelRecipeMaker;
import mezz.jei.plugins.vanilla.furnace.smelting.SmeltingRecipeHandler;
import mezz.jei.plugins.vanilla.furnace.smelting.SmeltingRecipeMaker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VanillaPlugin implements IModPlugin {

	@Override
	public Iterable<? extends IRecipeType> getRecipeTypes() {
		return Arrays.asList(
				new CraftingRecipeType(),
				new FurnaceRecipeType()
		);
	}

	@Override
	public Iterable<? extends IRecipeHandler> getRecipeHandlers() {
		return Arrays.asList(
				new ShapedOreRecipeHandler(),
				new ShapedRecipesHandler(),
				new ShapelessOreRecipeHandler(),
				new ShapelessRecipesHandler(),
				new FuelRecipeHandler(),
				new SmeltingRecipeHandler()
		);
	}

	@Override
	public Iterable<Object> getRecipes() {
		List<Object> recipes = new ArrayList<Object>();

		recipes.addAll(CraftingRecipeMaker.getCraftingRecipes());
		recipes.addAll(SmeltingRecipeMaker.getFurnaceRecipes());
		recipes.addAll(FuelRecipeMaker.getFuelRecipes());

		return recipes;
	}
}
