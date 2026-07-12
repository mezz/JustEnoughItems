package mezz.jei.plugins.vanilla.crafting.replacers;

import mezz.jei.api.constants.ModIds;
import net.minecraft.item.BannerItem;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class ShieldDecorationRecipeMaker {
	public static List<ICraftingRecipe> createRecipes() {
		Set<DyeColor> colors = EnumSet.noneOf(DyeColor.class);

		return ForgeRegistries.ITEMS.getValues().stream()
			.filter(BannerItem.class::isInstance)
			.map(BannerItem.class::cast)
			.filter(item -> colors.add(item.getColor()))
			.map(ShieldDecorationRecipeMaker::createRecipe)
			.collect(Collectors.toList());
	}

	private static ICraftingRecipe createRecipe(BannerItem banner) {
		NonNullList<Ingredient> inputs = NonNullList.of(
			Ingredient.EMPTY,
			Ingredient.of(Items.SHIELD),
			Ingredient.of(banner)
		);

		ItemStack output = createOutput(banner);
		ResourceLocation id = new ResourceLocation(ModIds.MINECRAFT_ID, "jei.shield.decoration." + banner.getDescriptionId());
		return new ShapelessRecipe(id, "jei.shield.decoration", output, inputs);
	}

	private static ItemStack createOutput(BannerItem banner) {
		DyeColor color = banner.getColor();
		ItemStack output = new ItemStack(Items.SHIELD);
		CompoundNBT tag = new CompoundNBT();
		tag.putInt("Base", color.getId());
		output.addTagElement("BlockEntityTag", tag);
		return output;
	}

	private ShieldDecorationRecipeMaker() {

	}
}
