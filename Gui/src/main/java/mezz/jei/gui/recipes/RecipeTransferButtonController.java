package mezz.jei.gui.recipes;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.api.gui.buttons.IButtonState;
import mezz.jei.api.gui.buttons.IIconButtonController;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.runtime.IJeiKeyMapping;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.JeiGuiColors;
import mezz.jei.common.gui.JeiGuiColors.GuiColor;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.input.keys.JeiKeyModifier;
import mezz.jei.common.transfer.RecipeTransferErrorInternal;
import mezz.jei.common.transfer.RecipeTransferService;
import mezz.jei.common.transfer.RecipeTransferUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;
import org.joml.Matrix3x2fStack;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class RecipeTransferButtonController implements IIconButtonController {
	private final IRecipeLayoutDrawable<?> recipeLayout;
	private final RecipeTransferService recipeTransferService;
	private final Supplier<@Nullable AbstractContainerScreen<?>> screenSupplier;
	private final BooleanSupplier maxTransferSupplier;
	private final Runnable onSuccessfulTransfer;
	private final boolean showButtonShortcuts;
	private @Nullable IRecipeTransferError recipeTransferError;

	public RecipeTransferButtonController(
		IRecipeLayoutDrawable<?> recipeLayout,
		RecipesGui recipesGui,
		RecipeTransferService recipeTransferService
	) {
		this(
			recipeLayout,
			recipeTransferService,
			recipesGui::getParentContainerScreen,
			() -> Minecraft.getInstance().hasShiftDown(),
			recipesGui::onClose,
			true
		);
	}

	public static RecipeTransferButtonController createForPinnedRecipe(
		IRecipeLayoutDrawable<?> recipeLayout,
		RecipeTransferService recipeTransferService
	) {
		PinnedTransferState transferState = new PinnedTransferState();
		return new RecipeTransferButtonController(
			recipeLayout,
			recipeTransferService,
			RecipeTransferButtonController::getCurrentContainerScreen,
			transferState::shouldTransferMax,
			transferState::onSuccessfulTransfer,
			false
		);
	}

	private RecipeTransferButtonController(
		IRecipeLayoutDrawable<?> recipeLayout,
		RecipeTransferService recipeTransferService,
		Supplier<@Nullable AbstractContainerScreen<?>> screenSupplier,
		BooleanSupplier maxTransferSupplier,
		Runnable onSuccessfulTransfer,
		boolean showButtonShortcuts
	) {
		this.recipeLayout = recipeLayout;
		this.recipeTransferService = recipeTransferService;
		this.screenSupplier = screenSupplier;
		this.maxTransferSupplier = maxTransferSupplier;
		this.onSuccessfulTransfer = onSuccessfulTransfer;
		this.showButtonShortcuts = showButtonShortcuts;
	}

	private static @Nullable AbstractContainerScreen<?> getCurrentContainerScreen() {
		Screen screen = Minecraft.getInstance().gui.screen();
		if (screen instanceof AbstractContainerScreen<?> containerScreen) {
			return containerScreen;
		}
		return null;
	}

	private static class PinnedTransferState {
		private boolean transferred;

		public boolean shouldTransferMax() {
			return transferred;
		}

		public void onSuccessfulTransfer() {
			this.transferred = true;
		}
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
		AbstractContainerScreen<?> parentScreen = screenSupplier.get();
		if (parentScreen != null && player != null) {
			this.recipeTransferError = recipeTransferService.getTransferRecipeError(parentScreen, recipeLayout, player)
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
		boolean maxTransfer = maxTransferSupplier.getAsBoolean();
		return transferRecipe(maxTransfer, input.isSimulate());
	}

	public boolean transferRecipe(boolean maxTransfer, boolean simulate) {
		LocalPlayer player = Minecraft.getInstance().player;
		AbstractContainerScreen<?> parentScreen = screenSupplier.get();
		if (parentScreen == null || player == null) {
			return false;
		}

		if (simulate) {
			IRecipeTransferError recipeTransferError = recipeTransferService.getTransferRecipeError(parentScreen, recipeLayout, player)
				.orElse(null);
			return recipeTransferError == null || recipeTransferError.getType().allowsTransfer;
		}

		if (recipeTransferService.transferRecipe(parentScreen, recipeLayout, player, maxTransfer)) {
			onSuccessfulTransfer.run();
			return true;
		}
		return false;
	}

	@Override
	public void getTooltips(ITooltipBuilder tooltip) {
		getTooltips(this.recipeTransferError, tooltip);
		if (!showButtonShortcuts ||
			(this.recipeTransferError != null && !this.recipeTransferError.getType().allowsTransfer)
		) {
			return;
		}

		IJeiKeyMapping leftClick = Internal.getKeyMappings().getLeftClick();
		tooltip.addKeyUsageComponent("jei.tooltip.bookmarks.tooltips.transfer.usage", leftClick);
		tooltip.add(JeiTooltip.createKeyUsageComponent(
			"jei.tooltip.bookmarks.tooltips.transfer.max.usage",
			JeiKeyModifier.SHIFT.getCombinedName(leftClick.getTranslatedKeyMessage()).copy()
		));
	}

	static void getTooltips(@Nullable IRecipeTransferError recipeTransferError, ITooltipBuilder tooltip) {
		RecipeTransferUtil.addTransferRecipeTooltip(recipeTransferError, tooltip);
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
