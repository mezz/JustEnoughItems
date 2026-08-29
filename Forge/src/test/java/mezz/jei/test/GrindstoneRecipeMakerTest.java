package mezz.jei.test;

import mezz.jei.common.platform.IPlatformRecipeHelper;
import mezz.jei.library.plugins.vanilla.grindstone.GrindstoneRecipeMaker;
import mezz.jei.test.lib.ForgeTestBootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class GrindstoneRecipeMakerTest {
	@BeforeAll
	public static void setup() {
		SharedConstants.tryDetectVersion();
		ForgeTestBootstrap.bootStrap();
	}

	@Test
	public void skipsEnchantmentsWhoseCompatibilityCheckThrows() throws ReflectiveOperationException {
		ItemStack stack = new ItemStack(Items.DIAMOND_SWORD);
		Enchantment enchantment = new Enchantment(
			Enchantment.Rarity.COMMON,
			EnchantmentCategory.WEAPON,
			new EquipmentSlot[]{EquipmentSlot.MAINHAND}
		) {
			@Override
			public boolean canEnchant(ItemStack stack) {
				throw new IllegalStateException("Test enchantability failure");
			}
		};
		IPlatformRecipeHelper platformHelper = createUnusedPlatformHelper();
		ResourceLocation enchantmentId = new ResourceLocation("test", "broken");

		Method canEnchant = GrindstoneRecipeMaker.class.getDeclaredMethod(
			"canEnchant",
			IPlatformRecipeHelper.class,
			ItemStack.class,
			Enchantment.class,
			ResourceLocation.class
		);
		canEnchant.setAccessible(true);
		Object result = Assertions.assertDoesNotThrow(() ->
			canEnchant.invoke(null, platformHelper, stack, enchantment, enchantmentId)
		);

		Assertions.assertEquals(false, result);
	}

	private static IPlatformRecipeHelper createUnusedPlatformHelper() {
		return (IPlatformRecipeHelper) Proxy.newProxyInstance(
			IPlatformRecipeHelper.class.getClassLoader(),
			new Class<?>[]{IPlatformRecipeHelper.class},
			(proxy, method, args) -> {
				throw new AssertionError("Platform helper should not be called after the enchantment check throws");
			}
		);
	}
}
