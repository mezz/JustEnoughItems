package mezz.jei.gui.recipes;

import javax.annotation.Nullable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.gui.elements.GuiIconButtonSmall;
import mezz.jei.recipes.RecipeTransferManager;
import mezz.jei.transfer.RecipeTransferErrorInternal;
import mezz.jei.transfer.RecipeTransferUtil;
import mezz.jei.util.Translator;

public class RecipeTransferButton extends GuiIconButtonSmall {
	private final RecipeLayout recipeLayout;
	@Nullable
	private IRecipeTransferError recipeTransferError;
	@Nullable
	private IOnClickHandler onClickHandler;

	public RecipeTransferButton(int xPos, int yPos, int width, int height, IDrawable icon, RecipeLayout recipeLayout) {
		super(xPos, yPos, width, height, icon, b -> {});
		this.recipeLayout = recipeLayout;
	}

	public void init(RecipeTransferManager recipeTransferManager, @Nullable Container container, PlayerEntity player) {
		if (container != null) {
			this.recipeTransferError = RecipeTransferUtil.getTransferRecipeError(recipeTransferManager, container, recipeLayout, player);
		} else {
			this.recipeTransferError = RecipeTransferErrorInternal.INSTANCE;
		}

		if (RecipeTransferUtil.allowsTransfer(recipeTransferError)) {
			this.active = true;
			this.visible = true;
		} else {
			this.active = false;
			IRecipeTransferError.Type type = this.recipeTransferError.getType();
			this.visible = (type == IRecipeTransferError.Type.USER_FACING);
		}
	}

	public void drawToolTip(int mouseX, int mouseY) {
		if (isMouseOver(mouseX, mouseY)) {
			if (recipeTransferError == null) {
				String tooltipTransfer = Translator.translateToLocal("jei.tooltip.transfer");
				TooltipRenderer.drawHoveringText(tooltipTransfer, mouseX, mouseY);
			} else {
				recipeTransferError.showError(mouseX, mouseY, recipeLayout, recipeLayout.getPosX(), recipeLayout.getPosY());
			}
		}
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return this.visible &&
			mouseX >= this.x &&
			mouseY >= this.y &&
			mouseX < this.x + this.width &&
			mouseY < this.y + this.height;
	}

	public void setOnClickHandler(IOnClickHandler onClickHandler) {
		this.onClickHandler = onClickHandler;
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		if (onClickHandler != null) {
			onClickHandler.onClick(mouseX, mouseY);
		}
	}
}
