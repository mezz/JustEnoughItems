package mezz.jei.library.plugins.vanilla.ingredients;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public final class ItemStackCodecs {
	private static final Codec<DataComponentPatch> NBT_PRESERVING_COMPONENT_PATCH_CODEC = new Codec<>() {
		@Override
		public <T> DataResult<Pair<DataComponentPatch, T>> decode(DynamicOps<T> ops, T input) {
			Optional<String> snbt = ops.getStringValue(input).result();
			if (snbt.isPresent()) {
				return parseComponentPatch(ops, snbt.get())
					.map(componentPatch -> Pair.of(componentPatch, input));
			}
			return DataComponentPatch.CODEC.decode(ops, input);
		}

		@Override
		public <T> DataResult<T> encode(DataComponentPatch input, DynamicOps<T> ops, T prefix) {
			return DataComponentPatch.CODEC.encodeStart(createNbtOps(ops), input)
				.flatMap(tag -> Codec.STRING.encode(tag.toString(), ops, prefix));
		}
	};

	private ItemStackCodecs() {
	}

	public static Codec<ItemStack> createStrictSingleItemCodec() {
		return RecordCodecBuilder.<ItemStack>create((i) ->
				i.group(
					Item.CODEC.fieldOf("id")
						.forGetter(ItemStack::getItemHolder),
					NBT_PRESERVING_COMPONENT_PATCH_CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY)
						.forGetter(ItemStack::getComponentsPatch)
				)
				.apply(i, (item, components) -> new ItemStack(item, 1, components))
			)
			.validate(ItemStack::validateStrict);
	}

	private static DataResult<DataComponentPatch> parseComponentPatch(DynamicOps<?> ops, String snbt) {
		try {
			CompoundTag tag = TagParser.parseCompoundFully(snbt);
			return DataComponentPatch.CODEC.parse(createNbtOps(ops), tag);
		} catch (CommandSyntaxException e) {
			return DataResult.error(() -> "Failed to parse item stack components: " + e.getMessage());
		}
	}

	private static DynamicOps<Tag> createNbtOps(DynamicOps<?> ops) {
		if (ops instanceof RegistryOps<?> registryOps) {
			return registryOps.withParent(NbtOps.INSTANCE);
		}
		return NbtOps.INSTANCE;
	}
}
