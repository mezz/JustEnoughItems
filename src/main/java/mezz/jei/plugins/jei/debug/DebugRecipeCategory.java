package mezz.jei.plugins.jei.debug;

import java.util.Collections;
import java.util.List;

import net.minecraftforge.fluids.FluidStack;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IIngredientFilter;
import mezz.jei.api.IIngredientListOverlay;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiIngredientGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.config.Constants;
import mezz.jei.plugins.jei.JEIInternalPlugin;
import mezz.jei.plugins.jei.ingredients.DebugIngredient;

public class DebugRecipeCategory implements IRecipeCategory<DebugRecipe> {
	public static final int RECIPE_WIDTH = 160;
	public static final int RECIPE_HEIGHT = 60;
	private final IDrawable background;
	private final String localizedName;
	private final IDrawable tankBackground;
	private final IDrawable tankOverlay;

	public DebugRecipeCategory(IGuiHelper guiHelper) {
		this.background = guiHelper.createBlankDrawable(RECIPE_WIDTH, RECIPE_HEIGHT);
		this.localizedName = "debug";

		ResourceLocation backgroundTexture = new ResourceLocation(Constants.RESOURCE_DOMAIN, Constants.TEXTURE_GUI_PATH + "debug.png");
		this.tankBackground = guiHelper.drawableBuilder(backgroundTexture, 220, 196, 18, 60)
			.addPadding(-1, -1, -1, -1)
			.build();
		this.tankOverlay = guiHelper.drawableBuilder(backgroundTexture, 238, 196, 18, 60)
			.addPadding(-1, -1, -1, -1)
			.build();
	}

	@Override
	public String getUid() {
		return "debug";
	}

	@Override
	public String getTitle() {
		return localizedName;
	}

	@Override
	public String getModName() {
		return Constants.NAME;
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void drawExtras(Minecraft minecraft) {
		IJeiRuntime runtime = JEIInternalPlugin.jeiRuntime;
		if (runtime != null) {
			IIngredientFilter ingredientFilter = runtime.getIngredientFilter();
			minecraft.fontRenderer.drawString(ingredientFilter.getFilterText(), 20, 52, 0);

			IIngredientListOverlay ingredientListOverlay = runtime.getIngredientListOverlay();
			Object ingredientUnderMouse = ingredientListOverlay.getIngredientUnderMouse();
			if (ingredientUnderMouse != null) {
				drawIngredientName(minecraft, ingredientUnderMouse);
			}
		}
	}

	private <T> void drawIngredientName(Minecraft minecraft, T ingredient) {
		IIngredientRegistry ingredientRegistry = JEIInternalPlugin.ingredientRegistry;
		if (ingredientRegistry != null) {
			IIngredientHelper<T> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);
			String jeiUid = ingredientHelper.getUniqueId(ingredient);
			minecraft.fontRenderer.drawString(jeiUid, 50, 52, 0);
		}
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, DebugRecipe recipeWrapper, IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

		guiItemStacks.addTooltipCallback((slotIndex, input, ingredient, tooltip) -> {
			if (input) {
				tooltip.add(slotIndex + " Input itemStack");
			} else {
				tooltip.add(slotIndex + " Output itemStack");
			}
		});

		guiItemStacks.init(0, false, 70, 0);
		guiItemStacks.init(1, true, 110, 0);
		guiItemStacks.set(0, new ItemStack(Items.WATER_BUCKET));
		guiItemStacks.set(1, new ItemStack(Items.LAVA_BUCKET));

		IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();
		guiFluidStacks.addTooltipCallback((slotIndex, input, ingredient, tooltip) -> {
			if (input) {
				tooltip.add(slotIndex + " Input fluidStack");
			} else {
				tooltip.add(slotIndex + " Output fluidStack");
			}
		});

		guiFluidStacks.init(0, false, 90, 0, 16, 58, 16000, false, tankOverlay);
		guiFluidStacks.init(1, true, 24, 0, 12, 47, 2000, true, null);

		guiFluidStacks.setBackground(0, tankBackground);

		List<FluidStack> fluidInputs = recipeWrapper.getFluidInputs();
		guiFluidStacks.set(0, fluidInputs.get(0));
		guiFluidStacks.set(1, fluidInputs.get(1));

		IGuiIngredientGroup<DebugIngredient> debugIngredientsGroup = recipeLayout.getIngredientsGroup(DebugIngredient.TYPE);
		debugIngredientsGroup.addTooltipCallback((slotIndex, input, ingredient, tooltip) -> {
			if (input) {
				tooltip.add(slotIndex + " Input DebugIngredient");
			} else {
				tooltip.add(slotIndex + " Output DebugIngredient");
			}
		});

		debugIngredientsGroup.init(0, true, 40, 0);
		debugIngredientsGroup.init(1, false, 40, 16);
		debugIngredientsGroup.init(2, false, 40, 32);

		debugIngredientsGroup.set(ingredients);
	}

	@Override
	public List<String> getTooltipStrings(int mouseX, int mouseY) {
		return Collections.singletonList("Debug Recipe Category Tooltip");
	}
}
