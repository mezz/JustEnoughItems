package mezz.jei.neoforge.tests.plugins.vanilla;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.extensions.vanilla.cooking.ICookingCategoryExtension;
import mezz.jei.api.recipe.category.extensions.vanilla.cooking.IExtendableCookingRecipeCategory;
import mezz.jei.library.focus.Focus;
import mezz.jei.library.ingredients.IIngredientManagerInternal;
import mezz.jei.library.ingredients.RecipeIngredientSupplier;
import mezz.jei.library.plugins.vanilla.cooking.AbstractCookingCategory;
import mezz.jei.library.plugins.vanilla.cooking.BlastingCategory;
import mezz.jei.library.plugins.vanilla.cooking.CampfireCookingCategory;
import mezz.jei.library.plugins.vanilla.cooking.CookingCategoryExtension;
import mezz.jei.library.plugins.vanilla.cooking.CookingRecipeTransferInfo;
import mezz.jei.library.plugins.vanilla.cooking.FurnaceSmeltingCategory;
import mezz.jei.library.plugins.vanilla.cooking.SmokingCategory;
import mezz.jei.library.plugins.vanilla.crafting.CraftingRecipeCategory;
import mezz.jei.library.transfer.RecipeTransferHandlerHelper;
import mezz.jei.library.util.IngredientSupplierHelper;
import mezz.jei.neoforge.tests.lib.JeiGameTestHelper;
import mezz.jei.neoforge.tests.lib.MenuFactory;
import mezz.jei.neoforge.tests.lib.TestGuiHelper;
import mezz.jei.neoforge.tests.lib.TestIngredientManagers;
import mezz.jei.neoforge.tests.lib.TestStackHelper;
import mezz.jei.neoforge.tests.lib.TestRecipeSlotView;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.context.ContextMap;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.SmokerMenu;
import net.minecraft.world.inventory.BlastFurnaceMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.FurnaceRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;

import java.util.List;
import java.util.Set;

@ForEachTest(groups = "cooking_extensions")
public final class CookingCategoryExtensionGameTests {
	private static final ICookingCategoryExtension<DynamicCookingRecipe> EXTENSION = (recipe, builder, input, output, focuses) -> {
		input.addItemStacks(recipe.inputs());
		output.addItemStacks(recipe.outputs());
		builder.createFocusLink(input, output);
	};

