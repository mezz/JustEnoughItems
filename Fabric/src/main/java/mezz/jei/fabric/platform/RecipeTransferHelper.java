package mezz.jei.fabric.platform;

import mezz.jei.common.platform.IPlatformRecipeTransferHelper;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public class RecipeTransferHelper implements IPlatformRecipeTransferHelper {
	@Override
	public boolean hasItemHandler(ItemStack itemStack) {
		return false;
	}

	@Override
	public Optional<List<ItemStack>> getItemHandlerContents(ItemStack itemStack) {
		return Optional.empty();
	}
}
