package mezz.jei.library.load.registration;

import com.google.common.collect.ImmutableListMultimap;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.collect.ListMultiMap;
import mezz.jei.common.ingredients.TypedIngredient;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.library.ingredients.SimpleIngredientAcceptor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class RecipeCatalystRegistration implements IRecipeCatalystRegistration {
	private static final Logger LOGGER = LogManager.getLogger();
	private final ListMultiMap<RecipeType<?>, Consumer<IIngredientAcceptor<?>>> recipeCatalysts = new ListMultiMap<>();
	private final IIngredientManager ingredientManager;
	private final IJeiHelpers jeiHelpers;

	public RecipeCatalystRegistration(IIngredientManager ingredientManager, IJeiHelpers jeiHelpers) {
		this.ingredientManager = ingredientManager;
		this.jeiHelpers = jeiHelpers;
	}

	@Override
	public IIngredientManager getIngredientManager() {
		return ingredientManager;
	}

	@Override
	public IJeiHelpers getJeiHelpers() {
		return jeiHelpers;
	}

	@Override
	public <T> void addRecipeCatalyst(IIngredientType<T> ingredientType, T ingredient, RecipeType<?>... recipeTypes) {
		ErrorUtil.checkNotEmpty(recipeTypes, "recipeTypes");
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotNull(ingredient, "ingredient");

		for (RecipeType<?> recipeType : recipeTypes) {
			ErrorUtil.checkNotNull(recipeType, "recipeType");
			addRecipeCatalyst(recipeType, ingredientType, ingredient);
		}
	}

	@Override
	public void addRecipeCatalysts(RecipeType<?> recipeType, ItemLike... ingredients) {
		ErrorUtil.checkNotNull(recipeType, "recipeType");
		ErrorUtil.checkNotNull(ingredients, "ingredients");

		for (ItemLike itemLike : ingredients) {
			ErrorUtil.checkNotNull(itemLike, "itemLike");
			ItemStack itemStack = itemLike.asItem().getDefaultInstance();
			addRecipeCatalyst(recipeType, VanillaTypes.ITEM_STACK, itemStack);
		}
	}

	@Override
	public <T> void addRecipeCatalysts(RecipeType<?> recipeType, IIngredientType<T> ingredientType, List<T> ingredients) {
		ErrorUtil.checkNotNull(recipeType, "recipeType");
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotNull(ingredients, "ingredients");

		for (T ingredient : ingredients) {
			ErrorUtil.checkNotNull(ingredient, "ingredient");
			addRecipeCatalyst(recipeType, ingredientType, ingredient);
		}
	}

	private <T> void addRecipeCatalyst(RecipeType<?> recipeType, IIngredientType<T> ingredientType, T ingredient) {
		@Nullable
		ITypedIngredient<T> typedIngredient = TypedIngredient.createAndFilterInvalid(this.ingredientManager, ingredientType, ingredient, true);
		if (typedIngredient != null) {
			addRecipeCatalyst(recipeType, typedIngredient);
		} else {
			String ingredientInfo = ErrorUtil.getIngredientInfo(ingredient, ingredientType, ingredientManager);
			LOGGER.warn("Skipping invalid recipe catalyst for {}: {}", recipeType.getUid(), ingredientInfo);
		}
	}

	private void addRecipeCatalyst(RecipeType<?> recipeType, ITypedIngredient<?> ingredient) {
		addRecipeCatalyst(recipeType, acceptor -> acceptor.addTypedIngredient(ingredient));
	}

	@Override
	public void addRecipeCatalyst(RecipeType<?> recipeType, Consumer<IIngredientAcceptor<?>> ingredientAdder) {
		ErrorUtil.checkNotNull(recipeType, "recipeType");
		ErrorUtil.checkNotNull(ingredientAdder, "ingredientAdder");

		Consumer<IIngredientAcceptor<?>> validatedAdder = acceptor -> addValidIngredients(ingredientAdder, acceptor);
		SimpleIngredientAcceptor acceptor = new SimpleIngredientAcceptor(ingredientManager);
		validatedAdder.accept(acceptor);
		if (!acceptor.getAllIngredients().isEmpty()) {
			this.recipeCatalysts.put(recipeType, validatedAdder);
		}
	}

	private void addValidIngredients(Consumer<IIngredientAcceptor<?>> ingredientAdder, IIngredientAcceptor<?> target) {
		SimpleIngredientAcceptor acceptor = new SimpleIngredientAcceptor(ingredientManager);
		ingredientAdder.accept(acceptor);
		for (ITypedIngredient<?> ingredient : acceptor.getAllIngredients()) {
			addValidIngredient(target, ingredient);
		}
	}

	private <T> void addValidIngredient(IIngredientAcceptor<?> target, ITypedIngredient<T> ingredient) {
		@Nullable
		ITypedIngredient<T> validatedIngredient = TypedIngredient.createAndFilterInvalid(ingredientManager, ingredient.getType(), ingredient.getIngredient(), false);
		if (validatedIngredient != null) {
			target.addTypedIngredient(validatedIngredient);
		}
	}

	public ImmutableListMultimap<RecipeType<?>, Consumer<IIngredientAcceptor<?>>> getRecipeCatalysts() {
		return recipeCatalysts.toImmutable();
	}
}
