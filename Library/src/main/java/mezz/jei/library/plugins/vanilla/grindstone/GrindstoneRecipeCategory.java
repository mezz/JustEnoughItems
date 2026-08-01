package mezz.jei.library.plugins.vanilla.grindstone;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.vanilla.IJeiGrindstoneRecipe;
import mezz.jei.common.gui.JeiGuiColors;
import mezz.jei.common.gui.JeiGuiColors.GuiColor;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GrindstoneRecipeCategory implements IRecipeCategory<IJeiGrindstoneRecipe> {
	private static final String topSlotName = "topSlot";
	private static final String bottomSlotName = "bottomSlot";
	private final IDrawable background;
	private final IDrawable icon;
	private final IDrawable arrow;
	private final Component localizedName;

	public GrindstoneRecipeCategory(IGuiHelper guiHelper) {
		background = guiHelper.createBlankDrawable(125, 52);
		icon = guiHelper.createDrawableItemLike(Blocks.GRINDSTONE);
		arrow = guiHelper.getRecipeArrow();
		localizedName = Blocks.GRINDSTONE.getName();
	}

	@Override
	public RecipeType<IJeiGrindstoneRecipe> getRecipeType() {
		return RecipeTypes.GRINDSTONE;
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
	public void setRecipe(IRecipeLayoutBuilder builder, IJeiGrindstoneRecipe recipe, IFocusGroup focuses) {
		List<ItemStack> topInputs = recipe.getTopInputs();
		List<ItemStack> bottomInputs = recipe.getBottomInputs();
		List<ItemStack> outputs = recipe.getOutputs();

		IRecipeSlotBuilder topInputSlot = builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
			.addItemStacks(topInputs)
			.setStandardSlotBackground()
			.setSlotName(topSlotName);

		IRecipeSlotBuilder bottomInputSlot = builder.addSlot(RecipeIngredientRole.INPUT, 1, 24)
			.addItemStacks(bottomInputs)
			.setStandardSlotBackground()
			.setSlotName(bottomSlotName);

		int outputSlotXPosition = 52;
		int outputSlotYPosition = 13;
		RecipeIngredientRole outputRole = recipe.isOutputRenderOnly() ?
			RecipeIngredientRole.RENDER_ONLY :
			RecipeIngredientRole.OUTPUT;
		IRecipeSlotBuilder outputSlot = builder.addSlot(outputRole, outputSlotXPosition, outputSlotYPosition)
			.setOutputSlotBackground()
			.addItemStacks(outputs);

		if (topInputs.size() == bottomInputs.size()) {
			if (topInputs.size() == outputs.size()) {
				builder.createFocusLink(topInputSlot, bottomInputSlot, outputSlot);
			}
		} else if (topInputs.size() == outputs.size() && bottomInputs.size() == 1) {
			builder.createFocusLink(topInputSlot, outputSlot);
		} else if (bottomInputs.size() == outputs.size() && topInputs.size() == 1) {
			builder.createFocusLink(bottomInputSlot, outputSlot);
		}
	}

	@Override
	public void draw(IJeiGrindstoneRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack poseStack, double mouseX, double mouseY) {
		arrow.draw(poseStack, 20, 12);

		int maxXpReward = recipe.getMaxXpReward();
		if (maxXpReward > 0) {
			int minXpReward = recipe.getMinXpReward();
			Component text = Component.translatable("gui.jei.category.grindstone.experience", minXpReward, maxXpReward);
			Minecraft minecraft = Minecraft.getInstance();
			int width = minecraft.font.width(text);
			minecraft.font.drawShadow(poseStack, text, getWidth() - width, 43, JeiGuiColors.getColor(GuiColor.GRINDSTONE_EXPERIENCE_REWARD_TEXT));
		}
	}

	@Override
	public @Nullable ResourceLocation getRegistryName(IJeiGrindstoneRecipe recipe) {
		return recipe.getUid();
	}
}
