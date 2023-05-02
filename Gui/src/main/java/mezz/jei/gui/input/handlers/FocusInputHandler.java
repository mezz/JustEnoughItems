package mezz.jei.gui.input.handlers;

import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.platform.IPlatformFluidHelperInternal;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.input.CombinedRecipeFocusSource;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.level.material.Fluid;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FocusInputHandler implements IUserInputHandler {
	private final CombinedRecipeFocusSource focusSource;
	private final IRecipesGui recipesGui;
	private final IFocusFactory focusFactory;
	private final IClientConfig clientConfig;
	private final IIngredientManager ingredientManager;

	public FocusInputHandler(
		CombinedRecipeFocusSource focusSource,
		IRecipesGui recipesGui,
		IFocusFactory focusFactory,
		IClientConfig clientConfig,
		IIngredientManager ingredientManager
	) {
		this.focusSource = focusSource;
		this.recipesGui = recipesGui;
		this.focusFactory = focusFactory;
		this.clientConfig = clientConfig;
		this.ingredientManager = ingredientManager;
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
		if (input.is(keyBindings.getShowRecipe())) {
			return handleShow(input, List.of(RecipeIngredientRole.OUTPUT), keyBindings);
		}

		if (input.is(keyBindings.getShowUses())) {
			return handleShow(input, List.of(RecipeIngredientRole.INPUT, RecipeIngredientRole.CATALYST), keyBindings);
		}

		return Optional.empty();
	}

	private Optional<IUserInputHandler> handleShow(UserInput input, List<RecipeIngredientRole> roles, IInternalKeyMappings keyBindings) {
		return focusSource.getIngredientUnderMouse(input, keyBindings)
			.findFirst()
			.map(clicked -> {
				if (!input.isSimulate()) {
					ITypedIngredient<?> ingredient = clicked.getTypedIngredient();
					List<IFocus<?>> focuses = createFocuses(ingredient, roles);
					recipesGui.show(focuses);
				}
				ImmutableRect2i area = clicked.getArea();
				return LimitedAreaInputHandler.create(this, area);
			});
	}

	private List<IFocus<?>> createFocuses(ITypedIngredient<?> ingredient, List<RecipeIngredientRole> roles) {
		List<ITypedIngredient<?>> ingredients = new ArrayList<>();
		ingredients.add(ingredient);
		if (clientConfig.isLookupFluidContents()) {
			IPlatformFluidHelperInternal<?> fluidHelper = Services.PLATFORM.getFluidHelper();
			getContainedFluid(fluidHelper, ingredient)
				.ifPresent(ingredients::add);
		}
		return roles.stream()
			.<IFocus<?>>flatMap(role ->
				ingredients.stream()
					.map(i -> focusFactory.createFocus(role, i))
			)
			.toList();
	}

	private <T> Optional<ITypedIngredient<T>> getContainedFluid(IPlatformFluidHelperInternal<T> fluidHelper, ITypedIngredient<?> ingredient) {
		return fluidHelper.getContainedFluid(ingredient)
			.flatMap(fluid -> {
				IIngredientTypeWithSubtypes<Fluid, T> type = fluidHelper.getFluidIngredientType();
				return ingredientManager.createTypedIngredient(type, fluid);
			});
	}
}
