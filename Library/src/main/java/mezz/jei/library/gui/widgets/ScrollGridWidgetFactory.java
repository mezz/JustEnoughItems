package mezz.jei.library.gui.widgets;

import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IScrollGridWidgetFactory;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.common.util.ImmutableSize2i;
import net.minecraft.client.gui.navigation.ScreenRectangle;

import java.util.List;

public class ScrollGridWidgetFactory<R> implements IScrollGridWidgetFactory<R> {
	private final int columns;
	private final int visibleRows;
	private final IGuiHelper guiHelper;
	private ScreenRectangle area;

	public ScrollGridWidgetFactory(IGuiHelper guiHelper, int columns, int visibleRows) {
		this.guiHelper = guiHelper;
		IDrawableStatic slotBackground = guiHelper.getSlotDrawable();

		this.columns = columns;
		this.visibleRows = visibleRows;
		ImmutableSize2i size = ScrollGridRecipeWidget.calculateSize(slotBackground, columns, visibleRows);
		this.area = new ScreenRectangle(0, 0, size.width(), size.height());
	}

	@Override
	public void setPosition(int x, int y) {
		area = new ScreenRectangle(x, y, area.width(), area.height());
	}

	@Override
	public ScreenRectangle getArea() {
		return area;
	}

	@Override
	public void createWidgetForSlots(IRecipeExtrasBuilder builder, R recipe, List<IRecipeSlotDrawable> slots) {
		ScrollGridRecipeWidget widget = new ScrollGridRecipeWidget(guiHelper, area, columns, visibleRows, slots);
		builder.addWidget(widget);
		builder.addInputHandler(widget);
	}
}
