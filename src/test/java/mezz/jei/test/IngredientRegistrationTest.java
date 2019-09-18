package mezz.jei.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IIngredientType;
import mezz.jei.ingredients.IngredientBlacklistInternal;
import mezz.jei.ingredients.IngredientRegistry;
import mezz.jei.plugins.vanilla.ingredients.fluid.FluidStackHelper;
import mezz.jei.plugins.vanilla.ingredients.fluid.FluidStackRenderer;
import mezz.jei.plugins.vanilla.ingredients.item.ItemStackHelper;
import mezz.jei.plugins.vanilla.ingredients.item.ItemStackRenderer;
import mezz.jei.runtime.SubtypeRegistry;
import mezz.jei.startup.ModIngredientRegistration;
import mezz.jei.startup.StackHelper;
import mezz.jei.test.lib.TestModIdHelper;
import net.minecraft.item.ItemStack;
import org.junit.Assert;
import org.junit.Test;

public class IngredientRegistrationTest {
	@Test
	public void preservesPluginRegistrationOrder() {
		ModIngredientRegistration registration = new ModIngredientRegistration();
		IIngredientType<ItemStack> items = () -> ItemStack.class;
		registration.register(VanillaTypes.FLUID, Collections.emptyList(), new FluidStackHelper(), new FluidStackRenderer());
		registration.register(items, Collections.emptyList(), new ItemStackHelper(new StackHelper(new SubtypeRegistry())), new ItemStackRenderer());

		IngredientRegistry registry = registration.createIngredientRegistry(new TestModIdHelper(), new IngredientBlacklistInternal());

		Assert.assertEquals(Arrays.asList(VanillaTypes.FLUID, items), new ArrayList<>(registry.getRegisteredIngredientTypes()));
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsDuplicateRegistrationBeforeReplacingIngredientData() {
		ModIngredientRegistration registration = new ModIngredientRegistration();
		registration.register(VanillaTypes.FLUID, Collections.emptyList(), new FluidStackHelper(), new FluidStackRenderer());

		registration.register(VanillaTypes.FLUID, Collections.emptyList(), new FluidStackHelper(), new FluidStackRenderer());
	}
}
