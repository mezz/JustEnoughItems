package mezz.jei.gui.recipes;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.buttons.IButtonState;
import mezz.jei.api.gui.buttons.IIconButtonController;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.JeiGuiColors;
import mezz.jei.common.gui.JeiGuiColors.GuiColor;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.transfer.RecipeTransferErrorInternal;
import mezz.jei.common.transfer.RecipeTransferService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class RecipeTransferButtonController implements IIconButtonController {
	private final IRecipeLayoutDrawable<?> recipeLayout;
	private final RecipesGui recipesGui;
	private final RecipeTransferService recipeTransferService;
	private @Nullable IRecipeTransferError recipeTransferError;

	public RecipeTransferButtonController(
		IRecipeLayoutDrawable<?> recipeLayout,
		RecipesGui recipesGui,
		RecipeTransferService recipeTransferService
	) {
		this.recipeLayout = recipeLayout;
		this.recipesGui = recipesGui;
		this.recipeTransferService = recipeTransferService;
	}

	@Override
	public void initState(IButtonState state) {
		Textures textures = Internal.getTextures();
		state.setIcon(textures.getRecipeTransfer());
		updateState(state);
	}

	@Override
	public void updateState(IButtonState state) {
		Player player = Minecraft.getInstance().player;
		AbstractContainerScreen<?> parentScreen = recipesGui.getParentContainerScreen();
		if (parentScreen != null && player != null) {
			this.recipeTransferError = recipeTransferService.getTransferRecipeError(parentScreen, recipeLayout, player)
				.orElse(null);
		} else {
			this.recipeTransferError = RecipeTransferErrorInternal.INSTANCE;
		}

		updateStateForTransferError(state, recipeTransferError);
	}

	static void updateStateForTransferError(IButtonState state, @Nullable IRecipeTransferError recipeTransferError) {
		if (recipeTransferError == null ||
			recipeTransferError.getType().allowsTransfer) {
			state.setActive(true);
			state.setVisible(true);
		} else {
			state.setActive(false);
			IRecipeTransferError.Type type = recipeTransferError.getType();
			state.setVisible(type == IRecipeTransferError.Type.USER_FACING);
		}
	}

	@Override
	public boolean onPress(IJeiUserInput input) {
		if (!input.isSimulate()) {
			boolean maxTransfer = Screen.hasShiftDown();
			Minecraft minecraft = Minecraft.getInstance();
			LocalPlayer player = minecraft.player;
			AbstractContainerScreen<?> parentScreen = recipesGui.getParentContainerScreen();
			if (parentScreen != null && player != null && recipeTransferService.transferRecipe(parentScreen, recipeLayout, player, maxTransfer)) {
				recipesGui.onClose();
			}
		}
		return true;
	}

	@Override
	public void getTooltips(ITooltipBuilder tooltip) {
		getTooltips(this.recipeTransferError, tooltip);
	}

	static void getTooltips(@Nullable IRecipeTransferError recipeTransferError, ITooltipBuilder tooltip) {
		if (recipeTransferError == null) {
			Component tooltipTransfer = Component.translatable("jei.tooltip.transfer");
			tooltip.add(tooltipTransfer);
		} else {
			recipeTransferError.getTooltip(tooltip);
		}
	}

	@Override
	public void drawExtras(GuiGraphics guiGraphics, Rect2i buttonArea, int mouseX, int mouseY, float partialTicks) {
		IRecipeTransferError recipeTransferError = this.recipeTransferError;
		if (recipeTransferError != null) {
			if (recipeTransferError.getType() == IRecipeTransferError.Type.COSMETIC) {
				int color = recipeTransferError.getButtonHighlightColor();
				if (color == IRecipeTransferError.DEFAULT_BUTTON_HIGHLIGHT_COLOR) {
					color = JeiGuiColors.getColor(GuiColor.RECIPE_TRANSFER_BUTTON_HIGHLIGHT);
				}
				guiGraphics.fill(
					RenderType.guiOverlay(),
					buttonArea.getX(),
					buttonArea.getY(),
					buttonArea.getX() + buttonArea.getWidth(),
					buttonArea.getY() + buttonArea.getHeight(),
					color
				);
			}
			if (buttonArea.contains(mouseX, mouseY)) {
				IRecipeSlotsView recipeSlotsView = recipeLayout.getRecipeSlotsView();
				Rect2i recipeRect = recipeLayout.getRect();
				PoseStack poseStack = guiGraphics.pose();
				runWithRestoredPose(poseStack, () -> recipeTransferError.showError(guiGraphics, mouseX, mouseY, recipeSlotsView, recipeRect.getX(), recipeRect.getY()));
			}
		}
	}

	static void runWithRestoredPose(PoseStack poseStack, Runnable action) {
		poseStack.pushPose();
		try {
			action.run();
		} finally {
			poseStack.popPose();
		}
	}

	public int getMissingCountHint() {
		return getMissingCountHint(this.recipeTransferError);
	}

	static int getMissingCountHint(@Nullable IRecipeTransferError recipeTransferError) {
		if (recipeTransferError == null) {
			return 0;
		}
		return recipeTransferError.getMissingCountHint();
	}
}
