package mezz.jei.gui.recipes;

import javax.annotation.Nullable;

import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.config.Constants;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.gui.elements.GuiIconButtonSmall;
import mezz.jei.transfer.RecipeTransferErrorInternal;
import mezz.jei.transfer.RecipeTransferUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

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

	@Override
	public void func_191745_a(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		super.func_191745_a(mc, mouseX, mouseY, partialTicks);
		if (hovered && visible) {
			if (recipeTransferError != null) {
				recipeTransferError.showError(mc, mouseX, mouseY, recipeLayout, recipeLayout.getPosX(), recipeLayout.getPosY());
			} else {
				TooltipRenderer.drawHoveringText(mc, Constants.RECIPE_TRANSFER_TOOLTIP, mouseX, mouseY);
			}
		}
	}
}
