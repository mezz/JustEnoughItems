package mezz.jei.neoforge.tests.plugins.vanilla;

import com.mojang.serialization.MapCodec;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.library.focus.Focus;
import mezz.jei.library.focus.FocusGroup;
import mezz.jei.library.gui.ingredients.CycleTicker;
import mezz.jei.library.gui.ingredients.RecipeSlotIngredients;
import mezz.jei.library.gui.recipes.layout.builder.RecipeSlotBuilder;
import mezz.jei.library.gui.recipes.supplier.builder.IngredientSupplierBuilder;
import mezz.jei.library.ingredients.DisplayIngredientAcceptor;
import mezz.jei.library.ingredients.IIngredientManagerInternal;
import mezz.jei.library.ingredients.RecipeIngredientSupplier;
import mezz.jei.library.ingredients.SlotDisplayData;
import mezz.jei.library.ingredients.SlotDisplayInfo;
import mezz.jei.library.ingredients.SlotDisplayIngredientExpander;
import mezz.jei.library.ingredients.SlotIngredient;
import mezz.jei.library.recipes.collect.RecipeIngredientRoleMap;
import mezz.jei.neoforge.tests.lib.JeiGameTestHelper;
import mezz.jei.neoforge.tests.lib.TestIngredientManagers;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@ForEachTest(groups = "vanilla_plugins")
public final class SlotDisplayIngredientGameTests {
	private static final IRecipeType<String> RECIPE_TYPE = IRecipeType.create("jei", "slot_display_test", String.class);

