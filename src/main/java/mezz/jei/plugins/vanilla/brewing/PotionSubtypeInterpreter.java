package mezz.jei.plugins.vanilla.brewing;

import javax.annotation.Nullable;
import java.util.List;

import mezz.jei.api.ISubtypeRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;

public class PotionSubtypeInterpreter implements ISubtypeRegistry.ISubtypeInterpreter {
	public static final PotionSubtypeInterpreter INSTANCE = new PotionSubtypeInterpreter();

	private PotionSubtypeInterpreter() {

	}

	@Nullable
	@Override
	public String getSubtypeInfo(ItemStack itemStack) {
		if (!itemStack.hasTagCompound()) {
			return null;
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
