package mezz.jei.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Table;
import mezz.jei.RecipeRegistry;
import mezz.jei.api.recipe.IRecipeType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * A RecipeMap efficiently links Recipes, IRecipeTypes, and ItemStacks.
 */
public class RecipeMap {

	@Nonnull
	private final Table<IRecipeType, String, List<Object>> recipeTable = HashBasedTable.create();
	@Nonnull
	private final ArrayListMultimap<String, IRecipeType> typeMap = ArrayListMultimap.create();
	@Nonnull
	private final Ordering<IRecipeType> recipeTypeOrdering;

	public RecipeMap(final RecipeRegistry recipeRegistry) {
		Comparator<IRecipeType> recipeTypeComparator = new Comparator<IRecipeType>() {
			public int compare(IRecipeType recipeType1, IRecipeType recipeType2) {
				int index1 = recipeRegistry.getRecipeTypeIndex(recipeType1);
				int index2 = recipeRegistry.getRecipeTypeIndex(recipeType2);
				return Integer.compare(index1, index2);
			}
		};
		this.recipeTypeOrdering = Ordering.from(recipeTypeComparator);
	}

	@Nonnull
	public ImmutableList<IRecipeType> getRecipeTypes(@Nonnull ItemStack itemStack) {
		ImmutableSet.Builder<IRecipeType> recipeTypeBuilder = ImmutableSet.builder();
		for (String stackKey : getNamesWithWildcard(itemStack)) {
			recipeTypeBuilder.addAll(typeMap.get(stackKey));
		}
		ImmutableSet<IRecipeType> recipeTypes = recipeTypeBuilder.build();
		return recipeTypeOrdering.immutableSortedCopy(recipeTypes);
	}

	private void addRecipeType(@Nonnull IRecipeType recipeType, @Nonnull ItemStack itemStack) {
		String stackKey = getName(itemStack);
		List<IRecipeType> recipeTypes = typeMap.get(stackKey);
		if (!recipeTypes.contains(recipeType))
			recipeTypes.add(recipeType);
	}

	@Nonnull
	private List<String> getNamesWithWildcard(@Nonnull ItemStack itemStack) {
		List<String> names = new ArrayList<String>(2);
		names.add(getName(itemStack));
		names.add(getWildcardName(itemStack));
		return names;
	}

	@Nonnull
	private String getName(@Nonnull ItemStack itemStack) {
		int meta = itemStack.getItemDamage();
		if (meta == OreDictionary.WILDCARD_VALUE) {
			return getWildcardName(itemStack);
		} else {
			return itemStack.getUnlocalizedName() + ":" + meta;
		}
	}

	@Nonnull
	private String getWildcardName(@Nonnull ItemStack itemStack) {
		return itemStack.getItem().getUnlocalizedName() + ":" + OreDictionary.WILDCARD_VALUE;
	}

	@Nonnull
	public ImmutableList<Object> getRecipes(@Nonnull IRecipeType recipeType, @Nonnull ItemStack stack) {
		Map<String, List<Object>> recipesForType = recipeTable.row(recipeType);

		ImmutableList.Builder<Object> listBuilder = ImmutableList.builder();
		for (String name : getNamesWithWildcard(stack)) {
			List<Object> recipes = recipesForType.get(name);
			if (recipes != null)
				listBuilder.addAll(recipes);
		}
		return listBuilder.build();
	}

	public void addRecipe(@Nonnull Object recipe, @Nonnull IRecipeType recipeType, @Nonnull Iterable<ItemStack> itemStacks) {
		Map<String, List<Object>> recipesForType = recipeTable.row(recipeType);

		for (ItemStack itemStack : itemStacks) {
			if (itemStack == null)
				continue;

			String stackKey = getName(itemStack);
			List<Object> recipes = recipesForType.get(stackKey);
			if (recipes == null) {
				recipes = Lists.newArrayList();
				recipesForType.put(stackKey, recipes);
			}
			recipes.add(recipe);

			addRecipeType(recipeType, itemStack);
		}
	}
}
