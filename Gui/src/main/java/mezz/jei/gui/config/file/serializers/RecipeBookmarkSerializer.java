package mezz.jei.gui.config.file.serializers;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.config.file.serializers.TypedIngredientSerializer;
import mezz.jei.common.transfer.RecipeTransferService;
import mezz.jei.gui.bookmarks.RecipeBookmark;
import mezz.jei.gui.overlay.elements.IElement;
import net.mezzdev.config.api.value.serializer.IDeserializeResult;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class RecipeBookmarkSerializer {
	private static final String SEPARATOR = "#";

	private final IRecipeManager recipeManager;
	private final IFocusFactory focusFactory;
	private final TypedIngredientSerializer ingredientSerializer;
	private final IIngredientManager ingredientManager;
	private final @Nullable RecipeTransferService recipeTransferService;

	public RecipeBookmarkSerializer(
		IRecipeManager recipeManager,
		IFocusFactory focusFactory,
		TypedIngredientSerializer ingredientSerializer,
		IIngredientManager ingredientManager
	) {
		this(recipeManager, focusFactory, ingredientSerializer, ingredientManager, null);
	}

	public RecipeBookmarkSerializer(
		IRecipeManager recipeManager,
		IFocusFactory focusFactory,
		TypedIngredientSerializer ingredientSerializer,
		IIngredientManager ingredientManager,
		RecipeTransferService recipeTransferService
	) {
		this.recipeManager = recipeManager;
		this.focusFactory = focusFactory;
		this.ingredientSerializer = ingredientSerializer;
		this.ingredientManager = ingredientManager;
		this.recipeTransferService = recipeTransferService;
	}

	public String serialize(RecipeBookmark<?, ?> value) {
		IRecipeCategory<?> recipeCategory = value.getRecipeCategory();
		RecipeType<?> recipeType = recipeCategory.getRecipeType();
		ResourceLocation recipeTypeUid = recipeType.getUid();
		ResourceLocation recipeUid = value.getRecipeUid();
		IElement<?> element = value.getElement();
		ITypedIngredient<?> typedIngredient = element.getTypedIngredient();
		String outputSerialized = ingredientSerializer.serialize(typedIngredient);
		RecipeIngredientRole displayRole = value.getDisplayRole();
		if (displayRole != RecipeIngredientRole.OUTPUT) {
			return recipeTypeUid + SEPARATOR + recipeUid + SEPARATOR + outputSerialized + SEPARATOR + displayRole.name();
		}
		return recipeTypeUid + SEPARATOR + recipeUid + SEPARATOR + outputSerialized;
	}

	public IDeserializeResult<RecipeBookmark<?, ?>> deserialize(String string) {
		String[] parts = string.split(SEPARATOR);
		if ((parts.length != 3) && (parts.length != 4)) {
			String error = "string must be 3 or 4 parts";
			return IDeserializeResult.failure(error);
		}
		ResourceLocation recipeTypeUid;
		try {
			recipeTypeUid = new ResourceLocation(parts[0]);
		} catch (RuntimeException e) {
			String error = "recipe type uid must be a valid resource location: %s\n%s".formatted(string, e.getMessage());
			return IDeserializeResult.failure(error);
		}
		ResourceLocation recipeUid;
		try {
			recipeUid = new ResourceLocation(parts[1]);
		} catch (RuntimeException e) {
			String error = "recipe uid must be a valid resource location: %s\n%s".formatted(string, e.getMessage());
			return IDeserializeResult.failure(error);
		}
		IDeserializeResult<ITypedIngredient<?>> deserialized = ingredientSerializer.deserialize(parts[2]);
		Optional<ITypedIngredient<?>> outputResult = deserialized.getResult();
		if (outputResult.isEmpty()) {
			return IDeserializeResult.failure(deserialized.getDiagnostics());
		}
		Optional<RecipeType<?>> recipeTypeResult = recipeManager.getRecipeType(recipeTypeUid);
		if (recipeTypeResult.isEmpty()) {
			String error = "could not find a recipe type matching the given uid: %s".formatted(recipeTypeUid);
			return IDeserializeResult.failure(error);
		}
		RecipeIngredientRole displayRole = RecipeIngredientRole.OUTPUT;
		if (parts.length == 4) {
			String value = parts[3];
			if ("false".equals(value)) {
				displayRole = RecipeIngredientRole.INPUT;
			} else {
				try {
					displayRole = RecipeIngredientRole.valueOf(value);
				} catch (IllegalArgumentException ignored) {

				}
			}
		}

		ITypedIngredient<?> output = ingredientManager.normalizeTypedIngredient(outputResult.get());
		RecipeType<?> recipeType = recipeTypeResult.get();

		IRecipeCategory<?> recipeCategory = recipeManager.getRecipeCategory(recipeType);
		return createBookmark(string, recipeCategory, recipeUid, output, displayRole);
	}

	private <T> IDeserializeResult<RecipeBookmark<?, ?>> createBookmark(String string, IRecipeCategory<T> recipeCategory, ResourceLocation recipeUid, ITypedIngredient<?> output, RecipeIngredientRole displayRole) {
		if (recipeTransferService == null) {
			return IDeserializeResult.failure("recipe transfer service is required to deserialize recipe bookmarks");
		}
		IFocus<?> focus = focusFactory.createFocus(displayRole, output);

		Optional<T> recipeResult = findRecipe(recipeCategory, List.of(focus), recipeUid);
		if (recipeResult.isEmpty()) {
			String error = "could not find a recipe for this string: %s".formatted(string);
			return IDeserializeResult.failure(error);
		}

		T recipe = recipeResult.get();
		RecipeBookmark<T, ?> recipeBookmark = new RecipeBookmark<>(recipeCategory, recipe, recipeUid, output, displayRole, recipeTransferService);
		return IDeserializeResult.success(recipeBookmark);
	}

	private <T> Optional<T> findRecipe(IRecipeCategory<T> recipeCategory, List<IFocus<?>> focus, ResourceLocation recipeUid) {
		RecipeType<T> recipeType = recipeCategory.getRecipeType();
		return recipeManager.createRecipeLookup(recipeType)
			.limitFocus(focus)
			.get()
			.filter(r -> Objects.equals(recipeCategory.getRegistryName(r), recipeUid))
			.findFirst();
	}

}
