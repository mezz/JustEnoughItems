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

def getGithubUrl() {
    def githubUrl = getGradleProperty('githubUrl')
    if (!githubUrl) {
        githubUrl = 'https://github.com/mezz/JustEnoughItems'
    }
    return removeTrailingSlashes(githubUrl)
}

def getCurseHomepageUrl() {
    def curseHomepageUrl = getGradleProperty('curseHomepageUrl')
    if (!curseHomepageUrl) {
        curseHomepageUrl = 'https://www.curseforge.com/minecraft/mc-mods/jei'
    }
    return removeTrailingSlashes(curseHomepageUrl)
}

def getModrinthId() {
    def modrinthId = getGradleProperty('modrinthId')
    if (!modrinthId) {
        modrinthId = 'jei'
    }
    return modrinthId
}

def getFirstLine(String text) {
    if (!text) {
        return ''
    }

    def valueEnd = text.length()
    def newlineIndex = text.indexOf('\n')
    if (newlineIndex >= 0 && newlineIndex < valueEnd) {
        valueEnd = newlineIndex
    }

    def returnIndex = text.indexOf('\r')
    if (returnIndex >= 0 && returnIndex < valueEnd) {
        valueEnd = returnIndex
    }

    return text.substring(0, valueEnd).trim()
}

def truncateText(String text, int maxLength) {
    if (!text || text.length() <= maxLength) {
        return text
    }

    if (maxLength <= 1) {
        return text.substring(0, maxLength)
    }

    return "${text.substring(0, maxLength - 1)}…"
}

def sanitizeDiscordText(String text) {
    def result = ''
    for (def index = 0; index < text.length(); index++) {
        def character = text.substring(index, index + 1)
        if (character == '@') {
            result += '@'
            result += '\u200B'
        } else {
            result += character
        }
    }
    return result
}

def getBuildVersion() {
    def specificationVersion = getGradleProperty('specificationVersion')
    if (specificationVersion) {
        return "${specificationVersion}.${env.BUILD_NUMBER}"
    }
    return ''
}

def getReleaseLoaders() {
    def loaders = []
    if (fileExists('NeoForge/build.gradle') || fileExists('NeoForge/build.gradle.kts')) {
        loaders.add('NeoForge')
    }
    if (fileExists('Fabric/build.gradle') || fileExists('Fabric/build.gradle.kts')) {
        loaders.add('Fabric')
    }
    if (fileExists('Forge/build.gradle') || fileExists('Forge/build.gradle.kts')) {
        loaders.add('Forge')
    }
    if (!loaders) {
        loaders.add('Forge')
    }
    return loaders
}

def hasLoader(String loaderName, List loaders) {
    for (def loader in loaders) {
        if (loader == loaderName) {
            return true
        }
    }
    return false
}

def hasText(String fileName, String text) {
    if (!fileExists(fileName)) {
        return false
    }
    return readFile(file: fileName).indexOf(text) >= 0
}

def hasModrinthPublishing() {
    if (getGradleProperty('modrinthId')) {
        return true
    }
    if (hasText('.jenkins/Jenkinsfile', 'modrinth')) {
        return true
    }
    if (hasText('build.gradle', 'modrinth')) {
        return true
    }
    if (hasText('build.gradle.kts', 'modrinth')) {
        return true
    }
    if (hasText('Fabric/build.gradle', 'modrinth')) {
        return true
    }
    if (hasText('Fabric/build.gradle.kts', 'modrinth')) {
        return true
    }
    if (hasText('Forge/build.gradle', 'modrinth')) {
        return true
    }
    if (hasText('Forge/build.gradle.kts', 'modrinth')) {
        return true
    }
    if (hasText('NeoForge/build.gradle', 'modrinth')) {
        return true
    }
    if (hasText('NeoForge/build.gradle.kts', 'modrinth')) {
        return true
    }
    return false
}

