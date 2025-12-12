package mezz.jei.library.plugins.vanilla.crafting.replacers;

import mezz.jei.api.constants.ModIds;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.common.util.RegistryUtil;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.util.List;

public final class TippedArrowRecipeMaker {

	public static List<RecipeHolder<CraftingRecipe>> createRecipes(IJeiHelpers jeiHelpers) {
		IVanillaRecipeFactory vanillaRecipeFactory = jeiHelpers.getVanillaRecipeFactory();

		String group = "jei.tipped.arrow";
		Ingredient arrowIngredient = Ingredient.of(Items.ARROW);

		Registry<Potion> potionRegistry = RegistryUtil.getRegistry(Registries.POTION);
		return potionRegistry.listElements()
			.map(potion -> {
				ItemStack input = PotionContents.createItemStack(Items.LINGERING_POTION, potion);
				ItemStack output = PotionContents.createItemStack(Items.TIPPED_ARROW, potion);
				output.setCount(8);

				Ingredient potionIngredient = Ingredient.of(input.getItem());
				Identifier potionId = potion.key().identifier();
				Identifier recipeId = Identifier.fromNamespaceAndPath(ModIds.MINECRAFT_ID, "jei.tipped.arrow." + potionId.getNamespace() + "." + potionId.getPath());
				ResourceKey<Recipe<?>> resourceKey = ResourceKey.create(Registries.RECIPE, recipeId);
				SlotDisplay slotDisplay = new SlotDisplay.ItemStackSlotDisplay(output);
				CraftingRecipe recipe = vanillaRecipeFactory.createShapedRecipeBuilder(CraftingBookCategory.MISC, slotDisplay)
					.group(group)
					.define('a', arrowIngredient)
					.define('p', potionIngredient, new SlotDisplay.ItemStackSlotDisplay(input))
					.pattern("aaa")
					.pattern("apa")
					.pattern("aaa")
					.build();
				return new RecipeHolder<>(resourceKey, recipe);
			})
			.toList();
	}

	private TippedArrowRecipeMaker() {

	}
}
