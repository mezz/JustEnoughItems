package mezz.jei.common.plugins.vanilla.brewing;

import java.util.List;

import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;

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
		List<MobEffectInstance> effects = PotionUtils.getMobEffects(itemStack);
		for (MobEffectInstance effect : effects) {
			stringBuilder.append(";").append(effect);
		}

		return stringBuilder.toString();
	}
}
