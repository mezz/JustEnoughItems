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
		helper.addExtension(BaseRecipe.class, baseExtension);
		helper.addExtension(CustomRecipe.class, customExtension);

		IBrewingCategoryExtension<? super CustomRecipe> result = helper.getRecipeExtension(new CustomRecipe());

		assertSame(customExtension, result);
	}

	@Test
	public void superclassExtensionHandlesSubclass() {
		BrewingExtensionHelper helper = new BrewingExtensionHelper();
		IBrewingCategoryExtension<BaseRecipe> baseExtension = (recipe, factory) -> List.of();
		helper.addExtension(BaseRecipe.class, baseExtension);

		IBrewingCategoryExtension<? super CustomRecipe> result = helper.getRecipeExtension(new CustomRecipe());

		assertSame(baseExtension, result);
	}

	@Test
	public void registeringDuplicateRecipeClassFails() {
		BrewingExtensionHelper helper = new BrewingExtensionHelper();
		helper.addExtension(BaseRecipe.class, (recipe, factory) -> List.of());

		assertThrows(
			IllegalArgumentException.class,
			() -> helper.addExtension(BaseRecipe.class, (recipe, factory) -> List.of())
		);
	}

	@Test
	public void mostSpecificExtensionIsIndependentOfRegistrationOrder() {
		BrewingExtensionHelper helper = new BrewingExtensionHelper();
		IBrewingCategoryExtension<BaseRecipe> baseExtension = (recipe, factory) -> List.of();
		IBrewingCategoryExtension<IntermediateRecipe> intermediateExtension = (recipe, factory) -> List.of();
		helper.addExtension(IntermediateRecipe.class, intermediateExtension);
		helper.addExtension(BaseRecipe.class, baseExtension);

		IBrewingCategoryExtension<? super CustomRecipe> result = helper.getRecipeExtension(new CustomRecipe());

		assertSame(intermediateExtension, result);
	}

	@Test
	public void unrelatedMatchingExtensionsAreAmbiguous() {
		BrewingExtensionHelper helper = new BrewingExtensionHelper();
		helper.addExtension(FirstRecipeType.class, (recipe, factory) -> List.of());
		helper.addExtension(SecondRecipeType.class, (recipe, factory) -> List.of());

		IBrewingCategoryExtension<? super AmbiguousRecipe> result = helper.getRecipeExtension(new AmbiguousRecipe());

		assertNull(result);
	}

	@Test
	public void unregisteredRecipeIsNotHandled() {
		BrewingExtensionHelper helper = new BrewingExtensionHelper();

		assertNull(helper.getRecipeExtension(new BaseRecipe()));
	}

	private static class BaseRecipe {
	}

	private static class IntermediateRecipe extends BaseRecipe {
	}

	private static class CustomRecipe extends IntermediateRecipe {
	}

	private interface FirstRecipeType {
	}

	private interface SecondRecipeType {
	}

	private static class AmbiguousRecipe implements FirstRecipeType, SecondRecipeType {
	}
}
