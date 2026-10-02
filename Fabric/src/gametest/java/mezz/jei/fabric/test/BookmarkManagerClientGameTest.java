package mezz.jei.fabric.test;

import com.mojang.serialization.JsonOps;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IBookmarkManager;
import mezz.jei.common.Internal;
import mezz.jei.common.transfer.RecipeTransferService;
import mezz.jei.gui.bookmarks.BookmarkCodec;
import mezz.jei.gui.bookmarks.BookmarkFactory;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.bookmarks.IBookmark;
import mezz.jei.gui.overlay.bookmarks.BookmarkOverlay;
import mezz.jei.test.lib.JUnitXmlTestReporter;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/** Exercises recipe bookmarks through the public API, real recipe layouts, and bookmark serialization. */
@SuppressWarnings("UnstableApiUsage")
public class BookmarkManagerClientGameTest implements FabricClientGameTest, IModPlugin {
	private static final IRecipeType<TestRecipe> TYPE = IRecipeType.create("jei_test", "bookmarks", TestRecipe.class);
	private static final IRecipeType<TestRecipe> OTHER_TYPE = IRecipeType.create("jei_test", "other_bookmarks", TestRecipe.class);
	private static final TestRecipe FIRST = recipe("first", LayoutKind.OUTPUT);
	private static final TestRecipe SECOND = recipe("second", LayoutKind.OUTPUT);
	private static final TestRecipe OVERRIDE = recipe("override", LayoutKind.OVERRIDE);
	private static final TestRecipe INPUT = recipe("input", LayoutKind.INPUT);
	private static final TestRecipe EMPTY = recipe("empty", LayoutKind.EMPTY);
	private static final TestRecipe NO_IDENTIFIER = new TestRecipe(null, LayoutKind.OUTPUT);

	@Override
	public Identifier getPluginUid() {
		return Identifier.fromNamespaceAndPath("jei_test", "bookmark_manager");
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		registration.addRecipeCategories(new TestCategory(TYPE), new TestCategory(OTHER_TYPE));
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		registration.addRecipes(TYPE, List.of(FIRST, SECOND, OVERRIDE, INPUT, EMPTY, NO_IDENTIFIER));
		registration.addRecipes(OTHER_TYPE, List.of(FIRST));
	}

	@Override
	public void runTest(ClientGameTestContext context) {
		JUnitXmlTestReporter.runAndReport("fabric-client-gametest", getClass().getSimpleName(), () -> {
			try (TestSingleplayerContext ignored = context.worldBuilder().create()) {
				JeiFabricClientGameTestAssertions.assertJeiStartedWithSyncedRecipes(context);
				checkBookmarks(context);
			}
		});
	}

