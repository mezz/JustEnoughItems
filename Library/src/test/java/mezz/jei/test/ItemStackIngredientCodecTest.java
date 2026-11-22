package mezz.jei.test;

import com.mojang.serialization.JsonOps;
import mezz.jei.library.plugins.vanilla.ingredients.ItemStackCodecs;
import net.minecraft.DetectedVersion;
import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemStackIngredientCodecTest {
	@BeforeAll
	static void bootstrap() {
		SharedConstants.setVersion(DetectedVersion.BUILT_IN);
		Bootstrap.bootStrap();
		BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(VanillaRegistries.createWorldLookup())
			.forEach(DataComponentInitializers.PendingComponents::apply);
	}

	@Test
	void persistentVariantsRoundTripWithoutChangingTheirComponents() {
		ItemStack stack = new ItemStack(Items.DIAMOND);
		stack.set(DataComponents.CUSTOM_NAME, Component.literal("Named diamond"));
		CompoundTag data = new CompoundTag();
		data.putByte("variant", (byte) 1);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
		var codec = ItemStackCodecs.createStrictSingleItemCodec();
		var ops = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY).createSerializationContext(JsonOps.INSTANCE);

		var encoded = codec.encodeStart(ops, stack).getOrThrow();
		ItemStack decoded = codec.parse(ops, encoded).getOrThrow();

		assertTrue(ItemStack.isSameItemSameComponents(stack, decoded));
		assertFalse(ItemStack.isSameItemSameComponents(new ItemStack(Items.DIAMOND), decoded));
	}

	@Test
	void rejectsTransientComponentsInsteadOfSavingADifferentVariant() {
		ItemStack stack = new ItemStack(Items.DIAMOND);
		stack.set(DataComponents.CREATIVE_SLOT_LOCK, Unit.INSTANCE);
		var codec = ItemStackCodecs.createStrictSingleItemCodec();
		var ops = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY).createSerializationContext(JsonOps.INSTANCE);

		var result = codec.encodeStart(ops, stack);

		assertTrue(result.error().isPresent(), "Saving a locked stack must not silently discard its lock");
		assertTrue(result.error().orElseThrow().message().contains("creative_slot_lock"));
	}
}
