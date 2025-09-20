package mezz.jei.test;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.library.ingredients.TypedIngredient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

public class TypedItemStackTest {
	@Test
	public void itemStackNbtIsCopiedWhenTypedIngredientIsCreated() {
		// Setup: create a typed ingredient from an ItemStack with mutable NBT.
		ItemStack source = new ItemStack(Items.DIAMOND);
		CompoundTag sourceTag = new CompoundTag();
		sourceTag.putString("key", "original");
		source.setTag(sourceTag);

		// Operation: create the typed ingredient, then mutate the source tag.
		ITypedIngredient<ItemStack> typedIngredient = TypedIngredient.createUnvalidated(VanillaTypes.ITEM_STACK, source);
		sourceTag.putString("key", "mutated");
		sourceTag.putString("added", "mutated");

		// Assertions: the typed ingredient kept the original NBT data.
		CompoundTag typedTag = typedIngredient.getIngredient().getTag();
		assertNotNull(typedTag);
		assertEquals("original", typedTag.getString("key"));
		assertFalse(typedTag.contains("added"));
	}

	@Test
	public void itemStackNbtIsCopiedWhenTypedIngredientIsMaterialized() {
		// Setup: create a typed ingredient from an ItemStack with mutable NBT.
		ItemStack source = new ItemStack(Items.DIAMOND);
		CompoundTag sourceTag = new CompoundTag();
		sourceTag.putString("key", "original");
		source.setTag(sourceTag);

		// Operation: materialize the typed ingredient back to an ItemStack.
		ITypedIngredient<ItemStack> typedIngredient = TypedIngredient.createUnvalidated(VanillaTypes.ITEM_STACK, source);
		ItemStack materialized = typedIngredient.getIngredient();

		// Assertions: the materialized ItemStack does not reuse the source tag instance.
		CompoundTag materializedTag = materialized.getTag();
		assertNotNull(materializedTag);
		assertNotSame(sourceTag, materializedTag);
		assertEquals("original", materializedTag.getString("key"));
	}
}
