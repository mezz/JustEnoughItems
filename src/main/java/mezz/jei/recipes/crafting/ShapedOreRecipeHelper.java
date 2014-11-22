package mezz.jei.recipes.crafting;

import mezz.jei.api.recipes.IRecipeGui;
import mezz.jei.api.recipes.IRecipeHelper;
import mezz.jei.api.recipes.IRecipeType;
import mezz.jei.api.recipes.RecipeType;
import mezz.jei.util.StackUtil;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;
import scala.actors.threadpool.Arrays;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ShapedOreRecipeHelper implements IRecipeHelper {

	@Nonnull
	@Override
	public IRecipeType getRecipeType() {
		return RecipeType.CRAFTING_TABLE;
	}

	@Nonnull
	@Override
	public Class getRecipeClass() {
		return ShapedOreRecipe.class;
	}

	@Nonnull
	@Override
	public IRecipeGui createGui() {
		return new ShapedOreRecipeGui();
	}

	@Nonnull
	@Override
	public List<ItemStack> getInputs(@Nonnull Object recipe) {
		ShapedOreRecipe shapedRecipe = (ShapedOreRecipe)recipe;
		List list = Arrays.asList(shapedRecipe.getInput());
		return StackUtil.getItemStacksRecursive(list);
	}

	@Nonnull
	@Override
	public List<ItemStack> getOutputs(@Nonnull Object recipe) {
		ShapedOreRecipe shapedRecipe = (ShapedOreRecipe)recipe;
		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		list.add(shapedRecipe.getRecipeOutput());
		return list;
	}
}