	private CookingCategoryExtensionGameTests() {
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "One interface extension can be reused independently in all four cooking categories without vanilla displays.")
	public static void interfaceExtensionCanBeReusedAcrossCookingCategories(JeiGameTestHelper helper) {
		var smelting = new FurnaceSmeltingCategory(TestGuiHelper.INSTANCE);
		var blasting = new BlastingCategory(TestGuiHelper.INSTANCE);
		var smoking = new SmokingCategory(TestGuiHelper.INSTANCE);
		var campfire = new CampfireCookingCategory(TestGuiHelper.INSTANCE);
		for (IExtendableCookingRecipeCategory category : List.of(smelting, blasting, smoking, campfire)) {
			category.addExtension(DynamicCookingRecipe.class, EXTENSION);
		}
		var ingredients = TestIngredientManagers.createVanillaItemStackIngredientManager(helper.getLevel());
		assertLinkedRecipe(helper, smelting, new DynamicSmeltingRecipe(), ingredients, true);
		assertLinkedRecipe(helper, blasting, new DynamicBlastingRecipe(), ingredients, true);
		assertLinkedRecipe(helper, smoking, new DynamicSmokingRecipe(), ingredients, true);
		assertLinkedRecipe(helper, campfire, new DynamicCampfireRecipe(), ingredients, false);

		var menu = helper.openMenu(FurnaceMenu::new);
		var transferInfo = new CookingRecipeTransferInfo<>(FurnaceMenu.class, MenuType.FURNACE, RecipeTypes.SMELTING, recipe -> smelting.getFuel(recipe.value()));
		helper.assertEquals(1, transferInfo.getRecipeSlots(menu, holder(new DynamicSmeltingRecipe())).size(), "Display-less recipe transfer inputs");
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Unmatched vanilla recipes retain their ingredients alongside a custom cooking extension in each category.")
	public static void unmatchedRecipesUseVanillaDisplays(JeiGameTestHelper helper) {
		var smelting = new FurnaceSmeltingCategory(TestGuiHelper.INSTANCE);
		var blasting = new BlastingCategory(TestGuiHelper.INSTANCE);
		var smoking = new SmokingCategory(TestGuiHelper.INSTANCE);
		var campfire = new CampfireCookingCategory(TestGuiHelper.INSTANCE);
		for (IExtendableCookingRecipeCategory category : List.of(smelting, blasting, smoking, campfire)) {
			category.addExtension(DynamicCookingRecipe.class, EXTENSION);
		}
		var ingredients = TestIngredientManagers.createVanillaItemStackIngredientManager(helper.getLevel());
		assertVanillaRecipe(helper, smelting, RecipeType.SMELTING, ingredients);
		assertVanillaRecipe(helper, blasting, RecipeType.BLASTING, ingredients);
		assertVanillaRecipe(helper, smoking, RecipeType.SMOKING, ingredients);
		assertVanillaRecipe(helper, campfire, RecipeType.CAMPFIRE_COOKING, ingredients);
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "A cooking interface extension affects only its selected category, and the same interface can have different extensions in other categories.")
	public static void cookingCategoryRegistrationsAreIndependent(JeiGameTestHelper helper) {
		var smelting = new FurnaceSmeltingCategory(TestGuiHelper.INSTANCE);
		var blasting = new BlastingCategory(TestGuiHelper.INSTANCE);
		var smoking = new SmokingCategory(TestGuiHelper.INSTANCE);
		var campfire = new CampfireCookingCategory(TestGuiHelper.INSTANCE);
		var ingredients = TestIngredientManagers.createVanillaItemStackIngredientManager(helper.getLevel());

		assertVanillaRecipe(helper, smelting, RecipeType.SMELTING, ingredients);
		assertVanillaRecipe(helper, blasting, RecipeType.BLASTING, ingredients);
		assertVanillaRecipe(helper, smoking, RecipeType.SMOKING, ingredients);
		assertVanillaRecipe(helper, campfire, RecipeType.CAMPFIRE_COOKING, ingredients);
		smelting.addExtension(Recipe.class, singleOutput(Items.DIAMOND));
		assertOutput(helper, smelting, RecipeType.SMELTING, ingredients, Items.DIAMOND);
		assertVanillaRecipe(helper, blasting, RecipeType.BLASTING, ingredients);
		assertVanillaRecipe(helper, smoking, RecipeType.SMOKING, ingredients);
		assertVanillaRecipe(helper, campfire, RecipeType.CAMPFIRE_COOKING, ingredients);

		blasting.addExtension(Recipe.class, singleOutput(Items.EMERALD));
		smoking.addExtension(Recipe.class, singleOutput(Items.GOLD_INGOT));
		campfire.addExtension(Recipe.class, singleOutput(Items.IRON_INGOT));
		assertOutput(helper, smelting, RecipeType.SMELTING, ingredients, Items.DIAMOND);
		assertOutput(helper, blasting, RecipeType.BLASTING, ingredients, Items.EMERALD);
		assertOutput(helper, smoking, RecipeType.SMOKING, ingredients, Items.GOLD_INGOT);
		assertOutput(helper, campfire, RecipeType.CAMPFIRE_COOKING, ingredients, Items.IRON_INGOT);
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Specific interface and exact class registrations control the indexed ingredients regardless of registration order.")
	public static void specificExtensionsOverrideGeneralOnes(JeiGameTestHelper helper) {
		var ingredients = TestIngredientManagers.createVanillaItemStackIngredientManager(helper.getLevel());
		for (boolean registerGeneralFirst : List.of(true, false)) {
			var category = new FurnaceSmeltingCategory(TestGuiHelper.INSTANCE);
			if (registerGeneralFirst) {
				category.addExtension(DynamicCookingRecipe.class, EXTENSION);
			}
			category.addExtension(SpecificCookingRecipe.class, singleOutput(Items.DIAMOND));
			if (!registerGeneralFirst) {
				category.addExtension(DynamicCookingRecipe.class, EXTENSION);
			}
			RecipeHolder<SmeltingRecipe> recipe = holder(new SpecificSmeltingRecipe());
			assertItems(helper, List.of(Items.DIAMOND), collect(category, recipe, ingredients), RecipeIngredientRole.OUTPUT);
			category.addExtension(SpecificSmeltingRecipe.class, singleOutput(Items.EMERALD));
			assertItems(helper, List.of(Items.EMERALD), collect(category, recipe, ingredients), RecipeIngredientRole.OUTPUT);
		}
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Ambiguous cooking interfaces are not handled until an exact class registration resolves the conflict.")
	public static void ambiguousInterfacesRequireDisambiguation(JeiGameTestHelper helper) {
		var category = new FurnaceSmeltingCategory(TestGuiHelper.INSTANCE);
		category.addExtension(DynamicCookingRecipe.class, EXTENSION);
		category.addExtension(OtherCookingRecipe.class, singleOutput(Items.EMERALD));
		RecipeHolder<SmeltingRecipe> recipe = holder(new AmbiguousSmeltingRecipe());
		var ingredients = TestIngredientManagers.createVanillaItemStackIngredientManager(helper.getLevel());
		helper.assertTrue(!category.isHandled(recipe), "Ambiguous recipe must not be handled");
		assertItems(helper, List.of(), collect(category, recipe, ingredients), RecipeIngredientRole.OUTPUT);
		category.addExtension(AmbiguousSmeltingRecipe.class, EXTENSION);
		assertItems(helper, List.of(Items.IRON_INGOT, Items.GOLD_INGOT), collect(category, recipe, ingredients), RecipeIngredientRole.OUTPUT);
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Extension fuel and remainders are indexed, and the chosen fuel transfers into each furnace type.")
	public static void fuelOverridesAreIndexedAndTransferred(JeiGameTestHelper helper) {
		assertFuelTransfer(helper, new FurnaceSmeltingCategory(TestGuiHelper.INSTANCE), firstVanillaRecipe(helper, RecipeType.SMELTING), FurnaceMenu.class, MenuType.FURNACE, FurnaceMenu::new);
		assertFuelTransfer(helper, new SmokingCategory(TestGuiHelper.INSTANCE), firstVanillaRecipe(helper, RecipeType.SMOKING), SmokerMenu.class, MenuType.SMOKER, SmokerMenu::new);
		assertFuelTransfer(helper, new BlastingCategory(TestGuiHelper.INSTANCE), firstVanillaRecipe(helper, RecipeType.BLASTING), BlastFurnaceMenu.class, MenuType.BLAST_FURNACE, BlastFurnaceMenu::new);
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Empty fuel removes the fuel slot, and ordinary fuel stays display-only when wrapped with a remainder.")
	public static void fuelOverridesCanDisableSpecificFuelTransfer(JeiGameTestHelper helper) {
		var ingredients = TestIngredientManagers.createVanillaItemStackIngredientManager(helper.getLevel());
		RecipeHolder<SmeltingRecipe> recipe = firstVanillaRecipe(helper, RecipeType.SMELTING);
		for (SlotDisplay fuel : List.of(SlotDisplay.Empty.INSTANCE, new SlotDisplay.WithRemainder(SlotDisplay.AnyFuel.INSTANCE, new SlotDisplay.ItemSlotDisplay(Items.BUCKET)))) {
			var category = new FurnaceSmeltingCategory(TestGuiHelper.INSTANCE);
			category.addExtension(AbstractCookingRecipe.class, new CookingCategoryExtension() {
				@Override
				public SlotDisplay getFuel(AbstractCookingRecipe ignoredRecipe, SlotDisplay ignoredFuel) {
					return fuel;
				}
			});
			var indexed = collect(category, recipe, ingredients);
			var display = (FurnaceRecipeDisplay) recipe.value().display().getFirst();
			helper.assertEquals(display.ingredient().resolveForStacks(new ContextMap.Builder().create(new ContextKeySet.Builder().build())).stream().map(ItemStack::getItem).toList(), items(indexed, RecipeIngredientRole.INPUT), "Fuel must not be indexed as an input");
			var menu = helper.openMenu(FurnaceMenu::new);
			var transfer = new CookingRecipeTransferInfo<>(FurnaceMenu.class, MenuType.FURNACE, RecipeTypes.SMELTING, holder -> category.getFuel(holder.value()));
			helper.assertEquals(1, transfer.getRecipeSlots(menu, recipe).size(), "Only the cooking input transfers");
		}
		helper.succeed();
	}

	@SuppressWarnings("removal")
	private static <C extends AbstractFurnaceMenu, T extends AbstractCookingRecipe> void assertFuelTransfer(
		JeiGameTestHelper helper,
		AbstractCookingCategory<T> category,
		RecipeHolder<T> recipe,
		Class<C> menuClass,
		MenuType<C> menuType,
		MenuFactory<C> menuFactory
	) {
		category.addExtension(AbstractCookingRecipe.class, new CookingCategoryExtension() {
			@Override
			public SlotDisplay getFuel(AbstractCookingRecipe ignoredRecipe, SlotDisplay ignoredFuel) {
				return new SlotDisplay.WithRemainder(new SlotDisplay.ItemSlotDisplay(Items.LAVA_BUCKET), new SlotDisplay.ItemSlotDisplay(Items.BUCKET));
			}
		});
		var ingredients = TestIngredientManagers.createVanillaItemStackIngredientManager(helper.getLevel());
		var indexed = collect(category, recipe, ingredients);
		helper.assertEquals(Items.LAVA_BUCKET, items(indexed, RecipeIngredientRole.INPUT).getLast(), "Indexed extension fuel");
		helper.assertEquals(Items.BUCKET, items(indexed, RecipeIngredientRole.OUTPUT).getLast(), "Indexed fuel remainder");

		ItemStack input = ((FurnaceRecipeDisplay) recipe.value().display().getFirst()).ingredient().resolveForStacks(SlotDisplayContext.fromLevel(helper.getLevel())).getFirst();
		C menu = helper.openMenu(menuFactory, input.copy(), new ItemStack(Items.LAVA_BUCKET));
		var transferInfo = new CookingRecipeTransferInfo<>(menuClass, menuType, category.getRecipeType(), holder -> category.getFuel(holder.value()));
		var connection = helper.createServerConnection();
		var transferHelper = new RecipeTransferHandlerHelper(new TestStackHelper(), ingredients, new CraftingRecipeCategory(TestGuiHelper.INSTANCE), connection);
		var handler = transferHelper.createUnregisteredRecipeTransferHandler(transferInfo);
		var error = helper.runWithServerConnection(connection, ignored -> handler.transferRecipe(menu, recipe, () -> List.of(TestRecipeSlotView.item(input), TestRecipeSlotView.item(Items.LAVA_BUCKET)), helper.getPlayer(), false, true));
		helper.assertTrue(error == null, "Fuel recipe transfer must succeed");
		helper.assertSameStack(input, menu.getSlot(0).getItem(), "Transferred cooking input");
		helper.assertSameStack(new ItemStack(Items.LAVA_BUCKET), menu.getSlot(1).getItem(), "Transferred extension fuel");
	}

	private static <T extends AbstractCookingRecipe> void assertLinkedRecipe(
		JeiGameTestHelper helper,
		AbstractCookingCategory<T> category,
		T recipe,
		IIngredientManagerInternal ingredientManager,
		boolean hasFuel
	) {
		RecipeHolder<T> holder = holder(recipe);
		helper.assertTrue(category.isHandled(holder), "Extension must handle " + recipe.getType());
		RecipeIngredientSupplier ingredients = collect(category, holder, ingredientManager);
		assertItems(helper, List.of(Items.RAW_IRON, Items.RAW_GOLD), ingredients, RecipeIngredientRole.INPUT);
		assertItems(helper, List.of(Items.IRON_INGOT, Items.GOLD_INGOT), ingredients, RecipeIngredientRole.OUTPUT);
		helper.assertEquals(1, ingredients.getFocusLinks().size(), "Linked ingredient pairs");
		var link = ingredients.getFocusLinks().getFirst();
		for (int i = 0; i < 2; i++) {
			for (RecipeIngredientRole role : List.of(RecipeIngredientRole.INPUT, RecipeIngredientRole.OUTPUT)) {
				ItemStack stack = ingredients.getIngredients(role).get(i).getItemStack().orElseThrow();
				Focus<ItemStack> focus = Focus.createFromApi(ingredientManager, role, VanillaTypes.ITEM_STACK, stack);
				helper.assertEquals(Set.of(i), link.getVisibleIngredientIndexes(focus, ingredientManager, ignored -> true), "Focused pair from " + role);
			}
		}
		var allRoles = IngredientSupplierHelper.getIngredientSupplier(holder, category, ingredientManager, SlotDisplayContext.fromLevel(helper.getLevel()));
		helper.assertEquals(hasFuel, !allRoles.getIngredients(RecipeIngredientRole.RENDER_ONLY).isEmpty(), "Fuel display for " + recipe.getType());
	}

	private static <T extends AbstractCookingRecipe> void assertVanillaRecipe(JeiGameTestHelper helper, AbstractCookingCategory<T> category, RecipeType<T> recipeType, IIngredientManagerInternal ingredientManager) {
		RecipeHolder<T> recipe = firstVanillaRecipe(helper, recipeType);
		var display = (FurnaceRecipeDisplay) recipe.value().display().getFirst();
		var ingredients = collect(category, recipe, ingredientManager);
		helper.assertEquals(display.ingredient().resolveForStacks(new ContextMap.Builder().create(new ContextKeySet.Builder().build())).stream().map(ItemStack::getItem).toList(), items(ingredients, RecipeIngredientRole.INPUT), "Vanilla inputs");
		helper.assertEquals(display.result().resolveForStacks(new ContextMap.Builder().create(new ContextKeySet.Builder().build())).stream().map(ItemStack::getItem).toList(), items(ingredients, RecipeIngredientRole.OUTPUT), "Vanilla outputs");
		helper.assertTrue(ingredients.getFocusLinks().isEmpty(), "Vanilla slots are independent");
	}

	private static <T extends AbstractCookingRecipe> void assertOutput(JeiGameTestHelper helper, AbstractCookingCategory<T> category, RecipeType<T> recipeType, IIngredientManagerInternal ingredientManager, Item expected) {
		assertItems(helper, List.of(expected), collect(category, firstVanillaRecipe(helper, recipeType), ingredientManager), RecipeIngredientRole.OUTPUT);
	}

	private static <T extends AbstractCookingRecipe> RecipeHolder<T> firstVanillaRecipe(JeiGameTestHelper helper, RecipeType<T> recipeType) {
		var recipes = helper.getLevel().getServer().getRecipeManager().recipeMap().byType(recipeType);
		return recipes.iterator().next();
	}

	private static <T extends AbstractCookingRecipe> RecipeIngredientSupplier collect(AbstractCookingCategory<T> category, RecipeHolder<T> recipe, IIngredientManagerInternal ingredients) {
		return IngredientSupplierHelper.getIngredientSupplier(recipe, category, ingredients, new ContextMap.Builder().create(new ContextKeySet.Builder().build()), Set.of(RecipeIngredientRole.INPUT, RecipeIngredientRole.OUTPUT));
	}

	private static <T extends AbstractCookingRecipe> RecipeHolder<T> holder(T recipe) {
		return new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath("jeitests", "dynamic_cooking")), recipe);
	}

	private static <R> ICookingCategoryExtension<R> singleOutput(Item result) {
		return (recipe, builder, input, output, focuses) -> {
			input.add(Items.RAW_IRON);
			output.add(result);
		};
	}

	private static void assertItems(JeiGameTestHelper helper, List<Item> expected, RecipeIngredientSupplier ingredients, RecipeIngredientRole role) {
		helper.assertEquals(expected, items(ingredients, role), "Indexed " + role + " ingredients");
	}

	private static List<Item> items(RecipeIngredientSupplier ingredients, RecipeIngredientRole role) {
		return ingredients.getIngredients(role).stream()
			.map(ingredient -> ingredient.getItemStack().orElseThrow().getItem())
			.toList();
	}

	private interface DynamicCookingRecipe {
		default List<ItemStack> inputs() {
			return List.of(new ItemStack(Items.RAW_IRON), new ItemStack(Items.RAW_GOLD));
		}

		default List<ItemStack> outputs() {
			return List.of(new ItemStack(Items.IRON_INGOT), new ItemStack(Items.GOLD_INGOT));
		}
	}

	private interface SpecificCookingRecipe extends DynamicCookingRecipe {
	}

	private interface OtherCookingRecipe {
	}

	private static class DynamicSmeltingRecipe extends SmeltingRecipe implements DynamicCookingRecipe {
		DynamicSmeltingRecipe() {
			super("", CookingBookCategory.MISC, Ingredient.of(Items.RAW_IRON), new ItemStack(Items.IRON_INGOT), 0.5f, 200);
		}

		@Override
		public List<RecipeDisplay> display() {
			return List.of();
		}
	}

	private static final class SpecificSmeltingRecipe extends DynamicSmeltingRecipe implements SpecificCookingRecipe {
	}

	private static final class AmbiguousSmeltingRecipe extends DynamicSmeltingRecipe implements OtherCookingRecipe {
	}

	private static final class DynamicBlastingRecipe extends BlastingRecipe implements DynamicCookingRecipe {
		DynamicBlastingRecipe() {
			super("", CookingBookCategory.MISC, Ingredient.of(Items.RAW_IRON), new ItemStack(Items.IRON_INGOT), 0.5f, 100);
		}

		@Override
		public List<RecipeDisplay> display() {
			return List.of();
		}
	}

	private static final class DynamicSmokingRecipe extends SmokingRecipe implements DynamicCookingRecipe {
		DynamicSmokingRecipe() {
			super("", CookingBookCategory.MISC, Ingredient.of(Items.RAW_IRON), new ItemStack(Items.IRON_INGOT), 0.5f, 100);
		}

		@Override
		public List<RecipeDisplay> display() {
			return List.of();
		}
	}

	private static final class DynamicCampfireRecipe extends CampfireCookingRecipe implements DynamicCookingRecipe {
		DynamicCampfireRecipe() {
			super("", CookingBookCategory.MISC, Ingredient.of(Items.RAW_IRON), new ItemStack(Items.IRON_INGOT), 0.5f, 400);
		}

		@Override
		public List<RecipeDisplay> display() {
			return List.of();
		}
	}
}
