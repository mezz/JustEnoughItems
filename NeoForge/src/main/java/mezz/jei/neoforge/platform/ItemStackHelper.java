package mezz.jei.neoforge.platform;

import mezz.jei.common.platform.IPlatformItemStackHelper;
import mezz.jei.common.util.ErrorUtil;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class ItemStackHelper implements IPlatformItemStackHelper {
	private static final Logger LOGGER = LogManager.getLogger();

	@Override
	public int getBurnTime(ItemStack itemStack) {
		try {
			return itemStack.getBurnTime(null);
		} catch (RuntimeException | LinkageError e) {
			String itemStackInfo = ErrorUtil.getItemStackInfo(itemStack);
			LOGGER.error("Failed to check if item is fuel {}.", itemStackInfo, e);
			return 0;
		}
	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
		Item item = stack.getItem();
		return item.isBookEnchantable(stack, book);
	}

	@Override
	public Optional<String> getCreatorModId(ItemStack stack) {
		Item item = stack.getItem();
		String creatorModId = item.getCreatorModId(stack);
		return Optional.ofNullable(creatorModId);
	}

	@Override
	public ItemAttributeModifiers getItemAttributeModifiers(ItemStack stack) {
		return stack.getAttributeModifiers();
	}

	@Override
	public boolean canEnchant(Holder<Enchantment> enchantment, ItemStack ingredient) {
		return ingredient.supportsEnchantment(enchantment);
	}
}
