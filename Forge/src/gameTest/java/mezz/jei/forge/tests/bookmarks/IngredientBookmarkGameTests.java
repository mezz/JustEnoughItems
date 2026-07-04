package mezz.jei.forge.tests.bookmarks;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.bookmarks.IngredientBookmark;
import mezz.jei.forge.tests.lib.JeiGameTestHelper;
import mezz.jei.forge.tests.lib.TestIngredientManagers;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@GameTestHolder("jei")
@PrefixGameTestTemplate(false)
public final class IngredientBookmarkGameTests {
	private IngredientBookmarkGameTests() {
	}

	@GameTest(template = "empty")
	public static void itemBookmarksWithDifferentNbtAreDistinct(GameTestHelper gameTestHelper) {
		JeiGameTestHelper helper = new JeiGameTestHelper(gameTestHelper);
		// Setup: these stacks have the same item id, but one has a custom name tag.
		ItemStack plainSword = new ItemStack(Items.DIAMOND_SWORD);
		ItemStack namedSword = new ItemStack(Items.DIAMOND_SWORD);
		namedSword.setHoverName(Component.literal("Excalibur"));

		IIngredientManager ingredientManager = TestIngredientManagers.createVanillaItemStackIngredientManager(List.of(
			plainSword,
			namedSword
		));

		// Operation: bookmark both stacks through the real ingredient bookmark creation path.
		IngredientBookmark<ItemStack> plainBookmark = createBookmark(helper, ingredientManager, plainSword);
		IngredientBookmark<ItemStack> namedBookmark = createBookmark(helper, ingredientManager, namedSword);
		Set<IngredientBookmark<ItemStack>> bookmarks = new HashSet<>();
		bookmarks.add(plainBookmark);
		bookmarks.add(namedBookmark);

		// Assertions: component differences participate in bookmark identity, so neither bookmark overwrites the other.
		helper.assertTrue(!plainBookmark.equals(namedBookmark), "Bookmarks with different item NBT should be distinct");
		helper.assertEquals(2, bookmarks.size(), "Bookmarks with different item NBT should both be retained");
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void itemBookmarksWithEquivalentNbtAreEqual(GameTestHelper gameTestHelper) {
		JeiGameTestHelper helper = new JeiGameTestHelper(gameTestHelper);
		// Setup: two separate stacks encode to the same item id and component payload.
		ItemStack firstSword = new ItemStack(Items.DIAMOND_SWORD);
		firstSword.setHoverName(Component.literal("Excalibur"));
		ItemStack secondSword = firstSword.copy();

		IIngredientManager ingredientManager = TestIngredientManagers.createVanillaItemStackIngredientManager(List.of(
			firstSword,
			secondSword
		));

		// Operation: create bookmarks from separate stack instances.
		IngredientBookmark<ItemStack> firstBookmark = createBookmark(helper, ingredientManager, firstSword);
		IngredientBookmark<ItemStack> secondBookmark = createBookmark(helper, ingredientManager, secondSword);

		// Assertions: equivalent serialized ingredients still deduplicate as the same bookmark.
		helper.assertEquals(firstBookmark, secondBookmark, "Bookmarks with equivalent item NBT should be equal");
		helper.assertEquals(firstBookmark.hashCode(), secondBookmark.hashCode(), "Equal bookmarks should have the same hash code");
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void itemBookmarksWithDifferentStackCountsAreEqual(GameTestHelper gameTestHelper) {
		JeiGameTestHelper helper = new JeiGameTestHelper(gameTestHelper);
		// Setup: two stacks have the same item id and NBT, but different counts.
		ItemStack singleBlock = new ItemStack(Items.COBBLESTONE);
		ItemStack blockStack = new ItemStack(Items.COBBLESTONE, 32);

		IIngredientManager ingredientManager = TestIngredientManagers.createVanillaItemStackIngredientManager(List.of(
			singleBlock,
			blockStack
		));

		// Operation: create bookmarks from typed ingredients that have not already been normalized.
		IngredientBookmark<ItemStack> singleBlockBookmark = createBookmark(helper, ingredientManager, singleBlock, false);
		IngredientBookmark<ItemStack> blockStackBookmark = createBookmark(helper, ingredientManager, blockStack, false);

		// Assertions: bookmark identity ignores stack count after bookmark normalization.
		helper.assertEquals(singleBlockBookmark, blockStackBookmark, "Bookmarks with different stack counts should be equal");
		helper.assertEquals(singleBlockBookmark.hashCode(), blockStackBookmark.hashCode(), "Equal bookmarks should have the same hash code");
		helper.succeed();
	}

	private static IngredientBookmark<ItemStack> createBookmark(
		JeiGameTestHelper helper,
		IIngredientManager ingredientManager,
		ItemStack stack
	) {
		return createBookmark(helper, ingredientManager, stack, true);
	}

	private static IngredientBookmark<ItemStack> createBookmark(
		JeiGameTestHelper helper,
		IIngredientManager ingredientManager,
		ItemStack stack,
		boolean normalize
	) {
		ITypedIngredient<ItemStack> typedIngredient = ingredientManager.createTypedIngredient(VanillaTypes.ITEM_STACK, stack, normalize)
			.orElseThrow(() -> helper.createFailException("Failed to create typed ingredient for " + stack));
		return IngredientBookmark.create(typedIngredient, ingredientManager);
	}
}
