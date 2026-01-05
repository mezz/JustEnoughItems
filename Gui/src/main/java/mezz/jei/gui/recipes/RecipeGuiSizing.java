package mezz.jei.gui.recipes;

import mezz.jei.common.config.IClientConfig;

final class RecipeGuiSizing {
	private RecipeGuiSizing() {

	}

	static Size calculateInitialSize(int screenHeight, boolean centerSearchBarEnabled, int maxHeight) {
		int ySize;
		if (centerSearchBarEnabled) {
			ySize = screenHeight - 76;
		} else {
			ySize = screenHeight - 58;
		}
		if (ySize < IClientConfig.minRecipeGuiHeight) {
			ySize = IClientConfig.minRecipeGuiHeight;
		}

		int extraSpace = 0;
		if (ySize > maxHeight) {
			extraSpace = ySize - maxHeight;
			ySize = maxHeight;
		}
		return new Size(ySize, extraSpace);
	}

	record Size(int ySize, int extraSpace) {}
}
