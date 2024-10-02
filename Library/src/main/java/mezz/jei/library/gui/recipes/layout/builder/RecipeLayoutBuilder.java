package mezz.jei.library.gui.recipes.layout.builder;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IScalableDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryDecorator;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.Internal;
import mezz.jei.common.util.ImmutablePoint2i;
import mezz.jei.core.collect.ListMultiMap;
import mezz.jei.core.util.Pair;
import mezz.jei.library.gui.ingredients.CycleTicker;
import mezz.jei.library.gui.recipes.OutputSlotTooltipCallback;
import mezz.jei.library.gui.recipes.RecipeLayout;
import mezz.jei.library.gui.recipes.ShapelessIcon;
import mezz.jei.library.ingredients.DisplayIngredientAcceptor;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class RecipeLayoutBuilder<T> implements IRecipeLayoutBuilder {
	private final List<RecipeSlotBuilder> slots = new ArrayList<>();
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

		this.slots.add(slot);
		return slot;
	}

	@SuppressWarnings("removal")
	@Override
	@Deprecated
	public IRecipeSlotBuilder addSlotToWidget(RecipeIngredientRole role, mezz.jei.api.gui.widgets.ISlottedWidgetFactory<?> widgetFactory) {
		RecipeSlotBuilder slot = new RecipeSlotBuilder(ingredientManager, nextSlotIndex++, role)
			.assignToWidgetFactory(widgetFactory);

		if (role == RecipeIngredientRole.OUTPUT) {
			addOutputSlotTooltipCallback(slot);
		}

		this.slots.add(slot);
		return slot;
	}

	private void addOutputSlotTooltipCallback(RecipeSlotBuilder slot) {
		ResourceLocation recipeName = recipeCategory.getRegistryName(recipe);
		if (recipeName != null) {
			RecipeType<T> recipeType = recipeCategory.getRecipeType();
			OutputSlotTooltipCallback callback = new OutputSlotTooltipCallback(recipeName, recipeType);
			slot.addRichTooltipCallback(callback);
		}
	}

	@Override
	public IIngredientAcceptor<?> addInvisibleIngredients(RecipeIngredientRole role) {
		// invisible slots are only used by IngredientSupplierBuilder, and are ignored here
		return IngredientAcceptorVoid.INSTANCE;
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
			List<Optional<ITypedIngredient<?>>> allIngredients = displayIngredientAcceptor.getAllIngredients();
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

	@SuppressWarnings("removal")
	public RecipeLayout<T> buildRecipeLayout(
		IFocusGroup focuses,
		Collection<IRecipeCategoryDecorator<T>> decorators,
		IScalableDrawable recipeBackground,
		int recipeBorderPadding
	) {
		ShapelessIcon shapelessIcon = createShapelessIcon(recipeCategory);
		ImmutablePoint2i recipeTransferButtonPosition = getRecipeTransferButtonPosition(recipeCategory, recipeBorderPadding);

		List<Pair<Integer, IRecipeSlotDrawable>> recipeCategorySlots = new ArrayList<>();
		List<Pair<Integer, IRecipeSlotDrawable>> allSlots = new ArrayList<>();
		ListMultiMap<mezz.jei.api.gui.widgets.ISlottedWidgetFactory<?>, Pair<Integer, IRecipeSlotDrawable>> widgetSlots = new ListMultiMap<>();

		CycleTicker cycleTicker = CycleTicker.createWithRandomOffset();

		Set<RecipeSlotBuilder> focusLinkedSlots = new HashSet<>();
		for (List<RecipeSlotBuilder> linkedSlots : this.focusLinkedSlots) {
			IntSet focusMatches = new IntArraySet();
			for (RecipeSlotBuilder slot : linkedSlots) {
				focusMatches.addAll(slot.getMatches(focuses));
			}
			for (RecipeSlotBuilder slotBuilder : linkedSlots) {
				mezz.jei.api.gui.widgets.ISlottedWidgetFactory<?> assignedWidget = slotBuilder.getAssignedWidget();
				Pair<Integer, IRecipeSlotDrawable> slotDrawable = slotBuilder.build(focusMatches, cycleTicker);
				if (assignedWidget == null) {
					recipeCategorySlots.add(slotDrawable);
				} else {
					widgetSlots.put(assignedWidget, slotDrawable);
				}
				allSlots.add(slotDrawable);
			}
			focusLinkedSlots.addAll(linkedSlots);
		}

		for (RecipeSlotBuilder slotBuilder : slots) {
			if (!focusLinkedSlots.contains(slotBuilder)) {
				mezz.jei.api.gui.widgets.ISlottedWidgetFactory<?> assignedWidget = slotBuilder.getAssignedWidget();
				Pair<Integer, IRecipeSlotDrawable> slotDrawable = slotBuilder.build(focuses, cycleTicker);
				if (assignedWidget == null) {
					recipeCategorySlots.add(slotDrawable);
				} else {
					widgetSlots.put(assignedWidget, slotDrawable);
				}
				allSlots.add(slotDrawable);
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
			sortSlots(recipeCategorySlots),
			sortSlots(allSlots),
			cycleTicker,
			focuses
		);

		for (Map.Entry<mezz.jei.api.gui.widgets.ISlottedWidgetFactory<?>, List<Pair<Integer, IRecipeSlotDrawable>>> e : widgetSlots.entrySet()) {
			@SuppressWarnings("unchecked")
			mezz.jei.api.gui.widgets.ISlottedWidgetFactory<T> factory = (mezz.jei.api.gui.widgets.ISlottedWidgetFactory<T>) e.getKey();
			List<IRecipeSlotDrawable> slots = sortSlots(e.getValue());
			factory.createWidgetForSlots(recipeLayout, recipe, slots);
		}

		return recipeLayout;
	}

	private static List<IRecipeSlotDrawable> sortSlots(List<Pair<Integer, IRecipeSlotDrawable>> indexedSlots) {
		return indexedSlots.stream()
			.sorted(Comparator.comparingInt(Pair::first))
			.map(Pair::second)
			.collect(Collectors.toCollection(ArrayList::new));
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
