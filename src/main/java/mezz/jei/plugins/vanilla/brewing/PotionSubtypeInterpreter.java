package mezz.jei.plugins.vanilla.brewing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mezz.jei.api.ISubtypeRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;

public class PotionSubtypeInterpreter implements ISubtypeRegistry.ISubtypeInterpreter {
	public static final PotionSubtypeInterpreter INSTANCE = new PotionSubtypeInterpreter();

	private PotionSubtypeInterpreter() {

	}

	@Nullable
	@Override
	public String getSubtypeInfo(@Nonnull ItemStack itemStack) {
		if (!itemStack.hasTagCompound()) {
			return null;
		}
		PotionType potionType = PotionUtils.getPotionFromItem(itemStack);
		return potionType.getNamePrefixed("");
	}
}
