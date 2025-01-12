package mezz.jei.test.lib;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.registration.IModIngredientRegistration;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@JeiPlugin
public class TestPlugin implements IModPlugin {
	public static final int BASE_INGREDIENT_COUNT = 2;

	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(ModIds.JEI_ID, "test");
	}

	@Override
	public CompletableFuture<Void> registerIngredients(IModIngredientRegistration registration, Executor executor) {
		Collection<TestIngredient> baseTestIngredients = new ArrayList<>();
		for (int i = 0; i < BASE_INGREDIENT_COUNT; i++) {
			baseTestIngredients.add(new TestIngredient(i));
		}

		registration.register(TestIngredient.TYPE, baseTestIngredients, new TestIngredientHelper(), new TestIngredientRenderer());
		return CompletableFuture.completedFuture(null);
	}

}
