package mezz.jei.plugins.jei.debug;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import mezz.jei.api.IIngredientFilter;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.plugins.jei.JEIInternalPlugin;
import mezz.jei.plugins.jei.ingredients.DebugIngredient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.client.config.HoverChecker;

import javax.annotation.Nullable;

public class DebugRecipe implements IRecipeWrapper {
	private final GuiButtonExt button;
	private final HoverChecker buttonHoverChecker;
	@Nullable
	private List<IRecipeWrapper> hiddenRecipes;

	public DebugRecipe() {
		this.button = new GuiButtonExt(0, 110, 30, "test");
		this.button.setWidth(40);
		this.buttonHoverChecker = new HoverChecker(this.button, 0);
	}

	@Override
	public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
		button.drawButton(minecraft, mouseX, mouseY, minecraft.getRenderPartialTicks());
	}

	@Override
	public void getIngredients(IIngredients ingredients) {
		FluidStack water = new FluidStack(FluidRegistry.WATER, 1000 + (int) (Math.random() * 1000));
		FluidStack lava = new FluidStack(FluidRegistry.LAVA, 1000 + (int) (Math.random() * 1000));

		ingredients.setInputs(FluidStack.class, Arrays.asList(water, lava));

		ingredients.setInput(ItemStack.class, new ItemStack(Items.STICK));

		ingredients.setInputLists(DebugIngredient.class, Collections.singletonList(
				Arrays.asList(new DebugIngredient(0), new DebugIngredient(1))
		));

		ingredients.setOutputs(DebugIngredient.class, Arrays.asList(
				new DebugIngredient(2),
				new DebugIngredient(3)
		));
	}

	public List<FluidStack> getFluidInputs() {
		return Arrays.asList(
				new FluidStack(FluidRegistry.WATER, 1000 + (int) (Math.random() * 1000)),
				new FluidStack(FluidRegistry.LAVA, 1000 + (int) (Math.random() * 1000))
		);
	}

	@Override
	public List<String> getTooltipStrings(int mouseX, int mouseY) {
		List<String> tooltipStrings = new ArrayList<>();
		if (buttonHoverChecker.checkHover(mouseX, mouseY)) {
			tooltipStrings.add("button tooltip!");
		} else {
			tooltipStrings.add(TextFormatting.BOLD + "tooltip debug");
		}
		tooltipStrings.add(mouseX + ", " + mouseY);
		return tooltipStrings;
	}

	@Override
	public boolean handleClick(Minecraft minecraft, int mouseX, int mouseY, int mouseButton) {
		if (mouseButton == 0 && button.mousePressed(minecraft, mouseX, mouseY)) {
			GuiScreen screen = new GuiInventory(minecraft.player);
			minecraft.displayGuiScreen(screen);

			IJeiRuntime runtime = JEIInternalPlugin.jeiRuntime;
			if (runtime != null) {
				IIngredientFilter ingredientFilter = runtime.getIngredientFilter();
				String filterText = ingredientFilter.getFilterText();
				ingredientFilter.setFilterText(filterText + " test");

				IRecipeRegistry recipeRegistry = runtime.getRecipeRegistry();
				if (hiddenRecipes == null) {
					@SuppressWarnings("unchecked")
					IRecipeCategory<IRecipeWrapper> craftingRecipeCategory = recipeRegistry.getRecipeCategory(VanillaRecipeCategoryUid.CRAFTING);
					if (craftingRecipeCategory != null) {
						hiddenRecipes = recipeRegistry.getRecipeWrappers(craftingRecipeCategory);
					}
					for (IRecipeWrapper recipeWrapper : hiddenRecipes) {
						recipeRegistry.hideRecipe(recipeWrapper);
					}
				} else {
					for (IRecipeWrapper recipeWrapper : hiddenRecipes) {
						recipeRegistry.unhideRecipe(recipeWrapper);
					}
					hiddenRecipes = null;
				}
			}
			return true;
		}
		return false;
	}
}
