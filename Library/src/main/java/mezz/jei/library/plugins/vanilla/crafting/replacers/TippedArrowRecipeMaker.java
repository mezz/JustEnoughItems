package mezz.jei.library.plugins.vanilla.crafting.replacers;

import mezz.jei.api.constants.ModIds;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.common.util.RegistryUtil;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.ImbueRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.util.function.Consumer;

public final class TippedArrowRecipeMaker implements IRecipeReplacer {
	private final IVanillaRecipeFactory vanillaRecipeFactory;

	public TippedArrowRecipeMaker(IVanillaRecipeFactory vanillaRecipeFactory) {
		this.vanillaRecipeFactory = vanillaRecipeFactory;
	}

	@Override
	public boolean replace(RecipeHolder<CraftingRecipe> recipe, Consumer<RecipeHolder<CraftingRecipe>> replacements) {
		if (recipe.value() instanceof ImbueRecipe) {
			Identifier identifier = recipe.id().identifier();
			if (identifier.getNamespace().equals(ModIds.MINECRAFT_ID) && identifier.getPath().equals("tipped_arrow")) {
				createRecipes(replacements);
				return true;
			}
		}
		return false;
	}

	private void createRecipes(Consumer<RecipeHolder<CraftingRecipe>> recipes) {
		String group = "jei.tipped.arrow";
		Ingredient arrowIngredient = Ingredient.of(Items.ARROW);

		Registry<Potion> potionRegistry = RegistryUtil.getRegistry(Registries.POTION);
		potionRegistry.listElements()
			.forEach(potion -> {
				ItemStack input = PotionContents.createItemStack(Items.LINGERING_POTION, potion);
				ItemStack output = PotionContents.createItemStack(Items.TIPPED_ARROW, potion);
				output.setCount(8);

				Ingredient potionIngredient = Ingredient.of(input.getItem());
				Identifier potionId = potion.key().identifier();
				Identifier recipeId = Identifier.fromNamespaceAndPath(ModIds.MINECRAFT_ID, "jei.tipped.arrow." + potionId.getNamespace() + "." + potionId.getPath());
				ResourceKey<Recipe<?>> resourceKey = ResourceKey.create(Registries.RECIPE, recipeId);
				SlotDisplay slotDisplay = new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(output));
				CraftingRecipe recipe = vanillaRecipeFactory.createShapedRecipeBuilder(CraftingBookCategory.MISC, slotDisplay)
					.group(group)
					.define('a', arrowIngredient)
					.define('p', potionIngredient, new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(input)))
					.pattern("aaa")
					.pattern("apa")
					.pattern("aaa")
					.build();
				RecipeHolder<CraftingRecipe> recipeHolder = new RecipeHolder<>(resourceKey, recipe);
				recipes.accept(recipeHolder);
			});
	}
}
