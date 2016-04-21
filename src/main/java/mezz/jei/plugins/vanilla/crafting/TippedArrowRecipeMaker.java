package mezz.jei.plugins.vanilla.crafting;

import net.minecraft.potion.PotionType;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class TippedArrowRecipeMaker {

	public static List<TippedArrowRecipeWrapper> getTippedArrowRecipes() {
		List<TippedArrowRecipeWrapper> recipes = new ArrayList<>();
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
