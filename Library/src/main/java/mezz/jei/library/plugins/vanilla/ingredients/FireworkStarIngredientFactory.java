package mezz.jei.library.plugins.vanilla.ingredients;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;

import java.util.ArrayList;
import java.util.List;

public final class FireworkStarIngredientFactory {
	private FireworkStarIngredientFactory() {
	}

	public static List<ItemStack> create() {
		return createExplosions().stream()
			.map(explosion -> {
				ItemStack star = new ItemStack(Items.FIREWORK_STAR);
				star.set(DataComponents.FIREWORK_EXPLOSION, explosion);
				return star;
			})
			.toList();
	}

	public static List<FireworkExplosion> createExplosions() {
		List<FireworkExplosion> explosions = new ArrayList<>();
		IntList red = IntList.of(FireworkExplosion.defaultFireworkTint(DyeColor.RED));
		IntList blue = IntList.of(FireworkExplosion.defaultFireworkTint(DyeColor.BLUE));
		IntList yellow = IntList.of(FireworkExplosion.defaultFireworkTint(DyeColor.YELLOW));
		IntList multicolor = IntList.of(FireworkExplosion.defaultFireworkTint(DyeColor.RED), FireworkExplosion.defaultFireworkTint(DyeColor.BLUE));
		IntList fadeColors = IntList.of(FireworkExplosion.defaultFireworkTint(DyeColor.YELLOW), FireworkExplosion.defaultFireworkTint(DyeColor.WHITE));

		// Cover each shape and a few effects without enumerating every possible combination.
		for (FireworkExplosion.Shape shape : FireworkExplosion.Shape.values()) {
			explosions.add(new FireworkExplosion(shape, red, IntList.of(), false, false));
		}
		explosions.add(new FireworkExplosion(FireworkExplosion.Shape.SMALL_BALL, blue, IntList.of(), true, false));
		explosions.add(new FireworkExplosion(FireworkExplosion.Shape.SMALL_BALL, yellow, IntList.of(), false, true));
		explosions.add(new FireworkExplosion(FireworkExplosion.Shape.SMALL_BALL, multicolor, IntList.of(), false, false));
		explosions.add(new FireworkExplosion(FireworkExplosion.Shape.SMALL_BALL, multicolor, fadeColors, true, true));

		return List.copyOf(explosions);
	}
}
