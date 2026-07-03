package mezz.jei.gui.overlay;

import mezz.jei.api.gui.handlers.IGuiProperties;

@SuppressWarnings({"rawtypes", "unchecked"})
record TestGuiProperties(
	int guiLeft,
	int guiTop,
	int guiXSize,
	int guiYSize,
	int screenWidth,
	int screenHeight
) implements IGuiProperties {
	@Override
	public Class screenClass() {
		return Object.class;
	}
}
