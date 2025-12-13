package mezz.jei.library.load.registration;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.library.ingredients.subtypes.SubtypeInterpreters;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SubtypeRegistration implements ISubtypeRegistration {
	private static final Logger LOGGER = LogManager.getLogger();

	private final SubtypeInterpreters interpreters = new SubtypeInterpreters();

	@Override
	public <B, I> void registerSubtypeInterpreter(IIngredientTypeWithSubtypes<B, I> type, B base, ISubtypeInterpreter<I> interpreter) {
		ErrorUtil.checkNotNull(type, "type");
		ErrorUtil.checkNotNull(base, "base");
		ErrorUtil.checkNotNull(interpreter, "interpreter");
		Class<? extends B> ingredientBaseClass = type.getIngredientBaseClass();
		if (!ingredientBaseClass.isInstance(base)) {
			throw new IllegalArgumentException(String.format("base (%s) must be an instance of %s", base.getClass(), ingredientBaseClass));
		}
		if (!this.interpreters.addInterpreter(type, base, interpreter)) {
			LOGGER.error("An interpreter is already registered for this: {}", base, new IllegalArgumentException());
		}
	}

	@Override
	public void registerFromDataComponentTypes(Item item, DataComponentType<?>... dataComponentTypes) {
		if (dataComponentTypes.length == 1) {
			registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, item, (i, c) -> i.get(dataComponentTypes[0]));
		} else if (dataComponentTypes.length > 1) {
			registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, item, (i, c) -> {
				List<@Nullable Object> list = new ArrayList<>(dataComponentTypes.length);
				for (DataComponentType<?> dataComponentType : dataComponentTypes) {
					Object o = i.get(dataComponentType);
					list.add(o);
				}
				return list;
			});
		}
	}

	public SubtypeInterpreters getInterpreters() {
		return interpreters;
	}
}
