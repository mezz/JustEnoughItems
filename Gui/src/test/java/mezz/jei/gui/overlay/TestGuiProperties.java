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
	public int getGuiLeft() { return guiLeft; }

	@Override
	public int getGuiTop() { return guiTop; }

	@Override
	public int getGuiXSize() { return guiXSize; }

	@Override
	public int getGuiYSize() { return guiYSize; }

	@Override
	public int getScreenWidth() { return screenWidth; }

	@Override
	public int getScreenHeight() { return screenHeight; }

	@Override
	public Class getScreenClass() {
		return Object.class;
	}
}
