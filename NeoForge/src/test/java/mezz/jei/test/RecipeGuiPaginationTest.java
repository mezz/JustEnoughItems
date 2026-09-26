package mezz.jei.test;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.common.gui.elements.DrawableBlank;
import mezz.jei.common.util.ImmutableSize2i;
import mezz.jei.gui.recipes.RecipeGuiGrid;
import mezz.jei.gui.recipes.lookups.SingleCategoryLookupState;
import mezz.jei.gui.recipes.lookups.StaticFocusedRecipes;
import mezz.jei.library.focus.FocusGroup;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RecipeGuiPaginationTest {
	@Test
	public void wideningOnLaterPageDoesNotSkipTheLastRecipes() {
		SingleCategoryLookupState state = createState(7);
		setWidth(state, 120);
		state.nextPage();
		assertEquals(3, state.getRecipeIndex());

		setWidth(state, 244);
		assertEquals(2, state.pageCount());
		assertEquals(3, state.getRecipeIndex());
		assertTrue(state.nextPage());
		assertEquals(6, state.getRecipeIndex());
		assertTrue(state.nextPage());
		assertEquals(0, state.getRecipeIndex());
		assertTrue(state.previousPage());
		assertEquals(6, state.getRecipeIndex());
	}

	@Test
	public void narrowingOnLaterPageKeepsNavigationAligned() {
		SingleCategoryLookupState state = createState(11);
		setWidth(state, 244);
		state.nextPage();

		setWidth(state, 120);
		assertEquals(4, state.pageCount());
		assertEquals(6, state.getRecipeIndex());
		assertTrue(state.nextPage());
		assertEquals(9, state.getRecipeIndex());
		assertTrue(state.nextPage());
		assertEquals(0, state.getRecipeIndex());
		assertTrue(state.previousPage());
		assertEquals(9, state.getRecipeIndex());
	}

	@Test
	public void draggingBackToTheOriginalWidthPreservesTheOriginalPage() {
		SingleCategoryLookupState state = createState(11);
		setWidth(state, 120);
		state.nextPage();

		setWidth(state, 244);
		setWidth(state, 120);
		assertEquals(3, state.getRecipeIndex());
		assertEquals(4, state.pageCount());
	}

	private static void setWidth(SingleCategoryLookupState state, int width) {
		RecipeGuiGrid grid = RecipeGuiGrid.calculate(new ImmutableSize2i(width, 158), new ImmutableSize2i(120, 50), 2);
		state.setRecipesPerPage(grid.recipesPerPage());
	}

	private static SingleCategoryLookupState createState(int recipeCount) {
		IRecipeCategory<Integer> category = new AbstractRecipeCategory<>(
			IRecipeType.create("test", "pagination", Integer.class),
			Component.literal("Pagination"), new DrawableBlank(16, 16), 100, 50
		) {
			@Override
			public void setRecipe(IRecipeLayoutBuilder builder, Integer recipe, IFocusGroup focuses) {

			}
		};
		List<Integer> recipes = IntStream.range(0, recipeCount).boxed().toList();
		return new SingleCategoryLookupState(new StaticFocusedRecipes<>(category, recipes), FocusGroup.EMPTY);
	}
}
