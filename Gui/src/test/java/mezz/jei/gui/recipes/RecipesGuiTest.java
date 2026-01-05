package mezz.jei.gui.recipes;

import mezz.jei.common.config.IClientConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RecipesGuiTest {
	@Test
	public void smallScreenUsesMinimumRecipeGuiHeight() {
		// Setup: the screen height is smaller than the recipe GUI's minimum height.
		int screenHeight = IClientConfig.minRecipeGuiHeight / 2;
		int maxHeight = IClientConfig.defaultRecipeGuiHeight;

		// Operation: calculate the initial recipe GUI size.
		RecipeGuiSizing.Size size = RecipeGuiSizing.calculateInitialSize(screenHeight, false, maxHeight);

		// Assertions: the recipe GUI keeps enough height for its internal layout.
		assertEquals(IClientConfig.minRecipeGuiHeight, size.ySize());
		assertEquals(0, size.extraSpace());
	}

	@Test
	public void smallScreenWithCenterSearchUsesMinimumRecipeGuiHeight() {
		// Setup: centered-search mode reserves more vertical space around the recipe GUI.
		int screenHeight = IClientConfig.minRecipeGuiHeight / 2;
		int maxHeight = IClientConfig.defaultRecipeGuiHeight;

		// Operation: calculate the initial recipe GUI size in centered-search mode.
		RecipeGuiSizing.Size size = RecipeGuiSizing.calculateInitialSize(screenHeight, true, maxHeight);

		// Assertions: the same minimum-height guard applies with centered search enabled.
		assertEquals(IClientConfig.minRecipeGuiHeight, size.ySize());
		assertEquals(0, size.extraSpace());
	}

	@Test
	public void tallScreenClampsToMaxRecipeGuiHeight() {
		// Setup: a tall screen has more available height than the configured maximum recipe GUI height.
		int maxHeight = IClientConfig.minRecipeGuiHeight + 20;
		int screenHeight = maxHeight + 200;

		// Operation: calculate the initial recipe GUI size.
		RecipeGuiSizing.Size size = RecipeGuiSizing.calculateInitialSize(screenHeight, false, maxHeight);

		// Assertions: the recipe GUI height is clamped to the configured maximum, with extra height for centering.
		assertEquals(maxHeight, size.ySize());
		assertTrue(size.extraSpace() > 0);
	}
}
