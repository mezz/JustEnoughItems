package mezz.jei.test;

import mezz.jei.api.recipe.category.extensions.vanilla.brewing.IBrewingCategoryExtension;
import mezz.jei.common.recipes.BrewingExtensionHelper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BrewingExtensionHelperTest {
	@Test
	public void exactClassExtensionIsPreferredOverSuperclassExtension() {
		BrewingExtensionHelper helper = new BrewingExtensionHelper();
		IBrewingCategoryExtension<BaseRecipe> baseExtension = (recipe, factory) -> List.of();
		IBrewingCategoryExtension<CustomRecipe> customExtension = (recipe, factory) -> List.of();
		helper.addRecipeExtension(BaseRecipe.class, baseExtension);
		helper.addRecipeExtension(CustomRecipe.class, customExtension);

		IBrewingCategoryExtension<? super CustomRecipe> result = helper.getRecipeExtension(new CustomRecipe());

		assertSame(customExtension, result);
	}

	@Test
	public void superclassExtensionHandlesSubclass() {
		BrewingExtensionHelper helper = new BrewingExtensionHelper();
		IBrewingCategoryExtension<BaseRecipe> baseExtension = (recipe, factory) -> List.of();
		helper.addRecipeExtension(BaseRecipe.class, baseExtension);

		IBrewingCategoryExtension<? super CustomRecipe> result = helper.getRecipeExtension(new CustomRecipe());

		assertSame(baseExtension, result);
	}

	@Test
	public void registeringDuplicateRecipeClassFails() {
		BrewingExtensionHelper helper = new BrewingExtensionHelper();
		helper.addRecipeExtension(BaseRecipe.class, (recipe, factory) -> List.of());

		assertThrows(
			IllegalArgumentException.class,
			() -> helper.addRecipeExtension(BaseRecipe.class, (recipe, factory) -> List.of())
		);
	}

	@Test
	public void unregisteredRecipeIsNotHandled() {
		BrewingExtensionHelper helper = new BrewingExtensionHelper();

		assertNull(helper.getRecipeExtension(new BaseRecipe()));
	}

	private static class BaseRecipe {
	}

	private static class CustomRecipe extends BaseRecipe {
	}
}
