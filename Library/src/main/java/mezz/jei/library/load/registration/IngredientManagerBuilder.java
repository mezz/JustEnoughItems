package mezz.jei.library.load.registration;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.ISubtypeManager;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.library.ingredients.IngredientInfo;
import mezz.jei.library.ingredients.IngredientManager;
import mezz.jei.library.ingredients.RegisteredIngredients;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

public class IngredientManagerBuilder implements IModIngredientRegistration {
	private final List<IngredientInfo<?>> ingredientInfos = new ArrayList<>();
	private final Set<IIngredientType<?>> registeredIngredientSet = Collections.newSetFromMap(new IdentityHashMap<>());
	private final ISubtypeManager subtypeManager;
	private final IColorHelper colorHelper;

	public IngredientManagerBuilder(ISubtypeManager subtypeManager, IColorHelper colorHelper) {
		this.subtypeManager = subtypeManager;
		this.colorHelper = colorHelper;
	}

	@SuppressWarnings("removal")
	@Override
	public <V> void register(IIngredientType<V> ingredientType, Collection<V> allIngredients, IIngredientHelper<V> ingredientHelper, IIngredientRenderer<V> ingredientRenderer) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotNull(allIngredients, "allIngredients");
		ErrorUtil.checkNotNull(ingredientHelper, "ingredientHelper");
		ErrorUtil.checkNotNull(ingredientRenderer, "ingredientRenderer");
		Preconditions.checkArgument(ingredientRenderer.getWidth() == 16,
			"the default ingredient renderer registered here will be used for drawing " +
				"ingredients in the ingredient list, and it must have a width of 16"
		);
		Preconditions.checkArgument(ingredientRenderer.getHeight() == 16,
			"the default ingredient renderer registered here will be used for drawing " +
				"ingredients in the ingredient list, and it must have a height of 16"
		);
		if (registeredIngredientSet.contains(ingredientType)) {
			throw new IllegalArgumentException("Ingredient type has already been registered: " + ingredientType.getIngredientClass());
		}

		ingredientInfos.add(new IngredientInfo<>(ingredientType, allIngredients, ingredientHelper, ingredientRenderer, null));
		registeredIngredientSet.add(ingredientType);
	}

	@Override
	public <V> void register(
		IIngredientType<V> ingredientType,
		Collection<V> allIngredients,
		IIngredientHelper<V> ingredientHelper,
		IIngredientRenderer<V> ingredientRenderer,
		Codec<V> ingredientCodec
	) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotNull(allIngredients, "allIngredients");
		ErrorUtil.checkNotNull(ingredientHelper, "ingredientHelper");
		ErrorUtil.checkNotNull(ingredientRenderer, "ingredientRenderer");
		ErrorUtil.checkNotNull(ingredientCodec, "ingredientCodec");
		Preconditions.checkArgument(ingredientRenderer.getWidth() == 16,
			"the default ingredient renderer registered here will be used for drawing " +
				"ingredients in the ingredient list, and it must have a width of 16"
		);
		Preconditions.checkArgument(ingredientRenderer.getHeight() == 16,
			"the default ingredient renderer registered here will be used for drawing " +
				"ingredients in the ingredient list, and it must have a height of 16"
		);

		if (registeredIngredientSet.contains(ingredientType)) {
			throw new IllegalArgumentException("Ingredient type has already been registered: " + ingredientType.getIngredientClass());
		}

		ingredientInfos.add(new IngredientInfo<>(ingredientType, allIngredients, ingredientHelper, ingredientRenderer, ingredientCodec));
		registeredIngredientSet.add(ingredientType);
	}

	@Override
	public ISubtypeManager getSubtypeManager() {
		return subtypeManager;
	}

	@Override
	public IColorHelper getColorHelper() {
		return colorHelper;
	}

	public IIngredientManager build() {
		RegisteredIngredients registeredIngredients = new RegisteredIngredients(ingredientInfos);
		return new IngredientManager(registeredIngredients);
	}
}
