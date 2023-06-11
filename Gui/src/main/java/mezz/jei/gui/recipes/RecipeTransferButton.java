package mezz.jei.gui.recipes;

import net.minecraft.client.gui.GuiGraphics;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import mezz.jei.common.gui.TooltipRenderer;
import mezz.jei.gui.elements.GuiIconButtonSmall;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.transfer.RecipeTransferErrorInternal;
import mezz.jei.common.transfer.RecipeTransferUtil;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RecipeTransferButton extends GuiIconButtonSmall {
	private final IRecipeLayoutDrawable<?> recipeLayout;
	private final Runnable onClose;
	@Nullable
	private IRecipeTransferError recipeTransferError;
	@Nullable
	private IOnClickHandler onClickHandler;

	public RecipeTransferButton(IDrawable icon, IRecipeLayoutDrawable<?> recipeLayout, Textures textures, Runnable onClose) {
		super(0, 0, 0, 0, icon, b -> {}, textures);
		this.recipeLayout = recipeLayout;
		this.onClose = onClose;
	}

	public void update(Rect2i area, IRecipeTransferManager recipeTransferManager, @Nullable AbstractContainerMenu container, Player player) {
		this.setX(area.getX());
		this.setY(area.getY());
		this.width = area.getWidth();
		this.height = area.getHeight();

		if (container != null) {
			this.recipeTransferError = RecipeTransferUtil.getTransferRecipeError(recipeTransferManager, container, recipeLayout, player)
				.orElse(null);
		} else {
			this.recipeTransferError = RecipeTransferErrorInternal.INSTANCE;
		}

		if (recipeTransferError == null ||
			recipeTransferError.getType().allowsTransfer) {
			this.active = true;
			this.visible = true;
		} else {
			this.active = false;
			IRecipeTransferError.Type type = this.recipeTransferError.getType();
			this.visible = (type == IRecipeTransferError.Type.USER_FACING);
		}

		this.onClickHandler = (mouseX, mouseY) -> {
			boolean maxTransfer = Screen.hasShiftDown();
			if (container != null && RecipeTransferUtil.transferRecipe(recipeTransferManager, container, recipeLayout, player, maxTransfer)) {
				onClose.run();
			}
		};
	}

	public void drawToolTip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		if (isMouseOver(mouseX, mouseY)) {
			if (recipeTransferError == null) {
				Component tooltipTransfer = Component.translatable("jei.tooltip.transfer");
				TooltipRenderer.drawHoveringText(guiGraphics, List.of(tooltipTransfer), mouseX, mouseY);
			} else {
				IRecipeSlotsView recipeSlotsView = recipeLayout.getRecipeSlotsView();
				Rect2i recipeRect = recipeLayout.getRect();
				recipeTransferError.showError(guiGraphics, mouseX, mouseY, recipeSlotsView, recipeRect.getX(), recipeRect.getY());
			}
		}
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return this.visible &&
			mouseX >= this.getX() &&
			mouseY >= this.getY() &&
			mouseX < this.getX() + this.getWidth() &&
			mouseY < this.getY() + this.getHeight();
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		if (this.visible && this.recipeTransferError != null && this.recipeTransferError.getType() == IRecipeTransferError.Type.COSMETIC) {
			guiGraphics.fill(
				RenderType.guiOverlay(),
				this.getX(),
				this.getY(),
				this.getX() + this.getWidth(),
				this.getY() + this.getHeight(),
				this.recipeTransferError.getButtonHighlightColor()
			);
		}
	}

	@Override
	public void onRelease(double mouseX, double mouseY) {
		if (!isMouseOver(mouseX, mouseY)) {
			return;
		}
		if (onClickHandler != null) {
			onClickHandler.onClick(mouseX, mouseY);
		}
	}
}
