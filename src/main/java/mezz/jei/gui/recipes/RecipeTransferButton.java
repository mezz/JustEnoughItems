package mezz.jei.gui.recipes;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.gui.elements.GuiIconButtonSmall;
import mezz.jei.transfer.RecipeTransferErrorInternal;
import mezz.jei.transfer.RecipeTransferUtil;
import mezz.jei.util.Translator;

public class RecipeTransferButton extends GuiIconButtonSmall {
	private final RecipeLayout recipeLayout;
	@Nullable
	private IRecipeTransferError recipeTransferError;

	public RecipeTransferButton(int id, int xPos, int yPos, int width, int height, IDrawable icon, RecipeLayout recipeLayout) {
		super(id, xPos, yPos, width, height, icon);
		this.recipeLayout = recipeLayout;
	}

	public void init(@Nullable Container container, EntityPlayer player) {
		if (container != null) {
			this.recipeTransferError = RecipeTransferUtil.getTransferRecipeError(container, recipeLayout, player);
		} else {
			this.recipeTransferError = RecipeTransferErrorInternal.INSTANCE;
		}

		if (this.recipeTransferError == null) {
			this.enabled = true;
			this.visible = true;
		} else {
			this.enabled = false;
			IRecipeTransferError.Type type = this.recipeTransferError.getType();
			this.visible = (type == IRecipeTransferError.Type.USER_FACING);
		}
	}

	public void drawToolTip(Minecraft mc, int mouseX, int mouseY) {
		if (hovered && visible) {
			if (recipeTransferError != null) {
				recipeTransferError.showError(mc, mouseX, mouseY, recipeLayout, recipeLayout.getPosX(), recipeLayout.getPosY());
			} else {
				String tooltipTransfer = Translator.translateToLocal("jei.tooltip.transfer");
				TooltipRenderer.drawHoveringText(mc, tooltipTransfer, mouseX, mouseY);
			}
		}
	}
}
