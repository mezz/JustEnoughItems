package mezz.jei.transfer;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;

import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.gui.RecipeLayout;

public class RecipeTransferErrorInternal implements IRecipeTransferError {
	public static final RecipeTransferErrorInternal instance = new RecipeTransferErrorInternal();

	private RecipeTransferErrorInternal() {

	}

	@Override
	public Type getType() {
		return Type.INTERNAL;
	}

	@Override
	public void showError(@Nonnull Minecraft minecraft, int mouseX, int mouseY, @Nonnull RecipeLayout recipeLayout) {

	}
}
