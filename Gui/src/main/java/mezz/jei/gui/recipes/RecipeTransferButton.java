package mezz.jei.gui.recipes;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.transfer.RecipeTransferErrorInternal;
import mezz.jei.common.transfer.RecipeTransferUtil;
import mezz.jei.gui.elements.GuiIconToggleButton;
import mezz.jei.gui.input.UserInput;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

public class RecipeTransferButton extends GuiIconToggleButton {
	public static RecipeTransferButton create(
		IRecipeLayoutDrawable<?> recipeLayout,
		Runnable onClose
	) {
		Rect2i buttonArea = recipeLayout.getRecipeTransferButtonArea();
		Rect2i layoutArea = recipeLayout.getRect();
		buttonArea.setX(buttonArea.getX() + layoutArea.getX());
		buttonArea.setY(buttonArea.getY() + layoutArea.getY());

		Textures textures = Internal.getTextures();
		IDrawable icon = textures.getRecipeTransfer();
		RecipeTransferButton transferButton = new RecipeTransferButton(icon, recipeLayout, onClose);
		transferButton.updateBounds(buttonArea);
		return transferButton;
	}

	private final IRecipeLayoutDrawable<?> recipeLayout;
	private final Runnable onClose;
	private @Nullable IRecipeTransferError recipeTransferError;
	private @Nullable AbstractContainerMenu parentContainer;
	private @Nullable Player player;
	private boolean initialized = false;

	private RecipeTransferButton(IDrawable icon, IRecipeLayoutDrawable<?> recipeLayout, Runnable onClose) {
		super(icon, icon);
		this.recipeLayout = recipeLayout;
		this.onClose = onClose;
	}

	public void update(@Nullable AbstractContainerMenu parentContainer, @Nullable Player player) {
		this.player = player;
		this.parentContainer = parentContainer;
		this.initialized = true;

		if (parentContainer != null && player != null) {
			IRecipeTransferManager recipeTransferManager = Internal.getJeiRuntime().getRecipeTransferManager();
			this.recipeTransferError = RecipeTransferUtil.getTransferRecipeError(recipeTransferManager, parentContainer, recipeLayout, player)
				.orElse(null);
		} else {
			this.recipeTransferError = RecipeTransferErrorInternal.INSTANCE;
		}

		if (recipeTransferError == null ||
			recipeTransferError.getType().allowsTransfer) {
			this.button.active = true;
			this.button.visible = true;
		} else {
			this.button.active = false;
			IRecipeTransferError.Type type = this.recipeTransferError.getType();
			this.button.visible = (type == IRecipeTransferError.Type.USER_FACING);
		}
	}

	@Override
	protected boolean onMouseClicked(UserInput input) {
		if (!input.isSimulate()) {
			IRecipeTransferManager recipeTransferManager = Internal.getJeiRuntime().getRecipeTransferManager();
			boolean maxTransfer = Screen.hasShiftDown();
			if (parentContainer != null && player != null && RecipeTransferUtil.transferRecipe(recipeTransferManager, parentContainer, recipeLayout, player, maxTransfer)) {
				onClose.run();
			}
		}
		return true;
	}

	@Override
	protected void getTooltips(JeiTooltip tooltip) {
		if (recipeTransferError == null) {
			Component tooltipTransfer = Component.translatable("jei.tooltip.transfer");
			tooltip.add(tooltipTransfer);
		} else {
			tooltip.addAll(recipeTransferError.getTooltip());
		}
	}

	@Override
	protected boolean isIconToggledOn() {
		return false;
	}

	@Override
	public void draw(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.draw(guiGraphics, mouseX, mouseY, partialTicks);
		if (this.recipeTransferError != null) {
			if (this.recipeTransferError.getType() == IRecipeTransferError.Type.COSMETIC) {
				guiGraphics.fill(
					RenderType.guiOverlay(),
					this.button.getX(),
					this.button.getY(),
					this.button.getX() + this.button.getWidth(),
					this.button.getY() + this.button.getHeight(),
					this.recipeTransferError.getButtonHighlightColor()
				);
			}
			if (isMouseOver(mouseX, mouseY)) {
				IRecipeSlotsView recipeSlotsView = recipeLayout.getRecipeSlotsView();
				Rect2i recipeRect = recipeLayout.getRect();
				PoseStack poseStack = guiGraphics.pose();
				poseStack.pushPose();
				{
					recipeTransferError.showError(guiGraphics, mouseX, mouseY, recipeSlotsView, recipeRect.getX(), recipeRect.getY());
				}
				poseStack.popPose();
			}
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
