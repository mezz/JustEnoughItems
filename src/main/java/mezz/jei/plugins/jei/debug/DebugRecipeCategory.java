package mezz.jei.plugins.jei.debug;

import mezz.jei.Internal;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IItemListOverlay;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ITooltipCallback;
import mezz.jei.api.recipe.BlankRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.config.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.List;

public class DebugRecipeCategory extends BlankRecipeCategory {
	public static final int recipeWidth = 160;
	public static final int recipeHeight = 60;
	@Nonnull
	private final IDrawable background;
	@Nonnull
	private final String localizedName;
	@Nonnull
	private final IDrawable tankBackground;
	@Nonnull
	private final IDrawable tankOverlay;

	public DebugRecipeCategory(IGuiHelper guiHelper) {
		background = guiHelper.createBlankDrawable(recipeWidth, recipeHeight);
		localizedName = "debug";

		ResourceLocation backgroundTexture = new ResourceLocation(Constants.RESOURCE_DOMAIN, Constants.TEXTURE_GUI_PATH + "recipeBackground.png");
		tankBackground = guiHelper.createDrawable(backgroundTexture, 176, 0, 20, 55);
		tankOverlay = guiHelper.createDrawable(backgroundTexture, 176, 55, 12, 47);
	}

	@Nonnull
	@Override
	public String getUid() {
		return "debug";
	}

	@Nonnull
	@Override
	public String getTitle() {
		return localizedName;
	}

	@Nonnull
	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void drawExtras(@Nonnull Minecraft minecraft) {
		tankBackground.draw(minecraft);
		IItemListOverlay itemListOverlay = Internal.getRuntime().getItemListOverlay();
		minecraft.fontRendererObj.drawString(itemListOverlay.getFilterText(), 20, 52, 0);
		minecraft.fontRendererObj.drawString(String.valueOf(itemListOverlay.getStackUnderMouse()), 50, 52, 0);
	}

	@Override
	public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull IRecipeWrapper recipeWrapper) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

		guiItemStacks.addTooltipCallback(new ITooltipCallback<ItemStack>() {
			@Override
			public void onTooltip(int slotIndex, boolean input, ItemStack ingredient, List<String> tooltip) {
				if (input) {
					tooltip.add(slotIndex + " Input itemStack");
				} else {
					tooltip.add(slotIndex + " Output itemStack");
				}
			}
		});

		guiItemStacks.init(0, false, 70, 0);
		guiItemStacks.init(1, true, 110, 0);
		guiItemStacks.set(0, new ItemStack(Items.WATER_BUCKET));
		guiItemStacks.set(1, new ItemStack(Items.LAVA_BUCKET));

		IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();
		guiFluidStacks.addTooltipCallback(new ITooltipCallback<FluidStack>() {
			@Override
			public void onTooltip(int slotIndex, boolean input, FluidStack ingredient, List<String> tooltip) {
				if (input) {
					tooltip.add(slotIndex + " Input fluidStack");
				} else {
					tooltip.add(slotIndex + " Output fluidStack");
				}
			}
		});

		guiFluidStacks.init(0, true, 4, 4, 12, 47, 2000, true, tankOverlay);
		guiFluidStacks.init(1, true, 24, 0, 12, 47, 16000, true, null);
		guiFluidStacks.init(2, false, 50, 0, 24, 24, 2000, true, tankOverlay);
		guiFluidStacks.init(3, false, 90, 0, 12, 47, 100, false, tankOverlay);

		DebugRecipe debugRecipe = (DebugRecipe) recipeWrapper;
		List<FluidStack> fluidInputs = debugRecipe.getFluidInputs();
		guiFluidStacks.set(0, fluidInputs.get(0));
		guiFluidStacks.set(1, fluidInputs.get(1));
		guiFluidStacks.set(3, fluidInputs.get(0));
	}
}
