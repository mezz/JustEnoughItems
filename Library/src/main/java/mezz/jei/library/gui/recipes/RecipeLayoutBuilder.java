package mezz.jei.library.gui.recipes;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IScalableDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.inputs.IJeiGuiEventListener;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.gui.widgets.ISlottedRecipeWidget;
import mezz.jei.api.gui.widgets.ISlottedWidgetFactory;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryDecorator;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.Internal;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.ImmutablePoint2i;
import mezz.jei.core.collect.ListMultiMap;
import mezz.jei.library.gui.recipes.layout.builder.InvisibleRecipeLayoutSlotSource;
import mezz.jei.library.gui.recipes.layout.builder.RecipeSlotBuilder;
import mezz.jei.library.gui.recipes.layout.builder.RecipeSlotIngredients;
import mezz.jei.library.ingredients.IIngredientSupplier;
import mezz.jei.library.ingredients.IngredientAcceptor;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

// TODO: make IRecipeLayoutBuilder take a generic parameter for ISlottedWidgetFactory
public class RecipeLayoutBuilder<T> implements IRecipeLayoutBuilder, IRecipeExtrasBuilder {
	private final List<RecipeSlotBuilder> slots = new ArrayList<>();
	private final List<InvisibleRecipeLayoutSlotSource> invisibleSlots = new ArrayList<>();
	private final List<List<RecipeSlotBuilder>> focusLinkedSlots = new ArrayList<>();
	private final List<IRecipeWidget> widgets = new ArrayList<>();
	private final List<IJeiInputHandler> inputHandlers = new ArrayList<>();
	private final List<IJeiGuiEventListener> guiEventListeners = new ArrayList<>();

	private final IIngredientManager ingredientManager;
	private final IRecipeCategory<T> recipeCategory;
	private final T recipe;
	private final @Nullable ResourceLocation recipeName;
	private final int ingredientCycleOffset = (int) ((Math.random() * 10000) % Integer.MAX_VALUE);;

	private boolean shapeless = false;
	private int shapelessX = -1;
	private int shapelessY = -1;
	private int recipeTransferX = -1;
	private int recipeTransferY = -1;

	public RecipeLayoutBuilder(IRecipeCategory<T> recipeCategory, T recipe, IIngredientManager ingredientManager) {
		this.recipeCategory = recipeCategory;
		this.recipe = recipe;
		this.ingredientManager = ingredientManager;

		this.recipeName = recipeCategory.getRegistryName(recipe);
	}

	@Override
	public IRecipeSlotBuilder addSlot(RecipeIngredientRole role, int x, int y) {
		RecipeSlotBuilder slot = new RecipeSlotBuilder(ingredientManager, role, x, y, ingredientCycleOffset);

		if (role == RecipeIngredientRole.OUTPUT) {
			if (recipeName != null) {
				OutputSlotTooltipCallback callback = new OutputSlotTooltipCallback(recipeName);
				slot.addTooltipCallback(callback);
			}
		}

		this.slots.add(slot);
		return slot;
	}

	@Override
	public IRecipeSlotBuilder addSlotToWidget(RecipeIngredientRole role, ISlottedWidgetFactory<?> widgetFactory) {
		RecipeSlotBuilder slot = new RecipeSlotBuilder(ingredientManager, role, 0, 0, ingredientCycleOffset)
			.assignToWidgetFactory(widgetFactory);

		if (role == RecipeIngredientRole.OUTPUT) {
			if (recipeName != null) {
				OutputSlotTooltipCallback callback = new OutputSlotTooltipCallback(recipeName);
				slot.addTooltipCallback(callback);
			}
		}

		this.slots.add(slot);
		return slot;
	}

	@Override
	public IIngredientAcceptor<?> addInvisibleIngredients(RecipeIngredientRole role) {
		InvisibleRecipeLayoutSlotSource slot = new InvisibleRecipeLayoutSlotSource(ingredientManager, role);
		this.invisibleSlots.add(slot);
		return slot;
	}

	@Override
	public void addWidget(IRecipeWidget widget) {
		ErrorUtil.checkNotNull(widget, "widget");
		this.widgets.add(widget);
	}

	@Override
	public void addInputHandler(IJeiInputHandler inputHandler) {
		ErrorUtil.checkNotNull(inputHandler, "inputHandler");
		this.inputHandlers.add(inputHandler);
	}

