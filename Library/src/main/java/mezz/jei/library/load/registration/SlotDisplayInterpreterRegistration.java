package mezz.jei.library.load.registration;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ISlotDisplayInterpreter;
import mezz.jei.api.ingredients.IUniversalSlotDisplayInterpreter;
import mezz.jei.api.registration.ISlotDisplayInterpreterRegistration;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.library.ingredients.SlotDisplayInterpreterRegistry;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class SlotDisplayInterpreterRegistration implements ISlotDisplayInterpreterRegistration {
	private static final Logger LOGGER = LogManager.getLogger();

	private final List<RegisteredUniversalSlotDisplayInterpreter<?>> universalInterpreters = new ArrayList<>();
	private final List<RegisteredSlotDisplayInterpreter<?, ?>> interpreters = new ArrayList<>();

	@Override
	public <D extends SlotDisplay> void registerUniversal(
		SlotDisplay.Type<D> slotDisplayType,
		IUniversalSlotDisplayInterpreter<D> interpreter
	) {
		ErrorUtil.checkNotNull(slotDisplayType, "slotDisplayType");
		ErrorUtil.checkNotNull(interpreter, "interpreter");

		boolean duplicate = universalInterpreters.stream()
			.anyMatch(registered -> registered.slotDisplayType().equals(slotDisplayType));
		if (duplicate) {
			LOGGER.error(
				"A universal slot display interpreter is already registered for {}",
				slotDisplayType,
				new IllegalArgumentException()
			);
			return;
		}

		universalInterpreters.add(new RegisteredUniversalSlotDisplayInterpreter<>(slotDisplayType, interpreter));
	}

	@Override
	public <D extends SlotDisplay, T> void register(
		SlotDisplay.Type<D> slotDisplayType,
		IIngredientType<T> ingredientType,
		ISlotDisplayInterpreter<D, T> interpreter
	) {
		ErrorUtil.checkNotNull(slotDisplayType, "slotDisplayType");
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotNull(interpreter, "interpreter");

		boolean duplicate = interpreters.stream()
			.anyMatch(registered -> registered.slotDisplayType().equals(slotDisplayType) &&
				registered.ingredientType().equals(ingredientType)
			);
		if (duplicate) {
			LOGGER.error(
				"A slot display interpreter is already registered for {} and {}",
				slotDisplayType,
				ingredientType.getUid(),
				new IllegalArgumentException()
			);
			return;
		}

		interpreters.add(new RegisteredSlotDisplayInterpreter<>(slotDisplayType, ingredientType, interpreter));
	}

	public SlotDisplayInterpreterRegistry createRegistry() {
		return new SlotDisplayInterpreterRegistry(universalInterpreters, interpreters);
	}

	public record RegisteredUniversalSlotDisplayInterpreter<D extends SlotDisplay>(
		SlotDisplay.Type<D> slotDisplayType,
		IUniversalSlotDisplayInterpreter<D> interpreter
	) {
	}

	public record RegisteredSlotDisplayInterpreter<D extends SlotDisplay, T>(
		SlotDisplay.Type<D> slotDisplayType,
		IIngredientType<T> ingredientType,
		ISlotDisplayInterpreter<D, T> interpreter
	) {
	}
}
