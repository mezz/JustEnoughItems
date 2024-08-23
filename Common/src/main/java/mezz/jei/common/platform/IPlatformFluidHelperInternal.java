package mezz.jei.common.platform;

import com.mojang.serialization.Codec;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;

import java.util.Optional;

public interface IPlatformFluidHelperInternal<T> extends IPlatformFluidHelper<T> {

	IIngredientRenderer<T> createRenderer(long capacity, boolean showCapacity, int width, int height);

	IIngredientRenderer<T> createSlotRenderer(long capacity);

	Optional<TextureAtlasSprite> getStillFluidSprite(T ingredient);

	Component getDisplayName(T ingredient);

	int getColorTint(T ingredient);

	long getAmount(T ingredient);

	DataComponentPatch getComponentsPatch(T ingredient);

	void getTooltip(ITooltipBuilder tooltip, T ingredient, TooltipFlag tooltipFlag);

	T copy(T ingredient);

	T copyWithAmount(T ingredient, long amount);

	T normalize(T ingredient);

	Optional<T> getContainedFluid(ITypedIngredient<?> ingredient);

	Codec<T> getCodec();
}
