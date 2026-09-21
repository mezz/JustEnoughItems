package mezz.jei.neoforge.platform;

import mezz.jei.common.platform.IPlatformRecipeTransferHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RecipeTransferHelper implements IPlatformRecipeTransferHelper {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final int MAX_ITEM_HANDLER_SLOTS = 256;

	@Override
	public boolean hasItemHandler(ItemStack itemStack) {
		try {
			return getItemHandler(itemStack) != null;
		} catch (RuntimeException e) {
			LOGGER.error("Failed to inspect an item handler while planning recipe transfer", e);
			return false;
		}
	}

	@Override
	public Optional<List<ItemStack>> getItemHandlerContents(ItemStack itemStack) {
		try {
			ResourceHandler<ItemResource> itemHandler = getItemHandler(itemStack);
			if (itemHandler == null) {
				return Optional.empty();
			}

			int size = Math.min(itemHandler.size(), MAX_ITEM_HANDLER_SLOTS);
			if (size <= 0) {
				return Optional.of(List.of());
			}
			List<ItemStack> contents = new ArrayList<>(size);
			for (int slot = 0; slot < size; slot++) {
				ItemResource resource = itemHandler.getResource(slot);
				int amount = itemHandler.getAmountAsInt(slot);
				contents.add(resource.toStack(amount));
			}
			return Optional.of(contents);
		} catch (RuntimeException e) {
			LOGGER.error("Failed to read an item handler while planning recipe transfer", e);
			return Optional.empty();
		}
	}

	@Nullable
	static ResourceHandler<ItemResource> getItemHandler(ItemStack itemStack) {
		if (itemStack.isEmpty() || itemStack.getCount() != 1) {
			return null;
		}
		return ItemAccess.forStack(itemStack)
			.getCapability(Capabilities.Item.ITEM);
	}
}
