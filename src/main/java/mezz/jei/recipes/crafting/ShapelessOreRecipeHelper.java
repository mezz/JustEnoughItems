package mezz.jei.recipes.crafting;

import mezz.jei.api.recipes.IRecipeGuiHelper;
import mezz.jei.api.recipes.IRecipeHelper;
import mezz.jei.api.recipes.IRecipeType;
import mezz.jei.api.recipes.RecipeType;
import mezz.jei.util.StackUtil;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ShapelessOreRecipeHelper implements IRecipeHelper {

	@Nonnull
	@Override
	public IRecipeType getRecipeType() {
		return RecipeType.CRAFTING_TABLE;
	}

	@Nonnull
	@Override
	public Class getRecipeClass() {
		return ShapelessOreRecipe.class;
	}

	@Nonnull
	@Override
	public IRecipeGuiHelper createGuiHelper() {
		return new ShapelessOreRecipeGui();
	}

	@Nullable
	@Override
	public List<ItemStack> getInputs(@Nonnull Object recipe) {
		ShapelessOreRecipe shapelessRecipe = (ShapelessOreRecipe)recipe;
		List<Object> input = shapelessRecipe.getInput();
		return StackUtil.getItemStacksRecursive(input);
	}

	@Nonnull
	@Override
	public List<ItemStack> getOutputs(@Nonnull Object recipe) {
		ShapelessOreRecipe shapelessRecipe = (ShapelessOreRecipe)recipe;
		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		list.add(shapelessRecipe.getRecipeOutput());
		return list;
	}
}
