package mezz.jei.plugins.vanilla.crafting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.BlockShulkerBox;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.DyeUtils;

public final class ShulkerBoxColoringRecipeMaker {
	public static List<ShapelessRecipes> getShulkerBoxColoringRecipes(Collection<ItemStack> ingredients) {
		Map<EnumDyeColor, List<ItemStack>> dyes = new EnumMap<>(EnumDyeColor.class);
		for (ItemStack ingredient : ingredients) {
			DyeUtils.colorFromStack(ingredient).ifPresent(color -> dyes.computeIfAbsent(color, key -> new ArrayList<>()).add(ingredient));
		}
		List<ItemStack> boxes = new ArrayList<>();
		for (EnumDyeColor color : EnumDyeColor.values()) {
			boxes.add(BlockShulkerBox.getColoredItemStack(color));
		}
		Ingredient shulkerBoxes = Ingredient.fromStacks(boxes.toArray(new ItemStack[0]));
		List<ShapelessRecipes> recipes = new ArrayList<>();
		for (Map.Entry<EnumDyeColor, List<ItemStack>> entry : dyes.entrySet()) {
			EnumDyeColor color = entry.getKey();
			Ingredient colorIngredient = Ingredient.fromStacks(entry.getValue().toArray(new ItemStack[0]));
			NonNullList<Ingredient> inputs = NonNullList.from(Ingredient.EMPTY, shulkerBoxes, colorIngredient);
			ShapelessRecipes recipe = new ShapelessRecipes("jei.shulker.color", BlockShulkerBox.getColoredItemStack(color), inputs);
			recipe.setRegistryName(new ResourceLocation("minecraft", "jei.shulker.color." + color.getName()));
			recipes.add(recipe);
		}
		return recipes;
	}

	private ShulkerBoxColoringRecipeMaker() {
	}
}
