package mezz.jei.plugins.vanilla;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.ContainerWorkbench;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JEIManager;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeTransferHelper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.plugins.vanilla.crafting.CraftingRecipeCategory;
import mezz.jei.plugins.vanilla.crafting.CraftingRecipeMaker;
import mezz.jei.plugins.vanilla.crafting.ShapedOreRecipeHandler;
import mezz.jei.plugins.vanilla.crafting.ShapedRecipesHandler;
import mezz.jei.plugins.vanilla.crafting.ShapelessOreRecipeHandler;
import mezz.jei.plugins.vanilla.crafting.ShapelessRecipesHandler;
import mezz.jei.plugins.vanilla.furnace.FuelRecipeHandler;
import mezz.jei.plugins.vanilla.furnace.FuelRecipeMaker;
import mezz.jei.plugins.vanilla.furnace.FurnaceFuelCategory;
import mezz.jei.plugins.vanilla.furnace.FurnaceSmeltingCategory;
import mezz.jei.plugins.vanilla.furnace.SmeltingRecipeHandler;
import mezz.jei.plugins.vanilla.furnace.SmeltingRecipeMaker;

public class VanillaPlugin implements IModPlugin {

	@Override
	public boolean isModLoaded() {
		return true;
	}

	@Override
	@Nonnull
	public Iterable<? extends IRecipeCategory> getRecipeCategories() {
		return Arrays.asList(
				new CraftingRecipeCategory(),
				new FurnaceFuelCategory(),
				new FurnaceSmeltingCategory()
		);
	}

	@Override
	@Nonnull
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
	@Nonnull
	public Iterable<? extends IRecipeTransferHelper> getRecipeTransferHelpers() {
		IGuiHelper guiHelper = JEIManager.guiHelper;
		return Arrays.asList(
				guiHelper.createRecipeTransferHelper(ContainerWorkbench.class, VanillaRecipeCategoryUid.CRAFTING, 1, 9, 10, 36),
				guiHelper.createRecipeTransferHelper(ContainerFurnace.class, VanillaRecipeCategoryUid.SMELTING, 0, 1, 1, 36),
				guiHelper.createRecipeTransferHelper(ContainerFurnace.class, VanillaRecipeCategoryUid.FUEL, 1, 1, 1, 36)
		);
	}

	@Override
	@Nonnull
	public Iterable<Object> getRecipes() {
		List<Object> recipes = new ArrayList<>();

		recipes.addAll(CraftingRecipeMaker.getCraftingRecipes());
		recipes.addAll(SmeltingRecipeMaker.getFurnaceRecipes());
		recipes.addAll(FuelRecipeMaker.getFuelRecipes());

		return recipes;
	}
}
