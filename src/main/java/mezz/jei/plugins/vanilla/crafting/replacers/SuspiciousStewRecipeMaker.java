package mezz.jei.plugins.vanilla.crafting.replacers;

import mezz.jei.api.constants.ModIds;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SuspiciousStewItem;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.potion.Effect;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import java.util.stream.Stream;

public final class SuspiciousStewRecipeMaker {

	public static Stream<ICraftingRecipe> createRecipes() {
		String group = "jei.suspicious.stew";
		Ingredient brownMushroom = Ingredient.of(Blocks.BROWN_MUSHROOM.asItem());
		Ingredient redMushroom = Ingredient.of(Blocks.RED_MUSHROOM.asItem());
		Ingredient bowl = Ingredient.of(Items.BOWL);

		return BlockTags.SMALL_FLOWERS.getValues().stream()
			.filter(FlowerBlock.class::isInstance)
			.map(FlowerBlock.class::cast)
			.map(flowerBlock -> {
				Ingredient flower = Ingredient.of(flowerBlock.asItem());
				NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, brownMushroom, redMushroom, bowl, flower);
				ItemStack output = new ItemStack(Items.SUSPICIOUS_STEW, 1);
				Effect mobeffect = flowerBlock.getSuspiciousStewEffect();
				SuspiciousStewItem.saveMobEffect(output, mobeffect, flowerBlock.getEffectDuration());
				ResourceLocation id = new ResourceLocation(ModIds.MINECRAFT_ID, "jei.suspicious.stew." + flowerBlock.getDescriptionId());
				return new ShapelessRecipe(id, group, output, inputs);
			});
	}

	private SuspiciousStewRecipeMaker() {

	}
}