	private SlotDisplayIngredientGameTests() {
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "A slot display interpreter receives all ingredients from the display in one call.")
	public static void interpreterReceivesAllDerivedIngredients(JeiGameTestHelper helper) {
		// Setup: a tag-display interpreter records how often it runs and how many derived ingredients it receives.
		AtomicInteger callCount = new AtomicInteger();
		AtomicInteger interpretedIngredientCount = new AtomicInteger();
		IIngredientManagerInternal ingredientManager = TestIngredientManagers.createVanillaItemStackIngredientManager(
			List.of(new ItemStack(Items.OAK_PLANKS), new ItemStack(Items.BIRCH_PLANKS)),
			registration -> registration.register(
				SlotDisplay.TagSlotDisplay.TYPE,
				(slotDisplay, context, infoBuilder) -> {
					callCount.incrementAndGet();
					interpretedIngredientCount.set(context.getIngredients().size());
					infoBuilder.setTagKey(slotDisplay.tag());
				}
			)
		);
		SlotDisplay slotDisplay = new SlotDisplay.TagSlotDisplay(ItemTags.PLANKS);

		// Operation: resolve the tag display through the registered interpreter.
		List<SlotIngredient<ItemStack>> resolved = resolve(helper, ingredientManager, slotDisplay);

		// Assertions: one interpreter call receives the complete group, which is shared by every resolved ingredient.
		helper.assertEquals(1, callCount.get(), "Expected one interpretation for the slot display");
		helper.assertEquals(resolved.size(), interpretedIngredientCount.get(), "Expected the complete derived ingredient list");
		helper.assertTrue(resolved.size() > 1, "Expected the tag display to resolve more than one ingredient");
		SlotDisplayData<ItemStack> slotDisplayData = getSlotDisplayData(resolved.getFirst());
		helper.assertEquals(resolved.size(), slotDisplayData.ingredients().size(), "Expected one group for the display");
		helper.assertTrue(
			resolved.stream().allMatch(ingredient -> ingredient.slotDisplayData() == slotDisplayData),
			"Expected every derived ingredient to refer to the same display group"
		);
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Item-only potion displays rotate every subtype registered at runtime.")
	public static void itemDisplayMatchesAndRotatesAllSubtypes(JeiGameTestHelper helper) {
		// Setup: only one potion subtype is initially registered for an item-only potion display.
		ItemStack waterPotion = PotionContents.createItemStack(Items.POTION, Potions.WATER);
		ItemStack healingPotion = PotionContents.createItemStack(Items.POTION, Potions.HEALING);
		IIngredientManagerInternal ingredientManager = createIngredientManager(waterPotion, new ItemStack(Items.GLASS_BOTTLE));
		SlotDisplay slotDisplay = Ingredient.of(Items.POTION).display();

		// Operation: resolve and expand the item-only display with the initially registered ingredients.
		SlotIngredient<ItemStack> anyPotion = resolve(helper, ingredientManager, slotDisplay).getFirst();
		List<SlotIngredient<?>> initialRotation = expandForDisplay(ingredientManager, List.of(anyPotion));

		// Assertions: the item-only input is a wildcard group with one initial rotation entry.
		helper.assertTrue(getInfo(anyPotion).matchesAllSubtypes(), "Expected every potion subtype to match");
		helper.assertTrue(getInfo(anyPotion).tooltipHeader().isPresent(), "Expected an any-subtype heading");
		helper.assertEquals(1, initialRotation.size(), "Expected the initial registered potion subtype");

		// Operation: add another potion subtype at runtime and expand the wildcard group again.
		ingredientManager.addIngredientsAtRuntime(VanillaTypes.ITEM_STACK, List.of(healingPotion));
		List<SlotIngredient<?>> expanded = expandForDisplay(ingredientManager, List.of(anyPotion));

		// Assertions: both runtime subtypes rotate within the original display group.
		helper.assertEquals(2, expanded.size(), "Expected every registered potion subtype in the rotation");
		helper.assertTrue(containsStack(expanded, waterPotion), "Expected the water potion in the rotation");
		helper.assertTrue(containsStack(expanded, healingPotion), "Expected the healing potion in the rotation");
		helper.assertTrue(
			expanded.stream().allMatch(ingredient -> ingredient.slotDisplayData() == anyPotion.slotDisplayData()),
			"Expected expanded ingredients to retain their display group"
		);

		// Operation: remove the original subtype at runtime and expand the group once more.
		ingredientManager.removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, List.of(waterPotion));
		List<SlotIngredient<?>> expandedAfterRemoval = expandForDisplay(ingredientManager, List.of(anyPotion));

		// Assertions: the grouping index and rotation retain only the remaining subtype.
		helper.assertEquals(1, expandedAfterRemoval.size(), "Expected the grouping index to update after runtime removal");
		helper.assertTrue(containsStack(expandedAfterRemoval, healingPotion), "Expected the remaining subtype after removal");
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "A wildcard display group expands each grouping UID only once.")
	public static void wildcardDisplayGroupExpandsOnce(JeiGameTestHelper helper) {
		// Setup: one display resolves two representatives with the same potion grouping UID.
		ItemStack waterPotion = PotionContents.createItemStack(Items.POTION, Potions.WATER);
		ItemStack healingPotion = PotionContents.createItemStack(Items.POTION, Potions.HEALING);
		IIngredientManagerInternal ingredientManager = TestIngredientManagers.createVanillaItemStackIngredientManager(
			List.of(waterPotion, healingPotion),
			registration -> registration.register(
				MultiPotionSlotDisplay.TYPE,
				(display, context, infoBuilder) -> infoBuilder.setWildcardForSubtypes(true)
			)
		);

		// Operation: resolve the wildcard display before expanding its grouped ingredients.
		List<SlotIngredient<ItemStack>> resolved = resolve(helper, ingredientManager, MultiPotionSlotDisplay.INSTANCE);

		// Assertions: both representatives initially belong to one interpreted display group.
		helper.assertEquals(2, resolved.size(), "Expected both representatives from the display");
		helper.assertTrue(
			resolved.get(0).slotDisplayData() == resolved.get(1).slotDisplayData(),
			"Expected both representatives to share one display group"
		);

		// Operation: expand every wildcard representative for display.
		List<SlotIngredient<?>> expanded = expandForDisplay(ingredientManager, resolved);

		// Assertions: the shared grouping UID expands once and contains both registered subtypes.
		helper.assertEquals(2, expanded.size(), "Expected the grouping UID to expand only once");
		helper.assertTrue(containsStack(expanded, waterPotion), "Expected the water potion in the rotation");
		helper.assertTrue(containsStack(expanded, healingPotion), "Expected the healing potion in the rotation");
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Tag displays retain their declared tag after resolution and wrapping.")
	public static void tagDisplayRetainsDeclaredTag(JeiGameTestHelper helper) {
		// Setup: only part of the planks tag is registered, so JEI cannot infer the tag from contents alone.
		ItemStack oakPlanks = new ItemStack(Items.OAK_PLANKS);
		IIngredientManagerInternal ingredientManager = createIngredientManager(oakPlanks);
		IIngredientHelper<ItemStack> ingredientHelper = ingredientManager.getIngredientHelper(VanillaTypes.ITEM_STACK);
		SlotDisplay tagDisplay = new SlotDisplay.TagSlotDisplay(ItemTags.PLANKS);

		// Operation: resolve the declared tag display despite its incomplete registered contents.
		SlotIngredient<ItemStack> resolvedTag = findStack(resolve(helper, ingredientManager, tagDisplay), Items.OAK_PLANKS);

		// Assertions: inferred metadata is absent, but the interpreter preserves the display's declared tag.
		helper.assertTrue(
			ingredientHelper.getTagKeyEquivalent(List.of(oakPlanks)).isEmpty(),
			"Expected incomplete contents to be insufficient for tag inference"
		);
		helper.assertEquals(ItemTags.PLANKS, getInfo(resolvedTag).tagKey().orElseThrow(), "Expected the declared tag");

		// Operation: wrap the same tag display in a remainder display and resolve it again.
		SlotDisplay withRemainder = new SlotDisplay.WithRemainder(
			tagDisplay,
			new SlotDisplay.ItemSlotDisplay(Items.STICK)
		);
		SlotIngredient<ItemStack> resolvedRemainder = findStack(
			resolve(helper, ingredientManager, withRemainder),
			Items.OAK_PLANKS
		);

		// Assertions: wrapper interpretation inherits the tag metadata from its input display.
		helper.assertEquals(
			ItemTags.PLANKS,
			getInfo(resolvedRemainder).tagKey().orElseThrow(),
			"Expected a wrapped input display to retain its tag"
		);
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Mixed composites retain independent matching and tooltip metadata for each child.")
	public static void mixedCompositeRetainsChildGroups(JeiGameTestHelper helper) {
		// Setup: a composite combines an exact tag child with an all-subtypes potion child.
		ItemStack oakPlanks = new ItemStack(Items.OAK_PLANKS);
		ItemStack waterPotion = PotionContents.createItemStack(Items.POTION, Potions.WATER);
		IIngredientManagerInternal ingredientManager = createIngredientManager(oakPlanks, waterPotion);
		SlotDisplay composite = new SlotDisplay.Composite(List.of(
			new SlotDisplay.TagSlotDisplay(ItemTags.PLANKS),
			new SlotDisplay.ItemSlotDisplay(Items.POTION)
		));

		// Operation: resolve both children through the composite interpreter.
		List<SlotIngredient<ItemStack>> resolved = resolve(helper, ingredientManager, composite);
		SlotIngredient<ItemStack> planks = findStack(resolved, Items.OAK_PLANKS);
		SlotIngredient<ItemStack> potion = findStack(resolved, Items.POTION);

		// Assertions: each child retains its own metadata and independent display group.
		helper.assertEquals(ItemTags.PLANKS, getInfo(planks).tagKey().orElseThrow(), "Expected the tag child to retain its tag");
		helper.assertTrue(!getInfo(planks).matchesAllSubtypes(), "Expected ordinary tag contents to remain exact");
		helper.assertTrue(getInfo(potion).tagKey().isEmpty(), "Expected the item child not to inherit another child's tag");
		helper.assertTrue(getInfo(potion).matchesAllSubtypes(), "Expected the item child to match every potion subtype");
		helper.assertTrue(
			planks.slotDisplayData() != potion.slotDisplayData(),
			"Expected mixed children to remain separate ingredient groups"
		);
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "All-subtype slot displays support recipe lookup and recipe focus matching.")
	public static void allSubtypeDisplaySupportsLookupAndFocus(JeiGameTestHelper helper) {
		// Setup: a recipe and display accept every potion subtype, while focus selects one concrete potion.
		ItemStack healingPotion = PotionContents.createItemStack(Items.POTION, Potions.HEALING);
		IIngredientManagerInternal ingredientManager = createIngredientManager(healingPotion);
		ITypedIngredient<ItemStack> focusedPotion = ingredientManager.createTypedIngredient(VanillaTypes.ITEM_STACK, healingPotion, false)
			.orElseThrow();
		ContextMap contextMap = SlotDisplayContext.fromLevel(helper.getLevel());
		SlotDisplay slotDisplay = Ingredient.of(Items.POTION).display();
		IngredientSupplierBuilder supplierBuilder = new IngredientSupplierBuilder(ingredientManager, contextMap);
		supplierBuilder.addSlot(RecipeIngredientRole.INPUT).add(slotDisplay);
		RecipeIngredientSupplier ingredientSupplier = supplierBuilder.buildIngredientSupplier();
		RecipeIngredientRoleMap roleMap = new RecipeIngredientRoleMap(
			Comparator.comparing(recipeType -> recipeType.getUid().toString()),
			ingredientManager,
			RecipeIngredientRole.INPUT
		);
		roleMap.addRecipe(RECIPE_TYPE, "any potion recipe", ingredientSupplier);

		DisplayIngredientAcceptor acceptor = new DisplayIngredientAcceptor(
			ingredientManager,
			contextMap,
			RecipeIngredientRole.INPUT
		);
		acceptor.add(slotDisplay);
		Focus<ItemStack> focus = new Focus<>(RecipeIngredientRole.INPUT, focusedPotion);

		// Operation: use the concrete focus for indexed recipe lookup and slot matching.
		List<String> matchingRecipes = roleMap.getRecipes(RECIPE_TYPE, focusedPotion);
		boolean slotMatches = acceptor.getMatches(focus, RecipeIngredientRole.INPUT).contains(0);

		// Assertions: the all-subtypes display matches the concrete subtype through both paths.
		helper.assertEquals(
			List.of("any potion recipe"),
			matchingRecipes,
			"Expected a concrete potion focus to find the all-subtype recipe"
		);
		helper.assertTrue(
			slotMatches,
			"Expected a concrete potion focus to match the all-subtype recipe slot"
		);
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Explicit, output, and enumerated displays remain exact.")
	public static void nonWildcardDisplaysRemainExact(JeiGameTestHelper helper) {
		// Setup: a subtype-aware potion and a non-potion item are registered.
		ItemStack healingPotion = PotionContents.createItemStack(Items.POTION, Potions.HEALING);
		IIngredientManagerInternal ingredientManager = createIngredientManager(healingPotion, new ItemStack(Items.GLASS_BOTTLE));
		SlotDisplay exactDisplay = new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(healingPotion));
		SlotDisplay itemOnlyDisplay = new SlotDisplay.ItemSlotDisplay(Items.POTION);
		SlotDisplay withAnyPotion = new SlotDisplay.WithAnyPotion(itemOnlyDisplay);

		// Operation: resolve explicit, output, and enumerated displays for the subtype-aware item.
		SlotIngredient<ItemStack> exactPotion = resolve(helper, ingredientManager, exactDisplay).getFirst();
		SlotIngredient<ItemStack> outputPotion = resolve(helper, ingredientManager, RecipeIngredientRole.OUTPUT, itemOnlyDisplay)
			.getFirst();
		SlotIngredient<ItemStack> enumeratedPotion = resolve(helper, ingredientManager, withAnyPotion).stream()
			.filter(ingredient -> ItemStack.isSameItemSameComponents(ingredient.typedIngredient().getIngredient(), healingPotion))
			.findFirst()
			.orElseThrow();

		// Assertions: none of these display forms opt into matching every registered subtype.
		assertExact(helper, exactPotion, "Expected an explicit stack to remain exact");
		assertExact(helper, outputPotion, "Expected an item-only output to remain exact");
		assertExact(helper, enumeratedPotion, "Expected an explicitly enumerated potion display to remain exact");
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Exact item-stack composites retain candidates across their independent display groups.")
	public static void exactItemStackCompositeKeepsCandidatesAcrossDisplayGroups(JeiGameTestHelper helper) {
		// Setup: each exact item-stack child contributes an independent, non-wildcard display group.
		ItemStack stick = new ItemStack(Items.STICK);
		ItemStack glassBottle = new ItemStack(Items.GLASS_BOTTLE);
		IIngredientManagerInternal ingredientManager = createIngredientManager(stick, glassBottle);
		SlotDisplay composite = new SlotDisplay.Composite(List.of(
			new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(stick)),
			new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(glassBottle))
		));
		List<SlotIngredient<ItemStack>> resolved = resolve(helper, ingredientManager, composite);

		// Operation: collect the current display group and the broader candidate set used by slot tooltips.
		List<SlotIngredient<?>> displayedGroup = RecipeSlotIngredients.getDisplayGroupIngredients(resolved, resolved.getFirst());
		List<ItemStack> visibleCandidates = RecipeSlotIngredients.getVisibleSlotIngredients(resolved, ingredientManager, ingredient -> true)
			.map(SlotIngredient::typedIngredient)
			.map(ITypedIngredient::getItemStack)
			.flatMap(Optional::stream)
			.toList();

		// Assertions: pinning can browse both exact children even though the currently displayed group is a singleton.
		helper.assertTrue(
			resolved.stream().noneMatch(ingredient -> getInfo(ingredient).matchesAllSubtypes()),
			"Expected exact item-stack displays not to match every subtype"
		);
		helper.assertEquals(1, displayedGroup.size(), "Expected one exact ingredient in the current display group");
		helper.assertEquals(2, visibleCandidates.size(), "Expected both exact ingredients in the interactive tooltip");
		helper.assertTrue(containsItemStack(visibleCandidates, stick), "Expected the stick candidate");
		helper.assertTrue(containsItemStack(visibleCandidates, glassBottle), "Expected the glass bottle candidate");
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Wildcard slot displays preserve focus and expand consistently wherever they are displayed.")
	public static void wildcardDisplaysPreserveFocusAndExpandConsistently(JeiGameTestHelper helper) {
		// Setup: an item-only potion display can expand to two registered potion subtypes.
		ItemStack waterPotion = PotionContents.createItemStack(Items.POTION, Potions.WATER);
		ItemStack healingPotion = PotionContents.createItemStack(Items.POTION, Potions.HEALING);
		IIngredientManagerInternal ingredientManager = createIngredientManager(waterPotion, healingPotion);
		ContextMap contextMap = SlotDisplayContext.fromLevel(helper.getLevel());
		SlotDisplay slotDisplay = Ingredient.of(Items.POTION).display();
		ITypedIngredient<ItemStack> focusedPotion = ingredientManager.createTypedIngredient(VanillaTypes.ITEM_STACK, healingPotion, false)
			.orElseThrow();
		Focus<ItemStack> focus = new Focus<>(RecipeIngredientRole.INPUT, focusedPotion);
		DisplayIngredientAcceptor acceptor = new DisplayIngredientAcceptor(
			ingredientManager,
			contextMap,
			RecipeIngredientRole.INPUT
		);
		acceptor.add(slotDisplay);

		// Operation: calculate displayed ingredients with a concrete healing-potion focus.
		List<@Nullable SlotIngredient<?>> focusedIngredients = RecipeSlotIngredients.calculateDisplayIngredients(
			acceptor.getAllSlotIngredients(),
			ingredientManager,
			focus,
			RecipeIngredientRole.INPUT,
			ingredient -> true
		);

		// Assertions: focus narrows the wildcard group to the requested concrete subtype.
		helper.assertTrue(
			containsStack(focusedIngredients, healingPotion),
			"Expected the concrete focused potion to remain displayed"
		);
		helper.assertEquals(1, focusedIngredients.size(), "Expected focus to select one concrete potion");

		// Operation: build the public drawable slot without a focus.
		RecipeSlotBuilder expandedBuilder = new RecipeSlotBuilder(ingredientManager, contextMap, 0, RecipeIngredientRole.INPUT);
		expandedBuilder.add(slotDisplay);
		IRecipeSlotDrawable expandedSlot = expandedBuilder.build(FocusGroup.EMPTY, CycleTicker.createWithRandomOffset()).second();
		List<ItemStack> allIngredients = expandedSlot.getItemStacks().toList();

		// Assertions: public slot ingredients expose the complete wildcard group.
		helper.assertTrue(containsItemStack(allIngredients, waterPotion), "Expected public slot ingredients to include water potion");
		helper.assertTrue(containsItemStack(allIngredients, healingPotion), "Expected public slot ingredients to include healing potion");

		// Operation: calculate the same display through the display-override path.
		List<@Nullable SlotIngredient<?>> overrideIngredients = RecipeSlotIngredients.calculateDisplayIngredients(
			acceptor.getAllSlotIngredients(),
			ingredientManager,
			FocusGroup.EMPTY,
			RecipeIngredientRole.INPUT,
			ingredient -> true
		);

		// Assertions: display overrides expand the wildcard group consistently with ordinary slots.
		helper.assertTrue(containsStack(overrideIngredients, waterPotion), "Expected display overrides to expand water potion");
		helper.assertTrue(containsStack(overrideIngredients, healingPotion), "Expected display overrides to expand healing potion");
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "A custom wrapper gets information from the interpreter for its wrapped display.")
	public static void customWrapperUsesWrappedDisplayInterpreter(JeiGameTestHelper helper) {
		// Setup: a custom wrapper delegates metadata to a child display that counts its resolutions.
		IIngredientManagerInternal ingredientManager = TestIngredientManagers.createVanillaItemStackIngredientManager(
			List.of(new ItemStack(Items.STICK)),
			registration -> {
				registration.register(
					CountingSlotDisplay.TYPE,
					(display, context, infoBuilder) -> infoBuilder.setTooltipHeader(Component.literal("Custom Display"))
				);
				registration.register(
					WrappingSlotDisplay.TYPE,
					(display, context, infoBuilder) -> infoBuilder.addChildDisplay(CountingSlotDisplay.INSTANCE)
				);
			}
		);
		CountingSlotDisplay.reset();

		// Operation: resolve the custom wrapper.
		SlotIngredient<ItemStack> resolved = resolve(helper, ingredientManager, WrappingSlotDisplay.INSTANCE).getFirst();

		// Assertions: the wrapper inherits child metadata without resolving the child contents twice.
		helper.assertEquals(
			Component.literal("Custom Display"),
			getInfo(resolved).tooltipHeader().orElseThrow(),
			"Expected information from the wrapped display's interpreter"
		);
		helper.assertEquals(1, CountingSlotDisplay.getResolveCount(), "Expected the wrapped display to resolve once");
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Explicit wrapper metadata takes priority over inherited child metadata.")
	public static void customWrapperOverridesWrappedDisplayInterpreter(JeiGameTestHelper helper) {
		// Setup: a child defines every metadata field, while its wrapper explicitly clears or replaces each field.
		IIngredientManagerInternal ingredientManager = TestIngredientManagers.createVanillaItemStackIngredientManager(
			List.of(new ItemStack(Items.STICK)),
			registration -> {
				registration.register(
					CountingSlotDisplay.TYPE,
					(display, context, infoBuilder) -> infoBuilder
						.setWildcardForSubtypes(true)
						.setTagKey(ItemTags.PLANKS)
						.setTooltipHeader(Component.literal("Inherited Header"))
				);
				registration.register(
					WrappingSlotDisplay.TYPE,
					(display, context, infoBuilder) -> infoBuilder
						.addChildDisplay(CountingSlotDisplay.INSTANCE)
						.setWildcardForSubtypes(false)
						.clearTagKey()
						.clearTooltipHeader()
				);
			}
		);
		CountingSlotDisplay.reset();

		// Operation: resolve the wrapper and merge its metadata with the child's metadata.
		SlotIngredient<ItemStack> resolved = resolve(helper, ingredientManager, WrappingSlotDisplay.INSTANCE).getFirst();

		// Assertions: explicit wrapper values win and inherited fields can be intentionally suppressed.
		helper.assertTrue(!getInfo(resolved).matchesAllSubtypes(), "Expected the wrapper's explicit false value to win");
		helper.assertTrue(getInfo(resolved).tagKey().isEmpty(), "Expected the wrapper to suppress the inherited tag");
		helper.assertTrue(
			getInfo(resolved).tagKeyOrElse(() -> Optional.of(ItemTags.PLANKS)).isEmpty(),
			"Expected the wrapper to suppress tag inference"
		);
		helper.assertTrue(getInfo(resolved).tooltipHeader().isEmpty(), "Expected the wrapper to suppress the inherited heading");
		helper.assertEquals(1, CountingSlotDisplay.getResolveCount(), "Expected the wrapped display to resolve once");
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "A wrapper that points to itself stops safely without resolving again.")
	public static void wrappedDisplayCycleStopsSafely(JeiGameTestHelper helper) {
		// Setup: a custom wrapper reports itself as its wrapped display.
		IIngredientManagerInternal ingredientManager = TestIngredientManagers.createVanillaItemStackIngredientManager(
			List.of(new ItemStack(Items.STICK)),
			registration -> registration.register(
				WrappingSlotDisplay.TYPE,
				(display, context, infoBuilder) -> infoBuilder.addChildDisplay(display)
			)
		);
		CountingSlotDisplay.reset();

		// Operation: resolve the cyclic wrapper.
		SlotIngredient<ItemStack> resolved = resolve(helper, ingredientManager, WrappingSlotDisplay.INSTANCE).getFirst();

		// Assertions: cycle detection discards recursive metadata without resolving contents again.
		helper.assertTrue(getSlotDisplayData(resolved).info().isEmpty(), "Expected cyclic wrapper information to be ignored");
		helper.assertEquals(1, CountingSlotDisplay.getResolveCount(), "Expected the display to resolve once");
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Interpreting a slot display does not resolve its contents a second time.")
	public static void interpretedDisplayResolvesContentsOnce(JeiGameTestHelper helper) {
		// Setup: a composite contains the same counting child display twice.
		IIngredientManagerInternal ingredientManager = createIngredientManager(new ItemStack(Items.STICK));
		CountingSlotDisplay.reset();
		SlotDisplay composite = new SlotDisplay.Composite(List.of(
			CountingSlotDisplay.INSTANCE,
			CountingSlotDisplay.INSTANCE
		));

		// Operation: resolve and interpret the composite display.
		List<SlotIngredient<ItemStack>> resolved = resolve(helper, ingredientManager, composite);

		// Assertions: contents resolve once, while each child occurrence retains an independent display group.
		helper.assertEquals(2, resolved.size(), "Expected each occurrence of the child display to produce an ingredient");
		helper.assertEquals(1, CountingSlotDisplay.getResolveCount(), "Expected the shared child display to resolve once");
		helper.assertTrue(
			resolved.get(0).slotDisplayData() != resolved.get(1).slotDisplayData(),
			"Expected repeated children to remain separate ingredient groups"
		);
		helper.succeed();
	}

	private static void assertExact(JeiGameTestHelper helper, SlotIngredient<ItemStack> ingredient, String message) {
		SlotDisplayData<ItemStack> data = getSlotDisplayData(ingredient);
		helper.assertTrue(!data.info().matchesAllSubtypes(), message);
	}

	private static boolean containsStack(List<? extends @Nullable SlotIngredient<?>> ingredients, ItemStack expected) {
		return ingredients.stream()
			.filter(Objects::nonNull)
			.map(SlotIngredient::typedIngredient)
			.map(ITypedIngredient::getItemStack)
			.flatMap(java.util.Optional::stream)
			.anyMatch(stack -> ItemStack.isSameItemSameComponents(stack, expected));
	}

	private static boolean containsItemStack(List<ItemStack> ingredients, ItemStack expected) {
		return ingredients.stream()
			.anyMatch(stack -> ItemStack.isSameItemSameComponents(stack, expected));
	}

	private static IIngredientManagerInternal createIngredientManager(ItemStack... itemStacks) {
		return TestIngredientManagers.createVanillaItemStackIngredientManager(List.of(itemStacks));
	}

	private static List<SlotIngredient<?>> expandForDisplay(
		IIngredientManagerInternal ingredientManager,
		List<? extends SlotIngredient<?>> ingredients
	) {
		return SlotDisplayIngredientExpander.streamForDisplay(ingredientManager, ingredients)
			.filter(Objects::nonNull)
			.toList();
	}

	private static List<SlotIngredient<ItemStack>> resolve(
		JeiGameTestHelper helper,
		IIngredientManagerInternal ingredientManager,
		SlotDisplay slotDisplay
	) {
		return resolve(helper, ingredientManager, RecipeIngredientRole.INPUT, slotDisplay);
	}

	private static List<SlotIngredient<ItemStack>> resolve(
		JeiGameTestHelper helper,
		IIngredientManagerInternal ingredientManager,
		RecipeIngredientRole role,
		SlotDisplay slotDisplay
	) {
		ContextMap contextMap = SlotDisplayContext.fromLevel(helper.getLevel());
		return ingredientManager.resolveSlotDisplay(VanillaTypes.ITEM_STACK, contextMap, role, slotDisplay)
			.toList();
	}

	private static SlotIngredient<ItemStack> findStack(
		List<SlotIngredient<ItemStack>> ingredients,
		net.minecraft.world.item.Item item
	) {
		return ingredients.stream()
			.filter(ingredient -> ingredient.typedIngredient().getIngredient().is(item))
			.findFirst()
			.orElseThrow();
	}

	private static <T> SlotDisplayInfo getInfo(SlotIngredient<T> ingredient) {
		return getSlotDisplayData(ingredient).info();
	}

	private static <T> SlotDisplayData<T> getSlotDisplayData(SlotIngredient<T> ingredient) {
		return Objects.requireNonNull(ingredient.slotDisplayData(), "Expected slot display data");
	}

	private static final class WrappingSlotDisplay implements SlotDisplay {
		private static final WrappingSlotDisplay INSTANCE = new WrappingSlotDisplay();
		private static final MapCodec<WrappingSlotDisplay> MAP_CODEC = MapCodec.unit(INSTANCE);
		private static final StreamCodec<RegistryFriendlyByteBuf, WrappingSlotDisplay> STREAM_CODEC = StreamCodec.unit(INSTANCE);
		private static final SlotDisplay.Type<WrappingSlotDisplay> TYPE = new SlotDisplay.Type<>(MAP_CODEC, STREAM_CODEC);

		@Override
		public <T> Stream<T> resolve(ContextMap context, DisplayContentsFactory<T> factory) {
			return CountingSlotDisplay.INSTANCE.resolve(context, factory);
		}

		@Override
		public Type<WrappingSlotDisplay> type() {
			return TYPE;
		}
	}

	private static final class CountingSlotDisplay implements SlotDisplay {
		private static final CountingSlotDisplay INSTANCE = new CountingSlotDisplay();
		private static final MapCodec<CountingSlotDisplay> MAP_CODEC = MapCodec.unit(INSTANCE);
		private static final StreamCodec<RegistryFriendlyByteBuf, CountingSlotDisplay> STREAM_CODEC = StreamCodec.unit(INSTANCE);
		private static final SlotDisplay.Type<CountingSlotDisplay> TYPE = new SlotDisplay.Type<>(MAP_CODEC, STREAM_CODEC);
		private static final AtomicInteger RESOLVE_COUNT = new AtomicInteger();

		@Override
		public <T> Stream<T> resolve(ContextMap context, DisplayContentsFactory<T> factory) {
			RESOLVE_COUNT.incrementAndGet();
			if (factory instanceof DisplayContentsFactory.ForStacks<T> stacks) {
				return Stream.of(stacks.forStack(new ItemStack(Items.STICK)));
			}
			return Stream.empty();
		}

		@Override
		public Type<CountingSlotDisplay> type() {
			return TYPE;
		}

		private static void reset() {
			RESOLVE_COUNT.set(0);
		}

		private static int getResolveCount() {
			return RESOLVE_COUNT.get();
		}
	}

	private static final class MultiPotionSlotDisplay implements SlotDisplay {
		private static final MultiPotionSlotDisplay INSTANCE = new MultiPotionSlotDisplay();
		private static final MapCodec<MultiPotionSlotDisplay> MAP_CODEC = MapCodec.unit(INSTANCE);
		private static final StreamCodec<RegistryFriendlyByteBuf, MultiPotionSlotDisplay> STREAM_CODEC = StreamCodec.unit(INSTANCE);
		private static final SlotDisplay.Type<MultiPotionSlotDisplay> TYPE = new SlotDisplay.Type<>(MAP_CODEC, STREAM_CODEC);

		@Override
		public <T> Stream<T> resolve(ContextMap context, DisplayContentsFactory<T> factory) {
			if (factory instanceof DisplayContentsFactory.ForStacks<T> stacks) {
				return Stream.of(
					stacks.forStack(PotionContents.createItemStack(Items.POTION, Potions.WATER)),
					stacks.forStack(PotionContents.createItemStack(Items.POTION, Potions.HEALING))
				);
			}
			return Stream.empty();
		}

		@Override
		public Type<MultiPotionSlotDisplay> type() {
			return TYPE;
		}
	}
}
