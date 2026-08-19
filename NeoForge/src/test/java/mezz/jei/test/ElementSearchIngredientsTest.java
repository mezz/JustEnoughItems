package mezz.jei.test;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.search.ISearchStorageBuilder;
import mezz.jei.api.search.ISearchStorageBuilderFactory;
import mezz.jei.common.search.GeneralizedSuffixTreeSearchStorage;
import mezz.jei.common.search.SearchStorageBuilderAdapter;
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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ElementSearchIngredientsTest {
	private static final TestModIdHelper MOD_ID_HELPER = new TestModIdHelper();
	private static final TestColorHelper COLOR_HELPER = new TestColorHelper();
	private static final TestIngredientFilterConfig FILTER_CONFIG = new TestIngredientFilterConfig();
	private static final IIngredientType<TestIngredient> OTHER_TEST_TYPE = new IIngredientType<>() {
		@Override
		public String getUid() {
			return "other_test";
		}

		@Override
		public Class<? extends TestIngredient> getIngredientClass() {
			return TestIngredient.class;
		}
	};

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
		fixture = fixture.withInitialIngredients(baseList);

		// Assertions: both plugin-registered ingredients are exposed by getAllIngredients.
		assertIngredientNumbers(fixture.search().getAllIngredients(), Set.of(0, 1));
	}

	@Test
	public void addAllIndexesExtraIngredientsFromBuilder() {
		// Setup: extra ingredients are added through IngredientManagerBuilder before the manager is built.
		SearchFixture fixture = createFixture(List.of(new TestIngredient(10), new TestIngredient(11)));
		List<IListElementInfo<?>> baseList = IngredientListElementFactory.createBaseList(fixture.ingredientManager(), MOD_ID_HELPER);

		// Operation: add the whole manager-backed list to the search index.
		fixture = fixture.withInitialIngredients(baseList);

		// Assertions: base and extra ingredients all become searchable ingredients.
		assertIngredientNumbers(fixture.search().getAllIngredients(), Set.of(0, 1, 10, 11));
	}

	@Test
	public void addAllCollapsesDuplicateIngredientUids() {
		// Setup: two list elements describe equivalent ingredients with the same UID.
		SearchFixture fixture = createFixture();
		List<IListElementInfo<?>> infos = fixture.createInfos(
			List.of(
				new TestIngredient(1),
				new TestIngredient(1),
				new TestIngredient(2)
			)
		);

		// Operation: index duplicate ingredient UIDs along with a distinct ingredient.
		fixture = fixture.withInitialIngredients(infos);

		// Assertions: getAllIngredients exposes one element per ingredient UID.
		assertIngredientNumbers(fixture.search().getAllIngredients(), Set.of(1, 2));
	}

	@Test
	public void laterDuplicateUidReplacesEarlierElement() {
		// Setup: two elements share an ingredient UID but have different created indexes.
		SearchFixture fixture = createFixture();
		List<IListElementInfo<?>> infos = fixture.createInfos(
			List.of(
				new TestIngredient(3),
				new TestIngredient(3)
			)
		);

		// Operation: index both elements in order.
		fixture = fixture.withInitialIngredients(infos);

		// Assertions: the UID map keeps the later element for direct ingredient lookup.
		IListElement<?> element = fixture.search().getAllIngredients().iterator().next();
		Assertions.assertSame(infos.get(1).getElement(), element);
		Assertions.assertEquals(3, getIngredientNumber(element));
	}

	@Test
	public void addIndexesSingleIngredient() {
		// Setup: one list element has been created outside the manager's base list.
		SearchFixture fixture = createFixture();
		IListElementInfo<?> info = fixture.createInfos(List.of(new TestIngredient(4))).getFirst();

		// Operation: add the single element to the search index.
		fixture.search().add(info, fixture.ingredientManager());

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
		fixture.search().add(infos.get(0), fixture.ingredientManager());
		fixture.search().add(infos.get(1), fixture.ingredientManager());

		// Assertions: both individually added ingredients are retained.
		assertIngredientNumbers(fixture.search().getAllIngredients(), Set.of(5, 6));
	}

	@Test
	public void addCanExtendExistingAddAllResults() {
		// Setup: the index already contains elements added in bulk.
		SearchFixture fixture = createFixture();
		fixture = fixture.withInitialIngredients(fixture.createInfos(List.of(new TestIngredient(7))));
		IListElementInfo<?> extraInfo = fixture.createInfos(List.of(new TestIngredient(8))).getFirst();

		// Operation: add another ingredient through the single-add path.
		fixture.search().add(extraInfo, fixture.ingredientManager());

		// Assertions: existing bulk results and the later single result are both exposed.
		assertIngredientNumbers(fixture.search().getAllIngredients(), Set.of(7, 8));
	}

	@Test
	public void getAllIngredientsIsUnmodifiable() {
		// Setup: at least one ingredient has been indexed.
		SearchFixture fixture = createFixture();
		fixture = fixture.withInitialIngredients(fixture.createInfos(List.of(new TestIngredient(9))));
		Collection<IListElement<?>> allIngredients = fixture.search().getAllIngredients();

		// Operation and assertions: callers cannot mutate the search index through the returned collection.
		Assertions.assertThrows(UnsupportedOperationException.class, allIngredients::clear);
	}

	@Test
	public void findElementUsesIngredientUid() {
		// Setup: the indexed element and lookup value are different instances with the same UID.
		SearchFixture fixture = createFixture();
		fixture = fixture.withInitialIngredients(fixture.createInfos(List.of(new TestIngredient(12))));
		ITypedIngredient<TestIngredient> typedIngredient = fixture.typedIngredient(new TestIngredient(12)).orElseThrow();
		IIngredientHelper<TestIngredient> ingredientHelper = fixture.ingredientManager().getIngredientHelper(TestIngredient.TYPE);

		// Operation: look up the indexed element using the equivalent typed ingredient.
		IListElement<TestIngredient> foundElement = fixture.search().findElement(typedIngredient, ingredientHelper);

		// Assertions: equivalent ingredient UIDs resolve to the indexed element.
		Assertions.assertNotNull(foundElement);
		Assertions.assertEquals(12, foundElement.getTypedIngredient().getIngredient().number());
	}

	@Test
	public void findElementReturnsNullForMissingUid() {
		// Setup: the search index contains a different ingredient UID than the lookup.
		SearchFixture fixture = createFixture();
		fixture = fixture.withInitialIngredients(fixture.createInfos(List.of(new TestIngredient(13))));
		ITypedIngredient<TestIngredient> typedIngredient = fixture.typedIngredient(new TestIngredient(14)).orElseThrow();
		IIngredientHelper<TestIngredient> ingredientHelper = fixture.ingredientManager().getIngredientHelper(TestIngredient.TYPE);

		// Operation: look up a UID that was not indexed.
		IListElement<TestIngredient> foundElement = fixture.search().findElement(typedIngredient, ingredientHelper);

		// Assertions: missing UIDs do not produce a match.
		Assertions.assertNull(foundElement);
	}

	@Test
	public void findElementReturnsNullForDifferentIngredientType() {
		// Setup: the indexed ingredient UID matches the lookup value, but the lookup uses another ingredient type.
		SearchFixture fixture = createFixture();
		fixture = fixture.withInitialIngredients(fixture.createInfos(List.of(new TestIngredient(15))));
		ITypedIngredient<TestIngredient> typedIngredient = new TestTypedIngredient(OTHER_TEST_TYPE, new TestIngredient(15));
		IIngredientHelper<TestIngredient> ingredientHelper = fixture.ingredientManager().getIngredientHelper(TestIngredient.TYPE);

		// Operation: look up the matching UID with a different ingredient type.
		IListElement<TestIngredient> foundElement = fixture.search().findElement(typedIngredient, ingredientHelper);

		// Assertions: type mismatches are not returned even when UIDs are equal.
		Assertions.assertNull(foundElement);
	}

	@Test
	public void emptySearchTokenReturnsNoResults() {
		// Setup: the search index contains an ingredient.
		SearchFixture fixture = createFixture();
		fixture = fixture.withInitialIngredients(fixture.createInfos(List.of(new TestIngredient(16))));

		// Operation: search with an empty token.
		Set<Integer> results = fixture.searchIngredientNumbers("");

		// Assertions: empty tokens are ignored instead of returning every ingredient.
		Assertions.assertTrue(results.isEmpty());
	}

	@Test
	public void displayNameSearchFindsIngredient() {
		// Setup: the display-name searchable contains two different ingredient names.
		SearchFixture fixture = createFixture();
		fixture = fixture.withInitialIngredients(
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
		fixture = fixture.withInitialIngredients(
			fixture.createInfos(List.of(new TestIngredient(19), new TestIngredient(20)))
		);

		// Operation: search by the identifier prefix for one ingredient.
		Set<Integer> results = fixture.searchIngredientNumbers("&jei_test_mod:test_ingredient_20");

		// Assertions: identifier search returns only the matching ingredient.
		Assertions.assertEquals(Set.of(20), results);
	}

	@Test
	public void colorSearchFindsIngredients() {
		// Setup: color search is enabled with a required prefix, and test ingredients expose black as their color.
		SearchFixture fixture = createFixture();
		fixture = fixture.withInitialIngredients(
			fixture.createInfos(List.of(new TestIngredient(21), new TestIngredient(22)))
		);

		// Operation: search by the color prefix.
		Set<Integer> results = fixture.searchIngredientNumbers("^black");

		// Assertions: color search indexes the color names supplied by the color helper.
		Assertions.assertEquals(Set.of(21, 22), results);
	}

	private static SearchFixture createFixture() {
		return createFixture(List.of());
	}

	private static SearchFixture createFixture(Collection<TestIngredient> extraIngredients) {
		IngredientManagerBuilder ingredientManagerBuilder = createIngredientManagerBuilder();
		new TestPlugin().registerIngredients(ingredientManagerBuilder);
		if (!extraIngredients.isEmpty()) {
			ingredientManagerBuilder.addExtraIngredients(TestIngredient.TYPE, extraIngredients);
		}
		IIngredientManager ingredientManager = ingredientManagerBuilder.build();
		ElementPrefixParser elementPrefixParser = new ElementPrefixParser(
			ingredientManager,
			FILTER_CONFIG,
			COLOR_HELPER,
			new ISearchStorageBuilderFactory() {
				@Override
				public <T> ISearchStorageBuilder<T> create() {
					return new SearchStorageBuilderAdapter<>(new GeneralizedSuffixTreeSearchStorage<>());
				}
			}
		);
		return new SearchFixture(ingredientManager, elementPrefixParser, new ElementSearch(elementPrefixParser, List.of(), ingredientManager));
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
		return ingredient.number();
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

		private Optional<ITypedIngredient<TestIngredient>> typedIngredient(TestIngredient ingredient) {
			return ingredientManager.createTypedIngredient(TestIngredient.TYPE, ingredient, false);
		}

		private SearchFixture withInitialIngredients(Collection<IListElementInfo<?>> infos) {
			ElementSearch search = new ElementSearch(elementPrefixParser, infos, ingredientManager);
			return new SearchFixture(ingredientManager, elementPrefixParser, search);
		}

		private Set<Integer> searchIngredientNumbers(String token) {
			ElementPrefixParser.TokenInfo tokenInfo = elementPrefixParser.parseToken(token)
				.orElse(new ElementPrefixParser.TokenInfo("", elementPrefixParser.getNoPrefix()));
			return getIngredientNumbers(search.getSearchResults(tokenInfo));
		}
	}

	private record TestTypedIngredient(
		IIngredientType<TestIngredient> type,
		TestIngredient ingredient
	) implements ITypedIngredient<TestIngredient> {
		@Override
		public ITypedIngredient<TestIngredient> normalize(IIngredientHelper<TestIngredient> ingredientHelper) {
			TestIngredient normalized = ingredientHelper.normalizeIngredient(ingredient);
			return new TestTypedIngredient(type, normalized);
		}

		@Override
		public IIngredientType<TestIngredient> getType() {
			return type;
		}

		@Override
		public TestIngredient getIngredient() {
			return ingredient;
		}
	}
}
