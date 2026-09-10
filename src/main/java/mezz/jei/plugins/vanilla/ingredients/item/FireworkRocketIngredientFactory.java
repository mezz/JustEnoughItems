package mezz.jei.plugins.vanilla.ingredients.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import mezz.jei.plugins.vanilla.ingredients.item.FireworkStarIngredientFactory.Explosion;

public final class FireworkRocketIngredientFactory {
	private FireworkRocketIngredientFactory() {
	}

	public static List<ItemStack> create() {
		List<Explosion> explosions = FireworkStarIngredientFactory.createExplosions();
		Explosion simple = explosions.get(0);
		Explosion decorated = explosions.get(explosions.size() - 1);
		List<List<Explosion>> decorations = Arrays.asList(
			Collections.singletonList(simple),
			Collections.singletonList(decorated),
			Arrays.asList(simple, decorated)
		);

		List<ItemStack> rockets = new ArrayList<>();
		for (int duration = 1; duration <= 3; duration++) {
			rockets.add(createRocket(duration, Collections.emptyList()));
			rockets.add(createRocket(duration, decorations.get(duration - 1)));
		}
		return Collections.unmodifiableList(rockets);
	}

	public static ItemStack createRocket(int duration, List<Explosion> explosions) {
		ItemStack rocket = new ItemStack(Items.FIREWORKS);
		NBTTagCompound fireworks = new NBTTagCompound();
		fireworks.setByte("Flight", (byte) duration);
		if (!explosions.isEmpty()) {
			NBTTagList explosionTags = new NBTTagList();
			for (Explosion explosion : explosions) {
				explosionTags.appendTag(FireworkStarIngredientFactory.createExplosionTag(explosion));
			}
			fireworks.setTag("Explosions", explosionTags);
		}
		rocket.setTagInfo("Fireworks", fireworks);
		return rocket;
	}
}
