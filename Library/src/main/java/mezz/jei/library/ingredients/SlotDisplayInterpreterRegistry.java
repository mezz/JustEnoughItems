package mezz.jei.library.ingredients;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ISlotDisplayInterpretationBuilder;
import mezz.jei.api.ingredients.ISlotDisplayInterpreter;
import mezz.jei.api.ingredients.IUniversalSlotDisplayInterpreter;
import mezz.jei.library.load.registration.SlotDisplayInterpreterRegistration.RegisteredSlotDisplayInterpreter;
import mezz.jei.library.load.registration.SlotDisplayInterpreterRegistration.RegisteredUniversalSlotDisplayInterpreter;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class SlotDisplayInterpreterRegistry {
	private final Map<SlotDisplay.Type<?>, IUniversalSlotDisplayInterpreter<?>> universalInterpreters = new HashMap<>();
	private final Map<Key, ISlotDisplayInterpreter<?, ?>> interpreters = new HashMap<>();

	public SlotDisplayInterpreterRegistry(
		Collection<RegisteredUniversalSlotDisplayInterpreter<?>> registeredUniversalInterpreters,
		Collection<RegisteredSlotDisplayInterpreter<?, ?>> registeredInterpreters
	) {
		registeredUniversalInterpreters.forEach(this::addUniversal);
		registeredInterpreters.forEach(this::add);
	}

	private <D extends SlotDisplay> void addUniversal(RegisteredUniversalSlotDisplayInterpreter<D> registered) {
		universalInterpreters.put(registered.slotDisplayType(), registered.interpreter());
	}

	private <D extends SlotDisplay, T> void add(RegisteredSlotDisplayInterpreter<D, T> registered) {
		Key key = new Key(registered.slotDisplayType(), registered.ingredientType());
		interpreters.put(key, registered.interpreter());
	}

	<T> void interpret(
		IIngredientType<T> ingredientType,
		SlotDisplay slotDisplay,
		ISlotDisplayInterpreter.IContext<T> context,
		ISlotDisplayInterpretationBuilder interpretationBuilder
	) {
		IUniversalSlotDisplayInterpreter<?> universalInterpreter = universalInterpreters.get(slotDisplay.type());
		if (universalInterpreter != null) {
			interpretUniversalUnchecked(universalInterpreter, slotDisplay, interpretationBuilder);
		}

		Key key = new Key(slotDisplay.type(), ingredientType);
		ISlotDisplayInterpreter<?, ?> interpreter = interpreters.get(key);
		if (interpreter != null) {
			interpretUnchecked(interpreter, slotDisplay, context, interpretationBuilder);
		}
	}

	@SuppressWarnings("unchecked")
	private static void interpretUniversalUnchecked(
		IUniversalSlotDisplayInterpreter<?> interpreter,
		SlotDisplay slotDisplay,
		ISlotDisplayInterpretationBuilder interpretationBuilder
	) {
		IUniversalSlotDisplayInterpreter<SlotDisplay> castInterpreter = (IUniversalSlotDisplayInterpreter<SlotDisplay>) interpreter;
		castInterpreter.interpret(slotDisplay, interpretationBuilder);
	}

	@SuppressWarnings("unchecked")
	private static <T> void interpretUnchecked(
		ISlotDisplayInterpreter<?, ?> interpreter,
		SlotDisplay slotDisplay,
		ISlotDisplayInterpreter.IContext<T> context,
		ISlotDisplayInterpretationBuilder interpretationBuilder
	) {
		ISlotDisplayInterpreter<SlotDisplay, T> castInterpreter = (ISlotDisplayInterpreter<SlotDisplay, T>) interpreter;
		castInterpreter.interpret(slotDisplay, context, interpretationBuilder);
	}

	private record Key(SlotDisplay.Type<?> slotDisplayType, IIngredientType<?> ingredientType) {
	}
}
