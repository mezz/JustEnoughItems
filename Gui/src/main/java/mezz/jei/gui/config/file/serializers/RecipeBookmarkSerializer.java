package mezz.jei.gui.config.file.serializers;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.config.IJeiConfigValueSerializer;
import mezz.jei.common.config.file.serializers.DeserializeResult;
import mezz.jei.common.config.file.serializers.TypedIngredientSerializer;
import mezz.jei.gui.bookmarks.RecipeBookmark;
import mezz.jei.gui.overlay.elements.IElement;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class RecipeBookmarkSerializer implements IJeiConfigValueSerializer<RecipeBookmark<?, ?>> {
	private static final String SEPARATOR = "#";

	private final IRecipeManager recipeManager;
	private final IFocusFactory focusFactory;
	private final TypedIngredientSerializer ingredientSerializer;

	public RecipeBookmarkSerializer(
		IRecipeManager recipeManager,
		IFocusFactory focusFactory,
		TypedIngredientSerializer ingredientSerializer
	) {
		this.recipeManager = recipeManager;
		this.focusFactory = focusFactory;
		this.ingredientSerializer = ingredientSerializer;
	}

	@Override
	public String serialize(RecipeBookmark<?, ?> value) {
		IRecipeCategory<?> recipeCategory = value.getRecipeCategory();
		RecipeType<?> recipeType = recipeCategory.getRecipeType();
		ResourceLocation recipeTypeUid = recipeType.getUid();
		ResourceLocation recipeUid = value.getRecipeUid();
		IElement<?> element = value.getElement();
		ITypedIngredient<?> typedIngredient = element.getTypedIngredient();
		String outputSerialized = ingredientSerializer.serialize(typedIngredient);
		return recipeTypeUid + SEPARATOR + recipeUid + SEPARATOR + outputSerialized;
	}

	@Override
	public IDeserializeResult<RecipeBookmark<?, ?>> deserialize(String string) {
		String[] parts = string.split(SEPARATOR);
		if (parts.length != 3) {
			String error = "string must be 3 parts";
			return new DeserializeResult<>(null, error);
		}
		ResourceLocation recipeTypeUid;
		try {
			recipeTypeUid = new ResourceLocation(parts[0]);
		} catch (RuntimeException e) {
			String error = "recipe type uid must be a valid resource location: %s\n%s".formatted(string, e.getMessage());
			return new DeserializeResult<>(null, error);
		}
		ResourceLocation recipeUid;
		try {
			recipeUid = new ResourceLocation(parts[1]);
		} catch (RuntimeException e) {
			String error = "recipe uid must be a valid resource location: %s\n%s".formatted(string, e.getMessage());
			return new DeserializeResult<>(null, error);
		}
		IDeserializeResult<ITypedIngredient<?>> deserialized = ingredientSerializer.deserialize(parts[2]);
		Optional<ITypedIngredient<?>> outputResult = deserialized.getResult();
		if (outputResult.isEmpty()) {
			List<String> errors = deserialized.getErrors();
			return new DeserializeResult<>(null, errors);
		}
		Optional<RecipeType<?>> recipeTypeResult = recipeManager.getRecipeType(recipeTypeUid);
		if (recipeTypeResult.isEmpty()) {
			String error = "could not find a recipe type matching the given uid: %s".formatted(recipeTypeUid);
			return new DeserializeResult<>(null, error);
		}

		ITypedIngredient<?> output = outputResult.get();
		RecipeType<?> recipeType = recipeTypeResult.get();

		IRecipeCategory<?> recipeCategory = recipeManager.getRecipeCategory(recipeType);
		return createBookmark(string, recipeCategory, recipeUid, output);
	}

	private <T> DeserializeResult<RecipeBookmark<?, ?>> createBookmark(String string, IRecipeCategory<T> recipeCategory, ResourceLocation recipeUid, ITypedIngredient<?> output) {
		IFocus<?> focus = focusFactory.createFocus(RecipeIngredientRole.OUTPUT, output);

		Optional<T> recipeResult = findRecipe(recipeCategory, List.of(focus), recipeUid);
		if (recipeResult.isEmpty()) {
			String error = "could not find a recipe for this string: %s".formatted(string);
			return new DeserializeResult<>(null, error);
		}

		T recipe = recipeResult.get();
		RecipeBookmark<T, ?> recipeBookmark = new RecipeBookmark<>(recipeCategory, recipe, recipeUid, output);
		return new DeserializeResult<>(recipeBookmark);
	}

	private <T> Optional<T> findRecipe(IRecipeCategory<T> recipeCategory, List<IFocus<?>> focus, ResourceLocation recipeUid) {
		RecipeType<T> recipeType = recipeCategory.getRecipeType();
		return recipeManager.createRecipeLookup(recipeType)
			.limitFocus(focus)
			.get()
			.filter(r -> Objects.equals(recipeCategory.getRegistryName(r), recipeUid))
			.findFirst();
	}

	@Override
	public boolean isValid(RecipeBookmark<?, ?> value) {
		return true;
	}

	@Override
	public Optional<Collection<RecipeBookmark<?, ?>>> getAllValidValues() {
		return Optional.empty();
	}

	@Override
	public String getValidValuesDescription() {
		return "";
	}
}
