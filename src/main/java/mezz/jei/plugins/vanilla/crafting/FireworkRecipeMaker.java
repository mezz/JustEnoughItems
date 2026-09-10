package mezz.jei.plugins.vanilla.crafting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;

import mezz.jei.plugins.vanilla.ingredients.item.FireworkRocketIngredientFactory;
import mezz.jei.plugins.vanilla.ingredients.item.FireworkStarIngredientFactory;
import mezz.jei.plugins.vanilla.ingredients.item.FireworkStarIngredientFactory.Explosion;

public final class FireworkRecipeMaker {
	private FireworkRecipeMaker() {
	}

	public static List<FireworkRecipeWrapper> getFireworkRecipes() {
		List<FireworkRecipeWrapper> recipes = new ArrayList<>();
		addStarRecipes(recipes);
		addFadeRecipes(recipes);
		addRocketRecipes(recipes);
		return recipes;
	}

	private static void addStarRecipes(List<FireworkRecipeWrapper> recipes) {
		recipes.add(createStarRecipe(0, Collections.emptyList(), false, false));
		recipes.add(createStarRecipe(1, Collections.singletonList(new ItemStack(Items.FIRE_CHARGE)), false, false));
		recipes.add(createStarRecipe(2, Collections.singletonList(new ItemStack(Items.GOLD_NUGGET)), false, false));
		recipes.add(createStarRecipe(3, getSkulls(), false, false));
		recipes.add(createStarRecipe(4, Collections.singletonList(new ItemStack(Items.FEATHER)), false, false));
		recipes.add(createStarRecipe(0, Collections.emptyList(), true, false));
		recipes.add(createStarRecipe(0, Collections.emptyList(), false, true));
		recipes.add(createStarRecipe(0, Collections.emptyList(), true, true));

		int red = FireworkStarIngredientFactory.getFireworkColor(EnumDyeColor.RED);
		int blue = FireworkStarIngredientFactory.getFireworkColor(EnumDyeColor.BLUE);
		List<List<ItemStack>> inputs = new ArrayList<>();
		inputs.add(Collections.singletonList(new ItemStack(Items.GUNPOWDER)));
		inputs.add(Collections.singletonList(new ItemStack(Items.DYE, 1, EnumDyeColor.RED.getDyeDamage())));
		inputs.add(Collections.singletonList(new ItemStack(Items.DYE, 1, EnumDyeColor.BLUE.getDyeDamage())));
		ItemStack output = FireworkStarIngredientFactory.createStar(
			new Explosion(0, new int[]{red, blue}, new int[0], false, false)
		);
		recipes.add(new FireworkRecipeWrapper(inputs, output));
	}

	private static FireworkRecipeWrapper createStarRecipe(int type, List<ItemStack> shapeModifier, boolean trail, boolean twinkle) {
		List<List<ItemStack>> inputs = new ArrayList<>();
		inputs.add(Collections.singletonList(new ItemStack(Items.GUNPOWDER)));
		inputs.add(getDyes());
		if (!shapeModifier.isEmpty()) {
			inputs.add(shapeModifier);
		}
		if (trail) {
			inputs.add(Collections.singletonList(new ItemStack(Items.DIAMOND)));
		}
		if (twinkle) {
			inputs.add(Collections.singletonList(new ItemStack(Items.GLOWSTONE_DUST)));
		}

		List<ItemStack> outputs = new ArrayList<>();
		for (EnumDyeColor color : EnumDyeColor.values()) {
			int fireworkColor = FireworkStarIngredientFactory.getFireworkColor(color);
			outputs.add(FireworkStarIngredientFactory.createStar(
				new Explosion(type, new int[]{fireworkColor}, new int[0], trail, twinkle)
			));
		}
		return new FireworkRecipeWrapper(inputs, outputs);
	}

	private static void addFadeRecipes(List<FireworkRecipeWrapper> recipes) {
		List<ItemStack> baseStars = new ArrayList<>();
		for (EnumDyeColor color : EnumDyeColor.values()) {
			int fireworkColor = FireworkStarIngredientFactory.getFireworkColor(color);
			baseStars.add(FireworkStarIngredientFactory.createStar(
				new Explosion(0, new int[]{fireworkColor}, new int[0], false, false)
			));
		}

		for (EnumDyeColor fadeColor : EnumDyeColor.values()) {
			int fireworkFadeColor = FireworkStarIngredientFactory.getFireworkColor(fadeColor);
			List<ItemStack> outputs = new ArrayList<>();
			for (EnumDyeColor baseColor : EnumDyeColor.values()) {
				int fireworkBaseColor = FireworkStarIngredientFactory.getFireworkColor(baseColor);
				outputs.add(FireworkStarIngredientFactory.createStar(
					new Explosion(0, new int[]{fireworkBaseColor}, new int[]{fireworkFadeColor}, false, false)
				));
			}
			List<List<ItemStack>> inputs = Arrays.asList(
				baseStars,
				Collections.singletonList(new ItemStack(Items.DYE, 1, fadeColor.getDyeDamage()))
			);
			recipes.add(new FireworkRecipeWrapper(inputs, outputs));
		}
	}

	private static void addRocketRecipes(List<FireworkRecipeWrapper> recipes) {
		List<Explosion> explosions = FireworkStarIngredientFactory.createExplosions();
		Explosion simple = explosions.get(0);
		Explosion decorated = explosions.get(explosions.size() - 1);
		for (int duration = 1; duration <= 3; duration++) {
			recipes.add(createRocketRecipe(duration, Collections.emptyList()));
		}
		recipes.add(createRocketRecipe(1, Collections.singletonList(simple)));
		recipes.add(createRocketRecipe(2, Collections.singletonList(decorated)));
		recipes.add(createRocketRecipe(3, Arrays.asList(simple, decorated)));
	}

	private static FireworkRecipeWrapper createRocketRecipe(int duration, List<Explosion> explosions) {
		List<List<ItemStack>> inputs = new ArrayList<>();
		inputs.add(Collections.singletonList(new ItemStack(Items.PAPER)));
		for (int i = 0; i < duration; i++) {
			inputs.add(Collections.singletonList(new ItemStack(Items.GUNPOWDER)));
		}
		for (Explosion explosion : explosions) {
			inputs.add(Collections.singletonList(FireworkStarIngredientFactory.createStar(explosion)));
		}
		ItemStack output = FireworkRocketIngredientFactory.createRocket(duration, explosions);
		output.setCount(3);
		return new FireworkRecipeWrapper(inputs, output);
	}

	private static List<ItemStack> getDyes() {
		List<ItemStack> dyes = new ArrayList<>();
		for (EnumDyeColor color : EnumDyeColor.values()) {
			dyes.add(new ItemStack(Items.DYE, 1, color.getDyeDamage()));
		}
		return dyes;
	}

	private static List<ItemStack> getSkulls() {
		List<ItemStack> skulls = new ArrayList<>();
		for (int metadata = 0; metadata <= 5; metadata++) {
			skulls.add(new ItemStack(Items.SKULL, 1, metadata));
		}
		return skulls;
	}
}
