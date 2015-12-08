package mezz.jei.plugins.vanilla.brewing;

import javax.annotation.Nonnull;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import mezz.jei.api.JEIManager;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.util.StackUtil;

public class BrewingRecipeCategory implements IRecipeCategory {

	private static final int brewPotionSlot1 = 0;
	private static final int brewPotionSlot2 = 1;
	private static final int brewPotionSlot3 = 2;
	private static final int brewIngredientSlot = 3;
	private static final int outputSlot = 4; // for display only

	private static final int outputSlotX = 80;
	private static final int outputSlotY = 1;

	@Nonnull
	private final IDrawable background;
	@Nonnull
	private final String localizedName;

	public BrewingRecipeCategory() {
		ResourceLocation location = new ResourceLocation("minecraft:textures/gui/container/brewing_stand.png");
		background = JEIManager.guiHelper.createDrawable(location, 55, 15, 64, 56, 0, 0, 0, 40);
		localizedName = StatCollector.translateToLocal("gui.jei.brewingRecipes");
	}

	@Nonnull
	@Override
	public String getUid() {
		return VanillaRecipeCategoryUid.BREWING;
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
	public void drawExtras(Minecraft minecraft) {
		JEIManager.guiHelper.getSlotDrawable().draw(minecraft, outputSlotX, outputSlotY);
	}

	@Override
	public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull IRecipeWrapper recipeWrapper) {
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();

		itemStacks.init(brewPotionSlot1, true, 0, 30);
		itemStacks.init(brewPotionSlot2, true, 23, 37);
		itemStacks.init(brewPotionSlot3, true, 46, 30);
		itemStacks.init(brewIngredientSlot, true, 23, 1);
		itemStacks.init(outputSlot, false, outputSlotX, outputSlotY);

		if (recipeWrapper instanceof BrewingRecipeWrapper) {
			List inputs = recipeWrapper.getInputs();
			List<ItemStack> inputStacks1 = StackUtil.toItemStackList(inputs.get(brewPotionSlot1));
			List<ItemStack> inputStacks2 = StackUtil.toItemStackList(inputs.get(brewPotionSlot2));
			List<ItemStack> inputStacks3 = StackUtil.toItemStackList(inputs.get(brewPotionSlot3));
			List<ItemStack> ingredientStacks = StackUtil.toItemStackList(inputs.get(brewIngredientSlot));

			itemStacks.setFromRecipe(brewPotionSlot1, inputStacks1);
			itemStacks.setFromRecipe(brewPotionSlot2, inputStacks2);
			itemStacks.setFromRecipe(brewPotionSlot3, inputStacks3);
			itemStacks.setFromRecipe(brewIngredientSlot, ingredientStacks);
			itemStacks.setFromRecipe(outputSlot, recipeWrapper.getOutputs());
		}
	}
}
