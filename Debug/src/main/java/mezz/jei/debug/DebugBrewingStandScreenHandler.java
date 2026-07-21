package mezz.jei.debug;

import mezz.jei.api.gui.builder.IClickableIngredientFactory;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IScreenHelper;
import net.minecraft.client.gui.screens.inventory.BrewingStandScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class DebugBrewingStandScreenHandler implements IGuiContainerHandler<BrewingStandScreen> {
	private final Supplier<Optional<IScreenHelper>> screenHelperSupplier;

	public DebugBrewingStandScreenHandler(Supplier<Optional<IScreenHelper>> screenHelperSupplier) {
		this.screenHelperSupplier = screenHelperSupplier;
	}

	@Override
	public List<Rect2i> getGuiExtraAreas(BrewingStandScreen containerScreen) {
		int widthMovement = (int) ((System.currentTimeMillis() / 100) % 100);
		int size = 25 + widthMovement;
		return this.screenHelperSupplier.get()
			.flatMap(screenHelper -> screenHelper.getGuiProperties(containerScreen))
			.map(guiProperties -> List.of(
				new Rect2i(guiProperties.guiRight(), guiProperties.guiTop() + 40, size, size)
			))
			.orElseGet(List::of);
	}

	@Override
	public Optional<? extends IClickableIngredient<?>> getClickableIngredientUnderMouse(IClickableIngredientFactory factory, BrewingStandScreen containerScreen, double mouseX, double mouseY) {
		Rect2i area = new Rect2i(0, 0, 10, 10);
		if (contains(area, mouseX, mouseY)) {
			return factory.createBuilder(new ItemStack(Items.BOW))
				.buildWithArea(area);
		}
		return Optional.empty();
	}

	private static boolean contains(Rect2i rect, double x, double y) {
		return x >= rect.getX() &&
			y >= rect.getY() &&
			x < rect.getX() + rect.getWidth() &&
			y < rect.getY() + rect.getHeight();
	}
}
