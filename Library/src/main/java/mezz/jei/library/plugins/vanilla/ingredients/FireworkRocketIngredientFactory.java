package mezz.jei.library.plugins.vanilla.ingredients;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;

import java.util.ArrayList;
import java.util.List;

public final class FireworkRocketIngredientFactory {
	private FireworkRocketIngredientFactory() {
	}

	public static List<ItemStack> create() {
		List<FireworkExplosion> explosions = FireworkStarIngredientFactory.createExplosions();
		FireworkExplosion simple = explosions.getFirst();
		FireworkExplosion decorated = explosions.getLast();
		List<List<FireworkExplosion>> decorations = List.of(List.of(simple), List.of(decorated), List.of(simple, decorated));
		List<ItemStack> rockets = new ArrayList<>();
		byte[] durations = FireworkRocketItem.CRAFTABLE_DURATIONS;
		for (int i = 0; i < durations.length; i++) {
			// Pair each plain flight duration with a simple, decorated, or multi-explosion example.
			rockets.add(rocket(durations[i], List.of()));
			rockets.add(rocket(durations[i], decorations.get(i % decorations.size())));
		}
		return List.copyOf(rockets);
	}

	private static ItemStack rocket(int duration, List<FireworkExplosion> explosions) {
		ItemStack rocket = new ItemStack(Items.FIREWORK_ROCKET);
		rocket.set(DataComponents.FIREWORKS, new Fireworks(duration, explosions));
		return rocket;
	}
}
