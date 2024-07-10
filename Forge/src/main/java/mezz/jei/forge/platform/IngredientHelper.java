package mezz.jei.forge.platform;

import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.common.platform.IPlatformIngredientHelper;
import mezz.jei.forge.ingredients.JeiIngredient;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class IngredientHelper implements IPlatformIngredientHelper {
	@Override
	public Ingredient createShulkerDyeIngredient(DyeColor color) {
		DyeItem dye = DyeItem.byColor(color);
		TagKey<Item> colorTag = color.getTag();
		Ingredient.Value colorList = new Ingredient.TagValue(colorTag);
		boolean contains = StreamSupport.stream(BuiltInRegistries.ITEM.getTagOrEmpty(colorTag).spliterator(), false).anyMatch(h -> h.value() == dye);
		Stream<Ingredient.Value> colorIngredientStream;
		if (!contains) {
			ItemStack dyeStack = new ItemStack(dye);
			Ingredient.Value dyeList = new Ingredient.ItemValue(dyeStack);
			colorIngredientStream = Stream.of(dyeList, colorList);
		} else {
			colorIngredientStream = Stream.of(colorList);
		}
		// Shulker box special recipe allows the matching dye item or any item in the tag.
		// we need to specify both in case someone removes the dye item from the dye tag
		// as the item will still be valid for this recipe.
		return Ingredient.fromValues(colorIngredientStream);
	}

	@Override
	public Ingredient createNbtIngredient(ItemStack stack, IStackHelper stackHelper) {
		return new JeiIngredient(stack, stackHelper);
	}

	@Override
	public Stream<Ingredient> getPotionIngredients(PotionBrewing potionBrewing) {
		return Stream.concat(
				potionBrewing.containerMixes.stream(),
				potionBrewing.potionMixes.stream()
			)
			.map(PotionBrewing.Mix::ingredient);
	}

	@Override
	public List<Ingredient> getPotionContainers(PotionBrewing potionBrewing) {
		return potionBrewing.containers;
	}
}
