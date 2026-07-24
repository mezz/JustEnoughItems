package mezz.jei.gui.util;

import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.platform.IPlatformFluidHelperInternal;
import mezz.jei.common.platform.Services;
import net.minecraft.world.level.material.Fluid;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FocusUtil {
	private final IFocusFactory focusFactory;
	private final IClientConfig clientConfig;
	private final IIngredientManager ingredientManager;

	public FocusUtil(IFocusFactory focusFactory, IClientConfig clientConfig, IIngredientManager ingredientManager) {
		this.focusFactory = focusFactory;
		this.clientConfig = clientConfig;
		this.ingredientManager = ingredientManager;
	}

	public List<IFocus<?>> createFocuses(ITypedIngredient<?> ingredient, List<RecipeIngredientRole> roles) {
		List<ITypedIngredient<?>> ingredients = new ArrayList<>();
		ingredients.add(ingredient);

		if (clientConfig.isLookupFluidContentsEnabled()) {
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
