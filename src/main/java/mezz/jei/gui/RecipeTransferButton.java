package mezz.jei.gui;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.config.Constants;
import mezz.jei.transfer.RecipeTransferErrorInternal;
import mezz.jei.transfer.RecipeTransferUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.client.config.GuiButtonExt;

public class RecipeTransferButton extends GuiButtonExt {
	private RecipeLayout recipeLayout;
	private IRecipeTransferError recipeTransferError;

	public RecipeTransferButton(int id, int xPos, int yPos, int width, int height, String displayString) {
		super(id, xPos, yPos, width, height, displayString);
	}

	public void init(@Nullable Container container, @Nonnull RecipeLayout recipeLayout, @Nonnull EntityPlayer player) {
		this.recipeLayout = recipeLayout;

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
	public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		super.drawButton(mc, mouseX, mouseY);
		if (hovered && visible) {
			if (recipeTransferError != null) {
				recipeTransferError.showError(mc, mouseX, mouseY, recipeLayout, recipeLayout.getPosX(), recipeLayout.getPosY());
			} else {
				TooltipRenderer.drawHoveringText(mc, Constants.RECIPE_TRANSFER_TOOLTIP, mouseX, mouseY);
			}
		}
	}
}
