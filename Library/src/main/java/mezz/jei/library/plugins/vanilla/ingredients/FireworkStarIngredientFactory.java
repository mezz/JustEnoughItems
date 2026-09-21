package mezz.jei.library.plugins.vanilla.ingredients;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class FireworkStarIngredientFactory {
	private FireworkStarIngredientFactory() {
	}

	public static List<ItemStack> create() {
		return createExplosions().stream()
			.map(FireworkStarIngredientFactory::createStar)
			.toList();
	}

	public static List<Explosion> createExplosions() {
		List<Explosion> explosions = new ArrayList<>();
		List<Integer> red = List.of(DyeColor.RED.getFireworkColor());
		List<Integer> blue = List.of(DyeColor.BLUE.getFireworkColor());
		List<Integer> yellow = List.of(DyeColor.YELLOW.getFireworkColor());
		List<Integer> multicolor = List.of(DyeColor.RED.getFireworkColor(), DyeColor.BLUE.getFireworkColor());
		List<Integer> fadeColors = List.of(DyeColor.YELLOW.getFireworkColor(), DyeColor.WHITE.getFireworkColor());

		for (FireworkRocketItem.Shape shape : FireworkRocketItem.Shape.values()) {
			explosions.add(new Explosion(shape, red, List.of(), false, false));
		}
		explosions.add(new Explosion(FireworkRocketItem.Shape.SMALL_BALL, blue, List.of(), true, false));
		explosions.add(new Explosion(FireworkRocketItem.Shape.SMALL_BALL, yellow, List.of(), false, true));
		explosions.add(new Explosion(FireworkRocketItem.Shape.SMALL_BALL, multicolor, List.of(), false, false));
		explosions.add(new Explosion(FireworkRocketItem.Shape.SMALL_BALL, multicolor, fadeColors, true, true));
		return List.copyOf(explosions);
	}

	public static ItemStack createStar(Explosion explosion) {
		ItemStack star = new ItemStack(Items.FIREWORK_STAR);
		setExplosion(star, explosion);
		return star;
	}

	public static void setExplosion(ItemStack stack, Explosion explosion) {
		stack.addTagElement("Explosion", createExplosionTag(explosion));
	}

	public static Optional<Explosion> getExplosion(ItemStack stack) {
		CompoundTag tag = stack.getTagElement("Explosion");
		if (tag == null) {
			return Optional.empty();
		}
		return Optional.of(readExplosionTag(tag));
	}

	public static CompoundTag createExplosionTag(Explosion explosion) {
		CompoundTag tag = new CompoundTag();
		tag.putByte("Type", (byte) explosion.shape().getId());
		tag.putIntArray("Colors", explosion.colors());
		tag.putIntArray("FadeColors", explosion.fadeColors());
		tag.putBoolean("Trail", explosion.trail());
		tag.putBoolean("Flicker", explosion.twinkle());
		return tag;
	}

	public static Explosion readExplosionTag(CompoundTag tag) {
		int shapeId = tag.getByte("Type");
		FireworkRocketItem.Shape[] shapes = FireworkRocketItem.Shape.values();
		FireworkRocketItem.Shape shape = shapes[Math.max(0, Math.min(shapeId, shapes.length - 1))];
		List<Integer> colors = readColors(tag, "Colors");
		List<Integer> fadeColors = readColors(tag, "FadeColors");
		return new Explosion(shape, colors, fadeColors, tag.getBoolean("Trail"), tag.getBoolean("Flicker"));
	}

	private static List<Integer> readColors(CompoundTag tag, String key) {
		if (!tag.contains(key, Tag.TAG_INT_ARRAY)) {
			return List.of();
		}
		return Arrays.stream(tag.getIntArray(key)).boxed().toList();
	}

	public record Explosion(
		FireworkRocketItem.Shape shape,
		List<Integer> colors,
		List<Integer> fadeColors,
		boolean trail,
		boolean twinkle
	) {
		public Explosion {
			colors = List.copyOf(colors);
			fadeColors = List.copyOf(fadeColors);
		}

		public Explosion withFadeColors(List<Integer> colors) {
			return new Explosion(shape, this.colors, colors, trail, twinkle);
		}
	}
}
