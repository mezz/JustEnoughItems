package mezz.jei.library.plugins.vanilla.anvil;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import mezz.jei.common.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class AnvilRecipeCategory implements IRecipeCategory<IJeiAnvilRecipe> {
	private final IDrawable background;
	private final IDrawable icon;
	private final String leftSlotName = "leftSlot";
	private final String rightSlotName = "rightSlot";

	public AnvilRecipeCategory(IGuiHelper guiHelper) {
		background = guiHelper.drawableBuilder(Constants.RECIPE_GUI_VANILLA, 0, 168, 125, 18)
			.addPadding(0, 20, 0, 0)
			.build();
		icon = guiHelper.createDrawableItemStack(new ItemStack(Blocks.ANVIL));
	}

	@Override
	public RecipeType<IJeiAnvilRecipe> getRecipeType() {
		return RecipeTypes.ANVIL;
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
	public void setRecipe(IRecipeLayoutBuilder builder, IJeiAnvilRecipe recipe, IFocusGroup focuses) {
		List<ItemStack> leftInputs = recipe.getLeftInputs();
		List<ItemStack> rightInputs = recipe.getRightInputs();
		List<ItemStack> outputs = recipe.getOutputs();

		IRecipeSlotBuilder leftInputSlot = builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
			.addItemStacks(leftInputs)
			.setSlotName(leftSlotName);

		IRecipeSlotBuilder rightInputSlot = builder.addSlot(RecipeIngredientRole.INPUT, 50, 1)
			.addItemStacks(rightInputs)
			.setSlotName(rightSlotName);

		IRecipeSlotBuilder outputSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, 108, 1)
			.addItemStacks(outputs);

		if (leftInputs.size() == rightInputs.size()) {
			if (leftInputs.size() == outputs.size()) {
				builder.createFocusLink(leftInputSlot, rightInputSlot, outputSlot);
			}
		} else if (leftInputs.size() == outputs.size() && rightInputs.size() == 1) {
			builder.createFocusLink(leftInputSlot, outputSlot);
		} else if (rightInputs.size() == outputs.size() && leftInputs.size() == 1) {
			builder.createFocusLink(rightInputSlot, outputSlot);
		}
	}

	@Override
	public void draw(IJeiAnvilRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack poseStack, double mouseX, double mouseY) {
		Optional<ItemStack> leftStack = recipeSlotsView.findSlotByName(leftSlotName)
			.flatMap(IRecipeSlotView::getDisplayedItemStack);

		Optional<ItemStack> rightStack = recipeSlotsView.findSlotByName(rightSlotName)
			.flatMap(IRecipeSlotView::getDisplayedItemStack);

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
		int width = minecraft.font.width(text);
		int x = getWidth() - 2 - width;
		int y = 27;
		minecraft.font.drawShadow(poseStack, text, x, y, mainColor);
	}
}
