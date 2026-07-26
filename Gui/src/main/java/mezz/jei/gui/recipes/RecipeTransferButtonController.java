package mezz.jei.gui.recipes;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.api.gui.buttons.IButtonState;
import mezz.jei.api.gui.buttons.IIconButtonController;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.JeiGuiColors;
import mezz.jei.common.gui.JeiGuiColors.GuiColor;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.transfer.RecipeTransferErrorInternal;
import mezz.jei.common.transfer.RecipeTransferUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jspecify.annotations.Nullable;
import org.joml.Matrix3x2fStack;

public class RecipeTransferButtonController implements IIconButtonController {
	private final IRecipeLayoutDrawable<?> recipeLayout;
	private final RecipesGui recipesGui;
	private @Nullable IRecipeTransferError recipeTransferError;
	private boolean showTransferError;

	public RecipeTransferButtonController(IRecipeLayoutDrawable<?> recipeLayout, RecipesGui recipesGui) {
		this.recipeLayout = recipeLayout;
		this.recipesGui = recipesGui;
	}

	@Override
	public void initState(IButtonState state) {
		Textures textures = Internal.getTextures();
		state.setIcon(textures.getRecipeTransfer());
		updateState(state);
	}

	@Override
	public void updateState(IButtonState state) {
		IRecipeTransferError recipeTransferError = updateRecipeTransferError();
		updateStateForTransferError(state, recipeTransferError);
	}

	public @Nullable IRecipeTransferError updateRecipeTransferError() {
		Player player = Minecraft.getInstance().player;
		AbstractContainerMenu parentContainer = recipesGui.getParentContainerMenu();
		if (parentContainer != null && player != null) {
			IRecipeTransferManager recipeTransferManager = Internal.getJeiRuntime().getRecipeTransferManager();
			this.recipeTransferError = RecipeTransferUtil.getTransferRecipeError(recipeTransferManager, parentContainer, recipeLayout, player)
				.orElse(null);
		} else {
			this.recipeTransferError = RecipeTransferErrorInternal.INSTANCE;
		}
		return this.recipeTransferError;
	}

	static void updateStateForTransferError(IButtonState state, @Nullable IRecipeTransferError recipeTransferError) {
		if (recipeTransferError == null ||
			recipeTransferError.getType().allowsTransfer
		) {
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
		Minecraft minecraft = Minecraft.getInstance();
		boolean maxTransfer = minecraft.hasShiftDown();
		return transferRecipe(maxTransfer, input.isSimulate());
	}

	public boolean transferRecipe(boolean maxTransfer, boolean simulate) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		AbstractContainerMenu parentContainer = recipesGui.getParentContainerMenu();
		if (parentContainer == null || player == null) {
			return false;
		}

		IRecipeTransferManager recipeTransferManager = Internal.getJeiRuntime().getRecipeTransferManager();
		if (simulate) {
			IRecipeTransferError recipeTransferError = RecipeTransferUtil.getTransferRecipeError(recipeTransferManager, parentContainer, recipeLayout, player).orElse(null);
			return recipeTransferError == null || recipeTransferError.getType().allowsTransfer;
		}

		if (RecipeTransferUtil.transferRecipe(recipeTransferManager, parentContainer, recipeLayout, player, maxTransfer)) {
			recipesGui.onClose();
			return true;
		}
		return false;
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
	public void drawExtras(GuiGraphicsExtractor guiGraphics, Rect2i buttonArea, int mouseX, int mouseY, float partialTicks) {
		IRecipeTransferError recipeTransferError = this.recipeTransferError;
		if (recipeTransferError != null) {
			if (recipeTransferError.getType() == IRecipeTransferError.Type.COSMETIC) {
				int color = recipeTransferError.getButtonHighlightColor();
				if (color == IRecipeTransferError.DEFAULT_BUTTON_HIGHLIGHT_COLOR) {
					color = JeiGuiColors.getColor(GuiColor.RECIPE_TRANSFER_BUTTON_HIGHLIGHT);
				}
				guiGraphics.fill(
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
				var poseStack = guiGraphics.pose();
				runWithRestoredPose(poseStack, () -> recipeTransferError.showError(guiGraphics, mouseX, mouseY, recipeSlotsView, recipeRect.getX(), recipeRect.getY()));
			}
		}
	}

	static void runWithRestoredPose(Matrix3x2fStack poseStack, Runnable action) {
		poseStack.pushMatrix();
		try {
			action.run();
		} finally {
			poseStack.popMatrix();
		}
	}

	public @Nullable IRecipeTransferError getRecipeTransferError() {
		return this.recipeTransferError;
	}

	public boolean isShowTransferError() {
		return this.showTransferError;
	}

	public void setShowTransferError(boolean showTransferError) {
		this.showTransferError = showTransferError;
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
