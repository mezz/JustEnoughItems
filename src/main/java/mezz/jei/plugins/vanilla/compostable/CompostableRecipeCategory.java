package mezz.jei.plugins.vanilla.compostable;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class CompostableRecipeCategory implements IRecipeCategory<CompostableRecipe> {
	private static final int inputSlot = 0;

	public static final int width = 120;
	public static final int height = 18;

	private final IDrawable background;
	private final IDrawable slot;
	private final IDrawable icon;
	private final Component localizedName;

	public CompostableRecipeCategory(IGuiHelper guiHelper) {
		background = guiHelper.createBlankDrawable(width, height);
		slot = guiHelper.getSlotDrawable();
		icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM, new ItemStack(Blocks.COMPOSTER));
		localizedName = new TranslatableComponent("gui.jei.category.compostable");
	}

	@Override
	public ResourceLocation getUid() {
		return VanillaRecipeCategoryUid.COMPOSTABLE;
	}

	@Override
	public Class<? extends CompostableRecipe> getRecipeClass() {
		return CompostableRecipe.class;
	}

	@Override
	public Component getTitle() {
		return localizedName;
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
	public void setIngredients(CompostableRecipe recipe, IIngredients ingredients) {
		ingredients.setInputLists(VanillaTypes.ITEM, recipe.getInputs());
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, CompostableRecipe recipe, IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		guiItemStacks.init(inputSlot, true, 0, 0);
		guiItemStacks.set(ingredients);
	}

	@Override
	public void draw(CompostableRecipe recipe, PoseStack poseStack, double mouseX, double mouseY) {
		slot.draw(poseStack);

		float chance = recipe.getChance();
		int chancePercent = (int) Math.floor(chance * 100);
		String text = Translator.translateToLocalFormatted("gui.jei.category.compostable.chance", chancePercent);

		Minecraft minecraft = Minecraft.getInstance();
		Font font = minecraft.font;
		font.draw(poseStack, text, 24, 5, 0xFF808080);
	}
}
