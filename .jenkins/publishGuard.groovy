def isHexCommit(String value) {
    if (!value) {
        return false
    }

    if (value.length() < 7 || value.length() > 40) {
        return false
    }

    def hexCharacters = '0123456789abcdefABCDEF'
    for (def index = 0; index < value.length(); index++) {
        def character = value.substring(index, index + 1)
        if (hexCharacters.indexOf(character) < 0) {
            return false
        }
    }

    return true
}

def normalizePath(String fileName) {
    def result = ''
    for (def index = 0; index < fileName.length(); index++) {
        def character = fileName.substring(index, index + 1)
        if (character == '\\') {
            result += '/'
        } else {
            result += character
        }
    }
    return result
}

def addUnique(List files, String fileName) {
    if (!fileName) {
        return
    }

    def normalizedFileName = normalizePath(fileName.trim())
    if (!normalizedFileName) {
        return
    }

    for (def existingFileName in files) {
        if (existingFileName == normalizedFileName) {
            return
        }
    }

    files.add(normalizedFileName)
}

def getChangedFilesFromText(String text) {
    def files = []
    for (def line in text.readLines()) {
        addUnique(files, line)
    }
    return files
}

def getCurrentCommit() {
    def gitCommit = env.GIT_COMMIT ?: ''
    if (isHexCommit(gitCommit)) {
        return gitCommit
    }

    def headCommit = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
    if (isHexCommit(headCommit)) {
        return headCommit
    }

    return ''
}

def getPreviousCommit() {
    def previousSuccessfulCommit = env.GIT_PREVIOUS_SUCCESSFUL_COMMIT ?: ''
    if (isHexCommit(previousSuccessfulCommit)) {
        return previousSuccessfulCommit
    }

    def previousCommit = env.GIT_PREVIOUS_COMMIT ?: ''
    if (isHexCommit(previousCommit)) {
        return previousCommit
    }

    return ''
}

def getChangedFilesFromGit() {
    def previousCommit = getPreviousCommit()
    if (!previousCommit) {
        return null
    }

    def currentCommit = getCurrentCommit()
    if (!currentCommit) {
        return null
    }

    if (previousCommit == currentCommit) {
        return []
    }

    def changedFiles = sh(script: "git diff --name-only ${previousCommit} ${currentCommit}", returnStdout: true)
    return getChangedFilesFromText(changedFiles)
}

def getChangedFilesFromChangeSets() {
    def files = []
    for (def changeSet in currentBuild.changeSets) {
        for (def item in changeSet.items) {
            for (def affectedFile in item.affectedFiles) {
                addUnique(files, affectedFile.path)
            }
        }
    }
    return files
}

def getChangedFiles() {
    try {
        def gitFiles = getChangedFilesFromGit()
        if (gitFiles != null) {
            return gitFiles
        }
    } catch (Throwable e) {
        echo "Publish guard git diff failed: ${e.getMessage()}"
    }

    try {
        return getChangedFilesFromChangeSets()
    } catch (Throwable e) {
        echo "Publish guard change set inspection failed: ${e.getMessage()}"
    }

    // If Jenkins cannot tell what changed, prefer publishing over accidentally
    // suppressing a real release.
    return null
}

def isDocumentationChange(String fileName) {
    if (fileName.endsWith('.md') || fileName.endsWith('.markdown')) {
        return true
    }
    if (fileName == 'LICENSE' || fileName == 'LICENSE.txt' || fileName == 'COPYING') {
        return true
    }
    if (fileName.startsWith('docs/')) {
        return true
    }
    if (fileName.startsWith('Changelog/')) {
        return true
    }
    return false
}

def isTestOnlyChange(String fileName) {
    def testSourceSets = [
        'test',
        'testFixtures',
        'clientTestFixtures',
        'gameTest',
        'gametest',
        'clientGameTest',
        'keyMappingGametest'
    ]

    for (def sourceSetName in testSourceSets) {
        def sourceSetPath = "src/${sourceSetName}/"
        if (fileName.startsWith(sourceSetPath)) {
            return true
        }
        if (fileName.indexOf("/${sourceSetPath}") >= 0) {
            return true
        }
    }

    return false
}

