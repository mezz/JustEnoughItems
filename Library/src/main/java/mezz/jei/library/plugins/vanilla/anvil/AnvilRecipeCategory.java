package mezz.jei.library.plugins.vanilla.anvil;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class AnvilRecipeCategory extends AbstractRecipeCategory<IJeiAnvilRecipe> {
	private static final String leftSlotName = "leftSlot";
	private static final String rightSlotName = "rightSlot";

	public AnvilRecipeCategory(IGuiHelper guiHelper) {
		super(
			RecipeTypes.ANVIL,
			Blocks.ANVIL.getName(),
			guiHelper.createDrawableItemLike(Blocks.ANVIL),
			125,
			38
		);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, IJeiAnvilRecipe recipe, IFocusGroup focuses) {
		List<ItemStack> leftInputs = recipe.getLeftInputs();
		List<ItemStack> rightInputs = recipe.getRightInputs();
		List<ItemStack> outputs = recipe.getOutputs();

		IRecipeSlotBuilder leftInputSlot = builder.addInputSlot(1, 1)
			.addItemStacks(leftInputs)
			.setStandardSlotBackground()
			.setSlotName(leftSlotName);

		IRecipeSlotBuilder rightInputSlot = builder.addInputSlot(50, 1)
			.addItemStacks(rightInputs)
			.setStandardSlotBackground()
			.setSlotName(rightSlotName);

		IRecipeSlotBuilder outputSlot = builder.addOutputSlot(108, 1)
			.setStandardSlotBackground()
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
	public void createRecipeExtras(IRecipeExtrasBuilder builder, IJeiAnvilRecipe recipe, IRecipeSlotsView recipeSlotsView, IFocusGroup focuses) {
		builder.addRecipePlusSign(27, 3);
		builder.addRecipeArrow(76, 1);

		Integer cost = getCost(recipeSlotsView);
		if (cost != null) {
			String costText = cost < 0 ? "err" : Integer.toString(cost);
			Component text = Component.translatable("container.repair.cost", costText);

			Minecraft minecraft = Minecraft.getInstance();
			LocalPlayer player = minecraft.player;
			// Show red if the player doesn't have enough levels
			int textColor = playerHasEnoughLevels(player, cost) ? 0xFF80FF20 : 0xFFFF6060;

			builder.addText(text, 2, 27, getWidth() - 4, 10)
				.setColor(textColor)
				.setShadow(true)
				.alignHorizontalRight();
		}
	}

	private @Nullable Integer getCost(IRecipeSlotsView recipeSlotsView) {
		Optional<ItemStack> leftStack = recipeSlotsView.findSlotByName(leftSlotName)
			.flatMap(IRecipeSlotView::getDisplayedItemStack);

		Optional<ItemStack> rightStack = recipeSlotsView.findSlotByName(rightSlotName)
			.flatMap(IRecipeSlotView::getDisplayedItemStack);

		if (leftStack.isEmpty() || rightStack.isEmpty()) {
			return null;
		}

		return AnvilRecipeMaker.findLevelsCost(leftStack.get(), rightStack.get());
	}

	@Override
	public @Nullable ResourceLocation getRegistryName(IJeiAnvilRecipe recipe) {
		return recipe.getUid();
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
}
