package mezz.jei.test;

import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.ingredients.SubtypeManager;
import mezz.jei.load.registration.SubtypeRegistration;
import mezz.jei.plugins.vanilla.ingredients.item.ItemStackHelper;
import mezz.jei.util.StackHelper;
import net.minecraft.item.ItemStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EmptyItemStackUidTest {
	@Test
	public void getsUidForEmptyItemStack() {
		SubtypeManager subtypeManager = new SubtypeManager(new SubtypeRegistration());
		ItemStackHelper itemStackHelper = new ItemStackHelper(new StackHelper(subtypeManager));

		String uid = itemStackHelper.getUniqueId(ItemStack.EMPTY, UidContext.Ingredient);

		assertEquals("minecraft:air", uid);
	}
}
