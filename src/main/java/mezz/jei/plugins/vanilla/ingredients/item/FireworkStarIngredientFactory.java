package mezz.jei.plugins.vanilla.ingredients.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public final class FireworkStarIngredientFactory {
	private FireworkStarIngredientFactory() {
	}

	public static List<ItemStack> create() {
		List<ItemStack> stars = new ArrayList<>();
		for (Explosion explosion : createExplosions()) {
			stars.add(createStar(explosion));
		}
		return Collections.unmodifiableList(stars);
	}

	public static List<Explosion> createExplosions() {
		int red = getFireworkColor(EnumDyeColor.RED);
		int blue = getFireworkColor(EnumDyeColor.BLUE);
		int yellow = getFireworkColor(EnumDyeColor.YELLOW);
		int white = getFireworkColor(EnumDyeColor.WHITE);

		List<Explosion> explosions = new ArrayList<>();
		for (int type = 0; type <= 4; type++) {
			explosions.add(new Explosion(type, new int[]{red}, new int[0], false, false));
		}
		explosions.add(new Explosion(0, new int[]{blue}, new int[0], true, false));
		explosions.add(new Explosion(0, new int[]{yellow}, new int[0], false, true));
		explosions.add(new Explosion(0, new int[]{red, blue}, new int[0], false, false));
		explosions.add(new Explosion(0, new int[]{red, blue}, new int[]{yellow, white}, true, true));
		return Collections.unmodifiableList(explosions);
	}

	public static int getFireworkColor(EnumDyeColor color) {
		return ItemDye.DYE_COLORS[color.getDyeDamage()];
	}

	public static ItemStack createStar(Explosion explosion) {
		ItemStack star = new ItemStack(Items.FIREWORK_CHARGE);
		star.setTagInfo("Explosion", createExplosionTag(explosion));
		return star;
	}

	public static boolean hasColors(ItemStack stack) {
		if (!stack.hasTagCompound()) {
			return false;
		}
		NBTTagCompound explosion = stack.getTagCompound().getCompoundTag("Explosion");
		return explosion.hasKey("Colors", 11) && explosion.getIntArray("Colors").length > 0;
	}

	public static NBTTagCompound createExplosionTag(Explosion explosion) {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setByte("Type", (byte) explosion.getType());
		tag.setIntArray("Colors", explosion.getColors());
		int[] fadeColors = explosion.getFadeColors();
		if (fadeColors.length > 0) {
			tag.setIntArray("FadeColors", fadeColors);
		}
		if (explosion.hasTrail()) {
			tag.setBoolean("Trail", true);
		}
		if (explosion.hasTwinkle()) {
			tag.setBoolean("Flicker", true);
		}
		return tag;
	}

	public static final class Explosion {
		private final int type;
		private final int[] colors;
		private final int[] fadeColors;
		private final boolean trail;
		private final boolean twinkle;

		public Explosion(int type, int[] colors, int[] fadeColors, boolean trail, boolean twinkle) {
			this.type = type;
			this.colors = Arrays.copyOf(colors, colors.length);
			this.fadeColors = Arrays.copyOf(fadeColors, fadeColors.length);
			this.trail = trail;
			this.twinkle = twinkle;
		}

		public int getType() {
			return type;
		}

		public int[] getColors() {
			return Arrays.copyOf(colors, colors.length);
		}

		public int[] getFadeColors() {
			return Arrays.copyOf(fadeColors, fadeColors.length);
		}

		public boolean hasTrail() {
			return trail;
		}

		public boolean hasTwinkle() {
			return twinkle;
		}
	}
}
