package mezz.jei.library.helpers;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mezz.jei.api.helpers.ICodecHelper;
import mezz.jei.api.ingredients.IIngredientSupplier;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.codecs.EnumCodec;
import mezz.jei.common.codecs.TupleCodec;
import mezz.jei.common.codecs.TypedIngredientCodecs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CodecHelper implements ICodecHelper {
	private static final Codec<RecipeHolder<?>> RECIPE_HOLDER_CODEC = Codec.lazyInitialized(() -> {
		Minecraft minecraft = Minecraft.getInstance();
		ClientLevel level = minecraft.level;
		assert level != null;
		RecipeManager recipeManager = level.getRecipeManager();

		return Codec.either(
			ResourceLocation.CODEC,
			TupleCodec.of(
				ResourceLocation.CODEC,
				Recipe.CODEC
			)
		)
		.flatXmap(
			either -> {
				return either.map(
					recipeHolderId -> {
						return recipeManager.byKey(recipeHolderId)
							.map(DataResult::success)
							.orElseGet(() -> DataResult.error(() -> "Could not find recipe for key: " + recipeHolderId));
					},
					pair -> {
						ResourceLocation recipeHolderId = pair.getFirst();
						Recipe<?> recipe = pair.getSecond();
						if (recipe == null) {
							return DataResult.error(() -> "Could not find recipe for key: " + recipeHolderId);
						}
						RecipeHolder<?> recipeHolder = new RecipeHolder<>(recipeHolderId, recipe);
						return DataResult.success(recipeHolder);
					}
				);
			},
			recipeHolder -> {
				ResourceLocation recipeHolderId = recipeHolder.id();
				Optional<RecipeHolder<?>> found = recipeManager.byKey(recipeHolderId);
				if (found.isPresent() && found.get().equals(recipeHolder)) {
					return DataResult.success(Either.left(recipeHolderId));
				}
				Recipe<?> recipe = recipeHolder.value();
				return DataResult.success(Either.right(Pair.of(recipeHolderId, recipe)));
			}
		);
	});

	private final IIngredientManager ingredientManager;
	private final IFocusFactory focusFactory;
	private final Map<RecipeType<?>, Codec<?>> defaultRecipeCodecs = new HashMap<>();
	private @Nullable Codec<RecipeType<?>> recipeTypeCodec;

	public CodecHelper(IIngredientManager ingredientManager, IFocusFactory focusFactory) {
		this.ingredientManager = ingredientManager;
		this.focusFactory = focusFactory;
	}

	@Override
	public Codec<IIngredientType<?>> getIngredientTypeCodec() {
		return TypedIngredientCodecs.getIngredientTypeCodec(ingredientManager);
	}

	@Override
	public MapCodec<ITypedIngredient<?>> getTypedIngredientCodec() {
		return TypedIngredientCodecs.getIngredientCodec(ingredientManager);
	}

	@Override
	public <T> Codec<ITypedIngredient<T>> getTypedIngredientCodec(IIngredientType<T> ingredientType) {
		return TypedIngredientCodecs.getIngredientCodec(ingredientType, ingredientManager);
	}

	@Override
	public <T extends RecipeHolder<?>> Codec<T> getRecipeHolderCodec() {
		@SuppressWarnings("unchecked")
		Codec<T> recipeHolderCodec = (Codec<T>) RECIPE_HOLDER_CODEC;
		return recipeHolderCodec;
	}

	@Override
	public <T> Codec<T> getSlowRecipeCategoryCodec(IRecipeCategory<T> recipeCategory, IRecipeManager recipeManager) {
		RecipeType<T> recipeType = recipeCategory.getRecipeType();
		@SuppressWarnings("unchecked")
		Codec<T> codec = (Codec<T>) defaultRecipeCodecs.get(recipeType);
		if (codec == null) {
			codec = createDefaultRecipeCategoryCodec(recipeManager, recipeCategory);
			defaultRecipeCodecs.put(recipeType, codec);
		}
		return codec;
	}

	private <T> Codec<T> createDefaultRecipeCategoryCodec(IRecipeManager recipeManager, IRecipeCategory<T> recipeCategory) {
		Codec<Data> dataCodec = RecordCodecBuilder.create((builder) -> {
			return builder.group(
				ResourceLocation.CODEC.fieldOf("registryName")
					.forGetter(Data::registryName),
				getTypedIngredientCodec().codec().fieldOf("ingredient")
					.forGetter(Data::ingredient),
				EnumCodec.create(RecipeIngredientRole.class).fieldOf("ingredient_role")
					.forGetter(Data::ingredientRole)
			).apply(builder, Data::new);
		});
		Codec<T> codec = dataCodec.flatXmap(
			data -> {
				ResourceLocation registryName = data.registryName();
				ITypedIngredient<?> ingredient = data.ingredient();
				IFocus<?> focus = focusFactory.createFocus(data.ingredientRole(), ingredient);

				RecipeType<T> recipeType = recipeCategory.getRecipeType();

				return recipeManager.createRecipeLookup(recipeType)
					.limitFocus(List.of(focus))
					.get()
					.filter(recipe -> registryName.equals(recipeCategory.getRegistryName(recipe)))
					.findFirst()
					.map(DataResult::success)
					.orElseGet(() -> DataResult.error(() -> "No recipe found for registry name: " + registryName));
			},
			recipe -> {
				ResourceLocation registryName = recipeCategory.getRegistryName(recipe);
				if (registryName == null) {
					return DataResult.error(() -> "No registry name for recipe");
				}
				IIngredientSupplier ingredients = recipeManager.getRecipeIngredients(recipeCategory, recipe);
				List<ITypedIngredient<?>> outputs = ingredients.getIngredients(RecipeIngredientRole.OUTPUT);
				if (!outputs.isEmpty()) {
					Data result = new Data(registryName, outputs.getFirst(), RecipeIngredientRole.OUTPUT);
					return DataResult.success(result);
				}
				List<ITypedIngredient<?>> inputs = ingredients.getIngredients(RecipeIngredientRole.INPUT);
				if (!inputs.isEmpty()) {
					Data result = new Data(registryName, inputs.getFirst(), RecipeIngredientRole.INPUT);
					return DataResult.success(result);
				}
				return DataResult.error(() -> "No inputs or outputs for recipe");
			}
		);

		return Codec.withAlternative(codec, createLegacyDefaultRecipeCategoryCodec(recipeManager, recipeCategory));
	}

	private record Data(ResourceLocation registryName, ITypedIngredient<?> ingredient, RecipeIngredientRole ingredientRole) {}

	private <T> Codec<T> createLegacyDefaultRecipeCategoryCodec(IRecipeManager recipeManager, IRecipeCategory<T> recipeCategory) {
		Codec<Pair<ResourceLocation, ITypedIngredient<?>>> legacyPairCodec = RecordCodecBuilder.create((builder) -> {
			return builder.group(
				ResourceLocation.CODEC.fieldOf("registryName")
					.forGetter(Pair::getFirst),
				getTypedIngredientCodec().codec().fieldOf("output")
					.forGetter(Pair::getSecond)
			).apply(builder, Pair::new);
		});

		Codec<Pair<ResourceLocation, ITypedIngredient<?>>> tupleCodec = TupleCodec.of(
			ResourceLocation.CODEC,
			getTypedIngredientCodec().codec()
		);

		return Codec.withAlternative(tupleCodec, legacyPairCodec)
		.flatXmap(
			pair -> {
				ResourceLocation registryName = pair.getFirst();
				ITypedIngredient<?> output = pair.getSecond();
				IFocus<?> focus = focusFactory.createFocus(RecipeIngredientRole.OUTPUT, output);

				RecipeType<T> recipeType = recipeCategory.getRecipeType();

				return recipeManager.createRecipeLookup(recipeType)
					.limitFocus(List.of(focus))
					.get()
					.filter(recipe -> registryName.equals(recipeCategory.getRegistryName(recipe)))
					.findFirst()
					.map(DataResult::success)
					.orElseGet(() -> DataResult.error(() -> "No recipe found for registry name: " + registryName));
			},
			recipe -> {
				ResourceLocation registryName = recipeCategory.getRegistryName(recipe);
				if (registryName == null) {
					return DataResult.error(() -> "No registry name for recipe");
				}
				IIngredientSupplier ingredients = recipeManager.getRecipeIngredients(recipeCategory, recipe);
				List<ITypedIngredient<?>> outputs = ingredients.getIngredients(RecipeIngredientRole.OUTPUT);
				if (outputs.isEmpty()) {
					return DataResult.error(() -> "No outputs for recipe");
				}
				Pair<ResourceLocation, ITypedIngredient<?>> result = new Pair<>(registryName, outputs.getFirst());
				return DataResult.success(result);
			}
		);
	}

	@Override
	public Codec<RecipeType<?>> getRecipeTypeCodec(IRecipeManager recipeManager) {
		if (recipeTypeCodec == null) {
			recipeTypeCodec = ResourceLocation.CODEC.flatXmap(
				resourceLocation -> {
					return recipeManager.getRecipeType(resourceLocation)
						.map(DataResult::success)
						.orElseGet(() -> DataResult.error(() -> "Failed to find recipe type " + resourceLocation));
				},
				recipeType -> {
					ResourceLocation uid = recipeType.getUid();
					return DataResult.success(uid);
				}
			);
		}
		return recipeTypeCodec;
	}
}
