package mezz.jei.plugins.forestry.centrifuge;

import forestry.factory.gadgets.MachineCentrifuge;

import java.util.ArrayList;
import java.util.List;

public class CentrifugeRecipeMaker {

	public static List<Object> getCentrifugeRecipes() {
		return new ArrayList<Object>(MachineCentrifuge.RecipeManager.recipes);
	}

}
