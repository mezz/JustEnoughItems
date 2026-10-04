package mezz.jei.neoforge.tests.ingredients;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.common.ingredients.TypedIngredient;
import mezz.jei.neoforge.tests.lib.JeiGameTestHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;

@ForEachTest(groups = "typed_item_stacks")
public final class TypedItemStackGameTests {
	private TypedItemStackGameTests() {
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "A typed item stack hands out the same ItemStack when it is materialized again right away.")
	public static void materializedItemStackIsReusedWhenAccessedAgainRightAway(JeiGameTestHelper helper) {
		// Setup: create typed ingredients with and without data components.
		ItemStack source = new ItemStack(Items.DIAMOND);
		source.set(DataComponents.CUSTOM_NAME, Component.literal("original"));
		ITypedIngredient<ItemStack> typedIngredient = TypedIngredient.createUnvalidated(VanillaTypes.ITEM_STACK, source);
		ITypedIngredient<ItemStack> plainTypedIngredient = TypedIngredient.createUnvalidated(VanillaTypes.ITEM_STACK, new ItemStack(Items.DIAMOND));

		// Operation: materialize each typed ingredient twice in a row.
		ItemStack first = typedIngredient.getIngredient();
		ItemStack second = typedIngredient.getIngredient();
		ItemStack plainFirst = plainTypedIngredient.getIngredient();
		ItemStack plainSecond = plainTypedIngredient.getIngredient();

		// Assertions: each typed ingredient hands out the same ItemStack instance again.
		helper.assertTrue(first == second, "Expected the same ItemStack instance when materialized again right away");
		helper.assertTrue(plainFirst == plainSecond, "Expected the same plain ItemStack instance when materialized again right away");
		helper.assertTrue(first != plainFirst, "Expected each typed ingredient to keep its own ItemStack");
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "A typed item stack creates a new ItemStack one second after its last access.")
	public static void materializedItemStackIsRecreatedOneSecondAfterItsLastAccess(JeiGameTestHelper helper) {
		// Setup: create a typed ingredient and materialize it.
		ItemStack source = new ItemStack(Items.DIAMOND);
		source.set(DataComponents.CUSTOM_NAME, Component.literal("original"));
		ITypedIngredient<ItemStack> typedIngredient = TypedIngredient.createUnvalidated(VanillaTypes.ITEM_STACK, source);
		ItemStack first = typedIngredient.getIngredient();

		// Operation: wait for longer than the materialized ItemStack is kept, then materialize it again.
		try {
			Thread.sleep(1100);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw helper.createFailException("Interrupted while waiting for the materialized ItemStack to expire");
		}
		ItemStack second = typedIngredient.getIngredient();
		ItemStack third = typedIngredient.getIngredient();

		// Assertions: a new, equal ItemStack was created and is now the one that is reused.
		helper.assertTrue(first != second, "Expected a new ItemStack instance one second after the last access");
		helper.assertTrue(ItemStack.matches(first, second), "Expected the new ItemStack to match the previous one");
		helper.assertTrue(second == third, "Expected the new ItemStack instance to be reused");
		helper.succeed();
	}
}
