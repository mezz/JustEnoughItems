package mezz.jei.common.deprecated.gui.ingredients.adapters;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;

import org.jetbrains.annotations.Nullable;
import java.util.List;

@SuppressWarnings({"removal", "DeprecatedIsStillUsed"})
@Deprecated
public class LegacyAdaptedIngredientRenderer<T> implements IIngredientRenderer<T> {
	public static <T> IIngredientRenderer<T> create(IIngredientRenderer<T> original, int width, int height, int xOffset, int yOffset) {
		if (xOffset == 0 && yOffset == 0 && original.getWidth() == width && original.getHeight() == height) {
			return original;
		}
		return new LegacyAdaptedIngredientRenderer<>(original, width, height, xOffset, yOffset);
	}

	private final IIngredientRenderer<T> original;
	private final int xOffset;
	private final int yOffset;
	private final int width;
	private final int height;

	private LegacyAdaptedIngredientRenderer(IIngredientRenderer<T> original, int width, int height, int xOffset, int yOffset) {
		Preconditions.checkArgument(width > 0, "width must be > 0");
		Preconditions.checkArgument(height > 0, "height must be > 0");
		this.original = original;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.width = width;
		this.height = height;
	}

	@Override
	public void render(PoseStack stack, T ingredient) {
		stack.pushPose();
		{
			stack.translate(xOffset, yOffset, 0);
			original.render(stack, ingredient);
		}
		stack.popPose();
	}

	@Override
	public void render(PoseStack stack, int xPosition, int yPosition, @Nullable T ingredient) {
		original.render(stack, xPosition + xOffset, yPosition + yOffset, ingredient);
	}

	@Override
	public List<Component> getTooltip(T ingredient, TooltipFlag tooltipFlag) {
		return original.getTooltip(ingredient, tooltipFlag);
	}

	@Override
	public Font getFontRenderer(Minecraft minecraft, T ingredient) {
		return original.getFontRenderer(minecraft, ingredient);
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}
}
