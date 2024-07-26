package mezz.jei.library.plugins.vanilla.ingredients.subtypes;

import com.google.common.collect.Lists;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.FireworkStarItem;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class FireworkRocketSubtypeInterpreter implements IIngredientSubtypeInterpreter<ItemStack> {
	public static final FireworkRocketSubtypeInterpreter INSTANCE = new FireworkRocketSubtypeInterpreter();

	private FireworkRocketSubtypeInterpreter() {

	}

	@Override
	public String apply(ItemStack itemStack, UidContext context) {
		CompoundTag compoundtag = itemStack.getTagElement("Fireworks");
		if (compoundtag == null) {
			return IIngredientSubtypeInterpreter.NONE;
		}
		int flightDuration = 0;
		if (compoundtag.contains("Flight", 99)) {
			flightDuration = compoundtag.getByte("Flight");
		}

		List<String> strings = new ArrayList<>();

		ListTag listtag = compoundtag.getList("Explosions", 10);
		if (!listtag.isEmpty()) {
			for (int i = 0; i < listtag.size(); ++i) {
				CompoundTag compoundtag1 = listtag.getCompound(i);
				List<Component> list = Lists.newArrayList();
				FireworkStarItem.appendHoverText(compoundtag1, list);
				FireworkRocketItem.Shape shape = FireworkRocketItem.Shape.byId(compoundtag1.getByte("Type"));
				strings.add(shape.getName());
			}
		}

		StringJoiner joiner = new StringJoiner(",", "[", "]");
		strings.sort(null);
		for (String s : strings) {
			joiner.add(s);
		}

		return flightDuration + ":" + joiner;
	}
}