	@Override
	public void addGuiEventListener(IJeiGuiEventListener guiEventListener) {
		ErrorUtil.checkNotNull(guiEventListener, "guiEventListener");
		this.guiEventListeners.add(guiEventListener);
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

			IngredientAcceptor ingredientAcceptor = builder.getIngredientAcceptor();
			List<Optional<ITypedIngredient<?>>> allIngredients = ingredientAcceptor.getAllIngredients();
			int ingredientCount = allIngredients.size();
			if (count == -1) {
				count = ingredientCount;
			} else if (count != ingredientCount) {
				IntSummaryStatistics stats = Arrays.stream(slots)
					.map(RecipeSlotBuilder.class::cast)
					.map(RecipeSlotBuilder::getIngredientAcceptor)
					.map(IngredientAcceptor::getAllIngredients)
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

	public boolean isEmpty() {
		return slots.isEmpty() &&
			invisibleSlots.isEmpty();
	}

	public IIngredientSupplier buildIngredientSupplier() {
		List<RecipeSlotIngredients> ingredients = new ArrayList<>();
		for (RecipeSlotBuilder slot : this.slots) {
			ingredients.add(slot.getRecipeSlotIngredients());
		}
		for (InvisibleRecipeLayoutSlotSource slot : this.invisibleSlots) {
			ingredients.add(slot.getRecipeSlotIngredients());
		}
		return new RecipeLayoutIngredientSupplier(ingredients);
	}

	public RecipeLayout<T> buildRecipeLayout(
		IFocusGroup focuses,
		Collection<IRecipeCategoryDecorator<T>> decorators,
		IScalableDrawable recipeBackground,
		int recipeBorderPadding
	) {
		ShapelessIcon shapelessIcon = createShapelessIcon(recipeCategory);
		ImmutablePoint2i recipeTransferButtonPosition = getRecipeTransferButtonPosition(recipeCategory, recipeBorderPadding);

		List<IRecipeSlotDrawable> recipeCategorySlots = new ArrayList<>();
		List<IRecipeSlotDrawable> allSlots = new ArrayList<>(recipeCategorySlots);
		ListMultiMap<ISlottedWidgetFactory<?>, IRecipeSlotDrawable> widgetSlots = new ListMultiMap<>();

		Set<RecipeSlotBuilder> focusLinkedSlots = new HashSet<>();
		for (List<RecipeSlotBuilder> linkedSlots : this.focusLinkedSlots) {
			IntSet focusMatches = new IntArraySet();
			for (RecipeSlotBuilder slot : linkedSlots) {
				focusMatches.addAll(slot.getMatches(focuses));
			}
			for (RecipeSlotBuilder slotBuilder : linkedSlots) {
				ISlottedWidgetFactory<?> assignedWidget = slotBuilder.getAssignedWidget();
				IRecipeSlotDrawable slotDrawable = slotBuilder.build(focusMatches);
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
				ISlottedWidgetFactory<?> assignedWidget = slotBuilder.getAssignedWidget();
				IRecipeSlotDrawable slotDrawable = slotBuilder.build(focuses);
				if (assignedWidget == null) {
					recipeCategorySlots.add(slotDrawable);
				} else {
					widgetSlots.put(assignedWidget, slotDrawable);
				}
				allSlots.add(slotDrawable);
			}
		}

		for (Map.Entry<ISlottedWidgetFactory<?>, List<IRecipeSlotDrawable>> e : widgetSlots.entrySet()) {
			// TODO: breaking change: add a type parameter to IRecipeLayoutBuilder to avoid this cast
			@SuppressWarnings("unchecked")
			ISlottedWidgetFactory<T> factory = (ISlottedWidgetFactory<T>) e.getKey();
			List<IRecipeSlotDrawable> slots = e.getValue();
			factory.createWidgetForSlots(this, recipe, slots);
		}

		List<ISlottedRecipeWidget> slottedWidgets = new ArrayList<>();
		for (IRecipeWidget widget : widgets) {
			if (widget instanceof ISlottedRecipeWidget slottedWidget) {
				slottedWidgets.add(slottedWidget);
			}
		}

		return new RecipeLayout<>(
			recipeCategory,
			decorators,
			recipe,
			recipeBackground,
			recipeBorderPadding,
			shapelessIcon,
			recipeTransferButtonPosition,
			recipeCategorySlots,
			allSlots,
			slottedWidgets,
			widgets,
			inputHandlers,
			guiEventListeners
		);
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
