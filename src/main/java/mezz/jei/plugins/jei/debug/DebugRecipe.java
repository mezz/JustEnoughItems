package mezz.jei.plugins.jei.debug;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import mezz.jei.api.IItemListOverlay;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.plugins.jei.JEIInternalPlugin;
import mezz.jei.plugins.jei.ingredients.DebugIngredient;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.Log;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.client.config.HoverChecker;

public class DebugRecipe extends BlankRecipeWrapper {
	private final GuiButtonExt button;
	private final HoverChecker buttonHoverChecker;

	public DebugRecipe() {
		this.button = new GuiButtonExt(0, 110, 30, "test");
		this.button.setWidth(40);
		this.buttonHoverChecker = new HoverChecker(this.button, 0);
	}

	@Override
	public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
		button.drawButton(minecraft, mouseX, mouseY);
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

	@Override
	public List<FluidStack> getFluidInputs() {
		return Arrays.asList(
				new FluidStack(FluidRegistry.WATER, 1000 + (int) (Math.random() * 1000)),
				new FluidStack(FluidRegistry.LAVA, 1000 + (int) (Math.random() * 1000))
		);
	}

	@Nullable
	@Override
	public List<String> getTooltipStrings(int mouseX, int mouseY) {
		List<String> tooltipStrings = new ArrayList<String>();
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
			if (GuiScreen.isCtrlKeyDown()) {
				IJeiRuntime jeiRuntime = JEIInternalPlugin.jeiRuntime;
				if (jeiRuntime != null) {
					List<IRecipe> recipeList = CraftingManager.getInstance().getRecipeList();
					if (!recipeList.isEmpty()) {
						IRecipe randomRecipe = recipeList.get(minecraft.theWorld.rand.nextInt(recipeList.size()));
						IRecipeRegistry recipeRegistry = jeiRuntime.getRecipeRegistry();
						IRecipeHandler<IRecipe> recipeHandler = recipeRegistry.getRecipeHandler(randomRecipe.getClass());
						if (recipeHandler != null) {
							String recipeInfo = ErrorUtil.getInfoFromRecipe(randomRecipe, recipeHandler);
							Log.warning("Removing random recipe: {}", recipeInfo);
							recipeRegistry.removeRecipe(randomRecipe);
						}
					}
				}
			} else {
				GuiScreen screen = new GuiInventory(minecraft.thePlayer);
				minecraft.displayGuiScreen(screen);

				IJeiRuntime runtime = JEIInternalPlugin.jeiRuntime;
				if (runtime != null) {
					IItemListOverlay itemListOverlay = runtime.getItemListOverlay();
					String filterText = itemListOverlay.getFilterText();
					itemListOverlay.setFilterText(filterText + " test");
				}
				return true;
			}
		}
		return false;
	}
}
