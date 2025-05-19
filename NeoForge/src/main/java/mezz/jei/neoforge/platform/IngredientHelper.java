package mezz.jei.neoforge.platform;

import com.google.common.collect.Streams;
import mezz.jei.common.platform.IPlatformIngredientHelper;
import mezz.jei.common.util.RegistryUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.ComposterBlock;

import java.util.List;
import java.util.stream.Stream;

public class IngredientHelper implements IPlatformIngredientHelper {
	@Override
	public Ingredient createShulkerDyeIngredient(DyeColor color) {
		DyeItem dye = DyeItem.byColor(color);
		TagKey<Item> colorTag = color.getTag();
		Registry<Item> itemRegistry = RegistryUtil.getRegistry(Registries.ITEM);

		HolderSet<Item> coloredItems = itemRegistry.get(colorTag)
			.map(i -> (HolderSet<Item>) i)
			.orElseGet(HolderSet::empty);

		boolean contains = coloredItems.stream().anyMatch(h -> h.value() == dye);

		Stream<? extends ItemLike> colorIngredientStream = coloredItems.stream().map(Holder::value);
		if (!contains) {
			colorIngredientStream = Streams.concat(Stream.of(dye), colorIngredientStream);
		}
		// Shulker box special recipe allows the matching dye item or any item in the tag.
		// we need to specify both in case someone removes the dye item from the dye tag
		// as the item will still be valid for this recipe.
		return Ingredient.of(colorIngredientStream);
	}

	@Override
	public List<Ingredient> getPotionContainers(PotionBrewing potionBrewing) {
		return potionBrewing.containers;
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
	public float getCompostValue(ItemStack itemStack) {
		return ComposterBlock.getValue(itemStack);
	}
}
