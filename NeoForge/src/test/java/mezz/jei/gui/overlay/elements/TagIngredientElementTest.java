package mezz.jei.gui.overlay.elements;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.common.ingredients.TypedIngredient;
import mezz.jei.gui.util.FocusUtil;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TagIngredientElementTest {
	private static final IIngredientType<String> INGREDIENT_TYPE = () -> String.class;
	private static final ResourceKey<Registry<String>> REGISTRY_KEY = ResourceKey.createRegistryKey(
		Identifier.fromNamespaceAndPath("test", "ingredients")
	);
	private static final TagKey<String> TAG_KEY = TagKey.create(
		REGISTRY_KEY,
		Identifier.fromNamespaceAndPath("test", "target_tag")
	);

	@Test
	void pausedRecipeCyclingUsesDisplayedIngredientLookup() {
		// Setup: a tag ingredient is paused on one concrete displayed ingredient.
		ITypedIngredient<String> displayedIngredient = TypedIngredient.createUnvalidated(INGREDIENT_TYPE, "displayed");
		IRecipeManager recipeManager = createFailingProxy(IRecipeManager.class);
		TagIngredientElement<String> element = new TagIngredientElement<>(
			displayedIngredient,
			TAG_KEY,
			recipeManager,
			() -> true
		);
		int[] normalShowCalls = {0};
		IRecipesGui recipesGui = createProxy(IRecipesGui.class, (proxy, method, args) -> {
			if (method.getName().equals("show")) {
				normalShowCalls[0]++;
				return null;
			}
			throw new AssertionError("Unexpected recipe GUI call: " + method.getName());
		});
		FocusUtil focusUtil = new FocusUtil(null, null, null) {
			@Override
			public List<IFocus<?>> createFocuses(ITypedIngredient<?> ingredient, List<RecipeIngredientRole> roles) {
				assertSame(displayedIngredient, ingredient);
				return List.of();
			}
		};

		// Operation: show recipes while recipe cycling is paused.
		element.show(recipesGui, focusUtil, List.of(RecipeIngredientRole.OUTPUT));

		// Assertions: tag lookup is skipped and the displayed ingredient uses the normal lookup path.
		assertEquals(1, normalShowCalls[0]);
	}

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

	private static <T> T createFailingProxy(Class<T> type) {
		return createProxy(type, (proxy, method, args) -> {
			throw new AssertionError("Unexpected call: " + method.getName());
		});
	}

	private static <T> T createProxy(Class<T> type, InvocationHandler handler) {
		Object proxy = Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
		return type.cast(proxy);
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
