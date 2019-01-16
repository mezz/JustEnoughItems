package mezz.jei.plugins.vanilla.brewing;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;

import mezz.jei.api.ISubtypeRegistry;

public class PotionSubtypeInterpreter implements ISubtypeRegistry.ISubtypeInterpreter {
	public static final PotionSubtypeInterpreter INSTANCE = new PotionSubtypeInterpreter();

	private PotionSubtypeInterpreter() {

	}

	@Override
	public String apply(ItemStack itemStack) {
		if (!itemStack.hasTagCompound()) {
			return ISubtypeRegistry.ISubtypeInterpreter.NONE;
		}
		PotionType potionType = PotionUtils.getPotionFromItem(itemStack);
		String potionTypeString = potionType.getNamePrefixed("");
		StringBuilder stringBuilder = new StringBuilder(potionTypeString);
		List<PotionEffect> effects = PotionUtils.getEffectsFromStack(itemStack);
		for (PotionEffect effect : effects) {
			stringBuilder.append(";").append(effect);
		}

		return stringBuilder.toString();
	}
}
