package mezz.jei.test;

import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.ingredients.IListElement;
import mezz.jei.gui.ingredients.IListElementInfo;
import mezz.jei.gui.ingredients.IngredientListElementFactory;
import mezz.jei.gui.search.ElementPrefixParser;
import mezz.jei.gui.search.ElementSearch;
import mezz.jei.library.ingredients.subtypes.SubtypeInterpreters;
import mezz.jei.library.ingredients.subtypes.SubtypeManager;
import mezz.jei.library.load.registration.IngredientManagerBuilder;
import mezz.jei.test.lib.TestColorHelper;
import mezz.jei.test.lib.TestIngredient;
import mezz.jei.test.lib.TestIngredientFilterConfig;
import mezz.jei.test.lib.TestModIdHelper;
import mezz.jei.test.lib.TestPlugin;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ElementSearchIngredientsTest {
	private static final TestModIdHelper MOD_ID_HELPER = new TestModIdHelper();
	private static final TestColorHelper COLOR_HELPER = new TestColorHelper();
	private static final TestIngredientFilterConfig FILTER_CONFIG = new TestIngredientFilterConfig();

	@Test
	public void newSearchHasNoIngredients() {
		// Setup: a search index has been created but no elements have been added yet.
		SearchFixture fixture = createFixture();

		// Operation: ask for all indexed ingredients.
		Collection<IListElement<?>> allIngredients = fixture.search().getAllIngredients();

		// Assertions: the empty search reports no ingredients.
		Assertions.assertTrue(allIngredients.isEmpty());
	}

	@Test
	public void addAllIndexesBaseIngredientsFromBuilder() {
		// Setup: IngredientManagerBuilder registered the base test plugin ingredients.
		SearchFixture fixture = createFixture();
		List<IListElementInfo<?>> baseList = IngredientListElementFactory.createBaseList(fixture.ingredientManager(), MOD_ID_HELPER);

		// Operation: add the whole base list to the search index.
		fixture.search().addAll(baseList);

		// Assertions: both plugin-registered ingredients are exposed by getAllIngredients.
		assertIngredientNumbers(fixture.search().getAllIngredients(), Set.of(0, 1));
	}

	@Test
	public void addIndexesSingleIngredient() {
		// Setup: one list element has been created outside the manager's base list.
		SearchFixture fixture = createFixture();
		IListElementInfo<?> info = fixture.createInfos(List.of(new TestIngredient(4))).get(0);

		// Operation: add the single element to the search index.
		fixture.search().add(info);

		// Assertions: getAllIngredients includes the individually added element.
		assertIngredientNumbers(fixture.search().getAllIngredients(), Set.of(4));
	}

	@Test
	public void addIndexesMultipleIngredients() {
		// Setup: two distinct elements will be added individually.
		SearchFixture fixture = createFixture();
		List<IListElementInfo<?>> infos = fixture.createInfos(
			List.of(
				new TestIngredient(5),
				new TestIngredient(6)
			)
		);

		// Operation: add each element through the single-add path.
		fixture.search().add(infos.get(0));
		fixture.search().add(infos.get(1));

		// Assertions: both individually added ingredients are retained.
		assertIngredientNumbers(fixture.search().getAllIngredients(), Set.of(5, 6));
	}

	@Test
	public void addCanExtendExistingAddAllResults() {
		// Setup: the index already contains elements added in bulk.
		SearchFixture fixture = createFixture();
		fixture.search().addAll(fixture.createInfos(List.of(new TestIngredient(7))));
		IListElementInfo<?> extraInfo = fixture.createInfos(List.of(new TestIngredient(8))).get(0);

		// Operation: add another ingredient through the single-add path.
		fixture.search().add(extraInfo);

		// Assertions: existing bulk results and the later single result are both exposed.
		assertIngredientNumbers(fixture.search().getAllIngredients(), Set.of(7, 8));
	}

	@Test
	public void emptySearchTokenReturnsNoResults() {
		// Setup: the search index contains an ingredient.
		SearchFixture fixture = createFixture();
		fixture.search().addAll(fixture.createInfos(List.of(new TestIngredient(16))));

		// Operation: search with an empty token.
		Set<Integer> results = fixture.searchIngredientNumbers("");

		// Assertions: empty tokens are ignored instead of returning every ingredient.
		Assertions.assertTrue(results.isEmpty());
	}

	@Test
	public void displayNameSearchFindsIngredient() {
		// Setup: the display-name searchable contains two different ingredient names.
		SearchFixture fixture = createFixture();
		fixture.search().addAll(
			fixture.createInfos(List.of(new TestIngredient(17), new TestIngredient(18)))
		);

		// Operation: search for the unique suffix of one display name.
		Set<Integer> results = fixture.searchIngredientNumbers("testingredient#17");

		// Assertions: the matching display-name ingredient is returned.
		Assertions.assertEquals(Set.of(17), results);
	}

	@Test
	public void identifierSearchFindsIngredient() {
		// Setup: identifier search is enabled and each test ingredient has a stable identifier.
		SearchFixture fixture = createFixture();
		fixture.search().addAll(
			fixture.createInfos(List.of(new TestIngredient(19), new TestIngredient(20)))
		);

		// Operation: search by the identifier prefix for one ingredient.
		Set<Integer> results = fixture.searchIngredientNumbers("&jei_test_mod:test_ingredient_20");

		// Assertions: identifier search returns only the matching ingredient.
		Assertions.assertEquals(Set.of(20), results);
	}

	private static SearchFixture createFixture() {
		IngredientManagerBuilder ingredientManagerBuilder = createIngredientManagerBuilder();
		new TestPlugin().registerIngredients(ingredientManagerBuilder);
		IIngredientManager ingredientManager = ingredientManagerBuilder.build();
		ElementPrefixParser elementPrefixParser = new ElementPrefixParser(
			ingredientManager,
			FILTER_CONFIG,
			COLOR_HELPER,
			MOD_ID_HELPER
		);
		return new SearchFixture(ingredientManager, elementPrefixParser, new ElementSearch(elementPrefixParser));
	}

	private static IngredientManagerBuilder createIngredientManagerBuilder() {
		SubtypeInterpreters subtypeInterpreters = new SubtypeInterpreters();
		SubtypeManager subtypeManager = new SubtypeManager(subtypeInterpreters);
		return new IngredientManagerBuilder(subtypeManager, COLOR_HELPER);
	}

	private static void assertIngredientNumbers(Collection<IListElement<?>> allIngredients, Set<Integer> expectedNumbers) {
		Assertions.assertEquals(expectedNumbers.size(), allIngredients.size());
		Assertions.assertEquals(expectedNumbers, getIngredientNumbers(allIngredients));
	}

	private static Set<Integer> getIngredientNumbers(Collection<IListElement<?>> allIngredients) {
		return allIngredients.stream()
			.map(ElementSearchIngredientsTest::getIngredientNumber)
			.collect(Collectors.toSet());
	}

	private static int getIngredientNumber(IListElement<?> element) {
		TestIngredient ingredient = (TestIngredient) element.getTypedIngredient().getIngredient();
		return ingredient.getNumber();
	}

	private record SearchFixture(
		IIngredientManager ingredientManager,
		ElementPrefixParser elementPrefixParser,
		ElementSearch search
	) {
		private List<IListElementInfo<?>> createInfos(Collection<TestIngredient> ingredients) {
			List<IListElementInfo<TestIngredient>> infos = IngredientListElementFactory.createTestList(
				ingredientManager,
				TestIngredient.TYPE,
				ingredients,
				MOD_ID_HELPER
			);
			return new ArrayList<>(infos);
		}

		private Set<Integer> searchIngredientNumbers(String token) {
			ElementPrefixParser.TokenInfo tokenInfo = elementPrefixParser.parseToken(token)
				.orElse(new ElementPrefixParser.TokenInfo("", ElementPrefixParser.NO_PREFIX));
			return getIngredientNumbers(search.getSearchResults(tokenInfo));
		}
	}
}
