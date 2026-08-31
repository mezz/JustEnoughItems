package mezz.jei.gui.recipes;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.JeiGuiColors;
import mezz.jei.common.gui.JeiGuiColors.GuiColor;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.transfer.RecipeTransferErrorInternal;
import mezz.jei.common.transfer.RecipeTransferService;
import mezz.jei.common.transfer.RecipeTransferUtil;
import mezz.jei.gui.elements.GuiIconToggleButton;
import mezz.jei.gui.input.UserInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class RecipeTransferButton extends GuiIconToggleButton {
	public static RecipeTransferButton create(
		IRecipeLayoutDrawable<?> recipeLayout,
		RecipeTransferService recipeTransferService,
		Supplier<@Nullable AbstractContainerScreen<?>> parentScreenSupplier,
		Runnable onClose
	) {
		RecipeTransferButton transferButton = create(
			recipeLayout,
			recipeTransferService,
			parentScreenSupplier,
			Screen::hasShiftDown,
			onClose
		);

		Rect2i buttonArea = recipeLayout.getRecipeTransferButtonArea();
		Rect2i layoutArea = recipeLayout.getRect();
		buttonArea.setX(buttonArea.getX() + layoutArea.getX());
		buttonArea.setY(buttonArea.getY() + layoutArea.getY());
		transferButton.updateBounds(buttonArea);
		return transferButton;
	}

	public static RecipeTransferButton createForPinnedRecipe(
		IRecipeLayoutDrawable<?> recipeLayout,
		RecipeTransferService recipeTransferService
	) {
		PinnedTransferState transferState = new PinnedTransferState();
		return create(
			recipeLayout,
			recipeTransferService,
			RecipeTransferButton::getCurrentContainerScreen,
			transferState::shouldTransferMax,
			transferState::onSuccessfulTransfer
		);
	}

	private static RecipeTransferButton create(
		IRecipeLayoutDrawable<?> recipeLayout,
		RecipeTransferService recipeTransferService,
		Supplier<@Nullable AbstractContainerScreen<?>> parentScreenSupplier,
		BooleanSupplier maxTransferSupplier,
		Runnable onSuccessfulTransfer
	) {
		Textures textures = Internal.getTextures();
		IDrawable icon = textures.getRecipeTransfer();
		return new RecipeTransferButton(
			icon,
			recipeLayout,
			recipeTransferService,
			parentScreenSupplier,
			maxTransferSupplier,
			onSuccessfulTransfer
		);
	}

	private static @Nullable AbstractContainerScreen<?> getCurrentContainerScreen() {
		Screen screen = Minecraft.getInstance().screen;
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

	private final IRecipeLayoutDrawable<?> recipeLayout;
	private final RecipeTransferService recipeTransferService;
	private final Supplier<@Nullable AbstractContainerScreen<?>> parentScreenSupplier;
	private final BooleanSupplier maxTransferSupplier;
	private final Runnable onSuccessfulTransfer;
	private @Nullable IRecipeTransferError recipeTransferError;
	private boolean initialized = false;

	private RecipeTransferButton(
		IDrawable icon,
		IRecipeLayoutDrawable<?> recipeLayout,
		RecipeTransferService recipeTransferService,
		Supplier<@Nullable AbstractContainerScreen<?>> parentScreenSupplier,
		BooleanSupplier maxTransferSupplier,
		Runnable onSuccessfulTransfer
	) {
		super(icon, icon);
		this.recipeLayout = recipeLayout;
		this.recipeTransferService = recipeTransferService;
		this.parentScreenSupplier = parentScreenSupplier;
		this.maxTransferSupplier = maxTransferSupplier;
		this.onSuccessfulTransfer = onSuccessfulTransfer;
	}

	public void update(@Nullable AbstractContainerMenu parentContainer, @Nullable Player player) {
		AbstractContainerScreen<?> parentScreen = parentContainer == null ? null : parentScreenSupplier.get();
		update(parentScreen, player);
	}

	@Override
	public void tick() {
		Minecraft minecraft = Minecraft.getInstance();
		update(parentScreenSupplier.get(), minecraft.player);
	}

	private void update(@Nullable AbstractContainerScreen<?> parentScreen, @Nullable Player player) {
		this.initialized = true;

		if (parentScreen != null && player != null) {
			this.recipeTransferError = recipeTransferService.getTransferRecipeError(parentScreen, recipeLayout, player)
				.orElse(null);
		} else {
			this.recipeTransferError = RecipeTransferErrorInternal.INSTANCE;
		}

		if (recipeTransferError == null || recipeTransferError.getType().allowsTransfer) {
			this.button.active = true;
			this.button.visible = true;
		} else {
			this.button.active = false;
			IRecipeTransferError.Type type = this.recipeTransferError.getType();
			this.button.visible = type == IRecipeTransferError.Type.USER_FACING;
		}
	}

	@Override
	protected boolean onMouseClicked(UserInput input) {
		if (!input.isSimulate()) {
			LocalPlayer player = Minecraft.getInstance().player;
			AbstractContainerScreen<?> parentScreen = parentScreenSupplier.get();
			boolean maxTransfer = maxTransferSupplier.getAsBoolean();
			if (parentScreen != null && player != null && recipeTransferService.transferRecipe(parentScreen, recipeLayout, player, maxTransfer)) {
				onSuccessfulTransfer.run();
			}
		}
		return true;
	}

	@Override
	protected void getTooltips(JeiTooltip tooltip) {
		RecipeTransferUtil.addTransferRecipeTooltip(this.recipeTransferError, tooltip);
	}

	@Override
	protected boolean isIconToggledOn() {
		return false;
	}

	@Override
	public void draw(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.draw(guiGraphics, mouseX, mouseY, partialTicks);
		IRecipeTransferError recipeTransferError = this.recipeTransferError;
		if (recipeTransferError != null) {
			if (recipeTransferError.getType() == IRecipeTransferError.Type.COSMETIC) {
				int color = recipeTransferError.getButtonHighlightColor();
				if (color == IRecipeTransferError.DEFAULT_BUTTON_HIGHLIGHT_COLOR) {
					color = JeiGuiColors.getColor(GuiColor.RECIPE_TRANSFER_BUTTON_HIGHLIGHT);
				}
				guiGraphics.fill(
					RenderType.guiOverlay(),
					this.button.getX(),
					this.button.getY(),
					this.button.getX() + this.button.getWidth(),
					this.button.getY() + this.button.getHeight(),
					color
				);
			}
			if (isMouseOver(mouseX, mouseY)) {
				IRecipeSlotsView recipeSlotsView = recipeLayout.getRecipeSlotsView();
				Rect2i recipeRect = recipeLayout.getRect();
				PoseStack poseStack = guiGraphics.pose();
				runWithRestoredPose(poseStack, () -> recipeTransferError.showError(guiGraphics, mouseX, mouseY, recipeSlotsView, recipeRect.getX(), recipeRect.getY()));
			}
		}
	}

	private static void runWithRestoredPose(PoseStack poseStack, Runnable action) {
		poseStack.pushPose();
		try {
			action.run();
		} finally {
			poseStack.popPose();
		}
	}

	public boolean isInitialized() {
		return initialized;
	}

	public int getMissingCountHint() {
		if (!initialized) {
			return -1;
		}
		if (recipeTransferError == null) {
			return 0;
		}
		return recipeTransferError.getMissingCountHint();
	}
}
