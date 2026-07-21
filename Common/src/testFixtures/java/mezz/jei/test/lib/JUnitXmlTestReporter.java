package mezz.jei.test.lib;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Writes one-test JUnit XML reports for game-test runners that do not expose JUnit results themselves.
 */
public final class JUnitXmlTestReporter {
	private JUnitXmlTestReporter() {

	}

	public static void runAndReport(
		String suiteName,
		String testName,
		ThrowingRunnable test
	) {
		long startNanos = System.nanoTime();
		Throwable failure = null;
		try {
			test.run();
		} catch (Throwable t) {
			failure = t;
		}
		double timeSeconds = (System.nanoTime() - startNanos) / 1_000_000_000.0D;

		try {
			Path outputDirectory = getOutputDirectory(suiteName);
			writeReport(outputDirectory, suiteName, testName, timeSeconds, failure);
		} catch (RuntimeException e) {
			if (failure != null) {
				e.addSuppressed(failure);
			}
			throw e;
		}

		if (failure != null) {
			rethrow(failure);
		}
	}

	public static void runAndReportWithBooleanVariant(
		String suiteName,
		String variantProperty,
		String variantSuffix,
		String testName,
		ThrowingRunnable test
	) {
		if (Boolean.getBoolean(variantProperty)) {
			suiteName += "-" + variantSuffix;
		}
		runAndReport(suiteName, testName, test);
	}

	private static Path getOutputDirectory(String suiteName) {
		Path moduleDirectory = getModuleDirectory();
		return moduleDirectory
			.resolve("build")
			.resolve("test-results")
			.resolve(safeFileName(suiteName));
	}

	private static Path getModuleDirectory() {
		Path directory = Path.of(System.getProperty("user.dir")).toAbsolutePath();
		while (directory != null) {
			if (
				Files.isRegularFile(directory.resolve("build.gradle.kts")) ||
					Files.isRegularFile(directory.resolve("build.gradle"))
			) {
				return directory;
			}
			directory = directory.getParent();
		}
		throw new IllegalStateException("Could not find a Gradle project directory from " + System.getProperty("user.dir"));
	}

	private static void writeReport(
		Path outputDirectory,
		String suiteName,
		String testName,
		double timeSeconds,
		Throwable failure
	) {
		try {
			Files.createDirectories(outputDirectory);

			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.newDocument();

			Element testSuite = document.createElement("testsuite");
			testSuite.setAttribute("name", suiteName);
			testSuite.setAttribute("tests", "1");
			testSuite.setAttribute("failures", failure == null ? "0" : "1");
			testSuite.setAttribute("errors", "0");
			testSuite.setAttribute("skipped", "0");
			testSuite.setAttribute("time", formatTime(timeSeconds));
			document.appendChild(testSuite);

			Element testCase = document.createElement("testcase");
			testCase.setAttribute("classname", suiteName);
			testCase.setAttribute("name", testName);
			testCase.setAttribute("time", formatTime(timeSeconds));
			testSuite.appendChild(testCase);

			if (failure != null) {
				Element failureElement = document.createElement("failure");
				failureElement.setAttribute("message", String.valueOf(failure.getMessage()));
				failureElement.setAttribute("type", failure.getClass().getName());
				failureElement.appendChild(document.createTextNode(stackTrace(failure)));
				testCase.appendChild(failureElement);
			}

			Path reportFile = outputDirectory.resolve("TEST-" + safeFileName(suiteName) + "." + safeFileName(testName) + ".xml");
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(new DOMSource(document), new StreamResult(reportFile.toFile()));
		} catch (IOException | ParserConfigurationException | TransformerException e) {
			throw new RuntimeException("Failed to write JUnit XML report for " + suiteName + "." + testName, e);
		}
	}

	private static String formatTime(double timeSeconds) {
		return String.format(Locale.ROOT, "%.3f", timeSeconds);
	}

	private static String stackTrace(Throwable throwable) {
		StringWriter writer = new StringWriter();
		throwable.printStackTrace(new PrintWriter(writer));
		return writer.toString();
	}

	private static String safeFileName(String value) {
		StringBuilder safeValue = new StringBuilder(value.length());
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (Character.isLetterOrDigit(c) || c == '-' || c == '_' || c == '.') {
				safeValue.append(c);
			} else {
				safeValue.append('_');
			}
		}
		return safeValue.toString();
	}

	private static void rethrow(Throwable throwable) {
		if (throwable instanceof RuntimeException runtimeException) {
			throw runtimeException;
		}
		if (throwable instanceof Error error) {
			throw error;
		}
		throw new RuntimeException(throwable);
	}

	@FunctionalInterface
	public interface ThrowingRunnable {
		void run() throws Throwable;
	}
}
