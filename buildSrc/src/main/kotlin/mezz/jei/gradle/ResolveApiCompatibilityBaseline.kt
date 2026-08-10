package mezz.jei.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.security.MessageDigest
import java.time.Duration

@UntrackedTask(because = "Always checks Maven metadata for the latest published API baseline.")
abstract class ResolveApiCompatibilityBaseline : DefaultTask() {
	@get:Input
	abstract val repositoryUrl: Property<String>

	@get:Input
	abstract val groupId: Property<String>

	@get:Input
	abstract val artifactId: Property<String>

	@get:Input
	abstract val versionSelector: Property<String>

	@get:Input
	abstract val offline: Property<Boolean>

	@get:OutputFile
	abstract val baselineJar: RegularFileProperty

	@get:OutputFile
	abstract val resolvedVersionFile: RegularFileProperty

	@TaskAction
	fun resolveBaseline() {
		val artifactId = artifactId.get()
		val versionSelector = versionSelector.get()
		val baselineJar = baselineJar.get().asFile
		val resolvedVersionFile = resolvedVersionFile.get().asFile
		val previousVersion = resolvedVersionFile.takeIf(File::isFile)?.readText()?.trim()
		if (offline.get()) {
			if (previousVersion != null && baselineJar.isFile && matchesVersionSelector(previousVersion, versionSelector)) {
				logger.lifecycle("Using offline API compatibility baseline {}:{}:{}", groupId.get(), artifactId, previousVersion)
				return
			}
			throw GradleException("No cached API compatibility baseline for $artifactId matches $versionSelector")
		}

		val client = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(30))
			.followRedirects(HttpClient.Redirect.NORMAL)
			.build()
		val repositoryUrl = repositoryUrl.get().trimEnd('/')
		val groupPath = groupId.get().replace('.', '/')
		val artifactBaseUrl = "$repositoryUrl/$groupPath/$artifactId"
		val resolvedVersion = if (versionSelector.startsWith('[') || versionSelector.startsWith('(')) {
			val metadata = download(client, URI.create("$artifactBaseUrl/maven-metadata.xml")).toString(Charsets.UTF_8)
			selectLatestVersion(metadata, versionSelector)
		} else {
			versionSelector
		}

		if (previousVersion == resolvedVersion && baselineJar.isFile) {
			logger.info("Using cached API compatibility baseline {}:{}:{}", groupId.get(), artifactId, resolvedVersion)
			return
		}

		val artifactFileName = "$artifactId-$resolvedVersion.jar"
		val artifactUrl = "$artifactBaseUrl/$resolvedVersion/$artifactFileName"
		val expectedChecksum = download(client, URI.create("$artifactUrl.sha256"))
			.toString(Charsets.UTF_8)
			.trim()
			.substringBefore(' ')
		val artifact = download(client, URI.create(artifactUrl))
		val actualChecksum = MessageDigest.getInstance("SHA-256")
			.digest(artifact)
			.joinToString("") { "%02x".format(it.toInt() and 0xff) }
		if (!actualChecksum.equals(expectedChecksum, ignoreCase = true)) {
			throw GradleException(
				"Checksum verification failed for $artifactUrl: expected $expectedChecksum but got $actualChecksum"
			)
		}

		baselineJar.parentFile.mkdirs()
		baselineJar.writeBytes(artifact)
		resolvedVersionFile.writeText("$resolvedVersion\n")
		logger.lifecycle("Resolved API compatibility baseline {}:{}:{}", groupId.get(), artifactId, resolvedVersion)
	}

	private fun download(client: HttpClient, uri: URI): ByteArray {
		val request = HttpRequest.newBuilder(uri)
			.timeout(Duration.ofSeconds(60))
			.header("Cache-Control", "no-cache")
			.GET()
			.build()
		val response = try {
			client.send(request, HttpResponse.BodyHandlers.ofByteArray())
		} catch (e: InterruptedException) {
			Thread.currentThread().interrupt()
			throw GradleException("Interrupted while downloading $uri", e)
		} catch (e: Exception) {
			throw GradleException("Failed to download $uri", e)
		}
		if (response.statusCode() !in 200..299) {
			throw GradleException("Failed to download $uri: HTTP ${response.statusCode()}")
		}
		return response.body()
	}

	private fun selectLatestVersion(metadata: String, selector: String): String {
		if (selector.length < 3 || selector.last() !in listOf(']', ')')) {
			throw GradleException("Unsupported API compatibility baseline version range: $selector")
		}
		val bounds = selector.substring(1, selector.lastIndex).split(',', limit = 2)
		if (bounds.size != 2) {
			throw GradleException("Unsupported API compatibility baseline version range: $selector")
		}

		val lowerBound = bounds[0].takeIf(String::isNotBlank)?.let(NumericVersion::parse)
		val upperBound = bounds[1].takeIf(String::isNotBlank)?.let(NumericVersion::parse)
		val lowerInclusive = selector.first() == '['
		val upperInclusive = selector.last() == ']'
		return VERSION_PATTERN.findAll(metadata)
			.map { it.groupValues[1] }
			.mapNotNull { version -> NumericVersion.parseOrNull(version)?.let { version to it } }
			.filter { (_, version) ->
				(lowerBound == null || version > lowerBound || lowerInclusive && version == lowerBound) &&
					(upperBound == null || version < upperBound || upperInclusive && version == upperBound)
			}
			.maxByOrNull { it.second }
			?.first
			?: throw GradleException("No published API compatibility baseline matches $selector")
	}

	private fun matchesVersionSelector(version: String, selector: String): Boolean =
		if (selector.startsWith('[') || selector.startsWith('(')) {
			runCatching { selectLatestVersion("<version>$version</version>", selector) }
				.getOrNull() == version
		} else {
			version == selector
		}

	private data class NumericVersion(val components: List<Int>) : Comparable<NumericVersion> {
		override fun compareTo(other: NumericVersion): Int {
			val componentCount = maxOf(components.size, other.components.size)
			for (index in 0 until componentCount) {
				val comparison = components.getOrElse(index) { 0 }.compareTo(other.components.getOrElse(index) { 0 })
				if (comparison != 0) {
					return comparison
				}
			}
			return 0
		}

		companion object {
			fun parse(version: String): NumericVersion =
				parseOrNull(version) ?: throw GradleException("Unsupported non-numeric JEI API version: $version")

			fun parseOrNull(version: String): NumericVersion? {
				val components = version.split('.').map { it.toIntOrNull() ?: return null }
				return NumericVersion(components)
			}
		}
	}

	companion object {
		private val VERSION_PATTERN = Regex("""<version>\s*([^<\s]+)\s*</version>""")
	}
}
