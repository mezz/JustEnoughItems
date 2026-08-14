package mezz.jei.gui.overlay.elements;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.SharedConstants;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TagIngredientElementTest {
	private static final ResourceKey<Registry<String>> REGISTRY_KEY = createRegistryKey(
		new ResourceLocation("test", "ingredients")
	);
	private static final TagKey<String> TAG_KEY = TagKey.create(
		REGISTRY_KEY,
		new ResourceLocation("test", "target_tag")
	);

	private static <T> ResourceKey<Registry<T>> createRegistryKey(ResourceLocation location) {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
		return ResourceKey.createRegistryKey(location);
	}

	@Test
	void findsExactTagRecipeAmongOtherTagRecipes() {
		TestRecipe otherRecipe = new TestRecipe(new ResourceLocation("test", "other_tag"));
		TestRecipe targetRecipe = new TestRecipe(TAG_KEY.location());
		TestRecipeCategory recipeCategory = new TestRecipeCategory();

		TestRecipe result = TagIngredientElement.findTagRecipe(
				TAG_KEY,
				recipeCategory,
				List.of(otherRecipe, targetRecipe).stream()
			)
			.orElseThrow();

		assertEquals(targetRecipe, result);
	}

	@Test
	void returnsEmptyWhenTagRecipeIsNotRegistered() {
		TestRecipeCategory recipeCategory = new TestRecipeCategory();
		TestRecipe otherRecipe = new TestRecipe(new ResourceLocation("test", "other_tag"));

		var result = TagIngredientElement.findTagRecipe(
			TAG_KEY,
			recipeCategory,
			List.of(otherRecipe).stream()
		);

		assertTrue(result.isEmpty());
	}

	private record TestRecipe(ResourceLocation identifier) {
	}

	private static class TestRecipeCategory implements IRecipeCategory<TestRecipe> {
		private static final RecipeType<TestRecipe> RECIPE_TYPE = RecipeType.create(
			"test",
			"tag_recipes/ingredients",
			TestRecipe.class
		);

		@Override
		public RecipeType<TestRecipe> getRecipeType() {
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
		public ResourceLocation getRegistryName(TestRecipe recipe) {
			return recipe.identifier();
		}
	}
}
