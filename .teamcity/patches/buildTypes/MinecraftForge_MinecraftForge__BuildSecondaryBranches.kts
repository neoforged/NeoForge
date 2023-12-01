package patches.buildTypes

import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.ui.*

/*
This patch script was generated by TeamCity on settings change in UI.
To apply the patch, change the buildType with id = 'MinecraftForge_MinecraftForge__BuildSecondaryBranches'
accordingly, and delete the patch script.
*/
changeBuildType(RelativeId("MinecraftForge_MinecraftForge__BuildSecondaryBranches")) {
    vcs {

        check(branchFilter == "+:*") {
            "Unexpected option value: branchFilter = $branchFilter"
        }
        branchFilter = """
            -:master
            -:main
            -:<default>
            -:%git_main_branch%
            -:main*
            -:master*
            -:default
            -:1.*
            +:*
        """.trimIndent()
    }
}
