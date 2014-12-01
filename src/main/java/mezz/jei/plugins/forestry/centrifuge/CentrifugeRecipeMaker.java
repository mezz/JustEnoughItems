package mezz.jei.plugins.forestry.centrifuge;

import forestry.factory.gadgets.MachineCentrifuge;

import java.util.List;

public class CentrifugeRecipeMaker {

	public static List getCentrifugeRecipes() {
		return MachineCentrifuge.RecipeManager.recipes;
	}

}
