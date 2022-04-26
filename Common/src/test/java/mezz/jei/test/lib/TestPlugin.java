package mezz.jei.test.lib;

import java.util.ArrayList;
import java.util.Collection;

import net.minecraft.resources.ResourceLocation;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.registration.IModIngredientRegistration;

@JeiPlugin
public class TestPlugin implements IModPlugin {
	public static final int BASE_INGREDIENT_COUNT = 2;

	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(ModIds.JEI_ID, "test");
	}

	@Override
	public void registerIngredients(IModIngredientRegistration registration) {
		Collection<TestIngredient> baseTestIngredients = new ArrayList<>();
		for (int i = 0; i < BASE_INGREDIENT_COUNT; i++) {
			baseTestIngredients.add(new TestIngredient(i));
		}

		registration.register(TestIngredient.TYPE, baseTestIngredients, new TestIngredientHelper(), new TestIngredientRenderer());
	}

}
