package mezz.jei.common.plugins.vanilla.crafting.replacers;

import mezz.jei.api.constants.ModIds;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet.ListBacked;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBlock;

import java.util.List;

public final class SuspiciousStewRecipeMaker {

	public static List<CraftingRecipe> createRecipes() {
		String group = "jei.suspicious.stew";
		Ingredient brownMushroom = Ingredient.of(Blocks.BROWN_MUSHROOM.asItem());
		Ingredient redMushroom = Ingredient.of(Blocks.RED_MUSHROOM.asItem());
		Ingredient bowl = Ingredient.of(Items.BOWL);

		return Registry.ITEM.getTag(ItemTags.SMALL_FLOWERS)
			.stream()
			.flatMap(ListBacked::stream)
			.map(Holder::value)
			.filter(BlockItem.class::isInstance)
			.map(item -> ((BlockItem) item).getBlock())
			.filter(FlowerBlock.class::isInstance)
			.map(FlowerBlock.class::cast)
			.<CraftingRecipe>map(flowerBlock -> {
				Ingredient flower = Ingredient.of(flowerBlock.asItem());
				NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, brownMushroom, redMushroom, bowl, flower);
				ItemStack output = new ItemStack(Items.SUSPICIOUS_STEW, 1);
				MobEffect mobeffect = flowerBlock.getSuspiciousStewEffect();
				SuspiciousStewItem.saveMobEffect(output, mobeffect, flowerBlock.getEffectDuration());
				ResourceLocation id = new ResourceLocation(ModIds.MINECRAFT_ID, "jei.suspicious.stew." + flowerBlock.getDescriptionId());
				return new ShapelessRecipe(id, group, output, inputs);
			})
			.toList();
	}

	private SuspiciousStewRecipeMaker() {

	}
}
