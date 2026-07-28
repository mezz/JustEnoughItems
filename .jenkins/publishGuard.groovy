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
    if (fileName == 'README.md' || fileName == 'UNSUPPORTED_VERSIONS.md') {
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

def isCiOnlyChange(String fileName) {
    if (fileName.startsWith('.jenkins/')) {
        return true
    }
    if (fileName.startsWith('.github/')) {
        return true
    }
    if (fileName == '.gitignore' || fileName == '.gitattributes') {
        return true
    }
    return false
}

def isCodeChange(String fileName) {
    if (!fileName) {
        return false
    }
    if (isCiOnlyChange(fileName)) {
        return false
    }
    if (isDocumentationChange(fileName)) {
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

def shouldPublishArtifacts() {
    def changedFiles = getChangedFiles()
    if (changedFiles == null) {
        echo 'Publishing artifacts because changed files could not be determined.'
        return true
    }

    if (!changedFiles) {
        echo 'Skipping artifact publishing because Jenkins reported no changed files.'
        return false
    }

    def codeChanges = []
    def ignoredChanges = []
    for (def changedFile in changedFiles) {
        if (isCodeChange(changedFile)) {
            codeChanges.add(changedFile)
        } else {
            ignoredChanges.add(changedFile)
        }
    }

    if (codeChanges) {
        echo "Publishing artifacts because code changes were detected: ${joinLimited(codeChanges, 20)}"
        return true
    }

    echo "Skipping artifact publishing because only CI/documentation changes were detected: ${joinLimited(ignoredChanges, 20)}"
    return false
}

return this
