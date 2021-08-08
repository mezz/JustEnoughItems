package mezz.jei.load.registration;

import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.resources.ResourceLocation;

import com.google.common.collect.ImmutableListMultimap;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.collect.ListMultiMap;
import mezz.jei.util.ErrorUtil;

public class RecipeCatalystRegistration implements IRecipeCatalystRegistration {
	private final ListMultiMap<ResourceLocation, Object> recipeCatalysts = new ListMultiMap<>();

	@Override
	public <T> void addRecipeCatalyst(IIngredientType<T> ingredientType, T catalystIngredient, ResourceLocation... recipeCategoryUids) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkIsInstance(ingredientType, catalystIngredient, "catalystIngredient");
		ErrorUtil.checkIsValidIngredient(catalystIngredient, "catalystIngredient");
		ErrorUtil.checkNotEmpty(recipeCategoryUids, "recipeCategoryUids");

		for (ResourceLocation recipeCategoryUid : recipeCategoryUids) {
			ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");
			this.recipeCatalysts.put(recipeCategoryUid, catalystIngredient);
		}
	}

	public ImmutableListMultimap<ResourceLocation, Object> getRecipeCatalysts() {
		return recipeCatalysts.toImmutable();
	}
}
