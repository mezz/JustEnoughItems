package mezz.jei.library.plugins.debug;

import mezz.jei.api.gui.builder.IClickableIngredientFactory;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.common.platform.IPlatformScreenHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.MathUtil;
import net.minecraft.client.gui.screens.inventory.BrewingStandScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Optional;

public class DebugBrewingStandScreenHandler implements IGuiContainerHandler<BrewingStandScreen> {
	@Override
	public List<Rect2i> getGuiExtraAreas(BrewingStandScreen containerScreen) {
		int widthMovement = (int) ((System.currentTimeMillis() / 100) % 100);
		int size = 25 + widthMovement;
		IPlatformScreenHelper screenHelper = Services.PLATFORM.getScreenHelper();
		int guiLeft = screenHelper.getLeftPos(containerScreen);
		int xSize = screenHelper.getImageWidth(containerScreen);
		int guiTop = screenHelper.getTopPos(containerScreen);
		return List.of(
			new Rect2i(guiLeft + xSize, guiTop + 40, size, size)
		);
	}

	@Override
	public Optional<? extends IClickableIngredient<?>> getClickableIngredientUnderMouse(IClickableIngredientFactory factory, BrewingStandScreen containerScreen, double mouseX, double mouseY) {
		Rect2i area = new Rect2i(0, 0, 10, 10);
		if (MathUtil.contains(area, mouseX, mouseY)) {
			return factory.createBuilder(new ItemStack(Items.BOW))
				.buildWithArea(area);
		}
		return Optional.empty();
	}
}
