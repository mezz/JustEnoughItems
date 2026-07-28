def getGradleProperty(String propertyName) {
    def propertyPrefix = "${propertyName}="
    for (def line in readFile(file: 'gradle.properties').readLines()) {
        def trimmedLine = line.trim()
        if (trimmedLine.startsWith(propertyPrefix)) {
            return trimmedLine.substring(propertyPrefix.length()).trim()
        }
    }
    return ''
}

def getJsonValue(String jsonText, String propertyName) {
    def key = "\"${propertyName}\""
    def keyIndex = jsonText.indexOf(key)
    if (keyIndex < 0) {
        return ''
    }

    def colonIndex = jsonText.indexOf(':', keyIndex + key.length())
    if (colonIndex < 0) {
        return ''
    }

    def valueStart = colonIndex + 1
    while (valueStart < jsonText.length()) {
        def character = jsonText.substring(valueStart, valueStart + 1)
        if (character != ' ' && character != '\n' && character != '\r' && character != '\t') {
            break
        }
        valueStart++
    }

    if (valueStart >= jsonText.length()) {
        return ''
    }

    if (jsonText.substring(valueStart, valueStart + 1) == '"') {
        valueStart++
        def valueEnd = jsonText.indexOf('"', valueStart)
        if (valueEnd < 0) {
            return ''
        }
        return jsonText.substring(valueStart, valueEnd)
    }

    def commaIndex = jsonText.indexOf(',', valueStart)
    def braceIndex = jsonText.indexOf('}', valueStart)
    def valueEnd = commaIndex
    if (valueEnd < 0 || (braceIndex >= 0 && braceIndex < valueEnd)) {
        valueEnd = braceIndex
    }
    if (valueEnd < 0) {
        valueEnd = jsonText.length()
    }

    return jsonText.substring(valueStart, valueEnd).trim()
}

def getLastPathSegment(String url) {
    def valueEnd = url.length()
    while (valueEnd > 0 && url.substring(valueEnd - 1, valueEnd) == '/') {
        valueEnd--
    }

    def slashIndex = url.lastIndexOf('/', valueEnd - 1)
    if (slashIndex < 0) {
        return url.substring(0, valueEnd)
    }
    return url.substring(slashIndex + 1, valueEnd)
}

def removeTrailingSlashes(String url) {
    def valueEnd = url.length()
    while (valueEnd > 0 && url.substring(valueEnd - 1, valueEnd) == '/') {
        valueEnd--
    }
    return url.substring(0, valueEnd)
}

def getBuildVersion() {
    def specificationVersion = getGradleProperty('specificationVersion')
    if (specificationVersion) {
        return "${specificationVersion}.${env.BUILD_NUMBER}"
    }
    return ''
}

def getArtifactLinks(boolean includeFallback) {
    def resultFiles = [
        'Forge/build/publishMods/publishCurseforge.json',
        'Forge/build/publishMods/publishModrinth.json',
        'Fabric/build/publishMods/publishCurseforge.json',
        'Fabric/build/publishMods/publishModrinth.json',
        'NeoForge/build/publishMods/publishCurseforge.json',
        'NeoForge/build/publishMods/publishModrinth.json'
    ]

    def links = []
    def curseHomepageUrl = getGradleProperty('curseHomepageUrl')
    def curseProjectSlug = curseHomepageUrl ? getLastPathSegment(curseHomepageUrl) : ''

    for (def resultFile in resultFiles) {
        if (!fileExists(resultFile)) {
            continue
        }

        def publishResult = readFile(file: resultFile)
        def moduleName = resultFile.substring(0, resultFile.indexOf('/'))
        def publishType = getJsonValue(publishResult, 'type')
        if (publishType == 'curseforge') {
            def fileId = getJsonValue(publishResult, 'fileId')
            def projectSlug = getJsonValue(publishResult, 'projectSlug') ?: curseProjectSlug
            if (fileId && projectSlug && projectSlug != 'dry-run') {
                links.add("[${moduleName} CurseForge](https://www.curseforge.com/minecraft/mc-mods/${projectSlug}/files/${fileId})")
            }
        } else if (publishType == 'modrinth') {
            def projectId = getJsonValue(publishResult, 'projectId')
            def versionId = getJsonValue(publishResult, 'id')
            if (projectId && projectId != 'dry-run' && versionId) {
                links.add("[${moduleName} Modrinth](https://modrinth.com/mod/${projectId}/version/${versionId})")
            }
        }
    }

    if (links || !includeFallback) {
        return links
    }

    // Fallback for branches or builds that do not generate mod-publish-plugin result files.
    if (curseHomepageUrl) {
        links.add("[CurseForge](${removeTrailingSlashes(curseHomepageUrl)}/files)")
    }

    def modrinthId = getGradleProperty('modrinthId')
    if (modrinthId) {
        links.add("[Modrinth](https://modrinth.com/mod/${modrinthId}/versions)")
    }
    return links
}

def notifyDiscordBuild(String result) {
    def buildResult = result ?: 'SUCCESS'
    def branchName = env.BRANCH_NAME ?: env.GIT_BRANCH ?: 'unknown'
    def descriptionLines = [
        "**Result:** ${buildResult}",
        "**Branch:** ${branchName}",
        "**Build:** #${env.BUILD_NUMBER}"
    ]

    def buildVersion = getBuildVersion()
    if (buildVersion) {
        descriptionLines.add("**Version:** ${buildVersion}")
    }

    def artifactLinks = getArtifactLinks(buildResult == 'SUCCESS')
    if (artifactLinks) {
        descriptionLines.add("**Artifacts:** ${artifactLinks.join(' | ')}")
    }

    def description = descriptionLines.join('\n')

    def args = [
        description: description,
        footer: 'JEI Jenkins',
        link: env.BUILD_URL,
        result: buildResult,
        title: "${env.JOB_NAME} #${env.BUILD_NUMBER}",
        webhookURL: env.DISCORD_WEBHOOK_URL
    ]

    discordSend(args)
}

def notifyDiscordBuildSafely(String result) {
    try {
        notifyDiscordBuild(result)
    } catch (Throwable e) {
        echo "Discord notification failed: ${e.getMessage()}"
    }
}

return this
