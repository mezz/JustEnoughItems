package mezz.jei.transfer;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;

import mezz.jei.gui.RecipeLayout;

public class RecipeTransferErrorTransferHelper implements IRecipeTransferError {
	public static final RecipeTransferErrorTransferHelper instance = new RecipeTransferErrorTransferHelper();

	private RecipeTransferErrorTransferHelper() {

	}

	@Override
	public Type getType() {
		return Type.TRANSFER_HELPER;
	}

	@Override
	public void showError(@Nonnull Minecraft minecraft, int mouseX, int mouseY, @Nonnull RecipeLayout recipeLayout) {

	}
}
