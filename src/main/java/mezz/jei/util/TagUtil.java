package mezz.jei.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class TagUtil {
	@Nullable
	public static ResourceLocation getTagEquivalent(Collection<ItemStack> itemStacks) {
		if (itemStacks.size() < 2) {
			return null;
		}

		List<Item> items = itemStacks.stream()
			.map(ItemStack::getItem)
			.toList();

		TagCollection<Item> collection = ItemTags.getAllTags();
		Collection<Tag<Item>> tags = collection.getAllTags().values();
		return tags.stream()
			.filter(tag -> tag.getValues().equals(items))
			.findFirst()
			.map(collection::getId)
			.orElse(null);
	}
}
