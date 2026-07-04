package mezz.jei.neoforge.tests.bookmarks;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.bookmarks.BookmarkFactory;
import mezz.jei.gui.bookmarks.IngredientBookmark;
import mezz.jei.library.focus.FocusFactory;
import mezz.jei.library.helpers.CodecHelper;
import mezz.jei.neoforge.tests.lib.JeiGameTestHelper;
import mezz.jei.neoforge.tests.lib.TestIngredientManagers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ForEachTest(groups = "bookmarks")
public final class BookmarkFactoryGameTests {
	private BookmarkFactoryGameTests() {
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Item bookmarks with the same item and different components are distinct.")
	public static void itemBookmarksWithDifferentComponentsAreDistinct(JeiGameTestHelper helper) {
		// Setup: these stacks have the same item id, but one has a custom item-name component.
		ItemStack plainSword = new ItemStack(Items.DIAMOND_SWORD);
		ItemStack namedSword = new ItemStack(Items.DIAMOND_SWORD);
		namedSword.set(DataComponents.ITEM_NAME, Component.literal("Excalibur"));

		IIngredientManager ingredientManager = TestIngredientManagers.createVanillaItemStackIngredientManager(List.of(
			plainSword,
			namedSword
		));
		BookmarkFactory bookmarkFactory = createBookmarkFactory(helper, ingredientManager);

		// Operation: bookmark both stacks through the real factory and item-stack codec path.
		IngredientBookmark<ItemStack> plainBookmark = createBookmark(helper, bookmarkFactory, ingredientManager, plainSword);
		IngredientBookmark<ItemStack> namedBookmark = createBookmark(helper, bookmarkFactory, ingredientManager, namedSword);
		Set<IngredientBookmark<ItemStack>> bookmarks = new HashSet<>();
		bookmarks.add(plainBookmark);
		bookmarks.add(namedBookmark);

		// Assertions: component differences participate in bookmark identity, so neither bookmark overwrites the other.
		helper.assertTrue(!plainBookmark.equals(namedBookmark), "Bookmarks with different item components should be distinct");
		helper.assertEquals(2, bookmarks.size(), "Bookmarks with different item components should both be retained");
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Item bookmarks with equivalent components are equal.")
	public static void itemBookmarksWithEquivalentComponentsAreEqual(JeiGameTestHelper helper) {
		// Setup: two separate stacks encode to the same item id and component payload.
		ItemStack firstSword = new ItemStack(Items.DIAMOND_SWORD);
		firstSword.set(DataComponents.ITEM_NAME, Component.literal("Excalibur"));
		ItemStack secondSword = firstSword.copy();

		IIngredientManager ingredientManager = TestIngredientManagers.createVanillaItemStackIngredientManager(List.of(
			firstSword,
			secondSword
		));
		BookmarkFactory bookmarkFactory = createBookmarkFactory(helper, ingredientManager);

		// Operation: create bookmarks from separate stack instances.
		IngredientBookmark<ItemStack> firstBookmark = createBookmark(helper, bookmarkFactory, ingredientManager, firstSword);
		IngredientBookmark<ItemStack> secondBookmark = createBookmark(helper, bookmarkFactory, ingredientManager, secondSword);

		// Assertions: equivalent serialized ingredients still deduplicate as the same bookmark.
		helper.assertEquals(firstBookmark, secondBookmark, "Bookmarks with equivalent item components should be equal");
		helper.assertEquals(firstBookmark.hashCode(), secondBookmark.hashCode(), "Equal bookmarks should have the same hash code");
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Item bookmarks with different stack counts are equal.")
	public static void itemBookmarksWithDifferentStackCountsAreEqual(JeiGameTestHelper helper) {
		// Setup: two stacks have the same item id and components, but different counts.
		ItemStack singleBlock = new ItemStack(Items.COBBLESTONE);
		ItemStack blockStack = new ItemStack(Items.COBBLESTONE, 32);

		IIngredientManager ingredientManager = TestIngredientManagers.createVanillaItemStackIngredientManager(List.of(
			singleBlock,
			blockStack
		));
		BookmarkFactory bookmarkFactory = createBookmarkFactory(helper, ingredientManager);

		// Operation: create bookmarks from typed ingredients that have not already been normalized.
		IngredientBookmark<ItemStack> singleBlockBookmark = createBookmark(helper, bookmarkFactory, ingredientManager, singleBlock, false);
		IngredientBookmark<ItemStack> blockStackBookmark = createBookmark(helper, bookmarkFactory, ingredientManager, blockStack, false);

		// Assertions: bookmark identity ignores stack count after factory normalization.
		helper.assertEquals(singleBlockBookmark, blockStackBookmark, "Bookmarks with different stack counts should be equal");
		helper.assertEquals(singleBlockBookmark.hashCode(), blockStackBookmark.hashCode(), "Equal bookmarks should have the same hash code");
		helper.succeed();
	}

	private static BookmarkFactory createBookmarkFactory(JeiGameTestHelper helper, IIngredientManager ingredientManager) {
		CodecHelper codecHelper = new CodecHelper(ingredientManager, new FocusFactory(ingredientManager));
		return new BookmarkFactory(codecHelper, helper.getLevel().registryAccess(), ingredientManager);
	}

	private static IngredientBookmark<ItemStack> createBookmark(
		JeiGameTestHelper helper,
		BookmarkFactory bookmarkFactory,
		IIngredientManager ingredientManager,
		ItemStack stack
	) {
		return createBookmark(helper, bookmarkFactory, ingredientManager, stack, true);
	}

	private static IngredientBookmark<ItemStack> createBookmark(
		JeiGameTestHelper helper,
		BookmarkFactory bookmarkFactory,
		IIngredientManager ingredientManager,
		ItemStack stack,
		boolean normalize
	) {
		ITypedIngredient<ItemStack> typedIngredient = ingredientManager.createTypedIngredient(VanillaTypes.ITEM_STACK, stack, normalize)
			.orElseThrow(() -> helper.createFailException("Failed to create typed ingredient for " + stack));
		return bookmarkFactory.create(typedIngredient);
	}
}
