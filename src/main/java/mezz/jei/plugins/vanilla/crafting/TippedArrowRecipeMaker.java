package mezz.jei.plugins.vanilla.crafting;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.potion.PotionType;
import net.minecraft.util.ResourceLocation;

public final class TippedArrowRecipeMaker {

	public static List<TippedArrowRecipeWrapper> getTippedArrowRecipes() {
		List<TippedArrowRecipeWrapper> recipes = new ArrayList<TippedArrowRecipeWrapper>();
		for (ResourceLocation potionTypeResourceLocation : PotionType.REGISTRY.getKeys()) {
			PotionType potionType = PotionType.REGISTRY.getObject(potionTypeResourceLocation);
			TippedArrowRecipeWrapper recipe = new TippedArrowRecipeWrapper(potionType);
			recipes.add(recipe);
		}
		return recipes;
	}

	private TippedArrowRecipeMaker() {

	}
}
