package mezz.jei.test;

import mezz.jei.common.util.StackHelper;
import mezz.jei.library.ingredients.subtypes.SubtypeInterpreters;
import mezz.jei.library.ingredients.subtypes.SubtypeManager;
import mezz.jei.library.plugins.vanilla.ingredients.ItemStackHelper;
import mezz.jei.test.lib.ForgeTestBootstrap;
import mezz.jei.test.lib.TestColorHelper;

import net.minecraft.DetectedVersion;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ItemStackHelperTest {
	@BeforeAll
	public static void setup() {
		SharedConstants.setVersion(DetectedVersion.BUILT_IN);
		ForgeTestBootstrap.bootStrap();
	}

	@Test
	public void registeredItemsAreOnServer() {
		// Setup: create the item stack helper the way the vanilla plugin does.
		SubtypeManager subtypeManager = new SubtypeManager(new SubtypeInterpreters());
		ItemStackHelper itemStackHelper = new ItemStackHelper(subtypeManager, new StackHelper(subtypeManager), new TestColorHelper());

		// Operation and assertions: every registered item is found, also when the same helper is asked again.
		for (Item item : BuiltInRegistries.ITEM) {
			assertTrue(itemStackHelper.isIngredientOnServer(new ItemStack(item)), () -> "Expected item to be on the server: " + item);
		}
		assertTrue(itemStackHelper.isIngredientOnServer(new ItemStack(Items.DIAMOND)));
	}
}
