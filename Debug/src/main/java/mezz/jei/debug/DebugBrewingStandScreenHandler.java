package mezz.jei.debug;

import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.runtime.IScreenHelper;
import net.minecraft.client.gui.screens.inventory.BrewingStandScreen;
import net.minecraft.client.renderer.Rect2i;

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
}
