package mezz.jei.plugins.jei.debug;

import java.util.Collections;
import java.util.List;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeRegistryPlugin;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.plugins.jei.JEIInternalPlugin;
import mezz.jei.plugins.vanilla.brewing.BrewingRecipeWrapper;
import net.minecraft.entity.passive.HorseArmorType;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

/**
 * This is a truly terrible example, it's only being used to test everything.
 */
public class DebugRecipeRegistryPlugin implements IRecipeRegistryPlugin {
	@Override
	public <V> List<String> getRecipeCategoryUids(IFocus<V> focus) {
		V focusValue = focus.getValue();
		if (focusValue instanceof ItemStack) {
			ItemStack itemStack = (ItemStack) focusValue;
			Item item = itemStack.getItem();
			switch (focus.getMode()) {
				case INPUT: {
					if (item == Items.BANNER || HorseArmorType.isHorseArmor(item)) {
						return Collections.singletonList(VanillaRecipeCategoryUid.BREWING);
					}
					break;
				}
				case OUTPUT: {
					if (item == Item.getItemFromBlock(Blocks.DIAMOND_BLOCK)) {
						return Collections.singletonList(VanillaRecipeCategoryUid.BREWING);
					}
				}
				case NONE:
					return Collections.singletonList(VanillaRecipeCategoryUid.BREWING);
			}
		}
		return Collections.emptyList();
	}

	@Override
	public <T extends IRecipeWrapper, V> List<T> getRecipeWrappers(IRecipeCategory<T> recipeCategory, IFocus<V> focus) {
		if (recipeCategory.getUid().equals(VanillaRecipeCategoryUid.BREWING)) {
			List<ItemStack> potionIngredients = null;
			ItemStack potionInput = new ItemStack(Items.DIAMOND_HORSE_ARMOR);
			ItemStack potionOutput = new ItemStack(Blocks.DIAMOND_BLOCK);

			IFocus.Mode focusMode = focus.getMode();
			if (focusMode != IFocus.Mode.NONE) {
				V focusValue = focus.getValue();
				if (focusValue instanceof ItemStack) {
					ItemStack itemStack = (ItemStack) focusValue;
					Item item = itemStack.getItem();

					if (focusMode == IFocus.Mode.INPUT) {
						if (item == Items.BANNER) {
							potionIngredients = Collections.singletonList(itemStack);
						} else if (HorseArmorType.isHorseArmor(item)) {
							potionInput = itemStack;
						}
					} else if (focusMode == IFocus.Mode.OUTPUT) {
						if (item == Item.getItemFromBlock(Blocks.DIAMOND_BLOCK)) {
							potionOutput = itemStack;
						}
					}
				} else {
					return Collections.emptyList();
				}
			}

			if (potionIngredients == null) {
				ItemStack blackBanner = new ItemStack(Items.BANNER, 1, OreDictionary.WILDCARD_VALUE);
				potionIngredients = JEIInternalPlugin.jeiHelpers.getStackHelper().getSubtypes(blackBanner);
			}

			//noinspection unchecked
			return Collections.singletonList(
					(T) new BrewingRecipeWrapper(potionIngredients, potionInput, potionOutput, 100)
			);
		}
		return Collections.emptyList();
	}
}
