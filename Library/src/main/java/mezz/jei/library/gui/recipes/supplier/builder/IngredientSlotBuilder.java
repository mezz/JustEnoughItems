package mezz.jei.library.gui.recipes.supplier.builder;

import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.library.gui.recipes.layout.builder.RecipeSlotBuilder;
import mezz.jei.library.ingredients.DisplayIngredientAcceptor;
import mezz.jei.library.ingredients.IIngredientManagerInternal;
import mezz.jei.library.ingredients.SlotIngredient;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Minimal version of {@link RecipeSlotBuilder} that can only return the ingredients,
 * but doesn't bother building anything for drawing on screen.
 */
public class IngredientSlotBuilder extends IgnoredIngredientSlotBuilder {
	private final DisplayIngredientAcceptor ingredients;
	private final RecipeIngredientRole role;

	public IngredientSlotBuilder(IIngredientManagerInternal ingredientManager, ContextMap contextMap, RecipeIngredientRole role) {
		super(contextMap);
		this.ingredients = new DisplayIngredientAcceptor(ingredientManager, contextMap, role);
		this.role = role;
	}

	@Override
	public IRecipeSlotBuilder add(SlotDisplay slotDisplay) {
		this.ingredients.add(slotDisplay);
		return this;
	}

	@Override
	public <I> IRecipeSlotBuilder add(IIngredientType<I> ingredientType, SlotDisplay slotDisplay) {
		this.ingredients.add(ingredientType, slotDisplay);
		return this;
	}

	@Override
	public IRecipeSlotBuilder add(ItemStack itemStack) {
		this.ingredients.add(itemStack);
		return this;
	}

	@Override
	public IRecipeSlotBuilder add(ItemLike itemLike) {
		this.ingredients.add(itemLike);
		return this;
	}

	@Override
	public IRecipeSlotBuilder add(ItemStackTemplate itemStackTemplate) {
		this.ingredients.add(itemStackTemplate);
		return this;
	}

	@Override
	public IRecipeSlotBuilder add(Fluid fluid) {
		this.ingredients.add(fluid);
		return this;
	}

	@Override
	public IRecipeSlotBuilder add(Fluid fluid, long amount) {
		this.ingredients.add(fluid, amount);
		return this;
	}

	@Override
	public IRecipeSlotBuilder add(Fluid fluid, long amount, DataComponentPatch component) {
		this.ingredients.add(fluid, amount, component);
		return this;
	}

	@Override
	public IRecipeSlotBuilder add(Ingredient ingredient) {
		this.ingredients.add(ingredient);
		return this;
	}

	@Override
	public <I> IRecipeSlotBuilder add(IIngredientType<I> ingredientType, Ingredient ingredient) {
		this.ingredients.add(ingredientType, ingredient);
		return this;
	}

	@Override
	public <I> IRecipeSlotBuilder add(ITypedIngredient<I> typedIngredient) {
		this.ingredients.add(typedIngredient);
		return this;
	}

	@Override
	public <I> IRecipeSlotBuilder add(IIngredientType<I> ingredientType, I ingredient) {
		this.ingredients.add(ingredientType, ingredient);
		return this;
	}

	@Override
	public <I> IRecipeSlotBuilder addIngredients(IIngredientType<I> ingredientType, List<@Nullable I> ingredients) {
		this.ingredients.addIngredients(ingredientType, ingredients);
		return this;
	}

	@SuppressWarnings("removal")
	@Override
	public <I> IRecipeSlotBuilder addIngredient(IIngredientType<I> ingredientType, I ingredient) {
		this.ingredients.add(ingredientType, ingredient);
		return this;
	}

	@Override
	public IRecipeSlotBuilder addIngredientsUnsafe(List<?> ingredients) {
		this.ingredients.addIngredientsUnsafe(ingredients);
		return this;
	}

	@Override
	public IRecipeSlotBuilder addTypedIngredients(List<ITypedIngredient<?>> ingredients) {
		this.ingredients.addTypedIngredients(ingredients);
		return this;
	}

	@Override
	public IRecipeSlotBuilder addOptionalTypedIngredients(List<Optional<ITypedIngredient<?>>> ingredients) {
		this.ingredients.addOptionalTypedIngredients(ingredients);
		return this;
	}

	@Override
	public IRecipeSlotBuilder addItemStacks(List<ItemStack> itemStacks) {
		this.ingredients.addItemStacks(itemStacks);
		return this;
	}

	public List<@Nullable SlotIngredient<?>> getAllSlotIngredients() {
		return this.ingredients.getAllSlotIngredients();
	}

	public RecipeIngredientRole getRole() {
		return role;
	}
}