def isCiOnlyChange(String fileName) {
    if (fileName.startsWith('.jenkins/')) {
        return true
    }
    if (fileName.startsWith('.github/')) {
        return true
    }
    if (fileName == '.travis.yml') {
        return true
    }
    if (fileName == '.gitignore' || fileName == '.gitattributes') {
        return true
    }
    return false
}

def isModuleLocalOutputChange(String fileName) {
    def outputDirectories = ['build', 'out', 'logs', 'run']

    for (def outputDirectory in outputDirectories) {
        if (fileName == outputDirectory || fileName.startsWith("${outputDirectory}/")) {
            return true
        }

        def moduleOutputPath = "/${outputDirectory}/"
        def moduleOutputIndex = fileName.indexOf(moduleOutputPath)
        if (moduleOutputIndex > 0) {
            def prefix = fileName.substring(0, moduleOutputIndex)
            if (prefix.indexOf('/') < 0) {
                return true
            }
        }
    }

    return false
}

def isDevelopmentOnlyChange(String fileName) {
    if (fileName.startsWith('Debug/')) {
        return true
    }
    if (fileName.startsWith('.idea/') || fileName.startsWith('.run/') || fileName.startsWith('.vscode/')) {
        return true
    }
    if (fileName.endsWith('.iml') || fileName.endsWith('.ipr') || fileName.endsWith('.iws')) {
        return true
    }
    if (isModuleLocalOutputChange(fileName)) {
        return true
    }
    return false
}

def isPublishableChange(String fileName) {
    if (!fileName) {
        return false
    }
    if (isCiOnlyChange(fileName)) {
        return false
    }
    if (isDocumentationChange(fileName)) {
        return false
    }
    if (isTestOnlyChange(fileName)) {
        return false
    }
    if (isDevelopmentOnlyChange(fileName)) {
        return false
    }
    return true
}

def joinLimited(List files, int maxFiles) {
    def result = []
    def count = 0
    for (def fileName in files) {
        if (count >= maxFiles) {
            break
        }
        result.add(fileName)
        count++
    }

    if (files.size() > maxFiles) {
        result.add("...and ${files.size() - maxFiles} more")
    }

    return result.join(', ')
}

def shouldPublishAfterPreviousBuildFailure() {
    def previousBuild = currentBuild.previousBuild
    if (previousBuild == null) {
        return false
    }

    def previousResult = previousBuild.result ?: ''
    if (!previousResult) {
        return false
    }

    if (previousResult != 'SUCCESS') {
        echo "Publishing artifacts because the previous Jenkins build result was ${previousResult}."
        return true
    }

    return false
}

def shouldPublishArtifacts() {
    def changedFiles = getChangedFiles()
    if (changedFiles == null) {
        echo 'Publishing artifacts because changed files could not be determined.'
        return true
    }

    if (!changedFiles) {
        // Retry publishing when Jenkins rebuilds the same revision after a
        // failure, but do not let that override the file classifier for
        // non-publishing commits.
        if (shouldPublishAfterPreviousBuildFailure()) {
            return true
        }

        echo 'Skipping artifact publishing because Jenkins reported no changed files.'
        return false
    }

    def publishableChanges = []
    def ignoredChanges = []
    for (def changedFile in changedFiles) {
        if (isPublishableChange(changedFile)) {
            publishableChanges.add(changedFile)
        } else {
            ignoredChanges.add(changedFile)
        }
    }

    if (publishableChanges) {
        echo "Publishing artifacts because publishable changes were detected: ${joinLimited(publishableChanges, 20)}"
        return true
    }

    echo "Skipping artifact publishing because only non-publishing changes were detected: ${joinLimited(ignoredChanges, 20)}"
    return false
}

return this
