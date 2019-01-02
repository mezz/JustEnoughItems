package mezz.jei.config;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Field;

import net.minecraftforge.fml.relauncher.FMLInjectionData;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/** Supplies Forge's launch directory for tests of its real configuration files. */
public abstract class ForgeConfigTestBase {
	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();
	@Nullable
	private File previousHome;

	@Before
	public void setForgeConfigurationHome() throws ReflectiveOperationException {
		Field home = FMLInjectionData.class.getDeclaredField("minecraftHome");
		home.setAccessible(true);
		previousHome = (File) home.get(null);
		home.set(null, temporaryFolder.getRoot());
	}

	@After
	public void restoreForgeConfigurationHome() throws ReflectiveOperationException {
		Field home = FMLInjectionData.class.getDeclaredField("minecraftHome");
		home.setAccessible(true);
		home.set(null, previousHome);
	}
}
