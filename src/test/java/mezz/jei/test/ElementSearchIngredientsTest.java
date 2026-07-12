package mezz.jei.test;

import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.ingredients.IIngredientListElementInfo;
import mezz.jei.ingredients.IngredientBlacklistInternal;
import mezz.jei.ingredients.IngredientListElementFactory;
import mezz.jei.ingredients.IngredientListElementInfo;
import mezz.jei.ingredients.IngredientManager;
import mezz.jei.ingredients.ModIngredientRegistration;
import mezz.jei.ingredients.RegisteredIngredient;
import mezz.jei.ingredients.SubtypeManager;
import mezz.jei.load.registration.SubtypeRegistration;
import mezz.jei.search.ElementPrefixParser;
import mezz.jei.search.ElementSearch;
import mezz.jei.test.lib.TestIngredient;
import mezz.jei.test.lib.TestIngredientFilterConfig;
import mezz.jei.test.lib.TestModIdHelper;
import mezz.jei.test.lib.TestPlugin;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ElementSearchIngredientsTest {
	private static final IModIdHelper MOD_ID_HELPER = new TestModIdHelper();
	private static final TestIngredientFilterConfig FILTER_CONFIG = new TestIngredientFilterConfig();

	@Test
	public void newSearchHasNoIngredients() {
		// Setup: a search index has been created but no elements have been added yet.
		SearchFixture fixture = createFixture();

		// Operation: ask for all indexed ingredients.
		Collection<IIngredientListElementInfo<?>> allIngredients = fixture.search.getAllIngredients();

		// Assertions: the empty search reports no ingredients.
		Assertions.assertTrue(allIngredients.isEmpty());
	}

	@Test
	public void addAllIndexesBaseIngredientsFromBuilder() {
		// Setup: the test plugin registered its base ingredients with the 1.16 ingredient manager.
		SearchFixture fixture = createFixture();
		List<IIngredientListElementInfo<?>> baseList = fixture.createBaseInfos();

		// Operation: add the whole base list to the search index.
		addAll(fixture.search, baseList);

		// Assertions: both plugin-registered ingredients are exposed by getAllIngredients.
		assertIngredientNumbers(fixture.search.getAllIngredients(), numbers(0, 1));
	}

	@Test
	public void addIndexesSingleIngredient() {
		// Setup: one list element has been created outside the manager's base list.
		SearchFixture fixture = createFixture();
		IIngredientListElementInfo<?> info = fixture.createInfos(Collections.singleton(new TestIngredient(4))).get(0);

		// Operation: add the single element to the search index.
		fixture.search.add(info);

		// Assertions: getAllIngredients includes the individually added element.
		assertIngredientNumbers(fixture.search.getAllIngredients(), numbers(4));
	}

	@Test
	public void addIndexesMultipleIngredients() {
		// Setup: two distinct elements will be added individually.
		SearchFixture fixture = createFixture();
		List<IIngredientListElementInfo<?>> infos = fixture.createInfos(
			Arrays.asList(
				new TestIngredient(5),
				new TestIngredient(6)
			)
		);

		// Operation: add each element through the single-add path.
		fixture.search.add(infos.get(0));
		fixture.search.add(infos.get(1));

		// Assertions: both individually added ingredients are retained.
		assertIngredientNumbers(fixture.search.getAllIngredients(), numbers(5, 6));
	}

	@Test
	public void addCanExtendExistingAddAllResults() {
		// Setup: the index already contains elements added in bulk.
		SearchFixture fixture = createFixture();
		addAll(fixture.search, fixture.createInfos(Collections.singleton(new TestIngredient(7))));
		IIngredientListElementInfo<?> extraInfo = fixture.createInfos(Collections.singleton(new TestIngredient(8))).get(0);

		// Operation: add another ingredient through the single-add path.
		fixture.search.add(extraInfo);

		// Assertions: existing bulk results and the later single result are both exposed.
		assertIngredientNumbers(fixture.search.getAllIngredients(), numbers(7, 8));
	}

	@Test
	public void emptySearchTokenReturnsNoResults() {
		// Setup: the search index contains an ingredient.
		SearchFixture fixture = createFixture();
		addAll(fixture.search, fixture.createInfos(Collections.singleton(new TestIngredient(16))));

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
			fixture.search,
			fixture.createInfos(Arrays.asList(new TestIngredient(17), new TestIngredient(18)))
		);

		// Operation: search for the unique suffix of one display name.
		Set<Integer> results = fixture.searchIngredientNumbers("testingredient#17");

		// Assertions: the matching display-name ingredient is returned.
		Assertions.assertEquals(numbers(17), results);
	}

	@Test
	public void identifierSearchFindsIngredient() {
		// Setup: identifier search is enabled and each test ingredient has a stable identifier.
		SearchFixture fixture = createFixture();
		addAll(
			fixture.search,
			fixture.createInfos(Arrays.asList(new TestIngredient(19), new TestIngredient(20)))
		);

		// Operation: search by the identifier prefix for one ingredient.
		Set<Integer> results = fixture.searchIngredientNumbers("&Test Ingredient Resource Id TestIngredient#20");

		// Assertions: identifier search returns only the matching ingredient.
		Assertions.assertEquals(numbers(20), results);
	}

	private static SearchFixture createFixture() {
		TestPlugin testPlugin = new TestPlugin();
		SubtypeRegistration subtypeRegistration = new SubtypeRegistration();
		testPlugin.registerItemSubtypes(subtypeRegistration);
		testPlugin.registerFluidSubtypes(subtypeRegistration);
		SubtypeManager subtypeManager = new SubtypeManager(subtypeRegistration);
		ModIngredientRegistration modIngredientRegistration = new ModIngredientRegistration(subtypeManager);
		testPlugin.registerIngredients(modIngredientRegistration);

		IngredientBlacklistInternal blacklist = new IngredientBlacklistInternal();
		List<RegisteredIngredient<?>> registeredIngredients = modIngredientRegistration.getRegisteredIngredients();
		IngredientManager ingredientManager = new IngredientManager(MOD_ID_HELPER, blacklist, registeredIngredients, true);
		ElementPrefixParser elementPrefixParser = new ElementPrefixParser(ingredientManager, FILTER_CONFIG);
		return new SearchFixture(ingredientManager, elementPrefixParser, new ElementSearch(elementPrefixParser));
	}

	private static void assertIngredientNumbers(Collection<IIngredientListElementInfo<?>> allIngredients, Set<Integer> expectedNumbers) {
		Assertions.assertEquals(expectedNumbers.size(), allIngredients.size());
		Assertions.assertEquals(expectedNumbers, getIngredientNumbers(allIngredients));
	}

	private static void addAll(ElementSearch search, Collection<IIngredientListElementInfo<?>> infos) {
		search.addAll(infos);
	}

	private static Set<Integer> getIngredientNumbers(Collection<IIngredientListElementInfo<?>> allIngredients) {
		return allIngredients.stream()
			.map(ElementSearchIngredientsTest::getIngredientNumber)
			.collect(Collectors.toSet());
	}

	private static int getIngredientNumber(IIngredientListElementInfo<?> info) {
		TestIngredient ingredient = (TestIngredient) info.getElement().getIngredient();
		return ingredient.getNumber();
	}

	private static Set<Integer> numbers(Integer... values) {
		return new HashSet<>(Arrays.asList(values));
	}

	private static final class SearchFixture {
		private final IngredientManager ingredientManager;
		private final ElementPrefixParser elementPrefixParser;
		private final ElementSearch search;

		private SearchFixture(IngredientManager ingredientManager, ElementPrefixParser elementPrefixParser, ElementSearch search) {
			this.ingredientManager = ingredientManager;
			this.elementPrefixParser = elementPrefixParser;
			this.search = search;
		}

		private List<IIngredientListElementInfo<?>> createBaseInfos() {
			return createInfosFromElements(IngredientListElementFactory.createBaseList(ingredientManager));
		}

		private List<IIngredientListElementInfo<?>> createInfos(Collection<TestIngredient> ingredients) {
			List<IIngredientListElement<TestIngredient>> elements = IngredientListElementFactory.createList(
				ingredientManager,
				TestIngredient.TYPE,
				ingredients
			);
			return createInfosFromElements(elements);
		}

		private List<IIngredientListElementInfo<?>> createInfosFromElements(Collection<? extends IIngredientListElement<?>> elements) {
			return elements.stream()
				.map(element -> IngredientListElementInfo.create(element, ingredientManager, MOD_ID_HELPER))
				.filter(Objects::nonNull)
				.collect(Collectors.toCollection(ArrayList::new));
		}

		private Set<Integer> searchIngredientNumbers(String token) {
			Optional<ElementPrefixParser.TokenInfo> parsedToken = elementPrefixParser.parseToken(token);
			ElementPrefixParser.TokenInfo tokenInfo = parsedToken.orElse(new ElementPrefixParser.TokenInfo("", ElementPrefixParser.NO_PREFIX));
			return getIngredientNumbers(search.getSearchResults(tokenInfo));
		}
	}
}
