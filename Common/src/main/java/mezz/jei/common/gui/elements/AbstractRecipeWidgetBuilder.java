package mezz.jei.common.gui.elements;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.gui.widgets.IRecipeWidgetBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidgetTooltipCallback;
import mezz.jei.common.util.PlaceableUtil;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public abstract class AbstractRecipeWidgetBuilder<THIS extends IRecipeWidgetBuilder<THIS>> implements IRecipeWidgetBuilder<THIS>, IRecipeWidget {
	private ScreenPosition position;
	private @Nullable IRecipeWidgetTooltipCallback tooltipCallback;

	protected AbstractRecipeWidgetBuilder(int xPos, int yPos) {
		this.position = new ScreenPosition(xPos, yPos);
	}

	protected abstract THIS getThis();

	@Override
	public ScreenPosition getPosition() {
		return position;
	}

	@Override
	public ScreenRectangle getScreenRectangle() {
		return new ScreenRectangle(position, getWidth(), getHeight());
	}

	@Override
	public THIS setPosition(int xPos, int yPos) {
		this.position = new ScreenPosition(xPos, yPos);
		return getThis();
	}

	@Override
	public THIS setPosition(
		int areaX,
		int areaY,
		int areaWidth,
		int areaHeight,
		HorizontalAlignment horizontalAlignment,
		VerticalAlignment verticalAlignment
	) {
		return PlaceableUtil.setPosition(
			getThis(),
			areaX,
			areaY,
			areaWidth,
			areaHeight,
			horizontalAlignment,
			verticalAlignment
		);
	}

	@Override
	public THIS setTooltip(FormattedText tooltip) {
		return setTooltip(builder -> builder.add(tooltip));
	}

	@Override
	public THIS setTooltip(Collection<? extends FormattedText> tooltip) {
		List<? extends FormattedText> tooltipCopy = List.copyOf(tooltip);
		return setTooltip(builder -> builder.addAll(tooltipCopy));
	}

	@Override
	public THIS setTooltip(TooltipComponent tooltip) {
		return setTooltip(builder -> builder.add(tooltip));
	}

	@Override
	public THIS setTooltip(IRecipeWidgetTooltipCallback tooltipCallback) {
		this.tooltipCallback = tooltipCallback;
		return getThis();
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltip, double mouseX, double mouseY) {
		if (isMouseOver(mouseX, mouseY)) {
			addConfiguredTooltip(tooltip);
		}
	}

	protected final boolean isMouseOver(double mouseX, double mouseY) {
		return mouseX >= 0 &&
			mouseY >= 0 &&
			mouseX < getWidth() &&
			mouseY < getHeight();
	}

	protected final boolean hasConfiguredTooltip() {
		return tooltipCallback != null;
	}

	protected final void addConfiguredTooltip(ITooltipBuilder tooltip) {
		IRecipeWidgetTooltipCallback tooltipCallback = this.tooltipCallback;
		if (tooltipCallback != null) {
			tooltipCallback.onTooltip(tooltip);
		}
	}
}
