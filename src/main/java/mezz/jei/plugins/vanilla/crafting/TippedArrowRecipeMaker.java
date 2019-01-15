package mezz.jei.plugins.vanilla.crafting;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.potion.PotionType;
import net.minecraft.util.ResourceLocation;

public final class TippedArrowRecipeMaker {

	public static List<TippedArrowRecipeWrapper> getTippedArrowRecipes() {
		List<TippedArrowRecipeWrapper> recipes = new ArrayList<>();
		for (ResourceLocation potionTypeResourceLocation : PotionType.REGISTRY.getKeys()) {
			PotionType potionType = PotionType.REGISTRY.get(potionTypeResourceLocation);
			TippedArrowRecipeWrapper recipe = new TippedArrowRecipeWrapper(potionType);
			recipes.add(recipe);
		}
		return recipes;
	}

	private TippedArrowRecipeMaker() {

	}
}
