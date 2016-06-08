package mezz.jei.transfer;

import javax.annotation.Nonnull;

import mezz.jei.api.gui.IRecipeLayout;
import net.minecraft.client.Minecraft;

import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.gui.RecipeLayout;

public class RecipeTransferErrorInternal implements IRecipeTransferError {
	public static final RecipeTransferErrorInternal instance = new RecipeTransferErrorInternal();

	private RecipeTransferErrorInternal() {

	}

	@Nonnull
	@Override
	public Type getType() {
		return Type.INTERNAL;
	}

	@Override
	public void showError(@Nonnull Minecraft minecraft, int mouseX, int mouseY, @Nonnull IRecipeLayout recipeLayout, int recipeX, int recipeY) {

	}
}
