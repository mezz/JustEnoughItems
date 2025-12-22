package mezz.jei.library.gui.recipes.layout.builder;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IScalableDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryDecorator;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.Internal;
import mezz.jei.common.util.ImmutablePoint2i;
import mezz.jei.core.util.Pair;
import mezz.jei.library.gui.ingredients.CycleTicker;
import mezz.jei.library.gui.recipes.IngredientsTooltipCallback;
import mezz.jei.library.gui.recipes.OutputSlotTooltipCallback;
import mezz.jei.library.gui.recipes.RecipeLayout;
import mezz.jei.library.gui.recipes.ShapelessIcon;
import mezz.jei.library.ingredients.DisplayIngredientAcceptor;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class RecipeLayoutBuilder<T> implements IRecipeLayoutBuilder {
	private final List<RecipeSlotBuilder> visibleSlots = new ArrayList<>();
	private final List<List<RecipeSlotBuilder>> focusLinkedSlots = new ArrayList<>();

	private final IIngredientManager ingredientManager;
	private final IRecipeCategory<T> recipeCategory;
	private final T recipe;

	private boolean shapeless = false;
	private int shapelessX = -1;
	private int shapelessY = -1;
	private int recipeTransferX = -1;
	private int recipeTransferY = -1;
	private int nextSlotIndex = 0;

	public RecipeLayoutBuilder(IRecipeCategory<T> recipeCategory, T recipe, IIngredientManager ingredientManager) {
		this.recipeCategory = recipeCategory;
		this.recipe = recipe;
		this.ingredientManager = ingredientManager;
	}

	@Override
	public IRecipeSlotBuilder addSlot(RecipeIngredientRole role) {
		RecipeSlotBuilder slot = new RecipeSlotBuilder(ingredientManager, nextSlotIndex++, role);

		if (role == RecipeIngredientRole.OUTPUT) {
			addOutputSlotTooltipCallback(slot);
		}

		this.visibleSlots.add(slot);
		return slot;
	}

	private void addOutputSlotTooltipCallback(RecipeSlotBuilder slot) {
		Identifier recipeId = recipeCategory.getIdentifier(recipe);
		if (recipeId != null) {
			IRecipeType<T> recipeType = recipeCategory.getRecipeType();
			OutputSlotTooltipCallback callback = new OutputSlotTooltipCallback(recipeId, recipeType);
			slot.addRichTooltipCallback(callback);
		}
	}

	@Override
	public IIngredientAcceptor<?> addInvisibleIngredients(RecipeIngredientRole role) {
		return new RecipeSlotBuilder(ingredientManager, nextSlotIndex++, role);
	}

	@Override
	public void moveRecipeTransferButton(int posX, int posY) {
		this.recipeTransferX = posX;
		this.recipeTransferY = posY;
	}

	@Override
	public void setShapeless() {
		this.shapeless = true;
	}

	@Override
	public void setShapeless(int posX, int posY) {
		this.shapeless = true;
		this.shapelessX = posX;
		this.shapelessY = posY;
	}

	@Override
	public void createFocusLink(IIngredientAcceptor<?>... slots) {
		List<RecipeSlotBuilder> builders = new ArrayList<>();
		// The focus-linked slots should have the same number of ingredients.
		// Users can technically add more ingredients to the slots later,
		// but it's probably not worth the effort of enforcing this very strictly.
		int count = -1;
		for (IIngredientAcceptor<?> slot : slots) {
			RecipeSlotBuilder builder = (RecipeSlotBuilder) slot;
			builders.add(builder);

			DisplayIngredientAcceptor displayIngredientAcceptor = builder.getIngredientAcceptor();
			List<@Nullable ITypedIngredient<?>> allIngredients = displayIngredientAcceptor.getAllIngredients();
			int ingredientCount = allIngredients.size();
			if (count == -1) {
				count = ingredientCount;
			} else if (count != ingredientCount) {
				IntSummaryStatistics stats = Arrays.stream(slots)
					.map(RecipeSlotBuilder.class::cast)
					.map(RecipeSlotBuilder::getIngredientAcceptor)
					.map(DisplayIngredientAcceptor::getAllIngredients)
					.mapToInt(Collection::size)
					.summaryStatistics();
				throw new IllegalArgumentException(
					"All slots must have the same number of ingredients in order to create a focus link. " +
						String.format("slot stats: %s", stats)
				);
			}
		}

		this.focusLinkedSlots.add(builders);
	}

	public RecipeLayout<T> buildRecipeLayout(
		IFocusGroup focuses,
		Collection<IRecipeCategoryDecorator<T>> decorators,
		IScalableDrawable recipeBackground,
		int recipeBorderPadding
	) {
		ShapelessIcon shapelessIcon = createShapelessIcon(recipeCategory);
		ImmutablePoint2i recipeTransferButtonPosition = getRecipeTransferButtonPosition(recipeCategory, recipeBorderPadding);

		List<Pair<Integer, IRecipeSlotDrawable>> slots = new ArrayList<>();

		CycleTicker cycleTicker = CycleTicker.createWithRandomOffset();

		Set<RecipeSlotBuilder> focusLinkedSlots = new HashSet<>();
		for (List<RecipeSlotBuilder> linkedSlots : this.focusLinkedSlots) {
			IntSet focusMatches = new IntArraySet();
			for (RecipeSlotBuilder slot : linkedSlots) {
				focusMatches.addAll(slot.getMatches(focuses));
			}
			for (RecipeSlotBuilder slotBuilder : linkedSlots) {
				if (!visibleSlots.contains(slotBuilder)) {
					continue;
				}
				Pair<Integer, IRecipeSlotDrawable> slotDrawable = slotBuilder.build(focusMatches, cycleTicker);
				slots.add(slotDrawable);
			}
			focusLinkedSlots.addAll(linkedSlots);
		}

		class LayoutSupplier implements Supplier<IRecipeLayoutDrawable<?>>{
			private @Nullable IRecipeLayoutDrawable<?> drawable;
			@Override
			public @Nullable IRecipeLayoutDrawable<?> get() {
				return drawable;
			}
		}
		final LayoutSupplier layoutSupplier = new LayoutSupplier();

		for (RecipeSlotBuilder slotBuilder : visibleSlots) {
			if (!focusLinkedSlots.contains(slotBuilder)) {
				if (slotBuilder.getRole() == RecipeIngredientRole.OUTPUT) {
					slotBuilder.addRichTooltipCallback(new IngredientsTooltipCallback(layoutSupplier));
				}
				Pair<Integer, IRecipeSlotDrawable> slotDrawable = slotBuilder.build(focuses, cycleTicker);
				slots.add(slotDrawable);
			}
		}

		RecipeLayout<T> recipeLayout = new RecipeLayout<>(
			recipeCategory,
			decorators,
			recipe,
			recipeBackground,
			recipeBorderPadding,
			shapelessIcon,
			recipeTransferButtonPosition,
			sortSlots(slots),
			cycleTicker,
			focuses
		);

		layoutSupplier.drawable = recipeLayout;

		return recipeLayout;
	}

	private static List<IRecipeSlotDrawable> sortSlots(List<Pair<Integer, IRecipeSlotDrawable>> indexedSlots) {
		List<Pair<Integer, IRecipeSlotDrawable>> sortedPairs = new ArrayList<>(indexedSlots);
		sortedPairs.sort(Comparator.comparingInt(Pair::first));

		List<IRecipeSlotDrawable> iRecipeSlotDrawables = new ArrayList<>(sortedPairs.size());
		for (Pair<Integer, IRecipeSlotDrawable> indexedSlot : sortedPairs) {
			IRecipeSlotDrawable second = indexedSlot.second();
			iRecipeSlotDrawables.add(second);
		}
		return iRecipeSlotDrawables;
	}

	@Nullable
	private ShapelessIcon createShapelessIcon(IRecipeCategory<?> recipeCategory) {
		if (!shapeless) {
			return null;
		}
		IDrawable icon = Internal.getTextures().getShapelessIcon();
		final int x;
		final int y;
		if (this.shapelessX >= 0 && this.shapelessY >= 0) {
			x = this.shapelessX;
			y = this.shapelessY;
		} else {
			// align to top-right
			x = recipeCategory.getWidth() - icon.getWidth();
			y = 0;
		}
		return new ShapelessIcon(icon, x, y);
	}

	private ImmutablePoint2i getRecipeTransferButtonPosition(IRecipeCategory<?> recipeCategory, int recipeBorderPadding) {
		if (this.recipeTransferX >= 0 && this.recipeTransferY >= 0) {
			return new ImmutablePoint2i(
				this.recipeTransferX,
				this.recipeTransferY
			);
		}
		return new ImmutablePoint2i(
			recipeCategory.getWidth() + recipeBorderPadding + RecipeLayout.RECIPE_BUTTON_SPACING,
			recipeCategory.getHeight() + recipeBorderPadding - RecipeLayout.RECIPE_BUTTON_SIZE
		);
	}
}
