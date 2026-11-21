package mezz.jei.library.load.registration;

import com.google.common.collect.ImmutableListMultimap;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.collect.ListMultiMap;
import mezz.jei.common.ingredients.TypedIngredient;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.library.ingredients.IIngredientManagerInternal;
import mezz.jei.library.ingredients.SimpleIngredientAcceptor;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.ItemLike;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.function.Consumer;

public class RecipeCatalystRegistration implements IRecipeCatalystRegistration {
	private static final Logger LOGGER = LogManager.getLogger();
	private final ListMultiMap<IRecipeType<?>, Consumer<IIngredientAcceptor<?>>> craftingStations = new ListMultiMap<>();
	private final IIngredientManagerInternal ingredientManager;
	private final IJeiHelpers jeiHelpers;
	private final ContextMap contextMap;

	public RecipeCatalystRegistration(
		IIngredientManagerInternal ingredientManager,
		IJeiHelpers jeiHelpers,
		ContextMap contextMap
	) {
		this.ingredientManager = ingredientManager;
		this.jeiHelpers = jeiHelpers;
		this.contextMap = contextMap;
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
	public <T> void addCraftingStations(IRecipeType<?> recipeType, IIngredientType<T> ingredientType, List<T> ingredients) {
		ErrorUtil.checkNotNull(recipeType, "recipeType");
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotNull(ingredients, "ingredients");

		for (T ingredient : ingredients) {
			addCraftingStation(recipeType, ingredientType, ingredient);
		}
	}

	@Override
	public void addCraftingStation(IRecipeType<?> recipeType, SlotDisplay slotDisplay) {
		ErrorUtil.checkNotNull(recipeType, "recipeType");
		ErrorUtil.checkNotNull(slotDisplay, "slotDisplay");

		addCraftingStation(recipeType, acceptor -> acceptor.add(slotDisplay));
	}

	@Override
	public void addCraftingStation(IRecipeType<?> recipeType, ItemLike... ingredients) {
		ErrorUtil.checkNotNull(recipeType, "recipeType");
		ErrorUtil.checkNotNull(ingredients, "ingredients");

		for (ItemLike itemLike : ingredients) {
			ErrorUtil.checkNotNull(itemLike, "itemLike");
			ItemStack itemStack = itemLike.asItem().getDefaultInstance();
			addCraftingStation(recipeType, VanillaTypes.ITEM_STACK, itemStack);
		}
	}

	@Override
	public <T> void addCraftingStation(IRecipeType<?> recipeType, IIngredientType<T> ingredientType, T ingredient) {
		ErrorUtil.checkNotNull(recipeType, "recipeType");
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotNull(ingredient, "ingredient");

		ITypedIngredient<T> typedIngredient = TypedIngredient.createAndFilterInvalid(this.ingredientManager, ingredientType, ingredient, true);
		if (typedIngredient != null) {
			addCraftingStation(recipeType, typedIngredient);
		} else {
			String ingredientInfo = ErrorUtil.getIngredientInfo(ingredient, ingredientType, ingredientManager);
			LOGGER.warn("Skipping invalid crafting station for {}: {}", recipeType.getUid(), ingredientInfo);
		}
	}

	@SuppressWarnings("removal")
	@Override
	public <T> void addRecipeCatalyst(IIngredientType<T> ingredientType, T ingredient, IRecipeType<?>... recipeTypes) {
		ErrorUtil.checkNotEmpty(recipeTypes, "recipeTypes");
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotNull(ingredient, "ingredient");

		for (IRecipeType<?> recipeType : recipeTypes) {
			addCraftingStation(recipeType, ingredientType, ingredient);
		}
	}

	private void addCraftingStation(IRecipeType<?> recipeType, ITypedIngredient<?> ingredient) {
		addCraftingStation(recipeType, acceptor -> acceptor.add(ingredient));
	}

	private void addCraftingStation(IRecipeType<?> recipeType, Consumer<IIngredientAcceptor<?>> craftingStation) {
		SimpleIngredientAcceptor acceptor = new SimpleIngredientAcceptor(ingredientManager, contextMap, RecipeIngredientRole.CRAFTING_STATION);
		craftingStation.accept(acceptor);
		if (!acceptor.getAllSlotIngredients().isEmpty()) {
			this.craftingStations.put(recipeType, craftingStation);
		}
	}

	public ImmutableListMultimap<IRecipeType<?>, Consumer<IIngredientAcceptor<?>>> getCraftingStations() {
		return craftingStations.toImmutable();
	}
}
