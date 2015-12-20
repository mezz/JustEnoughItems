package mezz.jei.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import net.minecraftforge.fml.client.config.GuiButtonExt;

import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.transfer.RecipeTransferUtil;

public class RecipeTransferButton extends GuiButtonExt {
	private RecipeLayout recipeLayout;
	private IRecipeTransferError recipeTransferError;

	public RecipeTransferButton(int id, int xPos, int yPos, int width, int height, String displayString) {
		super(id, xPos, yPos, width, height, displayString);
	}

	public void init(RecipeLayout recipeLayout, EntityPlayer player) {
		this.recipeLayout = recipeLayout;
		this.recipeTransferError = RecipeTransferUtil.getTransferRecipeError(recipeLayout, player);

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
		if (hovered && visible && recipeTransferError != null) {
			recipeTransferError.showError(mc, mouseX, mouseY, recipeLayout);
		}
	}
}
