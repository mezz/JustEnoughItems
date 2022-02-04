package mezz.jei.plugins.vanilla.anvil;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotId;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.config.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class AnvilRecipeCategory implements IRecipeCategory<AnvilRecipe> {
	private final IDrawable background;
	private final IDrawable icon;
	private final IRecipeSlotId leftInputId = () -> "left";
	private final IRecipeSlotId rightInputId = () -> "right";

	public AnvilRecipeCategory(IGuiHelper guiHelper) {
		background = guiHelper.drawableBuilder(Constants.RECIPE_GUI_VANILLA, 0, 168, 125, 18)
			.addPadding(0, 20, 0, 0)
			.build();
		icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM, new ItemStack(Blocks.ANVIL));
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
	public Component getTitle() {
		return Blocks.ANVIL.getName();
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
	public void setRecipe(IRecipeLayoutBuilder builder, AnvilRecipe recipe, List<? extends IFocus<?>> focuses) {
		builder.addSlot(RecipeIngredientRole.INPUT, 0, 0)
			.addItemStacks(recipe.getLeftInputs())
			.setContainerSlotIndex(0)
			.setSlotId(leftInputId);

		builder.addSlot(RecipeIngredientRole.INPUT, 49, 0)
			.addItemStacks(recipe.getRightInputs())
			.setContainerSlotIndex(1)
			.setSlotId(rightInputId);

		builder.addSlot(RecipeIngredientRole.OUTPUT, 107, 0)
			.addItemStacks(recipe.getOutputs())
			.setContainerSlotIndex(2);
	}

	@Override
	public void draw(AnvilRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack poseStack, double mouseX, double mouseY) {
		Optional<ItemStack> leftStack = recipeSlotsView.getSlotView(leftInputId)
			.flatMap(slotView -> slotView.getDisplayedIngredient(VanillaTypes.ITEM));

		Optional<ItemStack> rightStack = recipeSlotsView.getSlotView(rightInputId)
			.flatMap(slotView -> slotView.getDisplayedIngredient(VanillaTypes.ITEM));

		if (leftStack.isEmpty() || rightStack.isEmpty()) {
			return;
		}

		int cost = AnvilRecipeMaker.findLevelsCost(leftStack.get(), rightStack.get());
		String costText = cost < 0 ? "err" : Integer.toString(cost);
		String text = I18n.get("container.repair.cost", costText);

		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		// Show red if the player doesn't have enough levels
		int mainColor = playerHasEnoughLevels(player, cost) ? 0xFF80FF20 : 0xFFFF6060;
		drawRepairCost(minecraft, poseStack, text, mainColor);
	}

	private static boolean playerHasEnoughLevels(@Nullable LocalPlayer player, int cost) {
		if (player == null) {
			return true;
		}
		if (player.isCreative()) {
			return true;
		}
		return cost < 40 && cost <= player.experienceLevel;
	}

	private void drawRepairCost(Minecraft minecraft, PoseStack poseStack, String text, int mainColor) {
		int shadowColor = 0xFF000000 | (mainColor & 0xFCFCFC) >> 2;
		int width = minecraft.font.width(text);
		int x = background.getWidth() - 2 - width;
		int y = 27;

		// TODO 1.13 match the new GuiRepair style
		minecraft.font.draw(poseStack, text, x + 1, y, shadowColor);
		minecraft.font.draw(poseStack, text, x, y + 1, shadowColor);
		minecraft.font.draw(poseStack, text, x + 1, y + 1, shadowColor);
		minecraft.font.draw(poseStack, text, x, y, mainColor);
	}
}
