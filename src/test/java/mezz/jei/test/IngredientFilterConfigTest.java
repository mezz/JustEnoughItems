package mezz.jei.test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import mezz.jei.api.IIngredientFilter;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.config.Config;
import mezz.jei.config.ConfigValues;
import mezz.jei.config.FilterTextSource;
import mezz.jei.config.HideModeConfig;
import mezz.jei.config.IngredientFilterConfig;
import mezz.jei.ingredients.IngredientBlacklistInternal;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.ingredients.IngredientFilterApi;
import mezz.jei.ingredients.IngredientListElementFactory;
import mezz.jei.ingredients.IngredientRegistry;
import mezz.jei.startup.ModIngredientRegistration;
import mezz.jei.test.lib.TestIngredient;
import mezz.jei.test.lib.TestIngredientHelper;
import mezz.jei.test.lib.TestModIdHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import org.junit.Assert;
import org.junit.Test;

public class IngredientFilterConfigTest {
	@Test
	public void filtersKeepTheirQueriesAndConfiguredVisibilityIndependent() {
		Fixture first = new Fixture();
		Fixture second = new Fixture();
		first.hidden.add(first.helper.getUniqueId(first.pear));
		first.filter.updateHidden();
		first.filter.setFilterText("apple");
		second.filter.setFilterText("pear");

		Assert.assertEquals(Collections.singletonList(first.apple), first.filter.getFilteredIngredients());
		Assert.assertEquals(Collections.singletonList(second.pear), second.filter.getFilteredIngredients());
		Assert.assertEquals("apple", first.filter.getFilterText());
		Assert.assertEquals("pear", second.filter.getFilterText());
		first.filter.setFilterText("pear");
		Assert.assertTrue(first.filter.getFilteredIngredients().isEmpty());
		Assert.assertEquals(Collections.singletonList(second.pear), second.filter.getFilteredIngredients());
	}

	@Test
	public void tooltipIndexesUseEachFiltersSettingsAndPrefixModes() {
		Fixture advanced = new Fixture(true);
		Fixture normal = new Fixture();

		Assert.assertEquals(2, advanced.filter.getFilteredIngredients("#cobalt").size());
		Assert.assertTrue(normal.filter.getFilteredIngredients("#cobalt").isEmpty());
		advanced.values.tooltipSearchMode = Config.SearchMode.DISABLED;
		advanced.filter.modesChanged();
		Assert.assertTrue(advanced.filter.getFilteredIngredients("#cobalt").isEmpty());
		advanced.values.tooltipSearchMode = Config.SearchMode.REQUIRE_PREFIX;
		advanced.filter.modesChanged();
		Assert.assertTrue(advanced.filter.getFilteredIngredients("cobalt").isEmpty());
		Assert.assertEquals(2, advanced.filter.getFilteredIngredients("#cobalt").size());
		Assert.assertTrue(normal.filter.getFilteredIngredients("#cobalt").isEmpty());
	}

	@Test
	public void editModeRevealsConfiguredEntriesButKeepsApiHiddenEntriesHidden() {
		Fixture fixture = new Fixture();
		fixture.hidden.add(fixture.helper.getUniqueId(fixture.apple));
		fixture.apiBlacklist.addIngredientToBlacklist(fixture.pear, fixture.helper);

		fixture.filter.updateHidden();
		List<Object> ordinary = fixture.filter.getFilteredIngredients();
		fixture.values.editModeEnabled = true;
		fixture.filter.updateHidden();
		List<Object> editing = fixture.filter.getFilteredIngredients();

		Assert.assertTrue(ordinary.isEmpty());
		Assert.assertEquals(Collections.singletonList(fixture.apple), editing);
	}

	@Test
	public void publicFilterViewSharesTextAndNotifiesOnlyWhenItChanges() {
		Fixture fixture = new Fixture();
		IIngredientFilter view = new IngredientFilterApi(fixture.filter, new FilterTextSource(fixture.values));
		AtomicInteger changes = new AtomicInteger();
		fixture.filter.addListener(changes::incrementAndGet);

		view.setFilterText("APPLE");
		view.setFilterText("APPLE");
		List<Object> throughView = view.getFilteredIngredients();
		fixture.filter.setFilterText("pear");

		Assert.assertEquals(Collections.singletonList(fixture.apple), throughView);
		Assert.assertEquals(Collections.singletonList(fixture.pear), view.getFilteredIngredients());
		Assert.assertEquals("pear", view.getFilterText());
		Assert.assertEquals(2, changes.get());
	}

	private static final class Fixture {
		private final ConfigValues values = new ConfigValues();
		private final Set<String> hidden = new HashSet<>();
		private final IngredientBlacklistInternal apiBlacklist = new IngredientBlacklistInternal();
		private final TestIngredientHelper helper = new TestIngredientHelper();
		private final TestIngredient apple = new TestIngredient(10);
		private final TestIngredient pear = new TestIngredient(20);
		private final IngredientFilter filter;

		private Fixture() {
			this(false);
		}

		private Fixture(boolean advancedTooltips) {
			apple.setDisplayName("apple");
			pear.setDisplayName("pear");
			values.tooltipSearchMode = Config.SearchMode.ENABLED;
			values.searchAdvancedTooltips = advancedTooltips;
			ModIngredientRegistration registration = new ModIngredientRegistration();
			registration.register(TestIngredient.TYPE, Arrays.asList(apple, pear), helper, new TooltipRenderer());
			TestModIdHelper mods = new TestModIdHelper();
			IngredientRegistry registry = registration.createIngredientRegistry(mods, apiBlacklist);
			filter = new IngredientFilter(apiBlacklist, new IngredientFilterConfig(values), new HideModeConfig(values, hidden), new FilterTextSource(values));
			filter.addIngredients(IngredientListElementFactory.createBaseList(registry, mods));
		}
	}

	private static final class TooltipRenderer implements IIngredientRenderer<TestIngredient> {
		@Override
		public void render(Minecraft minecraft, int x, int y, TestIngredient ingredient) {
		}

		@Override
		public List<String> getTooltip(Minecraft minecraft, TestIngredient ingredient, ITooltipFlag flag) {
			return Collections.singletonList(flag.isAdvanced() ? "cobalt" : "ordinary");
		}
	}
}
