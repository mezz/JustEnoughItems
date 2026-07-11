package mezz.jei.test;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.gui.recipes.RecipeTransferButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import org.junit.Assert;
import org.junit.Test;

public class RecipeTransferButtonTest {
	@Test
	public void successfulTransferLeavesButtonActiveAndVisible() {
		GuiButton button = createDisabledHiddenButton();

		RecipeTransferButton.updateStateForTransferError(button, null);

		Assert.assertTrue(button.enabled);
		Assert.assertTrue(button.visible);
	}

	@Test
	public void cosmeticTransferErrorsLeaveButtonActiveAndVisible() {
		GuiButton button = createDisabledHiddenButton();

		RecipeTransferButton.updateStateForTransferError(button, new TestTransferError(IRecipeTransferError.Type.COSMETIC));

		Assert.assertTrue(button.enabled);
		Assert.assertTrue(button.visible);
	}

	@Test
	public void userFacingTransferErrorsDisableButShowButton() {
		GuiButton button = createEnabledVisibleButton();

		RecipeTransferButton.updateStateForTransferError(button, new TestTransferError(IRecipeTransferError.Type.USER_FACING));

		Assert.assertFalse(button.enabled);
		Assert.assertTrue(button.visible);
	}

	@Test
	public void internalTransferErrorsDisableAndHideButton() {
		GuiButton button = createEnabledVisibleButton();

		RecipeTransferButton.updateStateForTransferError(button, new TestTransferError(IRecipeTransferError.Type.INTERNAL));

		Assert.assertFalse(button.enabled);
		Assert.assertFalse(button.visible);
	}

	private static GuiButton createEnabledVisibleButton() {
		GuiButton button = new GuiButton(0, 0, 0, "");
		button.enabled = true;
		button.visible = true;
		return button;
	}

	private static GuiButton createDisabledHiddenButton() {
		GuiButton button = new GuiButton(0, 0, 0, "");
		button.enabled = false;
		button.visible = false;
		return button;
	}

	private static class TestTransferError implements IRecipeTransferError {
		private final Type type;

		private TestTransferError(Type type) {
			this.type = type;
		}

		@Override
		public Type getType() {
			return type;
		}

		@Override
		public void showError(Minecraft minecraft, int mouseX, int mouseY, IRecipeLayout recipeLayout, int recipeX, int recipeY) {

		}
	}
}
