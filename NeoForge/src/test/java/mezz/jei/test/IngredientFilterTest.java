package mezz.jei.test;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.IAdvancedSearchRegistration;
import mezz.jei.api.runtime.IEditModeConfig;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.api.search.ISearchStorageBuilder;
import mezz.jei.api.search.ISearchStorageBuilderFactory;
import mezz.jei.api.search.ISearchStorage;
import mezz.jei.api.search.ISearchStorageFactory;
import mezz.jei.common.Internal;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IClientConfigs;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.config.IIngredientFilterConfig;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.config.ClientToggleState;
import mezz.jei.common.config.SearchMode;
import mezz.jei.common.config.IngredientSortStage;
import mezz.jei.common.util.Translator;
import mezz.jei.gui.filter.FilterTextSource;
import mezz.jei.gui.filter.IFilterTextSource;
import mezz.jei.gui.config.IngredientTypeSortingConfig;
import mezz.jei.gui.config.ModNameSortingConfig;
import mezz.jei.gui.ingredients.IListElementInfo;
import mezz.jei.gui.ingredients.IngredientFilter;
import mezz.jei.gui.ingredients.IngredientListElementFactory;
import mezz.jei.gui.ingredients.IngredientSorter;
import mezz.jei.gui.ingredients.ListElementInfo;
import mezz.jei.library.config.EditModeConfig;
import mezz.jei.library.ingredients.IngredientBlacklistInternal;
import mezz.jei.library.ingredients.IngredientVisibility;
import mezz.jei.library.ingredients.subtypes.SubtypeInterpreters;
import mezz.jei.library.ingredients.subtypes.SubtypeManager;
import mezz.jei.library.load.PluginLoader;
import mezz.jei.library.load.registration.IngredientManagerBuilder;
import mezz.jei.test.lib.TestClientConfig;
import mezz.jei.test.lib.TestColorHelper;
import mezz.jei.test.lib.TestIngredient;
import mezz.jei.test.lib.TestIngredientFilterConfig;
import mezz.jei.test.lib.TestIngredientHelper;
import mezz.jei.test.lib.TestModIdHelper;
import mezz.jei.test.lib.TestPlugin;
import net.mezzdev.config.api.sorting.ISortingConfig;
import net.mezzdev.config.file.ConfigFileWatcherSettings;
import net.mezzdev.config.file.ConfigManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.resources.Identifier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class IngredientFilterTest {
	private static final int EXTRA_INGREDIENT_COUNT = 5;
	@Nullable
	private IIngredientManager ingredientManager;
	@Nullable
	private IngredientFilter ingredientFilter;
	@Nullable
	private IIngredientVisibility ingredientVisibility;
	@Nullable
	private List<IListElementInfo<?>> baseList;
	@Nullable
	private EditModeConfig editModeConfig;
	@Nullable
	private FilterTextSource filterTextSource;
	@Nullable
	private IModIdHelper modIdHelper;
	@Nullable
	private TestIngredientFilterConfig ingredientFilterConfig;
	@Nullable
	private ClientToggleState toggleState;
	@Nullable
	private ISortingConfig<String> ingredientTypeSortingConfig;
	@Nullable
	private RecordingSearchStorageBuilderFactory searchStorageBuilderFactory;

	private static final TestIngredient ALIASED_INGREDIENT = new TestIngredient(0);
	private static final String INGREDIENT_ALIAS = "aliasedingredientzero";
	private static final String ALIASED_MOD_ID = "jei_test_mod";
	private static final String MOD_ALIAS = "testmodalias";

	@BeforeEach
	public void setup() {
		setupFilter();
	}

	@AfterEach
	public void tearDown() {
		Internal.getOptionalClientConfigs().ifPresent(IClientConfigs::onRuntimeStopped);
	}

	private void setupFilter() {
		setupFilter(null, SearchBackend.LUCENE);
	}

	private void setupFilter(@Nullable DynamicIngredientData data, SearchBackend backend) {
		TestPlugin testPlugin = new TestPlugin();

		SubtypeInterpreters subtypeInterpreters = new SubtypeInterpreters();
		SubtypeManager subtypeManager = new SubtypeManager(subtypeInterpreters);

		IColorHelper colorHelper = new TestColorHelper();
		IngredientManagerBuilder ingredientManagerBuilder = new IngredientManagerBuilder(
			subtypeManager,
			colorHelper,
			new ContextMap.Builder().create(new ContextKeySet.Builder().build())
		);
		if (data == null) {
			testPlugin.registerIngredients(ingredientManagerBuilder);
		} else {
			ingredientManagerBuilder.register(TestIngredient.TYPE, List.of(new TestIngredient(0), new TestIngredient(1)), data, data, TestIngredient.CODEC);
		}
		ingredientManagerBuilder.addAlias(TestIngredient.TYPE, ALIASED_INGREDIENT, INGREDIENT_ALIAS);
		this.ingredientManager = ingredientManagerBuilder.build();

		IngredientBlacklistInternal blacklist = new IngredientBlacklistInternal(ingredientManager);
		this.modIdHelper = new TestModIdHelper() {
			@Override
			public Set<String> getModAliases(String modId) {
				if (ALIASED_MOD_ID.equals(modId)) {
					return Set.of(MOD_ALIAS);
				}
				return Set.of();
			}
		};
		IClientConfig clientConfig = new TestClientConfig();
		clientConfig.ingredientSorterStages().set(List.of(IngredientSortStage.ALPHABETICAL, IngredientSortStage.CREATIVE_MENU));

		this.ingredientFilterConfig = new TestIngredientFilterConfig();
		TestIngredientFilterConfig ingredientFilterConfig = this.ingredientFilterConfig;
		Internal.getOptionalClientConfigs().ifPresent(IClientConfigs::onRuntimeStopped);
		Internal.setClientConfigs(new TestClientConfigs(clientConfig, ingredientFilterConfig));

		this.baseList = IngredientListElementFactory.createBaseList(ingredientManager, ingredientFilterConfig, modIdHelper);

		this.editModeConfig = new EditModeConfig(new NullSerializer(), ingredientManager);

		this.toggleState = new ClientToggleState();
		IClientToggleState toggleState = this.toggleState;
		this.ingredientTypeSortingConfig = createSortingConfig();

		this.ingredientVisibility = new IngredientVisibility(blacklist, toggleState, editModeConfig, ingredientManager);
		this.filterTextSource = new FilterTextSource();
		IModPlugin searchPlugin = new IModPlugin() {
			@Override
			public Identifier getPluginUid() {
				return Identifier.fromNamespaceAndPath("jei_test", "prefix_search");
			}

			@Override
			public void registerAdvancedSearch(IAdvancedSearchRegistration registration) {
				switch (backend) {
					case LUCENE -> {}
					case CUSTOM_STORAGE -> registration.replaceSearchStorage((ISearchStorageFactory) PrefixSearchStorage::new);
					case CUSTOM_BUILDER -> registration.replaceSearchStorage((ISearchStorageBuilderFactory) PrefixSearchStorage::new);
				}
			}
		};
		this.searchStorageBuilderFactory = new RecordingSearchStorageBuilderFactory(PluginLoader.createSearchStorageFactory(List.of(searchPlugin)));
		this.ingredientFilter = new IngredientFilter(
			filterTextSource,
			clientConfig,
			ingredientFilterConfig,
			ingredientManager,
			ingredients -> IngredientSorter.sortIngredients(clientConfig, new ModNameSortingConfig(createSortingConfig()),
				new IngredientTypeSortingConfig(ingredientTypeSortingConfig), ingredientManager, ingredients),
			baseList,
			modIdHelper,
			ingredientVisibility,
			new IngredientTypeSortingConfig(ingredientTypeSortingConfig),
			colorHelper,
			searchStorageBuilderFactory,
			toggleState
		);
		searchStorageBuilderFactory.clearCreatedIds();
		Internal.registerRuntimeListenerRemoval(ingredientTypeSortingConfig.addChangeListener(ingredientFilter::onIngredientTypeSortOrderConfigChanged));

		this.ingredientManager.registerIngredientListener(blacklist);
		this.ingredientManager.registerIngredientListener(ingredientFilter);

		this.ingredientVisibility.registerListener(this.ingredientFilter);
	}

	@Test
	public void testSetup() {
		Assertions.assertNotNull(ingredientFilter);

		List<?> ingredientList = ingredientFilter.getElements();
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, ingredientList.size());
	}

	@ParameterizedTest
	@EnumSource(SearchBackend.class)
	public void refreshCoalescesBatchesAndOnlyReadsChangedIngredients(SearchBackend backend) {
		DynamicIngredientData data = new DynamicIngredientData();
		setupFilter(data, backend);
		Assertions.assertNotNull(ingredientManager);
		Assertions.assertNotNull(ingredientFilter);
		Assertions.assertNotNull(searchStorageBuilderFactory);
		data.nameReads.clear();
		data.tooltipReads.clear();
		data.discover(0, "Aer");
		for (int i = 0; i < 10; i++) {
			ingredientManager.refreshIngredientsAtRuntime(TestIngredient.TYPE, List.of(new TestIngredient(0), new TestIngredient(0)));
		}
		Assertions.assertTrue(data.nameReads.isEmpty());
		Assertions.assertTrue(data.tooltipReads.isEmpty());
		Assertions.assertEquals(List.of(new TestIngredient(0), new TestIngredient(1)), ingredientFilter.getFilteredIngredients(TestIngredient.TYPE));
		Assertions.assertEquals(List.of(0), data.nameReads);
		Assertions.assertEquals(List.of(0), data.tooltipReads);
		Assertions.assertTrue(searchStorageBuilderFactory.getAndClearCreatedIds().isEmpty());
	}

	@ParameterizedTest
	@EnumSource(SearchBackend.class)
	public void refreshReplacesSearchTermsSortingAndSuggestions(SearchBackend backend) {
		DynamicIngredientData data = new DynamicIngredientData();
		setupFilter(data, backend);
		Assertions.assertNotNull(ingredientManager);
		Assertions.assertNotNull(ingredientFilter);
		Assertions.assertNotNull(filterTextSource);
		Assertions.assertNotNull(ingredientFilterConfig);
		Assertions.assertNotNull(searchStorageBuilderFactory);
		ingredientFilterConfig.searchIngredientAliases().set(true);
		Assertions.assertEquals(List.of(new TestIngredient(1), new TestIngredient(0)), ingredientFilter.getFilteredIngredients(TestIngredient.TYPE));
		IListElementInfo<?> original = ingredientFilter.getAllElementInfos().stream()
			.filter(i -> i.getTypedIngredient().getIngredient().equals(new TestIngredient(0)))
			.findFirst().orElseThrow();
		filterTextSource.setFilterText("mysterious");
		Assertions.assertEquals(List.of(new TestIngredient(0)), ingredientFilter.getFilteredIngredients(TestIngredient.TYPE));
		searchStorageBuilderFactory.clearCreatedIds();
		long revision = ingredientFilter.getCompletionRevision();

		data.discover(0, "Aer");
		ingredientManager.refreshIngredientsAtRuntime(TestIngredient.TYPE, List.of(new TestIngredient(0)));
		Assertions.assertTrue(ingredientFilter.getCompletionRevision() > revision);
		IListElementInfo<?> updated = ingredientFilter.getAllElementInfos().stream()
			.filter(i -> i.getElement() == original.getElement())
			.findFirst().orElseThrow();
		Assertions.assertEquals(List.of("aer crystal", INGREDIENT_ALIAS), updated.getNames());
		Assertions.assertSame(original.getTypedIngredient(), updated.getTypedIngredient());
		Assertions.assertTrue(ingredientFilter.getFilteredIngredients(TestIngredient.TYPE).isEmpty());
		for (String search : List.of("AER", "$aer", "#aer", INGREDIENT_ALIAS)) {
			filterTextSource.setFilterText(search);
			Assertions.assertEquals(List.of(new TestIngredient(0)), ingredientFilter.getFilteredIngredients(TestIngredient.TYPE), search);
		}
		for (String search : List.of("$mysterious", "#mysterious")) {
			filterTextSource.setFilterText(search);
			Assertions.assertTrue(ingredientFilter.getFilteredIngredients(TestIngredient.TYPE).isEmpty(), search);
		}
		filterTextSource.setFilterText("er crystal");
		if (backend != SearchBackend.LUCENE) {
			Assertions.assertTrue(ingredientFilter.getFilteredIngredients(TestIngredient.TYPE).isEmpty());
		} else {
			Assertions.assertEquals(List.of(new TestIngredient(0)), ingredientFilter.getFilteredIngredients(TestIngredient.TYPE));
		}
		filterTextSource.setFilterText("");
		Assertions.assertEquals(List.of(new TestIngredient(0), new TestIngredient(1)), ingredientFilter.getFilteredIngredients(TestIngredient.TYPE));
		Assertions.assertTrue(searchStorageBuilderFactory.getAndClearCreatedIds().isEmpty());
	}

	@ParameterizedTest
	@EnumSource(value = SearchBackend.class, names = {"CUSTOM_STORAGE", "CUSTOM_BUILDER"})
	public void repeatedRefreshesCompactCustomStorageWithoutRevivingOldNames(SearchBackend backend) {
		DynamicIngredientData data = new DynamicIngredientData();
		setupFilter(data, backend);
		Assertions.assertNotNull(ingredientManager);
		Assertions.assertNotNull(ingredientFilter);
		Assertions.assertNotNull(filterTextSource);
		Assertions.assertNotNull(searchStorageBuilderFactory);
		TestIngredient changed = new TestIngredient(0);
		for (int i = 0; i < 1100; i++) {
			data.nameReads.clear();
			String name = "updated_" + i + "_end";
			data.discover(0, name);
			ingredientManager.refreshIngredientsAtRuntime(TestIngredient.TYPE, List.of(changed));
			filterTextSource.setFilterText("$" + name);
			Assertions.assertEquals(List.of(changed), ingredientFilter.getFilteredIngredients(TestIngredient.TYPE));
			Assertions.assertEquals(List.of(0), data.nameReads);
		}
		Assertions.assertEquals(List.of("unprefixed", "tags", "tooltips", "identifiers"), searchStorageBuilderFactory.getAndClearCreatedIds());
		filterTextSource.setFilterText("#updated_1099_end");
		Assertions.assertEquals(List.of(changed), ingredientFilter.getFilteredIngredients(TestIngredient.TYPE));
		for (String search : List.of("mysterious", "$mysterious", "#mysterious", "updated_0_end", "$updated_0_end", "#updated_0_end")) {
			filterTextSource.setFilterText(search);
			Assertions.assertTrue(ingredientFilter.getElements().isEmpty(), search);
		}
		filterTextSource.setFilterText("$ignis");
		Assertions.assertEquals(List.of(new TestIngredient(1)), ingredientFilter.getFilteredIngredients(TestIngredient.TYPE));
		filterTextSource.setFilterText("$pdated");
		Assertions.assertTrue(ingredientFilter.getElements().isEmpty());
	}

	@ParameterizedTest
	@EnumSource(SearchBackend.class)
	public void refreshPreservesOtherIngredientsAndRuntimeAdditions(SearchBackend backend) {
		DynamicIngredientData data = new DynamicIngredientData();
		setupFilter(data, backend);
		Assertions.assertNotNull(ingredientManager);
		Assertions.assertNotNull(ingredientFilter);
		Assertions.assertNotNull(filterTextSource);
		TestIngredient changed = new TestIngredient(0);
		TestIngredient unchanged = new TestIngredient(1);
		TestIngredient added = new TestIngredient(2);
		data.discover(2, "Ignis");
		ingredientManager.addIngredientsAtRuntime(TestIngredient.TYPE, List.of(added));

		for (String name : List.of("Aer", "Terra")) {
			data.discover(0, name);
			ingredientManager.refreshIngredientsAtRuntime(TestIngredient.TYPE, List.of(changed));
			for (String prefix : List.of("", "$", "#")) {
				filterTextSource.setFilterText(prefix + name);
				Assertions.assertEquals(List.of(changed), ingredientFilter.getFilteredIngredients(TestIngredient.TYPE));
				filterTextSource.setFilterText(prefix + "ignis");
				Assertions.assertEquals(List.of(unchanged, added), ingredientFilter.getFilteredIngredients(TestIngredient.TYPE));
			}
		}

		data.discover(2, "Aqua");
		ingredientManager.refreshIngredientsAtRuntime(TestIngredient.TYPE, List.of(added));
		for (String prefix : List.of("", "$", "#")) {
			filterTextSource.setFilterText(prefix + "aqua");
			Assertions.assertEquals(List.of(added), ingredientFilter.getFilteredIngredients(TestIngredient.TYPE));
			filterTextSource.setFilterText(prefix + "ignis");
			Assertions.assertEquals(List.of(unchanged), ingredientFilter.getFilteredIngredients(TestIngredient.TYPE));
			for (String oldName : List.of("mysterious", "aer")) {
				filterTextSource.setFilterText(prefix + oldName);
				Assertions.assertTrue(ingredientFilter.getElements().isEmpty());
			}
		}
	}

	@ParameterizedTest
	@EnumSource(SearchBackend.class)
	public void refreshUsesTheCurrentRegisteredInstanceAfterReregistration(SearchBackend backend) {
		TestIngredient replacement = new TestIngredient(0);
		DynamicIngredientData data = new DynamicIngredientData() {
			@Override
			public String getDisplayName(TestIngredient ingredient) {
				if (ingredient == replacement) {
					return "Replacement Crystal";
				}
				return super.getDisplayName(ingredient);
			}
		};
		setupFilter(data, backend);
		Assertions.assertNotNull(ingredientManager);
		Assertions.assertNotNull(ingredientFilter);
		Assertions.assertNotNull(filterTextSource);
		ingredientManager.removeIngredientsAtRuntime(TestIngredient.TYPE, List.of(new TestIngredient(0)));
		ingredientManager.addIngredientsAtRuntime(TestIngredient.TYPE, List.of(replacement));
		ingredientManager.refreshIngredientsAtRuntime(TestIngredient.TYPE, List.of(new TestIngredient(0)));
		filterTextSource.setFilterText("replacement");
		List<TestIngredient> results = ingredientFilter.getFilteredIngredients(TestIngredient.TYPE);
		Assertions.assertEquals(1, results.size());
		Assertions.assertSame(replacement, results.getFirst());
		filterTextSource.setFilterText("\"mysterious crystal\"");
		Assertions.assertTrue(ingredientFilter.getElements().isEmpty());
	}

	@Test
	public void refreshPreservesVisibilityAndIgnoresUnregisteredIngredients() {
		DynamicIngredientData data = new DynamicIngredientData();
		setupFilter(data, SearchBackend.LUCENE);
		Assertions.assertNotNull(ingredientManager);
		Assertions.assertNotNull(ingredientFilter);
		Assertions.assertNotNull(ingredientVisibility);
		Assertions.assertNotNull(filterTextSource);
		TestIngredient hidden = new TestIngredient(0);
		TestIngredient removed = new TestIngredient(1);
		ingredientVisibility.hideIngredients(TestIngredient.TYPE, List.of(hidden), Set.of(UidContext.Ingredient));
		ingredientManager.removeIngredientsAtRuntime(TestIngredient.TYPE, List.of(removed));
		long revision = ingredientFilter.getCompletionRevision();
		ingredientManager.refreshIngredientsAtRuntime(TestIngredient.TYPE, List.of());
		ingredientManager.refreshIngredientsAtRuntime(TestIngredient.TYPE, List.of(removed, new TestIngredient(99), new TestIngredient(-1)));
		Assertions.assertEquals(revision, ingredientFilter.getCompletionRevision());
		data.discover(0, "Aer");
		ingredientManager.refreshIngredientsAtRuntime(TestIngredient.TYPE, List.of(hidden));
		filterTextSource.setFilterText("aer");
		Assertions.assertTrue(ingredientFilter.getElements().isEmpty());
		ingredientVisibility.unhideIngredients(TestIngredient.TYPE, List.of(hidden), Set.of(UidContext.Ingredient));
		Assertions.assertEquals(List.of(hidden), ingredientFilter.getFilteredIngredients(TestIngredient.TYPE));
		Assertions.assertEquals(List.of(hidden), List.copyOf(ingredientManager.getAllIngredients(TestIngredient.TYPE)));
	}

	@ParameterizedTest
	@EnumSource(SearchBackend.class)
	public void refreshSurvivesConfigRebuildsAndReloads(SearchBackend backend) {
		DynamicIngredientData data = new DynamicIngredientData();
		setupFilter(data, backend);
		Assertions.assertNotNull(ingredientManager);
		Assertions.assertNotNull(ingredientFilter);
		Assertions.assertNotNull(ingredientFilterConfig);
		Assertions.assertNotNull(filterTextSource);
		Assertions.assertNotNull(searchStorageBuilderFactory);
		TestIngredient ingredient = new TestIngredient(0);
		data.nameReads.clear();
		data.tooltipReads.clear();
		data.discover(0, "Aer");
		ingredientManager.refreshIngredientsAtRuntime(TestIngredient.TYPE, List.of(ingredient));
		ingredientFilterConfig.searchIngredientAliases().set(true);
		ingredientFilterConfig.tooltipSearchMode().set(SearchMode.DISABLED);
		filterTextSource.setFilterText("aer");
		Assertions.assertEquals(List.of(ingredient), ingredientFilter.getFilteredIngredients(TestIngredient.TYPE));
		Assertions.assertEquals(List.of(0), data.nameReads);
		Assertions.assertTrue(data.tooltipReads.isEmpty());
		Assertions.assertEquals(List.of("unprefixed", "tooltips"), searchStorageBuilderFactory.getAndClearCreatedIds());
		filterTextSource.setFilterText(INGREDIENT_ALIAS);
		Assertions.assertEquals(List.of(ingredient), ingredientFilter.getFilteredIngredients(TestIngredient.TYPE));

		data.nameReads.clear();
		data.tooltipReads.clear();
		data.discover(0, "Terra");
		ingredientManager.refreshIngredientsAtRuntime(TestIngredient.TYPE, List.of(ingredient));
		ingredientFilterConfig.searchIngredientAliases().set(false);
		ingredientFilterConfig.tooltipSearchMode().set(SearchMode.REQUIRE_PREFIX);
		filterTextSource.setFilterText("$terra");
		Assertions.assertEquals(List.of(ingredient), ingredientFilter.getFilteredIngredients(TestIngredient.TYPE));
		Assertions.assertEquals(List.of(0), data.nameReads);
		Assertions.assertEquals(List.of(0, 1), data.tooltipReads.stream().sorted().toList());
		Assertions.assertEquals(List.of("unprefixed", "tooltips"), searchStorageBuilderFactory.getAndClearCreatedIds());
		filterTextSource.setFilterText(INGREDIENT_ALIAS);
		Assertions.assertTrue(ingredientFilter.getElements().isEmpty());
		filterTextSource.setFilterText("aer");
		Assertions.assertTrue(ingredientFilter.getElements().isEmpty());
		data.discover(0, "Aqua");
		ingredientManager.refreshIngredientsAtRuntime(TestIngredient.TYPE, List.of(ingredient));
		ingredientFilterConfig.searchIngredientAliases().set(true);
		ingredientFilter.rebuildItemFilter();
		data.nameReads.clear();
		searchStorageBuilderFactory.clearCreatedIds();
		filterTextSource.setFilterText("$aqua");
		Assertions.assertEquals(List.of(ingredient), ingredientFilter.getFilteredIngredients(TestIngredient.TYPE));
		Assertions.assertTrue(data.nameReads.isEmpty());
		Assertions.assertTrue(searchStorageBuilderFactory.getAndClearCreatedIds().isEmpty());
		filterTextSource.setFilterText("$qua");
		if (backend == SearchBackend.LUCENE) {
			Assertions.assertEquals(List.of(ingredient), ingredientFilter.getFilteredIngredients(TestIngredient.TYPE));
		} else {
			Assertions.assertTrue(ingredientFilter.getFilteredIngredients(TestIngredient.TYPE).isEmpty());
		}
	}

	@Test
	public void testRuntimeIngredientsRemainSearchableAfterReload() {
		Assertions.assertNotNull(ingredientFilter);
		Assertions.assertNotNull(ingredientManager);
		Assertions.assertNotNull(filterTextSource);
		TestIngredient ingredient = new TestIngredient(42);
		ingredientManager.addIngredientsAtRuntime(TestIngredient.TYPE, List.of(ingredient));
		filterTextSource.setFilterText("testingredient#42");
		Assertions.assertEquals(List.of(ingredient), ingredientFilter.getFilteredIngredients(TestIngredient.TYPE));

		ingredientFilter.rebuildItemFilter();
		Assertions.assertEquals(List.of(ingredient), ingredientFilter.getFilteredIngredients(TestIngredient.TYPE));
	}

	@ParameterizedTest
	@CsvSource({
		"tr-TR, en-US, ingredient",
		"en-US, tr-TR, ıngredient"
	})
	public void testSearchTextIsLowercasedWithTheSameLocaleAsTheSearchIndex(String systemLanguageTag, String minecraftLanguageTag, String expectedLowercase) {
		Locale defaultLocale = Locale.getDefault();
		try {
			Locale.setDefault(Locale.forLanguageTag(systemLanguageTag));
			Locale minecraftLocale = Locale.forLanguageTag(minecraftLanguageTag);
			Translator.setLocaleSupplier(() -> minecraftLocale);
			// Build the search index with the same language that JEI uses for the query.
			setupFilter();
			Assertions.assertNotNull(ingredientFilter);
			Assertions.assertNotNull(filterTextSource);
			Assertions.assertNotNull(ingredientFilterConfig);
			// Lowercase ASCII in identifiers or tooltips must not mask a mismatched display-name query.
			ingredientFilterConfig.identifierSearchMode().set(SearchMode.REQUIRE_PREFIX);
			ingredientFilterConfig.tooltipSearchMode().set(SearchMode.REQUIRE_PREFIX);

			String filterText = "Ingredient";
			Assertions.assertEquals(expectedLowercase, Translator.toLowercaseWithLocale(filterText));
			filterTextSource.setFilterText(filterText);
			Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, ingredientFilter.getElements().size());
		} finally {
			Translator.setLocaleSupplier(() -> Locale.ROOT);
			Locale.setDefault(defaultLocale);
		}
	}

	@Test
	public void testIngredientTypeVisibilityChangesApplyToExistingAndNewIngredients() {
		Assertions.assertNotNull(ingredientFilter);
		Assertions.assertNotNull(ingredientTypeSortingConfig);
		Assertions.assertNotNull(ingredientManager);

		String typeId = IngredientTypeSortingConfig.getIngredientTypeString(TestIngredient.TYPE);
		ingredientTypeSortingConfig.setSortedValues(List.of(typeId), List.of());
		Assertions.assertTrue(ingredientFilter.getElements().isEmpty());

		TestIngredient added = new TestIngredient(TestPlugin.BASE_INGREDIENT_COUNT);
		ingredientManager.addIngredientsAtRuntime(TestIngredient.TYPE, List.of(added));
		Assertions.assertTrue(ingredientFilter.getElements().isEmpty());

		ingredientTypeSortingConfig.setSortedValues(List.of(typeId), List.of(typeId));
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT + 1, ingredientFilter.getElements().size());
		Assertions.assertTrue(ingredientFilter.getFilteredIngredients(TestIngredient.TYPE).contains(added));
	}

	@Test
	public void testAddingAndRemovingIngredients() {
		Assertions.assertNotNull(ingredientFilter);
		Assertions.assertNotNull(ingredientManager);
		Assertions.assertNotNull(ingredientVisibility);
		Assertions.assertNotNull(filterTextSource);
		Assertions.assertNotNull(modIdHelper);
		Assertions.assertNotNull(ingredientFilterConfig);

		List<TestIngredient> ingredients = createIngredients();

		addIngredients(ingredientFilter, filterTextSource, ingredientVisibility, ingredientManager, modIdHelper, ingredientFilterConfig, ingredients);
		removeIngredients(ingredientFilter, filterTextSource, ingredientVisibility, ingredientManager, modIdHelper, ingredientFilterConfig, ingredients);
		addIngredients(ingredientFilter, filterTextSource, ingredientVisibility, ingredientManager, modIdHelper, ingredientFilterConfig, ingredients);
	}

	@Test
	public void testHidingIngredientsInMultipleContexts() {
		Assertions.assertNotNull(ingredientFilter);
		Assertions.assertNotNull(ingredientManager);
		Assertions.assertNotNull(ingredientVisibility);
		Assertions.assertNotNull(filterTextSource);
		Assertions.assertNotNull(modIdHelper);
		Assertions.assertNotNull(ingredientFilterConfig);

		List<TestIngredient> ingredients = createIngredients();
		addIngredients(ingredientFilter, filterTextSource, ingredientVisibility, ingredientManager, modIdHelper, ingredientFilterConfig, ingredients);

		ingredientVisibility.hideIngredients(
			TestIngredient.TYPE,
			ingredients,
			Set.of(
				UidContext.Ingredient,
				UidContext.Recipe
			)
		);

		filterTextSource.setFilterText("");
		List<TestIngredient> filteredIngredients = ingredientFilter.getFilteredIngredients(TestIngredient.TYPE);
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, filteredIngredients.size());
		for (TestIngredient ingredient : ingredients) {
			Assertions.assertFalse(filteredIngredients.contains(ingredient));
			Assertions.assertFalse(ingredientVisibility.isIngredientVisible(
				TestIngredient.TYPE,
				ingredient,
				UidContext.Ingredient
			));
			Assertions.assertFalse(ingredientVisibility.isIngredientVisible(
				TestIngredient.TYPE,
				ingredient,
				UidContext.Recipe
			));
		}

		Collection<TestIngredient> registeredIngredients = ingredientManager.getAllIngredients(TestIngredient.TYPE);
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT + EXTRA_INGREDIENT_COUNT, registeredIngredients.size());
		Assertions.assertTrue(registeredIngredients.containsAll(ingredients));

		ingredientFilter.updateHidden();
		filteredIngredients = ingredientFilter.getFilteredIngredients(TestIngredient.TYPE);
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, filteredIngredients.size());

		ingredientVisibility.unhideIngredients(
			TestIngredient.TYPE,
			ingredients,
			Set.of(UidContext.Ingredient)
		);
		filteredIngredients = ingredientFilter.getFilteredIngredients(TestIngredient.TYPE);
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT + EXTRA_INGREDIENT_COUNT, filteredIngredients.size());
		for (TestIngredient ingredient : ingredients) {
			Assertions.assertTrue(ingredientVisibility.isIngredientVisible(TestIngredient.TYPE, ingredient));
			Assertions.assertFalse(ingredientVisibility.isIngredientVisible(
				TestIngredient.TYPE,
				ingredient,
				UidContext.Recipe
			));
		}
	}

	@Test
	public void testUnhidingIngredients() {
		Assertions.assertNotNull(ingredientFilter);
		Assertions.assertNotNull(ingredientManager);
		Assertions.assertNotNull(ingredientVisibility);
		Assertions.assertNotNull(filterTextSource);
		Assertions.assertNotNull(modIdHelper);
		Assertions.assertNotNull(ingredientFilterConfig);

		List<TestIngredient> ingredients = createIngredients();
		addIngredients(ingredientFilter, filterTextSource, ingredientVisibility, ingredientManager, modIdHelper, ingredientFilterConfig, ingredients);
		ingredientVisibility.hideIngredients(
			TestIngredient.TYPE,
			ingredients,
			Set.of(UidContext.Ingredient)
		);
		Assertions.assertEquals(
			TestPlugin.BASE_INGREDIENT_COUNT,
			ingredientFilter.getFilteredIngredients(TestIngredient.TYPE).size()
		);

		ingredientVisibility.unhideIngredients(
			TestIngredient.TYPE,
			ingredients,
			Set.of(UidContext.Ingredient)
		);
		Assertions.assertEquals(
			TestPlugin.BASE_INGREDIENT_COUNT + EXTRA_INGREDIENT_COUNT,
			ingredientFilter.getFilteredIngredients(TestIngredient.TYPE).size()
		);
	}

	@Test
	public void testRecipeVisibilityUsesRecipeUid() {
		Assertions.assertNotNull(ingredientFilter);
		Assertions.assertNotNull(ingredientManager);
		Assertions.assertNotNull(ingredientVisibility);
		Assertions.assertNotNull(filterTextSource);
		Assertions.assertNotNull(modIdHelper);
		Assertions.assertNotNull(ingredientFilterConfig);

		List<TestIngredient> ingredients = createIngredients();
		addIngredients(ingredientFilter, filterTextSource, ingredientVisibility, ingredientManager, modIdHelper, ingredientFilterConfig, ingredients);

		TestIngredient hiddenIngredient = ingredients.getFirst();
		TestIngredient recipeEquivalentIngredient = ingredients.get(2);
		TestIngredient differentRecipeIngredient = ingredients.get(1);
		ingredientVisibility.hideIngredients(
			TestIngredient.TYPE,
			Set.of(hiddenIngredient),
			Set.of(UidContext.Recipe)
		);

		Assertions.assertTrue(ingredientVisibility.isIngredientVisible(
			TestIngredient.TYPE,
			recipeEquivalentIngredient,
			UidContext.Ingredient
		));
		Assertions.assertFalse(ingredientVisibility.isIngredientVisible(
			TestIngredient.TYPE,
			recipeEquivalentIngredient,
			UidContext.Recipe
		));
		Assertions.assertTrue(ingredientVisibility.isIngredientVisible(
			TestIngredient.TYPE,
			differentRecipeIngredient,
			UidContext.Recipe
		));

		List<TestIngredient> filteredIngredients = ingredientFilter.getFilteredIngredients(TestIngredient.TYPE);
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT + EXTRA_INGREDIENT_COUNT, filteredIngredients.size());

		ingredientVisibility.unhideIngredients(
			TestIngredient.TYPE,
			Set.of(hiddenIngredient),
			Set.of(UidContext.Recipe)
		);
		Assertions.assertTrue(ingredientVisibility.isIngredientVisible(
			TestIngredient.TYPE,
			recipeEquivalentIngredient,
			UidContext.Recipe
		));
	}

	@Test
	public void testAddingAndRemovingIngredientsWithTooltipStrings() {
		Assertions.assertNotNull(ingredientFilter);
		Assertions.assertNotNull(ingredientManager);
		Assertions.assertNotNull(ingredientVisibility);
		Assertions.assertNotNull(filterTextSource);
		Assertions.assertNotNull(modIdHelper);
		Assertions.assertNotNull(ingredientFilterConfig);

		List<TestIngredient> ingredients = createIngredients();
		TestIngredient testIngredient = ingredients.getFirst();
		IIngredientRenderer<TestIngredient> ingredientRenderer = ingredientManager.getIngredientRenderer(TestIngredient.TYPE);
		Set<String> tooltipStrings = getTooltipStrings(ingredientRenderer, testIngredient);

		addIngredients(ingredientFilter, filterTextSource, ingredientVisibility, ingredientManager, modIdHelper, ingredientFilterConfig, ingredients);
		for (String tooltipString : tooltipStrings) {
			filterTextSource.setFilterText(tooltipString);
			List<TestIngredient> filteredIngredients = ingredientFilter.getFilteredIngredients(TestIngredient.TYPE);
			Assertions.assertTrue(filteredIngredients.contains(testIngredient), tooltipString);
		}

		removeIngredients(ingredientFilter, filterTextSource, ingredientVisibility, ingredientManager, modIdHelper, ingredientFilterConfig, ingredients);
		for (String tooltipString : tooltipStrings) {
			filterTextSource.setFilterText(tooltipString);
			List<TestIngredient> filteredIngredients = ingredientFilter.getFilteredIngredients(TestIngredient.TYPE);
			Assertions.assertFalse(filteredIngredients.contains(testIngredient), tooltipString);
		}

		addIngredients(ingredientFilter, filterTextSource, ingredientVisibility, ingredientManager, modIdHelper, ingredientFilterConfig, ingredients);
		for (String tooltipString : tooltipStrings) {
			filterTextSource.setFilterText(tooltipString);
			List<TestIngredient> filteredIngredients = ingredientFilter.getFilteredIngredients(TestIngredient.TYPE);
			Assertions.assertTrue(filteredIngredients.contains(testIngredient), tooltipString);
		}
	}

	@ParameterizedTest
	@EnumSource(SearchBackend.class)
	public void testIngredientAliasesCanBeToggledWithoutRestart(SearchBackend backend) {
		DynamicIngredientData data = new DynamicIngredientData();
		setupFilter(data, backend);
		Assertions.assertNotNull(ingredientFilter);
		Assertions.assertNotNull(filterTextSource);
		Assertions.assertNotNull(ingredientFilterConfig);
		Assertions.assertNotNull(searchStorageBuilderFactory);

		filterTextSource.setFilterText(INGREDIENT_ALIAS);
		List<TestIngredient> filteredIngredients = ingredientFilter.getFilteredIngredients(TestIngredient.TYPE);
		Assertions.assertFalse(filteredIngredients.contains(ALIASED_INGREDIENT));

		data.nameReads.clear();
		ingredientFilterConfig.searchIngredientAliases().set(true);
		filteredIngredients = ingredientFilter.getFilteredIngredients(TestIngredient.TYPE);
		Assertions.assertEquals(List.of(ALIASED_INGREDIENT), filteredIngredients);
		Assertions.assertTrue(data.nameReads.isEmpty());
		Assertions.assertEquals(List.of("unprefixed"), searchStorageBuilderFactory.getAndClearCreatedIds());

		data.nameReads.clear();
		ingredientFilterConfig.searchModAliases().set(true);
		Assertions.assertEquals(List.of(ALIASED_INGREDIENT), ingredientFilter.getFilteredIngredients(TestIngredient.TYPE));
		Assertions.assertTrue(data.nameReads.isEmpty());
		Assertions.assertEquals(List.of("mod_names"), searchStorageBuilderFactory.getAndClearCreatedIds());

		data.nameReads.clear();
		ingredientFilterConfig.searchIngredientAliases().set(false);
		filteredIngredients = ingredientFilter.getFilteredIngredients(TestIngredient.TYPE);
		Assertions.assertFalse(filteredIngredients.contains(ALIASED_INGREDIENT));
		Assertions.assertTrue(data.nameReads.isEmpty());
		Assertions.assertEquals(List.of("unprefixed"), searchStorageBuilderFactory.getAndClearCreatedIds());
	}

	@Test
	public void brokenAliasLookupDoesNotPreventSearchingOtherIngredients() {
		Set<TestIngredient> brokenIngredients = new HashSet<>();
		DynamicIngredientData data = new DynamicIngredientData() {
			@Override
			public Object getUid(ITypedIngredient<TestIngredient> ingredient, UidContext context) {
				if (brokenIngredients.contains(ingredient.getIngredient())) {
					throw new IllegalStateException("Broken ingredient alias lookup");
				}
				return super.getUid(ingredient, context);
			}
		};
		setupFilter(data, SearchBackend.LUCENE);
		Assertions.assertNotNull(ingredientFilter);
		Assertions.assertNotNull(filterTextSource);
		Assertions.assertNotNull(ingredientFilterConfig);
		brokenIngredients.add(ALIASED_INGREDIENT);
		data.nameReads.clear();
		ingredientFilterConfig.searchIngredientAliases().set(true);
		filterTextSource.setFilterText("ignis");
		Assertions.assertEquals(List.of(new TestIngredient(1)), ingredientFilter.getFilteredIngredients(TestIngredient.TYPE));
		filterTextSource.setFilterText("mysterious");
		Assertions.assertEquals(List.of(ALIASED_INGREDIENT), ingredientFilter.getFilteredIngredients(TestIngredient.TYPE));
		Assertions.assertTrue(data.nameReads.isEmpty());

		brokenIngredients.clear();
		ingredientFilterConfig.searchIngredientAliases().set(false);
		ingredientFilterConfig.searchIngredientAliases().set(true);
		filterTextSource.setFilterText(INGREDIENT_ALIAS);
		Assertions.assertEquals(List.of(ALIASED_INGREDIENT), ingredientFilter.getFilteredIngredients(TestIngredient.TYPE));
	}

	@Test
	public void testModAliasesCanBeToggledWithoutRestart() {
		Assertions.assertNotNull(ingredientFilter);
		Assertions.assertNotNull(filterTextSource);
		Assertions.assertNotNull(ingredientFilterConfig);
		Assertions.assertNotNull(searchStorageBuilderFactory);

		filterTextSource.setFilterText("@" + MOD_ALIAS);
		List<TestIngredient> filteredIngredients = ingredientFilter.getFilteredIngredients(TestIngredient.TYPE);
		Assertions.assertTrue(filteredIngredients.isEmpty());

		ingredientFilterConfig.searchModAliases().set(true);
		filteredIngredients = ingredientFilter.getFilteredIngredients(TestIngredient.TYPE);
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, filteredIngredients.size());
		Assertions.assertTrue(filteredIngredients.contains(new TestIngredient(0)));
		Assertions.assertTrue(filteredIngredients.contains(new TestIngredient(1)));
		Assertions.assertEquals(List.of("mod_names"), searchStorageBuilderFactory.getAndClearCreatedIds());

		ingredientFilterConfig.searchModAliases().set(false);
		filteredIngredients = ingredientFilter.getFilteredIngredients(TestIngredient.TYPE);
		Assertions.assertTrue(filteredIngredients.isEmpty());
		Assertions.assertEquals(List.of("mod_names"), searchStorageBuilderFactory.getAndClearCreatedIds());
	}

	@ParameterizedTest
	@EnumSource(SearchBackend.class)
	public void testSearchConfigChangesReuseMetadataAndRebuildEachAffectedIndexOnce(SearchBackend backend) {
		DynamicIngredientData data = new DynamicIngredientData();
		setupFilter(data, backend);
		Assertions.assertNotNull(ingredientFilter);
		Assertions.assertNotNull(ingredientFilterConfig);
		Assertions.assertNotNull(searchStorageBuilderFactory);

		data.nameReads.clear();
		data.tooltipReads.clear();
		ingredientFilterConfig.searchModIds().set(true);
		ingredientFilterConfig.searchModAliases().set(true);
		ingredientFilterConfig.tagSearchMode().set(SearchMode.DISABLED);

		ingredientFilter.getElements();
		Assertions.assertTrue(data.nameReads.isEmpty());
		Assertions.assertTrue(data.tooltipReads.isEmpty());
		Assertions.assertEquals(List.of("mod_names", "tags"), searchStorageBuilderFactory.getAndClearCreatedIds());
	}

	@Test
	public void testConfigBlacklist() {
		Assertions.assertNotNull(ingredientFilter);
		Assertions.assertNotNull(baseList);
		Assertions.assertNotNull(editModeConfig);

		IListElementInfo<?> elementInfo = baseList.getFirst();
		ITypedIngredient<?> typedIngredient = elementInfo.getTypedIngredient();
		@SuppressWarnings("unchecked")
		ITypedIngredient<TestIngredient> blacklistedIngredient = (ITypedIngredient<TestIngredient>) typedIngredient;
		TestIngredientHelper testIngredientHelper = new TestIngredientHelper();
		editModeConfig.addIngredientToConfigBlacklist(blacklistedIngredient, IEditModeConfig.HideMode.SINGLE, testIngredientHelper);

		ingredientFilter.updateHidden();

		List<?> ingredientList = ingredientFilter.getElements();
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT - 1, ingredientList.size());
	}

	@Test
	public void testConfigBlacklistInEditMode() {
		Assertions.assertNotNull(ingredientFilter);
		Assertions.assertNotNull(baseList);
		Assertions.assertNotNull(editModeConfig);
		Assertions.assertNotNull(toggleState);

		IListElementInfo<?> elementInfo = baseList.getFirst();
		ITypedIngredient<?> typedIngredient = elementInfo.getTypedIngredient();
		@SuppressWarnings("unchecked")
		ITypedIngredient<TestIngredient> blacklistedIngredient = (ITypedIngredient<TestIngredient>) typedIngredient;
		TestIngredientHelper testIngredientHelper = new TestIngredientHelper();

		toggleState.toggleEditModeEnabled();
		editModeConfig.addIngredientToConfigBlacklist(
			blacklistedIngredient,
			IEditModeConfig.HideMode.SINGLE,
			testIngredientHelper
		);

		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, ingredientFilter.getElements().size());

		toggleState.toggleEditModeEnabled();
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT - 1, ingredientFilter.getElements().size());

		toggleState.toggleEditModeEnabled();
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, ingredientFilter.getElements().size());
	}

	public static Set<String> getTooltipStrings(IIngredientRenderer<TestIngredient> ingredientRenderer, TestIngredient testIngredient) {
		List<Component> components = ingredientRenderer.getTooltip(testIngredient, Item.TooltipContext.EMPTY, null, TooltipFlag.Default.NORMAL);
		return ListElementInfo.getStrings(components);
	}

	public static List<TestIngredient> createIngredients() {
		List<TestIngredient> ingredients = new ArrayList<>();
		for (int i = TestPlugin.BASE_INGREDIENT_COUNT; i < TestPlugin.BASE_INGREDIENT_COUNT + EXTRA_INGREDIENT_COUNT; i++) {
			ingredients.add(new TestIngredient(i));
		}
		Assertions.assertEquals(EXTRA_INGREDIENT_COUNT, ingredients.size());
		return ingredients;
	}

	private static void addIngredients(
		IngredientFilter ingredientFilter,
		IFilterTextSource filterTextSource,
		IIngredientVisibility ingredientVisibility,
		IIngredientManager ingredientManager,
		IModIdHelper modIdHelper,
		TestIngredientFilterConfig ingredientFilterConfig,
		List<TestIngredient> ingredientsToAdd
	) {
		List<IListElementInfo<TestIngredient>> listToAdd = IngredientListElementFactory.createTestList(ingredientManager, TestIngredient.TYPE, ingredientsToAdd, ingredientFilterConfig, modIdHelper);
		Assertions.assertEquals(EXTRA_INGREDIENT_COUNT, listToAdd.size());

		ingredientManager.addIngredientsAtRuntime(TestIngredient.TYPE, ingredientsToAdd);

		Collection<TestIngredient> testIngredients = ingredientManager.getAllIngredients(TestIngredient.TYPE);
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT + EXTRA_INGREDIENT_COUNT, testIngredients.size());
		for (TestIngredient testIngredient : ingredientsToAdd) {
			Assertions.assertTrue(testIngredients.contains(testIngredient));
		}

		filterTextSource.setFilterText("");
		List<TestIngredient> filteredIngredients = ingredientFilter.getFilteredIngredients(TestIngredient.TYPE);
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT + EXTRA_INGREDIENT_COUNT, filteredIngredients.size());
		for (TestIngredient testIngredient : filteredIngredients) {
			Assertions.assertTrue(testIngredients.contains(testIngredient));
		}

		for (TestIngredient ingredient : ingredientsToAdd) {
			Assertions.assertTrue(ingredientVisibility.isIngredientVisible(TestIngredient.TYPE, ingredient));
		}
	}

	private static void removeIngredients(
		IngredientFilter ingredientFilter,
		IFilterTextSource filterTextSource,
		IIngredientVisibility ingredientVisibility,
		IIngredientManager ingredientManager,
		IModIdHelper modIdHelper,
		TestIngredientFilterConfig ingredientFilterConfig,
		List<TestIngredient> ingredientsToRemove
	) {
		List<IListElementInfo<TestIngredient>> listToRemove = IngredientListElementFactory.createTestList(ingredientManager, TestIngredient.TYPE, ingredientsToRemove, ingredientFilterConfig, modIdHelper);
		Assertions.assertEquals(EXTRA_INGREDIENT_COUNT, listToRemove.size());

		ingredientManager.removeIngredientsAtRuntime(TestIngredient.TYPE, ingredientsToRemove);

		filterTextSource.setFilterText("");
		List<TestIngredient> filteredIngredients = ingredientFilter.getFilteredIngredients(TestIngredient.TYPE);
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, filteredIngredients.size());
		for (TestIngredient testIngredient : filteredIngredients) {
			Assertions.assertFalse(ingredientsToRemove.contains(testIngredient));
		}

		Collection<TestIngredient> testIngredients = ingredientManager.getAllIngredients(TestIngredient.TYPE);
		Assertions.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, testIngredients.size());
		for (TestIngredient testIngredient : testIngredients) {
			Assertions.assertFalse(ingredientsToRemove.contains(testIngredient));
		}

		for (TestIngredient ingredient : ingredientsToRemove) {
			Assertions.assertFalse(ingredientVisibility.isIngredientVisible(TestIngredient.TYPE, ingredient));
			for (UidContext context : UidContext.values()) {
				Assertions.assertFalse(ingredientVisibility.isIngredientVisible(TestIngredient.TYPE, ingredient, context));
			}
		}

		ingredientVisibility.unhideIngredients(
			TestIngredient.TYPE,
			ingredientsToRemove,
			Set.of(UidContext.values())
		);
		for (TestIngredient ingredient : ingredientsToRemove) {
			for (UidContext context : UidContext.values()) {
				Assertions.assertFalse(ingredientVisibility.isIngredientVisible(TestIngredient.TYPE, ingredient, context));
			}
		}
	}

	private static class NullSerializer implements EditModeConfig.ISerializer {
		@Override
		public void initialize(EditModeConfig config) {

		}

		@Override
		public void save(EditModeConfig config) {

		}

		@Override
		public void load(EditModeConfig config) {

		}
	}

	private enum SearchBackend {
		LUCENE,
		CUSTOM_STORAGE,
		CUSTOM_BUILDER
	}

	private static class RecordingSearchStorageBuilderFactory implements ISearchStorageBuilderFactory {
		private final List<String> createdIds = new ArrayList<>();
		private final ISearchStorageBuilderFactory delegate;

		private RecordingSearchStorageBuilderFactory(ISearchStorageBuilderFactory delegate) {
			this.delegate = delegate;
		}

		@Override
		public <T> ISearchStorageBuilder<T> create() {
			return delegate.create();
		}

		@Override
		public <T> ISearchStorageBuilder<T> create(String id) {
			createdIds.add(id);
			return delegate.create(id);
		}

		@Override
		public boolean supportsDeletion() {
			return delegate.supportsDeletion();
		}

		public List<String> getAndClearCreatedIds() {
			List<String> result = List.copyOf(createdIds);
			clearCreatedIds();
			return result;
		}

		public void clearCreatedIds() {
			createdIds.clear();
		}
	}

	private static class DynamicIngredientData extends TestIngredientHelper implements IIngredientRenderer<TestIngredient> {
		private final Map<Integer, String> aspects = new HashMap<>(Map.of(0, "Mysterious", 1, "Ignis"));
		private final List<Integer> nameReads = new ArrayList<>();
		private final List<Integer> tooltipReads = new ArrayList<>();

		public void discover(int ingredient, String aspect) {
			aspects.put(ingredient, aspect);
		}

		@Override
		public String getDisplayName(TestIngredient ingredient) {
			nameReads.add(ingredient.number());
			return aspects.get(ingredient.number()) + " Crystal";
		}

		@Override
		public boolean isValidIngredient(TestIngredient ingredient) {
			return ingredient.number() >= 0;
		}

		@Override
		public Stream<Identifier> getTagStream(TestIngredient ingredient) {
			return Stream.of(Identifier.fromNamespaceAndPath("test", aspects.get(ingredient.number()).toLowerCase(Locale.ROOT)));
		}

		@Override
		public void render(GuiGraphics guiGraphics, TestIngredient ingredient) {
		}

		@Override
		@Deprecated(since = "27.32.0", forRemoval = true)
		@SuppressWarnings("removal")
		public List<Component> getTooltip(TestIngredient ingredient, TooltipFlag tooltipFlag) {
			tooltipReads.add(ingredient.number());
			return List.of(Component.literal(aspects.get(ingredient.number()) + " aspect tooltip"));
		}
	}

	// Deliberately inherits the default removeAll to exercise compatibility with existing backends.
	private static class PrefixSearchStorage<T> implements ISearchStorage<T>, ISearchStorageBuilder<T> {
		private final List<Map.Entry<String, T>> entries = new ArrayList<>();

		@Override
		public void put(String key, T value) {
			entries.add(Map.entry(key, value));
		}

		@Override
		public ISearchStorage<T> build() {
			return this;
		}

		@Override
		public void getSearchResults(String token, Consumer<Collection<T>> resultsConsumer) {
			resultsConsumer.accept(entries.stream().filter(e -> e.getKey().startsWith(token)).map(Map.Entry::getValue).toList());
		}

		@Override
		public void getAllElements(Consumer<Collection<T>> resultsConsumer) {
			resultsConsumer.accept(entries.stream().map(Map.Entry::getValue).toList());
		}

		@Override
		public String statistics() {
			return "PrefixSearchStorage";
		}
	}

	private static ISortingConfig<String> createSortingConfig() {
		var disabledWatcher = ConfigFileWatcherSettings.clientDefaults().withEnabled(false);
		var manager = new ConfigManager("Ingredient filter test", disabledWatcher, disabledWatcher);
		return manager.createInMemorySortingConfig(Comparator.naturalOrder(), true);
	}

	private static class TestClientConfigs implements IClientConfigs {
		private final IClientConfig clientConfig;
		private final IIngredientFilterConfig ingredientFilterConfig;
		private final List<Runnable> listenerRemovals = new ArrayList<>();

		private TestClientConfigs(IClientConfig clientConfig, IIngredientFilterConfig ingredientFilterConfig) {
			this.clientConfig = clientConfig;
			this.ingredientFilterConfig = ingredientFilterConfig;
		}

		@Override
		public IClientConfig getClientConfig() {
			return clientConfig;
		}

		@Override
		public IIngredientFilterConfig getIngredientFilterConfig() {
			return ingredientFilterConfig;
		}

		@Override
		public IIngredientGridConfig getIngredientListConfig() {
			throw new UnsupportedOperationException();
		}

		@Override
		public IIngredientGridConfig getBookmarkListConfig() {
			throw new UnsupportedOperationException();
		}

		@Override
		public ISortingConfig<String> getRecipeCategorySortingConfig() {
			return createSortingConfig();
		}

		@Override
		public void registerRuntimeListenerRemoval(Runnable listenerRemoval) {
			listenerRemovals.add(listenerRemoval);
		}

		@Override
		public void onRuntimeStopped() {
			listenerRemovals.forEach(Runnable::run);
			listenerRemovals.clear();
		}
	}
}
