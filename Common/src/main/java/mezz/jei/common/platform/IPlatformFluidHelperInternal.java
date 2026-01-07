package mezz.jei.common.platform;

import com.mojang.serialization.Codec;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Optional;

public interface IPlatformFluidHelperInternal<T> extends IPlatformFluidHelper<T> {

	IIngredientRenderer<T> createRenderer(long capacity, boolean showCapacity, int width, int height);

	Optional<TextureAtlasSprite> getStillFluidSprite(T ingredient);

	Component getDisplayName(T ingredient);

	int getColorTint(T ingredient);

	long getAmount(T ingredient);

	DataComponentPatch getComponentsPatch(T ingredient);

	@Unmodifiable
	List<Component> getTooltip(T ingredient, @Nullable Player player, Item.TooltipContext tooltipContext, TooltipFlag tooltipFlag);

	T copy(T ingredient);

	T copyWithAmount(T ingredient, long amount);

	T normalize(T ingredient);

	Optional<T> getContainedFluid(ITypedIngredient<?> ingredient);

	Codec<T> getCodec();

	Optional<DisplayContentsFactory<T>> getDisplayContentsFactoryForStacks();
}
