package mezz.jei.test;

import mezz.jei.common.gui.ingredients.IListElement;
import mezz.jei.common.ingredients.IListElementInfo;
import mezz.jei.common.ingredients.IngredientListElementFactory;
import mezz.jei.common.ingredients.ListElementInfo;
import mezz.jei.common.ingredients.RegisteredIngredients;
import mezz.jei.common.ingredients.subtypes.SubtypeManager;
import mezz.jei.common.load.registration.RegisteredIngredientsBuilder;
import mezz.jei.common.load.registration.SubtypeRegistration;
import mezz.jei.common.search.ElementPrefixParser;
import mezz.jei.common.search.ElementSearch;
import mezz.jei.test.lib.TestIngredient;
import mezz.jei.test.lib.TestIngredientFilterConfig;
import mezz.jei.test.lib.TestModIdHelper;
import mezz.jei.test.lib.TestPlugin;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ElementSearchIngredientsTest {
	private static final TestModIdHelper MOD_ID_HELPER = new TestModIdHelper();
	private static final TestIngredientFilterConfig FILTER_CONFIG = new TestIngredientFilterConfig();

	@Test
	public void newSearchHasNoIngredients() {
		// Setup: a search index has been created but no elements have been added yet.
		SearchFixture fixture = createFixture();

		// Operation: ask for all indexed ingredients.
		Collection<IListElementInfo<?>> allIngredients = fixture.search().getAllIngredients();

		// Assertions: the empty search reports no ingredients.
		Assertions.assertTrue(allIngredients.isEmpty());
	}

	@Test
	public void addAllIndexesBaseIngredientsFromBuilder() {
		// Setup: RegisteredIngredientsBuilder registered the base test plugin ingredients.
		SearchFixture fixture = createFixture();
		List<IListElementInfo<?>> baseList = fixture.createBaseInfos();

		// Operation: add the whole base list to the search index.
		addAll(fixture.search(), baseList);

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
		addAll(fixture.search(), fixture.createInfos(List.of(new TestIngredient(7))));
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
		addAll(fixture.search(), fixture.createInfos(List.of(new TestIngredient(16))));

		// Operation: search with an empty token.
		Set<Integer> results = fixture.searchIngredientNumbers("");

		// Assertions: empty tokens are ignored instead of returning every ingredient.
		Assertions.assertTrue(results.isEmpty());
	}

	@Test
	public void displayNameSearchFindsIngredient() {
		// Setup: the display-name searchable contains two different ingredient names.
		SearchFixture fixture = createFixture();
		addAll(
			fixture.search(),
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
		addAll(
			fixture.search(),
			fixture.createInfos(List.of(new TestIngredient(19), new TestIngredient(20)))
		);

		// Operation: search by the identifier prefix for one ingredient.
		Set<Integer> results = fixture.searchIngredientNumbers("&jei_test_mod:test_ingredient_20");

		// Assertions: identifier search returns only the matching ingredient.
		Assertions.assertEquals(Set.of(20), results);
	}

	private static SearchFixture createFixture() {
		TestPlugin testPlugin = new TestPlugin();
		RegisteredIngredientsBuilder registeredIngredientsBuilder = createRegisteredIngredientsBuilder(testPlugin);
		testPlugin.registerIngredients(registeredIngredientsBuilder);
		RegisteredIngredients registeredIngredients = registeredIngredientsBuilder.build();
		ElementPrefixParser elementPrefixParser = new ElementPrefixParser(registeredIngredients, FILTER_CONFIG);
		return new SearchFixture(registeredIngredients, elementPrefixParser, new ElementSearch(elementPrefixParser));
	}

	private static RegisteredIngredientsBuilder createRegisteredIngredientsBuilder(TestPlugin testPlugin) {
		SubtypeRegistration subtypeRegistration = new SubtypeRegistration();
		testPlugin.registerItemSubtypes(subtypeRegistration);
		SubtypeManager subtypeManager = new SubtypeManager(subtypeRegistration);
		return new RegisteredIngredientsBuilder(subtypeManager);
	}

	private static void assertIngredientNumbers(Collection<IListElementInfo<?>> allIngredients, Set<Integer> expectedNumbers) {
		Assertions.assertEquals(expectedNumbers.size(), allIngredients.size());
		Assertions.assertEquals(expectedNumbers, getIngredientNumbers(allIngredients));
	}

	private static void addAll(ElementSearch search, Collection<IListElementInfo<?>> infos) {
		infos.forEach(search::add);
	}

	private static Set<Integer> getIngredientNumbers(Collection<IListElementInfo<?>> allIngredients) {
		return allIngredients.stream()
			.map(ElementSearchIngredientsTest::getIngredientNumber)
			.collect(Collectors.toSet());
	}

	private static int getIngredientNumber(IListElementInfo<?> info) {
		TestIngredient ingredient = (TestIngredient) info.getTypedIngredient().getIngredient();
		return ingredient.getNumber();
	}

	private record SearchFixture(
		RegisteredIngredients registeredIngredients,
		ElementPrefixParser elementPrefixParser,
		ElementSearch search
	) {
		private List<IListElementInfo<?>> createBaseInfos() {
			return createInfosFromElements(IngredientListElementFactory.createBaseList(registeredIngredients));
		}

		private List<IListElementInfo<?>> createInfos(Collection<TestIngredient> ingredients) {
			List<IListElement<TestIngredient>> elements = IngredientListElementFactory.createList(
				registeredIngredients,
				TestIngredient.TYPE,
				ingredients
			);
			return createInfosFromElements(elements);
		}

		private List<IListElementInfo<?>> createInfosFromElements(Collection<? extends IListElement<?>> elements) {
			return elements.stream()
				.map(element -> ListElementInfo.create(element, registeredIngredients, MOD_ID_HELPER))
				.filter(Objects::nonNull)
				.collect(Collectors.toCollection(ArrayList::new));
		}

		private Set<Integer> searchIngredientNumbers(String token) {
			ElementPrefixParser.TokenInfo tokenInfo = elementPrefixParser.parseToken(token)
				.orElse(new ElementPrefixParser.TokenInfo("", ElementPrefixParser.NO_PREFIX));
			return getIngredientNumbers(search.getSearchResults(tokenInfo));
		}
	}
}
