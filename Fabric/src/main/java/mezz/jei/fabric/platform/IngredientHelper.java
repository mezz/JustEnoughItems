package mezz.jei.fabric.platform;

import mezz.jei.common.platform.IPlatformIngredientHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Compostable;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.providers.number.ResolvableNumber;

public class IngredientHelper implements IPlatformIngredientHelper {
	@Override
	public float getCompostValue(ItemStack itemStack) {
		Compostable compostable = itemStack.get(DataComponents.COMPOSTABLE);
		if (compostable == null) {
			return 0;
		}
		if (compostable.layers() instanceof ResolvableNumber.Constant constant) {
			return constant.value();
		}
		if (compostable.layers() instanceof ResolvableNumber.Reference reference &&
			reference.key().identifier().getNamespace().equals("minecraft")
		) {
			return switch (reference.key().identifier().getPath()) {
				case "compostable/low" -> 0.3f;
				case "compostable/low_medium" -> 0.5f;
				case "compostable/medium" -> 0.65f;
				case "compostable/medium_high" -> 0.85f;
				case "compostable/always_add_one" -> 1.0f;
				default -> 0;
			};
		}
		return 0;
	}

	@Override
	public HolderSet<Item> getSupportedItems(Holder<Enchantment> enchantment) {
		return enchantment.value()
			.getSupportedItems();
	}
}
