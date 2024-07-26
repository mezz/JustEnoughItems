package mezz.jei.library.plugins.vanilla.ingredients.subtypes;

import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class SuspiciousStewSubtypeInterpreter implements IIngredientSubtypeInterpreter<ItemStack> {
	public static final SuspiciousStewSubtypeInterpreter INSTANCE = new SuspiciousStewSubtypeInterpreter();

	private SuspiciousStewSubtypeInterpreter() {

	}

	@Override
	public String apply(ItemStack itemStack, UidContext context) {
		List<String> strings = getPotionEffectStrings(itemStack);
		if (strings.isEmpty()) {
			return IIngredientSubtypeInterpreter.NONE;
		}

		StringJoiner joiner = new StringJoiner(",", "[", "]");
		strings.sort(null);
		for (String s : strings) {
			joiner.add(s);
		}
		return joiner.toString();
	}

	private static List<String> getPotionEffectStrings(ItemStack itemStack) {
		List<String> effects = new ArrayList<>();
		CompoundTag compoundtag = itemStack.getTag();
		if (compoundtag != null && compoundtag.contains("Effects", 9)) {
			ListTag effectsTag = compoundtag.getList("Effects", 10);

			for (int i = 0; i < effectsTag.size(); ++i) {
				CompoundTag effectTag = effectsTag.getCompound(i);
				int duration;
				if (effectTag.contains("EffectDuration", 99)) {
					duration = effectTag.getInt("EffectDuration");
				} else {
					duration = 160;
				}

				int effectId = effectTag.getInt("EffectId");
				effects.add(effectId + "." + duration);
			}
		}
		return effects;
	}
}
