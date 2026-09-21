package mezz.jei.common.transfer;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * A recipe slot and the concrete item stacks that the client considers valid for it.
 */
public record RecipeTransferRequirement(int craftingSlotId, List<ItemStack> acceptedStacks) {
	public static final int MAX_REQUIREMENTS = 256;
	public static final int MAX_ALTERNATIVES = 256;
	public static final int MAX_TOTAL_ALTERNATIVES = 4096;
	public static final StreamCodec<RegistryFriendlyByteBuf, RecipeTransferRequirement> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT,
		RecipeTransferRequirement::craftingSlotId,
		ItemStack.OPTIONAL_UNTRUSTED_STREAM_CODEC.apply(ByteBufCodecs.list(MAX_ALTERNATIVES)),
		RecipeTransferRequirement::acceptedStacks,
		RecipeTransferRequirement::new
	);

	public RecipeTransferRequirement {
		acceptedStacks = acceptedStacks.stream()
			.map(ItemStack::copy)
			.toList();
	}
}
