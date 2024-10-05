package mezz.jei.library.plugins.vanilla.ingredients.fluid;

import com.google.common.base.MoreObjects;
import mezz.jei.api.constants.Tags;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.subtypes.ISubtypeManager;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.common.platform.IPlatformFluidHelperInternal;
import mezz.jei.common.util.RegistryUtil;
import mezz.jei.common.util.TagUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class FluidIngredientHelper<T> implements IIngredientHelper<T> {
	private final ISubtypeManager subtypeManager;
	private final IColorHelper colorHelper;
	private final IPlatformFluidHelperInternal<T> platformFluidHelper;
	private final Registry<Fluid> registry;
	private final IIngredientTypeWithSubtypes<Fluid, T> fluidType;
	private final TagKey<Fluid> hiddenFromRecipeViewers;

	public FluidIngredientHelper(ISubtypeManager subtypeManager, IColorHelper colorHelper, IPlatformFluidHelperInternal<T> platformFluidHelper) {
		this.subtypeManager = subtypeManager;
		this.colorHelper = colorHelper;
		this.platformFluidHelper = platformFluidHelper;
		this.registry = RegistryUtil.getRegistry(Registries.FLUID);
		this.fluidType = platformFluidHelper.getFluidIngredientType();
		//noinspection deprecation
		this.hiddenFromRecipeViewers = new TagKey<>(Registries.FLUID, Tags.HIDDEN_FROM_RECIPE_VIEWERS);
	}

	@Override
	public IIngredientTypeWithSubtypes<Fluid, T> getIngredientType() {
		return fluidType;
	}

	@Override
	public String getDisplayName(T ingredient) {
		Component displayName = platformFluidHelper.getDisplayName(ingredient);
		return displayName.getString();
	}

	@SuppressWarnings("removal")
	@Override
	public String getUniqueId(T ingredient, UidContext context) {
		Fluid fluid = fluidType.getBase(ingredient);
		ResourceLocation registryName = getRegistryName(ingredient, fluid);

		StringBuilder result = new StringBuilder()
			.append("fluid:")
			.append(registryName);

		String subtypeInfo = subtypeManager.getSubtypeInfo(fluidType, ingredient, context);
		if (!subtypeInfo.isEmpty()) {
			result.append(":");
			result.append(subtypeInfo);
		}

		return result.toString();
	}

	@Override
	public Object getGroupingUid(T ingredient) {
		return fluidType.getBase(ingredient);
	}

	@SuppressWarnings("removal")
	@Override
	public String getWildcardId(T ingredient) {
		Fluid fluid = fluidType.getBase(ingredient);
		ResourceLocation registryName = getRegistryName(ingredient, fluid);
		return "fluid:" + registryName;
	}

	@Override
	public Object getUid(T ingredient, UidContext context) {
		Fluid fluid = fluidType.getBase(ingredient);
		Object subtypeData = subtypeManager.getSubtypeData(fluidType, ingredient, context);
		if (subtypeData != null) {
			return List.of(fluid, subtypeData);
		}
		return fluid;
	}

	@Override
	public long getAmount(T ingredient) {
		return platformFluidHelper.getAmount(ingredient);
	}

	@Override
	public T copyWithAmount(T ingredient, long amount) {
		return platformFluidHelper.copyWithAmount(ingredient, amount);
	}

	@Override
	public Iterable<Integer> getColors(T ingredient) {
		return platformFluidHelper.getStillFluidSprite(ingredient)
			.map(fluidStillSprite -> {
				int renderColor = platformFluidHelper.getColorTint(ingredient);
				return colorHelper.getColors(fluidStillSprite, renderColor, 1);
			})
			.orElseGet(List::of);
	}

	@Override
	public ResourceLocation getResourceLocation(T ingredient) {
		Fluid fluid = fluidType.getBase(ingredient);
		return getRegistryName(ingredient, fluid);
	}

	private ResourceLocation getRegistryName(T ingredient, Fluid fluid) {
		ResourceLocation key = registry.getKey(fluid);
		if (key == null) {
			String ingredientInfo = getErrorInfo(ingredient);
			throw new IllegalStateException("null registry name for: " + ingredientInfo);
		}
		return key;
	}

	@Override
	public ItemStack getCheatItemStack(T ingredient) {
		Fluid fluid = fluidType.getBase(ingredient);
		Item filledBucket = fluid.getBucket();
		return filledBucket.getDefaultInstance();
	}

	@Override
	public T copyIngredient(T ingredient) {
		return platformFluidHelper.copy(ingredient);
	}

	@Override
	public T normalizeIngredient(T ingredient) {
		return platformFluidHelper.normalize(ingredient);
	}

	@Override
	public Stream<ResourceLocation> getTagStream(T ingredient) {
		Fluid fluid = fluidType.getBase(ingredient);

		return registry.getResourceKey(fluid)
			.flatMap(registry::getHolder)
			.map(Holder::tags)
			.orElse(Stream.of())
			.map(TagKey::location);
	}

	@Override
	public boolean isHiddenFromRecipeViewersByTags(T ingredient) {
		Fluid fluid = fluidType.getBase(ingredient);
		return registry.getResourceKey(fluid)
			.flatMap(registry::getHolder)
			.map(holder -> holder.is(hiddenFromRecipeViewers))
			.orElse(false);
	}

	@SuppressWarnings("ConstantConditions")
	@Override
	public String getErrorInfo(@Nullable T ingredient) {
		if (ingredient == null) {
			return "null";
		}
		MoreObjects.ToStringHelper toStringHelper = MoreObjects.toStringHelper(ingredient.getClass());
		Fluid fluid = fluidType.getBase(ingredient);
		if (fluid != null) {
			Component displayName = platformFluidHelper.getDisplayName(ingredient);
			toStringHelper.add("Fluid", displayName.getString());
		} else {
			toStringHelper.add("Fluid", "null");
		}

		toStringHelper.add("Amount", platformFluidHelper.getAmount(ingredient));

		DataComponentPatch components = platformFluidHelper.getComponentsPatch(ingredient);
		if (!components.isEmpty()) {
			toStringHelper.add("Components", components.toString());
		}

		return toStringHelper.toString();
	}

	@Override
	public Optional<TagKey<?>> getTagKeyEquivalent(Collection<T> ingredients) {
		Registry<Fluid> fluidRegistry = RegistryUtil.getRegistry(Registries.FLUID);
		return TagUtil.getTagEquivalent(ingredients, fluidType::getBase, fluidRegistry::getTags);
	}

	@Override
	public boolean isIngredientOnServer(T ingredient) {
		Fluid fluid = fluidType.getBase(ingredient);
		Registry<Fluid> registry = RegistryUtil.getRegistry(Registries.FLUID);
		return registry.getKey(fluid) != null;
	}
}
