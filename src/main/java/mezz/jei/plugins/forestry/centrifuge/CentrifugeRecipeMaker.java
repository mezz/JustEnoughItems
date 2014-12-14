package mezz.jei.plugins.forestry.centrifuge;

import java.util.ArrayList;
import java.util.List;

import forestry.factory.gadgets.MachineCentrifuge;

public class CentrifugeRecipeMaker {

	public static List<Object> getCentrifugeRecipes() {
		return new ArrayList<Object>(MachineCentrifuge.RecipeManager.recipes);
	}

}
