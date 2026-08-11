package mezz.jei.plugins.vanilla.furnace;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.config.Constants;
import mezz.jei.plugins.vanilla.ingredients.item.ItemStackRenderer;
import mezz.jei.util.Translator;

public class FurnaceSmeltingCategory extends FurnaceRecipeCategory<SmeltingRecipe> {
	private final IDrawable background;
	private final IDrawable icon;
	private final IDrawableStatic slotDrawable;
	private final IDrawableStatic outputSlotDrawable;
	private final IDrawableStatic flameBackground;
	private final IDrawableStatic arrowBackground;
	private final IIngredientRenderer<ItemStack> itemStackRenderer;
	private final String localizedName;

	public FurnaceSmeltingCategory(IGuiHelper guiHelper) {
		super(guiHelper);
		background = guiHelper.createBlankDrawable(116, 54);
		icon = guiHelper.createDrawableIngredient(new ItemStack(Blocks.FURNACE));
		slotDrawable = guiHelper.getSlotDrawable();
		outputSlotDrawable = guiHelper.createDrawable(Constants.RECIPE_GUI_VANILLA, 90, 74, 26, 26);
		flameBackground = guiHelper.createDrawable(Constants.RECIPE_GUI_VANILLA, 1, 134, 14, 14);
		arrowBackground = guiHelper.createDrawable(Constants.RECIPE_GUI_VANILLA, 24, 132, 24, 17);
		itemStackRenderer = new ItemStackRenderer();
		localizedName = Translator.translateToLocal("gui.jei.category.smelting");
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
	public void drawExtras(Minecraft minecraft) {
		flameBackground.draw(minecraft, 1, 20);
		animatedFlame.draw(minecraft, 1, 20);
		arrowBackground.draw(minecraft, 44, 18);
		arrow.draw(minecraft, 44, 18);
	}

	@Override
	public String getTitle() {
		return localizedName;
	}

	@Override
	public String getModName() {
		return Constants.MINECRAFT_NAME;
	}

	@Override
	public String getUid() {
		return VanillaRecipeCategoryUid.SMELTING;
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, SmeltingRecipe recipeWrapper, IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

		guiItemStacks.init(inputSlot, true, 0, 0);
		guiItemStacks.init(fuelSlot, true, 0, 36);
		initOutputSlot(guiItemStacks, outputSlot, recipeWrapper.hasFuelOutput() ? 0 : 14);

		guiItemStacks.setBackground(inputSlot, slotDrawable);
		guiItemStacks.setBackground(fuelSlot, slotDrawable);

		if (recipeWrapper.hasFuelOutput()) {
			initOutputSlot(guiItemStacks, 3, 28);
		}

		guiItemStacks.set(ingredients);
	}

	private void initOutputSlot(IGuiItemStackGroup guiItemStacks, int slotIndex, int y) {
		guiItemStacks.init(slotIndex, false, itemStackRenderer, 90, y, 26, 26, 5, 5);
		guiItemStacks.setBackground(slotIndex, outputSlotDrawable);
	}
}
