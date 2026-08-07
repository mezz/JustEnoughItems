package mezz.jei.fabric.platform;

import mezz.jei.common.platform.IPlatformItemStackHelper;
import net.fabricmc.fabric.api.item.v1.EnchantingContext;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.providers.number.ResolvableNumber;

import java.util.Optional;

public class ItemStackHelper implements IPlatformItemStackHelper {
	@Override
	public int getBurnTime(ItemStack itemStack) {
		CookingFuel cookingFuel = itemStack.get(DataComponents.COOKING_FUEL);
		if (cookingFuel == null) {
			return 0;
		}
		return resolve(cookingFuel.burnTime());
	}

	private static int resolve(ResolvableNumber resolvableNumber) {
		if (resolvableNumber instanceof ResolvableNumber.Constant constant) {
			return Math.round(constant.value());
		}
		if (resolvableNumber instanceof ResolvableNumber.Reference reference &&
			reference.key().identifier().getNamespace().equals("minecraft")
		) {
			return switch (reference.key().identifier().getPath()) {
				case "cooking/time_bamboo", "cooking/time_wool_slabs" -> 50;
				case "cooking/time_wool_carpets" -> 67;
				case "cooking/time_dry_plants", "cooking/time_wood_items_extra_small", "cooking/time_wool" -> 100;
				case "cooking/time_wood_slabs" -> 150;
				case "cooking/time_wood_items_large" -> 200;
				case "cooking/time_roots", "cooking/time_wood_blocks", "cooking/time_wood_items_small" -> 300;
				case "cooking/time_hanging_signs" -> 800;
				case "cooking/time_boats" -> 1200;
				case "cooking/time_coal" -> 1600;
				case "cooking/time_blaze_rod" -> 2400;
				case "cooking/time_dried_kelp_block" -> 4001;
				case "cooking/time_coal_block" -> 16000;
				case "cooking/time_lava_bucket" -> 20000;
				default -> 0;
			};
		}
		return 0;
	}

	@Override
	public Optional<String> getCreatorModId(ItemStack stack) {
		return Optional.of(stack.getCreatorNamespace());
	}

	@Override
	public boolean canEnchant(Holder<Enchantment> enchantment, ItemStack ingredient) {
		return ingredient.canBeEnchantedWith(enchantment, EnchantingContext.ACCEPTABLE);
	}
}
