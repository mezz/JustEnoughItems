package mezz.jei.test;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.library.ingredients.subtypes.SubtypeInterpreters;
import mezz.jei.library.ingredients.subtypes.SubtypeManager;
import mezz.jei.library.load.registration.IngredientManagerBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IngredientTypeRegistrationTest {
	private static final IIngredientType<String> STRING_TYPE = () -> String.class;
	private static final IIngredientType<Integer> INTEGER_TYPE = () -> Integer.class;

	@Test
	void rejectedDuplicateDoesNotReplaceTheRegisteringPlugin() {
		IngredientManagerBuilder builder = createBuilder();
		IModPlugin firstPlugin = plugin("first", registration -> registerStrings(registration, List.of()));
		builder.registerIngredients(firstPlugin);

		assertThrows(IllegalArgumentException.class, () -> builder.registerIngredients(
			plugin("duplicate", registration -> registerStrings(registration, List.of()))
		));

		assertEquals(Optional.of(firstPlugin.getPluginUid()), builder.build().getRegisteringPluginUid(STRING_TYPE));
	}

	@Test
	void failedPluginDoesNotLeakItsIdentityIntoLaterRegistrations() {
		IngredientManagerBuilder builder = createBuilder();
		IModPlugin failedPlugin = plugin("failed", registration -> {
			registerStrings(registration, List.of());
			throw new IllegalStateException("plugin failed after registration");
		});
		assertThrows(IllegalStateException.class, () -> builder.registerIngredients(failedPlugin));
		registerIntegers(builder);

		IIngredientManager manager = builder.build();
		assertEquals(Optional.of(failedPlugin.getPluginUid()), manager.getRegisteringPluginUid(STRING_TYPE));
		assertEquals(Optional.empty(), manager.getRegisteringPluginUid(INTEGER_TYPE));
	}

	private static IngredientManagerBuilder createBuilder() {
		return new IngredientManagerBuilder(new SubtypeManager(new SubtypeInterpreters()), DummyColorHelper.INSTANCE);
	}

	private static IModPlugin plugin(String modId, Consumer<IModIngredientRegistration> register) {
		return new IModPlugin() {
			@Override
			public ResourceLocation getPluginUid() {
				return new ResourceLocation(modId, "jei");
			}

			@Override
			public void registerIngredients(IModIngredientRegistration registration) {
				register.accept(registration);
			}
		};
	}

	private static void registerStrings(IModIngredientRegistration registration, List<String> ingredients) {
		registration.register(STRING_TYPE, ingredients, new TestHelper<>(STRING_TYPE), new TestRenderer<>());
	}

	private static void registerIntegers(IModIngredientRegistration registration) {
		registration.register(INTEGER_TYPE, List.of(), new TestHelper<>(INTEGER_TYPE), new TestRenderer<>());
	}

	private record TestHelper<T>(IIngredientType<T> type) implements IIngredientHelper<T> {
		@Override
		public IIngredientType<T> getIngredientType() {
			return type;
		}

		@Override
		public String getDisplayName(T ingredient) {
			return ingredient.toString();
		}

		@SuppressWarnings("removal")
		@Override
		public String getUniqueId(T ingredient, UidContext context) {
			return ingredient.toString();
		}

		@Override
		public ResourceLocation getResourceLocation(T ingredient) {
			return new ResourceLocation("ingredient_content_mod", ingredient.toString());
		}

		@Override
		public T copyIngredient(T ingredient) {
			return ingredient;
		}

		@Override
		public String getErrorInfo(@Nullable T ingredient) {
			return String.valueOf(ingredient);
		}
	}

	private static class TestRenderer<T> implements IIngredientRenderer<T> {
		@Override
		public void render(PoseStack poseStack, T ingredient) {
		}

		@SuppressWarnings("removal")
		@Override
		public List<Component> getTooltip(T ingredient, TooltipFlag tooltipFlag) {
			return List.of();
		}
	}
}
