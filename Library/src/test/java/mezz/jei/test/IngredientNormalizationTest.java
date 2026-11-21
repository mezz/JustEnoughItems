package mezz.jei.test;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.common.util.RegistryUtil;
import mezz.jei.common.util.StackHelper;
import mezz.jei.library.ingredients.IngredientManager;
import mezz.jei.library.ingredients.subtypes.SubtypeInterpreters;
import mezz.jei.library.ingredients.subtypes.SubtypeManager;
import mezz.jei.library.load.registration.IngredientManagerBuilder;
import mezz.jei.library.plugins.vanilla.ingredients.ItemStackCodecs;
import mezz.jei.library.plugins.vanilla.ingredients.ItemStackHelper;
import mezz.jei.library.render.ItemStackRenderer;
import net.minecraft.DetectedVersion;
import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IngredientNormalizationTest {
	@BeforeAll
	static void setup() {
		SharedConstants.setVersion(DetectedVersion.BUILT_IN);
		Bootstrap.bootStrap();
		// Bind vanilla components so ItemStacks can use the bootstrapped item holders.
		BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(VanillaRegistries.createWorldLookup())
			.forEach(DataComponentInitializers.PendingComponents::apply);
	}

	@BeforeEach
	void setRegistryAccess() {
		RegistryUtil.setRegistryAccess(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
	}

	@AfterEach
	void clearRegistryAccess() {
		RegistryUtil.setRegistryAccess(null);
	}

	@ParameterizedTest
	@EnumSource(Registration.class)
	void itemListNormalizesCountsWithoutChangingComponentsOrSourceStacks(Registration registration) {
		ItemStack stack = new ItemStack(Items.DIAMOND, 64);
		Component name = Component.literal("Named diamond");
		stack.set(DataComponents.CUSTOM_NAME, name);
		List<ItemStack> initialIngredients = List.of();
		if (registration == Registration.INITIAL) {
			initialIngredients = List.of(stack);
		}
		IngredientManagerBuilder builder = createBuilder(initialIngredients);
		if (registration == Registration.EXTRA) {
			builder.addExtraIngredients(VanillaTypes.ITEM_STACK, List.of(stack));
		}
		IngredientManager ingredientManager = builder.build();
		if (registration == Registration.RUNTIME) {
			ingredientManager.addIngredientsAtRuntime(VanillaTypes.ITEM_STACK, List.of(stack));
		}

		List<ITypedIngredient<ItemStack>> ingredients = List.copyOf(ingredientManager.getAllTypedIngredients(VanillaTypes.ITEM_STACK));
		assertEquals(1, ingredients.size());
		ItemStack registered = ingredients.getFirst().getIngredient();
		assertEquals(1, registered.getCount());
		assertTrue(ItemStack.isSameItemSameComponents(stack, registered));
		assertEquals(64, stack.getCount());
		assertEquals(name, stack.get(DataComponents.CUSTOM_NAME));
	}

	private static IngredientManagerBuilder createBuilder(List<ItemStack> ingredients) {
		SubtypeManager subtypeManager = new SubtypeManager(new SubtypeInterpreters());
		IngredientManagerBuilder builder = new IngredientManagerBuilder(subtypeManager, DummyColorHelper.INSTANCE, ContextMap.EMPTY);
		builder.register(
			VanillaTypes.ITEM_STACK,
			ingredients,
			new ItemStackHelper(new StackHelper(subtypeManager), DummyColorHelper.INSTANCE),
			new ItemStackRenderer(),
			ItemStackCodecs.createStrictSingleItemCodec()
		);
		return builder;
	}

	private enum Registration {
		INITIAL,
		EXTRA,
		RUNTIME
	}
}
