package mezz.jei.common.platform;

import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public interface IPlatformFluidHelperInternal<T> extends IPlatformFluidHelper<T> {

	IIngredientRenderer<T> createRenderer(long capacity, boolean showCapacity, int width, int height);

	Optional<TextureAtlasSprite> getStillFluidSprite(T ingredient);

	Component getDisplayName(T ingredient);

	int getColorTint(T ingredient);

	long getAmount(T ingredient);

	@Nullable
	T merge(T first, T second);

	DataComponentPatch getComponentsPatch(T ingredient);

	List<Component> getTooltip(T ingredient, TooltipFlag tooltipFlag);

	T copy(T ingredient);

	T normalize(T ingredient);

	Optional<T> getContainedFluid(ITypedIngredient<?> ingredient);
}
