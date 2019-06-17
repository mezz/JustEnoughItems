package mezz.jei.plugins.vanilla.brewing;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;

import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;

public class PotionSubtypeInterpreter implements ISubtypeInterpreter {
	public static final PotionSubtypeInterpreter INSTANCE = new PotionSubtypeInterpreter();

	private PotionSubtypeInterpreter() {

	}

	@Override
	public String apply(ItemStack itemStack) {
		if (!itemStack.hasTag()) {
			return ISubtypeInterpreter.NONE;
		}
		Potion potionType = PotionUtils.getPotionFromItem(itemStack);
		String potionTypeString = potionType.getNamePrefixed("");
		StringBuilder stringBuilder = new StringBuilder(potionTypeString);
		List<EffectInstance> effects = PotionUtils.getEffectsFromStack(itemStack);
		for (EffectInstance effect : effects) {
			stringBuilder.append(";").append(effect);
		}

		return stringBuilder.toString();
	}
}
