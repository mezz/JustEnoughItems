package mezz.jei.plugins.vanilla.crafting;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

public final class ShieldDecorationRecipeMaker {
	public static List<ShapelessRecipes> getShieldDecorationRecipes() {
		List<ShapelessRecipes> recipes = new ArrayList<>();
		for (EnumDyeColor color : EnumDyeColor.values()) {
			ItemStack banner = new ItemStack(Items.BANNER, 1, color.getDyeDamage());
			ItemStack shield = new ItemStack(Items.SHIELD);
			NBTTagCompound blockEntityTag = new NBTTagCompound();
			blockEntityTag.setInteger("Base", color.getDyeDamage());
			shield.setTagInfo("BlockEntityTag", blockEntityTag);

			NonNullList<Ingredient> inputs = NonNullList.create();
			inputs.add(Ingredient.fromStacks(new ItemStack(Items.SHIELD)));
			inputs.add(Ingredient.fromStacks(banner));

			ShapelessRecipes recipe = new ShapelessRecipes("jei.shield.decoration", shield, inputs);
			recipe.setRegistryName(new ResourceLocation("minecraft", "jei.shield.decoration." + color.getName()));
			recipes.add(recipe);
		}
		return recipes;
	}

	private ShieldDecorationRecipeMaker() {

	}
}
