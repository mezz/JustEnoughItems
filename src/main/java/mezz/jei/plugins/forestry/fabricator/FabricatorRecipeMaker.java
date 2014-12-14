package mezz.jei.plugins.forestry.fabricator;

import java.util.ArrayList;
import java.util.List;

import forestry.factory.gadgets.MachineFabricator;

public class FabricatorRecipeMaker {

	public static List<Object> getSmeltingRecipes() {
		return new ArrayList<Object>(MachineFabricator.RecipeManager.smeltings);
	}

	public static List<Object> getCraftingRecipes() {
		return new ArrayList<Object>(MachineFabricator.RecipeManager.recipes);
	}

}