	private static void checkBookmarks(ClientGameTestContext context) {
		context.runOnClient(client -> {
			var runtime = Internal.getJeiRuntime();
			IBookmarkManager manager = runtime.getBookmarkManager();
			BookmarkList bookmarks = ((BookmarkOverlay) runtime.getBookmarkOverlay()).getBookmarkList();
			List<IBookmark> originalBookmarks = getBookmarks(bookmarks);
			bookmarks.setFromConfigFile(List.of());
			try {
				check(!manager.contains(TYPE, FIRST), "An absent recipe must not be bookmarked");
				check(!manager.remove(TYPE, FIRST), "Removing an absent recipe must return false");
				check(manager.add(TYPE, FIRST), "The first recipe must be added");
				TestRecipe equivalentRecipe = new TestRecipe(FIRST.id(), LayoutKind.INPUT);
				check(manager.contains(TYPE, equivalentRecipe), "Recipe identifiers must match across instances with different ingredients");
				check(!manager.add(TYPE, equivalentRecipe), "Equivalent recipes must not create duplicate bookmarks");
				check(manager.add(TYPE, SECOND), "Different recipes with the same output must both be added");
				check(!manager.contains(OTHER_TYPE, FIRST), "Recipe types must participate in bookmark identity");
				check(manager.add(OTHER_TYPE, FIRST), "The same identifier in a different recipe type must be added");
				check(bookmarks.getElements().size() == 3, "Expected three distinct recipe bookmarks");

				check(!manager.add(TYPE, NO_IDENTIFIER), "A recipe without an identifier must not be bookmarked");
				check(!manager.add(TYPE, EMPTY), "A recipe without inputs or outputs must not be bookmarked");
				check(bookmarks.getElements().size() == 3, "Rejected recipes must leave bookmarks unchanged");
				check(manager.add(TYPE, OVERRIDE), "A recipe with a display override must be added");
				var overridden = Objects.requireNonNull(bookmarks.getMatchingBookmark(TYPE, OVERRIDE));
				check(overridden.getDisplayIngredient().getItemStack().orElseThrow().is(Items.EMERALD),
					"The bookmark icon must honor the displayed output override");
				check(manager.add(TYPE, INPUT), "A recipe with only an input must be added");
				var input = Objects.requireNonNull(bookmarks.getMatchingBookmark(TYPE, INPUT));
				check(input.getDisplayIngredient().getItemStack().orElseThrow().is(Items.COAL),
					"A recipe without an output must use an input as its icon");

				var ingredients = runtime.getIngredientManager();
				var recipes = runtime.getRecipeManager();
				var codecHelper = runtime.getJeiHelpers().getCodecHelper();
				var registryAccess = Objects.requireNonNull(client.level).registryAccess();
				var bookmarkFactory = new BookmarkFactory(codecHelper, registryAccess, ingredients);
				var transferService = new RecipeTransferService(runtime.getRecipeTransferManager());
				var codec = BookmarkCodec.create(codecHelper, ingredients, recipes, transferService, bookmarkFactory).codec().listOf();
				var ops = registryAccess.createSerializationContext(JsonOps.INSTANCE);
				var encoded = codec.encodeStart(ops, getBookmarks(bookmarks)).getOrThrow();
				bookmarks.setFromConfigFile(List.of());
				bookmarks.setFromConfigFile(codec.parse(ops, encoded).getOrThrow());
				check(bookmarks.getElements().size() == 5, "All recipe bookmarks must survive serialization");
				check(manager.contains(TYPE, equivalentRecipe) && manager.contains(TYPE, SECOND) &&
					manager.contains(OTHER_TYPE, FIRST) && manager.contains(TYPE, OVERRIDE) && manager.contains(TYPE, INPUT),
					"Reloaded bookmarks must remain accessible through the public API");
				check(!manager.add(TYPE, equivalentRecipe), "A reloaded recipe must still deduplicate");
				check(manager.remove(TYPE, equivalentRecipe), "A reloaded recipe must be removable by identifier");
				check(!manager.contains(TYPE, FIRST), "The removed recipe must no longer be bookmarked");
				check(manager.contains(TYPE, SECOND) && manager.contains(OTHER_TYPE, FIRST),
					"Removing a recipe must preserve other identifiers and types");
				check(!manager.remove(TYPE, FIRST), "Removing the recipe again must return false");
			} finally {
				bookmarks.setFromConfigFile(originalBookmarks);
			}
		});
	}

	private static List<IBookmark> getBookmarks(BookmarkList bookmarks) {
		return bookmarks.getElements().stream()
			.flatMap(element -> element.getBookmark().stream())
			.toList();
	}

	private static TestRecipe recipe(String path, LayoutKind layout) {
		return new TestRecipe(Identifier.fromNamespaceAndPath("jei_test", path), layout);
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}

	private enum LayoutKind {
		OUTPUT,
		OVERRIDE,
		INPUT,
		EMPTY
	}

	private record TestRecipe(@Nullable Identifier id, LayoutKind layout) {}

	private record TestCategory(IRecipeType<TestRecipe> recipeType) implements IRecipeCategory<TestRecipe> {
		@Override
		public IRecipeType<TestRecipe> getRecipeType() {
			return recipeType;
		}

		@Override
		public Component getTitle() {
			return Component.literal("Bookmark API test");
		}

		@Override
		public int getWidth() {
			return 18;
		}

		@Override
		public int getHeight() {
			return 18;
		}

		@Override
		public @Nullable IDrawable getIcon() {
			return null;
		}

		@Override
		public void setRecipe(IRecipeLayoutBuilder builder, TestRecipe recipe, IFocusGroup focuses) {
			switch (recipe.layout()) {
				case OUTPUT, OVERRIDE -> builder.addOutputSlot(0, 0).add(Items.DIAMOND);
				case INPUT -> builder.addInputSlot(0, 0).add(Items.COAL);
				case EMPTY -> {}
			}
		}

		@Override
		public void onDisplayedIngredientsUpdate(TestRecipe recipe, List<IRecipeSlotDrawable> slots, IFocusGroup focuses) {
			if (recipe.layout() == LayoutKind.OVERRIDE) {
				slots.getFirst().createDisplayOverrides().add(new ItemStack(Items.EMERALD));
			}
		}

		@Override
		public @Nullable Identifier getIdentifier(TestRecipe recipe) {
			return recipe.id();
		}
	}
}
