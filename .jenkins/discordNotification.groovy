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

def getOrderedLoaderLinks(Map linksByModule) {
    def links = []

    def neoForgeLink = linksByModule['NeoForge']
    if (neoForgeLink) {
        links.add(neoForgeLink)
    }

    def fabricLink = linksByModule['Fabric']
    if (fabricLink) {
        links.add(fabricLink)
    }

    def forgeLink = linksByModule['Forge']
    if (forgeLink) {
        links.add(forgeLink)
    }

    return links
}

def getReleaseLinkLines(boolean includeFallback) {
    def resultFiles = [
        'Forge/build/publishMods/publishCurseforge.json',
        'Forge/build/publishMods/publishModrinth.json',
        'Fabric/build/publishMods/publishCurseforge.json',
        'Fabric/build/publishMods/publishModrinth.json',
        'NeoForge/build/publishMods/publishCurseforge.json',
        'NeoForge/build/publishMods/publishModrinth.json'
    ]

    def curseForgeLinksByModule = [:]
    def modrinthLinksByModule = [:]
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
                curseForgeLinksByModule[moduleName] = "[${moduleName}](https://www.curseforge.com/minecraft/mc-mods/${projectSlug}/files/${fileId})"
            }
        } else if (publishType == 'modrinth') {
            def projectId = getJsonValue(publishResult, 'projectId')
            def versionId = getJsonValue(publishResult, 'id')
            if (projectId && projectId != 'dry-run' && versionId) {
                modrinthLinksByModule[moduleName] = "[${moduleName}](https://modrinth.com/mod/${projectId}/version/${versionId})"
            }
        }
    }

    def releaseLinkLines = []

    def curseForgeLinks = getOrderedLoaderLinks(curseForgeLinksByModule)
    if (curseForgeLinks) {
        releaseLinkLines.add("**CurseForge:** ${curseForgeLinks.join(' | ')}")
    }

    def modrinthLinks = getOrderedLoaderLinks(modrinthLinksByModule)
    if (modrinthLinks) {
        releaseLinkLines.add("**Modrinth:** ${modrinthLinks.join(' | ')}")
    }

    if (releaseLinkLines || !includeFallback) {
        return releaseLinkLines
    }

    // Fallback for branches or builds that do not generate mod-publish-plugin result files.
    if (curseHomepageUrl) {
        def curseFilesUrl = "${removeTrailingSlashes(curseHomepageUrl)}/files"
        releaseLinkLines.add("**CurseForge:** [NeoForge](${curseFilesUrl}) | [Fabric](${curseFilesUrl}) | [Forge](${curseFilesUrl})")
    }

    def modrinthId = getGradleProperty('modrinthId')
    if (modrinthId) {
        def modrinthVersionsUrl = "https://modrinth.com/mod/${modrinthId}/versions"
        releaseLinkLines.add("**Modrinth:** [NeoForge](${modrinthVersionsUrl}) | [Fabric](${modrinthVersionsUrl}) | [Forge](${modrinthVersionsUrl})")
    }
    return releaseLinkLines
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
    def githubUrl = removeTrailingSlashes(getGradleProperty('githubUrl'))
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