def getReleaseLinkEntries(boolean includeFallback) {
    def resultFiles = [
        [module: 'Forge', service: 'curseforge', file: 'Forge/build/publishMods/publishCurseforge.json'],
        [module: 'Forge', service: 'modrinth', file: 'Forge/build/publishMods/publishModrinth.json'],
        [module: 'Fabric', service: 'curseforge', file: 'Fabric/build/publishMods/publishCurseforge.json'],
        [module: 'Fabric', service: 'modrinth', file: 'Fabric/build/publishMods/publishModrinth.json'],
        [module: 'NeoForge', service: 'curseforge', file: 'NeoForge/build/publishMods/publishCurseforge.json'],
        [module: 'NeoForge', service: 'modrinth', file: 'NeoForge/build/publishMods/publishModrinth.json'],
        [module: 'Forge', service: 'curseforge', file: 'build/publishMods/publishCurseforge.json'],
        [module: 'Forge', service: 'modrinth', file: 'build/publishMods/publishModrinth.json']
    ]

    def releaseLoaders = getReleaseLoaders()
    def curseForgeLinksByModule = [:]
    def modrinthLinksByModule = [:]
    def curseHomepageUrl = getCurseHomepageUrl()
    def curseProjectSlug = getLastPathSegment(curseHomepageUrl)

    for (def resultFile in resultFiles) {
        def resultFileName = resultFile['file']
        if (!fileExists(resultFileName)) {
            continue
        }

        def moduleName = resultFile['module']
        if (!hasLoader(moduleName, releaseLoaders)) {
            continue
        }

        def publishResult = readFile(file: resultFileName)
        def publishType = getJsonValue(publishResult, 'type')
        if (publishType == 'curseforge') {
            def fileId = getJsonValue(publishResult, 'fileId')
            def projectSlug = getJsonValue(publishResult, 'projectSlug') ?: curseProjectSlug
            if (fileId && projectSlug && projectSlug != 'dry-run') {
                curseForgeLinksByModule[moduleName] = "https://www.curseforge.com/minecraft/mc-mods/${projectSlug}/files/${fileId}"
            }
        } else if (publishType == 'modrinth') {
            def projectId = getJsonValue(publishResult, 'projectId')
            def versionId = getJsonValue(publishResult, 'id')
            if (projectId && projectId != 'dry-run' && versionId) {
                modrinthLinksByModule[moduleName] = "https://modrinth.com/mod/${projectId}/version/${versionId}"
            }
        }
    }

    def releaseLinkEntries = []

    for (def loader in releaseLoaders) {
        def curseForgeUrl = curseForgeLinksByModule[loader]
        if (curseForgeUrl) {
            releaseLinkEntries.add([platform: 'CurseForge', loader: loader, url: curseForgeUrl])
        }
    }

    for (def loader in releaseLoaders) {
        def modrinthUrl = modrinthLinksByModule[loader]
        if (modrinthUrl) {
            releaseLinkEntries.add([platform: 'Modrinth', loader: loader, url: modrinthUrl])
        }
    }

    if (releaseLinkEntries || !includeFallback) {
        return releaseLinkEntries
    }

    def curseForgeUrl = "${curseHomepageUrl}/files"
    for (def loader in releaseLoaders) {
        releaseLinkEntries.add([
            platform: 'CurseForge',
            loader: loader,
            url: curseForgeUrl
        ])
    }

    if (hasModrinthPublishing()) {
        def modrinthUrl = "https://modrinth.com/mod/${getModrinthId()}/versions"
        for (def loader in releaseLoaders) {
            releaseLinkEntries.add([
                platform: 'Modrinth',
                loader: loader,
                url: modrinthUrl
            ])
        }
    }

    return releaseLinkEntries
}

def getReleaseLinkLines(boolean includeFallback) {
    def entries = getReleaseLinkEntries(includeFallback)
    def releaseLinkLines = []
    for (def platform in ['CurseForge', 'Modrinth']) {
        def links = entries.findAll { it.platform == platform }.collect { "[${it.loader}](${it.url})" }
        if (links) {
            releaseLinkLines.add("**${platform}:** ${links.join(' | ')}")
        }
    }

    return releaseLinkLines
}

def getReleaseLinks(boolean includeFallback) {
    return getReleaseLinkEntries(includeFallback).collect { entry ->
        return [label: "${entry.platform} (${entry.loader})", url: entry.url]
    }
}

def formatCommitLink(String githubUrl, String commitId, String message) {
    if (!commitId) {
        return ''
    }

    def shortCommit = commitId.length() > 10 ? commitId.substring(0, 10) : commitId
    def subject = sanitizeDiscordText(truncateText(getFirstLine(message), 100))
    if (!subject) {
        subject = '(no commit message)'
    }
    return "- [`${shortCommit}`](${githubUrl}/commit/${commitId}) ${subject}"
}

def getCommitLinksFromChangeSets(String githubUrl, int maxCommits) {
    def links = []
    def totalCommits = 0

    for (def changeSet in currentBuild.changeSets) {
        for (def item in changeSet.items) {
            def commitId = item.commitId ?: ''
            if (!commitId) {
                continue
            }

            totalCommits++
            if (links.size() < maxCommits) {
                def commitLink = formatCommitLink(githubUrl, commitId, item.msg ?: '')
                if (commitLink) {
                    links.add(commitLink)
                }
            }
        }
    }

    if (totalCommits > maxCommits) {
        links.add("- …and ${totalCommits - maxCommits} more")
    }

    return links
}

def getHeadCommitLink(String githubUrl) {
    def gitLog = sh(script: 'git log -1 --format=%H%x09%s', returnStdout: true).trim()
    if (!gitLog) {
        return []
    }

    def separatorIndex = gitLog.indexOf('\t')
    if (separatorIndex < 0) {
        return [formatCommitLink(githubUrl, gitLog, '')]
    }

    def commitId = gitLog.substring(0, separatorIndex)
    def message = gitLog.substring(separatorIndex + 1)
    return [formatCommitLink(githubUrl, commitId, message)]
}

def getCommitLinks() {
    def githubUrl = getGithubUrl()
    if (!githubUrl) {
        return []
    }

    def links = getCommitLinksFromChangeSets(githubUrl, 10)
    if (links) {
        return links
    }

    return getHeadCommitLink(githubUrl)
}

def getCommitLinksSafely() {
    try {
        return getCommitLinks()
    } catch (Throwable e) {
        echo "Discord commit collection failed: ${e.getMessage()}"
        return []
    }
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

    def commitLinks = getCommitLinksSafely()
    if (commitLinks) {
        descriptionLines.add("**Commits:**\n${commitLinks.join('\n')}")
    }

    if (env.SHOULD_PUBLISH == 'false') {
        descriptionLines.add('**Publish:** skipped (no code changes)')
    } else {
        def releaseLinkLines = getReleaseLinkLines(buildResult == 'SUCCESS')
        for (def releaseLinkLine in releaseLinkLines) {
            if (releaseLinkLine) {
                descriptionLines.add(releaseLinkLine)
            }
        }
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
