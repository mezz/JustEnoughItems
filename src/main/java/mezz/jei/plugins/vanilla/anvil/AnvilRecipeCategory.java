package mezz.jei.plugins.vanilla.anvil;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Map;

import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.config.Constants;

public class AnvilRecipeCategory implements IRecipeCategory<AnvilRecipe> {

	private final IDrawable background;
	private final IDrawable icon;
	private final LoadingCache<AnvilRecipe, AnvilRecipeDisplayData> cachedDisplayData;

	public AnvilRecipeCategory(IGuiHelper guiHelper) {
		background = guiHelper.drawableBuilder(Constants.RECIPE_GUI_VANILLA, 0, 168, 125, 18)
			.addPadding(0, 20, 0, 0)
			.build();
		icon = guiHelper.createDrawableIngredient(new ItemStack(Blocks.ANVIL));
		cachedDisplayData = CacheBuilder.newBuilder()
			.maximumSize(25)
			.build(new CacheLoader<AnvilRecipe, AnvilRecipeDisplayData>() {
				@Override
				public AnvilRecipeDisplayData load(AnvilRecipe key) {
					return new AnvilRecipeDisplayData();
				}
			});
	}

	@Override
	public ResourceLocation getUid() {
		return VanillaRecipeCategoryUid.ANVIL;
	}

	@Override
	public Class<? extends AnvilRecipe> getRecipeClass() {
		return AnvilRecipe.class;
	}

	@Override
	public String getTitle() {
		return Blocks.ANVIL.func_235333_g_().getString();
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public void setIngredients(AnvilRecipe recipe, IIngredients ingredients) {
		ingredients.setInputLists(VanillaTypes.ITEM, recipe.getInputs());
		ingredients.setOutputLists(VanillaTypes.ITEM, recipe.getOutputs());
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, AnvilRecipe recipe, IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

		guiItemStacks.init(0, true, 0, 0);
		guiItemStacks.init(1, true, 49, 0);
		guiItemStacks.init(2, false, 107, 0);

		guiItemStacks.set(ingredients);

		AnvilRecipeDisplayData displayData = cachedDisplayData.getUnchecked(recipe);
		displayData.setCurrentIngredients(guiItemStacks.getGuiIngredients());
	}

	@Override
	public void draw(AnvilRecipe recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
		AnvilRecipeDisplayData displayData = cachedDisplayData.getUnchecked(recipe);
		Map<Integer, ? extends IGuiIngredient<ItemStack>> currentIngredients = displayData.getCurrentIngredients();
		if (currentIngredients == null) {
			return;
		}

		ItemStack newLeftStack = currentIngredients.get(0).getDisplayedIngredient();
		ItemStack newRightStack = currentIngredients.get(1).getDisplayedIngredient();

		if (newLeftStack == null || newRightStack == null) {
			return;
		}

		ItemStack lastLeftStack = displayData.getLastLeftStack();
		ItemStack lastRightStack = displayData.getLastRightStack();
		int lastCost = displayData.getLastCost();
		if (lastLeftStack == null || lastRightStack == null
			|| !ItemStack.areItemStacksEqual(lastLeftStack, newLeftStack)
			|| !ItemStack.areItemStacksEqual(lastRightStack, newRightStack)) {
			lastCost = AnvilRecipeMaker.findLevelsCost(newLeftStack, newRightStack);
			displayData.setLast(newLeftStack, newRightStack, lastCost);
		}

		if (lastCost != 0) {
			String costText = lastCost < 0 ? "err" : Integer.toString(lastCost);
			String text = I18n.format("container.repair.cost", costText);

			Minecraft minecraft = Minecraft.getInstance();
			int mainColor = 0xFF80FF20;
			ClientPlayerEntity player = minecraft.player;
			if (player != null &&
				(lastCost >= 40 || lastCost > player.experienceLevel) &&
				!player.isCreative()) {
				// Show red if the player doesn't have enough levels
				mainColor = 0xFFFF6060;
			}

			drawRepairCost(minecraft, matrixStack, text, mainColor);
		}
	}

	private void drawRepairCost(Minecraft minecraft, MatrixStack matrixStack, String text, int mainColor) {
		int shadowColor = 0xFF000000 | (mainColor & 0xFCFCFC) >> 2;
		int width = minecraft.fontRenderer.getStringWidth(text);
		int x = background.getWidth() - 2 - width;
		int y = 27;

		// TODO 1.13 match the new GuiRepair style
		minecraft.fontRenderer.func_238421_b_(matrixStack, text, x + 1, y, shadowColor);
		minecraft.fontRenderer.func_238421_b_(matrixStack, text, x, y + 1, shadowColor);
		minecraft.fontRenderer.func_238421_b_(matrixStack, text, x + 1, y + 1, shadowColor);
		minecraft.fontRenderer.func_238421_b_(matrixStack, text, x, y, mainColor);
	}
}
