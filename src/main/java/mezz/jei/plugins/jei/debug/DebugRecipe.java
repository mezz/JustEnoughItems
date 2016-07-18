package mezz.jei.plugins.jei.debug;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mezz.jei.Internal;
import mezz.jei.api.IItemListOverlay;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
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
	public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
		button.drawButton(minecraft, mouseX, mouseY);
	}

	@Nonnull
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
	public boolean handleClick(@Nonnull Minecraft minecraft, int mouseX, int mouseY, int mouseButton) {
		if (mouseButton == 0 && button.mousePressed(minecraft, mouseX, mouseY)) {
			GuiScreen screen = new GuiInventory(minecraft.thePlayer);
			minecraft.displayGuiScreen(screen);

			IItemListOverlay itemListOverlay = Internal.getRuntime().getItemListOverlay();
			String filterText = itemListOverlay.getFilterText();
			itemListOverlay.setFilterText(filterText + " test");
			return true;
		}
		return false;
	}
}
