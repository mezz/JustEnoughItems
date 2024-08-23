package mezz.jei.library.plugins.vanilla.ingredients.subtypes;

import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class SuspiciousStewSubtypeInterpreter implements ISubtypeInterpreter<ItemStack> {
	public static final SuspiciousStewSubtypeInterpreter INSTANCE = new SuspiciousStewSubtypeInterpreter();

	private SuspiciousStewSubtypeInterpreter() {

	}

	@Override
	public @Nullable Object getSubtypeData(ItemStack ingredient, UidContext context) {
		return ingredient.get(DataComponents.SUSPICIOUS_STEW_EFFECTS);
	}

	@Override
	public String getLegacyStringSubtypeInfo(ItemStack itemStack, UidContext context) {
		SuspiciousStewEffects suspiciousStewEffects = itemStack.get(DataComponents.SUSPICIOUS_STEW_EFFECTS);
		if (suspiciousStewEffects == null) {
			return "";
		}
		List<SuspiciousStewEffects.Entry> effects = suspiciousStewEffects.effects();
		List<String> strings = new ArrayList<>();
		for (SuspiciousStewEffects.Entry e : effects) {
			String effect = e.effect().getRegisteredName();
			int duration = e.duration();
			strings.add(effect + "." + duration);
		}

		StringJoiner joiner = new StringJoiner(",", "[", "]");
		strings.sort(null);
		for (String s : strings) {
			joiner.add(s);
		}
		return joiner.toString();
	}
}
