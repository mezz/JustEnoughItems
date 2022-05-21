package mezz.jei.plugins.vanilla.brewing;

import java.util.List;

import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;

public class PotionSubtypeInterpreter implements IIngredientSubtypeInterpreter<ItemStack> {
	public static final PotionSubtypeInterpreter INSTANCE = new PotionSubtypeInterpreter();

	private PotionSubtypeInterpreter() {

	}

	@Override
	public String apply(ItemStack itemStack, UidContext context) {
		if (!itemStack.hasTag()) {
			return IIngredientSubtypeInterpreter.NONE;
		}
		Potion potionType = PotionUtils.getPotion(itemStack);
		String potionTypeString = potionType.getName("");
		StringBuilder stringBuilder = new StringBuilder(potionTypeString);
		List<EffectInstance> effects = PotionUtils.getMobEffects(itemStack);
		for (EffectInstance effect : effects) {
			stringBuilder.append(";").append(effect);
		}

		return stringBuilder.toString();
	}
}
