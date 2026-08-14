package mezz.jei.gui.overlay.elements;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TagIngredientElementTest {
	private static final ResourceKey<Registry<String>> REGISTRY_KEY = ResourceKey.createRegistryKey(
		Identifier.fromNamespaceAndPath("test", "ingredients")
	);
	private static final TagKey<String> TAG_KEY = TagKey.create(
		REGISTRY_KEY,
		Identifier.fromNamespaceAndPath("test", "target_tag")
	);

	@Test
	void findsExactTagRecipeAmongOtherTagRecipes() {
		// Setup: a tag recipe category contains multiple tag recipes.
		TestRecipe otherRecipe = new TestRecipe(Identifier.fromNamespaceAndPath("test", "other_tag"));
		TestRecipe targetRecipe = new TestRecipe(TAG_KEY.location());
		TestRecipeCategory recipeCategory = new TestRecipeCategory();

		// Operation: look up the recipe represented by the clicked tag ingredient.
		TestRecipe result = TagIngredientElement.findTagRecipe(
				TAG_KEY,
				recipeCategory,
				List.of(otherRecipe, targetRecipe).stream()
			)
			.orElseThrow();

		// Assertions: navigation selects the exact tag instead of the first recipe in the category.
		assertEquals(targetRecipe, result);
	}

	@Test
	void returnsEmptyWhenTagRecipeIsNotRegistered() {
		// Setup: the tag recipe category does not contain the clicked tag.
		TestRecipeCategory recipeCategory = new TestRecipeCategory();
		TestRecipe otherRecipe = new TestRecipe(Identifier.fromNamespaceAndPath("test", "other_tag"));

		// Operation: look up the recipe represented by the clicked tag ingredient.
		var result = TagIngredientElement.findTagRecipe(
			TAG_KEY,
			recipeCategory,
			List.of(otherRecipe).stream()
		);

		// Assertions: callers can fall back to normal ingredient recipe lookup.
		assertTrue(result.isEmpty());
	}

	private record TestRecipe(Identifier identifier) {
	}

	private static class TestRecipeCategory implements IRecipeCategory<TestRecipe> {
		private static final IRecipeType<TestRecipe> RECIPE_TYPE = IRecipeType.create(
			"test",
			"tag_recipes/ingredients",
			TestRecipe.class
		);

		@Override
		public IRecipeType<TestRecipe> getRecipeType() {
			return RECIPE_TYPE;
		}

		@Override
		public Component getTitle() {
			return Component.empty();
		}

		@Override
		public int getWidth() {
			return 1;
		}

		@Override
		public int getHeight() {
			return 1;
		}

		@Override
		public @Nullable IDrawable getIcon() {
			return null;
		}

		@Override
		public void setRecipe(IRecipeLayoutBuilder builder, TestRecipe recipe, IFocusGroup focuses) {

		}

		@Override
		public Identifier getIdentifier(TestRecipe recipe) {
			return recipe.identifier();
		}
	}
}
