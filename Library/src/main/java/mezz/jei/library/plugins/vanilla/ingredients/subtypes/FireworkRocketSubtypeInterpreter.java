package mezz.jei.library.plugins.vanilla.ingredients.subtypes;

import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class FireworkRocketSubtypeInterpreter implements IIngredientSubtypeInterpreter<ItemStack> {
	public static final FireworkRocketSubtypeInterpreter INSTANCE = new FireworkRocketSubtypeInterpreter();

	private FireworkRocketSubtypeInterpreter() {

	}

	@Override
	public String apply(ItemStack itemStack, UidContext context) {
		Fireworks fireworks = itemStack.get(DataComponents.FIREWORKS);
		if (fireworks == null) {
			return IIngredientSubtypeInterpreter.NONE;
		}
		List<FireworkExplosion> explosions = fireworks.explosions();
		List<String> strings = new ArrayList<>();
		for (FireworkExplosion e : explosions) {
			FireworkExplosion.Shape shape = e.shape();
			strings.add(shape.getSerializedName());
		}

		StringJoiner joiner = new StringJoiner(",", "[", "]");
		strings.sort(null);
		for (String s : strings) {
			joiner.add(s);
		}

		int flightDuration = fireworks.flightDuration();
		return flightDuration + ":" + joiner;
	}
}
