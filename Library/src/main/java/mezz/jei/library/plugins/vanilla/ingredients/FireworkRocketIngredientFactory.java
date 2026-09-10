package mezz.jei.library.plugins.vanilla.ingredients;

import mezz.jei.library.plugins.vanilla.ingredients.FireworkStarIngredientFactory.Explosion;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class FireworkRocketIngredientFactory {
	private static final int[] CRAFTABLE_DURATIONS = {1, 2, 3};

	private FireworkRocketIngredientFactory() {
	}

	public static List<ItemStack> create() {
		List<Explosion> explosions = FireworkStarIngredientFactory.createExplosions();
		Explosion simple = explosions.get(0);
		Explosion decorated = explosions.get(explosions.size() - 1);
		List<List<Explosion>> decorations = List.of(List.of(simple), List.of(decorated), List.of(simple, decorated));
		List<ItemStack> rockets = new ArrayList<>();
		for (int i = 0; i < CRAFTABLE_DURATIONS.length; i++) {
			rockets.add(createRocket(CRAFTABLE_DURATIONS[i], List.of()));
			rockets.add(createRocket(CRAFTABLE_DURATIONS[i], decorations.get(i % decorations.size())));
		}
		return List.copyOf(rockets);
	}

	public static ItemStack createRocket(int duration, List<Explosion> explosions) {
		ItemStack rocket = new ItemStack(Items.FIREWORK_ROCKET);
		CompoundTag fireworks = new CompoundTag();
		fireworks.putByte("Flight", (byte) duration);
		ListTag explosionTags = new ListTag();
		for (Explosion explosion : explosions) {
			explosionTags.add(FireworkStarIngredientFactory.createExplosionTag(explosion));
		}
		fireworks.put("Explosions", explosionTags);
		rocket.addTagElement("Fireworks", fireworks);
		return rocket;
	}

	public static Optional<Rocket> getRocket(ItemStack stack) {
		CompoundTag fireworks = stack.getTagElement("Fireworks");
		if (fireworks == null) {
			return Optional.empty();
		}
		int duration = fireworks.getByte("Flight");
		ListTag explosionTags = fireworks.getList("Explosions", Tag.TAG_COMPOUND);
		List<Explosion> explosions = new ArrayList<>(explosionTags.size());
		for (int i = 0; i < explosionTags.size(); i++) {
			explosions.add(FireworkStarIngredientFactory.readExplosionTag(explosionTags.getCompound(i)));
		}
		return Optional.of(new Rocket(duration, explosions));
	}

	public static boolean isCraftable(Rocket rocket) {
		return rocket.duration() >= 1 && rocket.duration() <= 3 &&
			1 + rocket.duration() + rocket.explosions().size() <= 9;
	}

	public record Rocket(int duration, List<Explosion> explosions) {
		public Rocket {
			explosions = List.copyOf(explosions);
		}
	}
}
