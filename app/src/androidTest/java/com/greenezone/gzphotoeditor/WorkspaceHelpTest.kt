package com.greenezone.gzphotoeditor

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.greenezone.gzphotoeditor.ui.ProHelpDialog
import org.junit.Rule
import org.junit.Test

class WorkspaceHelpTest {
    @get:Rule val rule = createComposeRule()

    @Test fun aUserCanFindFeatherInstructionsAndReturnToFilteredTopics() {
        rule.setContent { MaterialTheme { ProHelpDialog {} } }
        rule.onNode(hasSetTextAction()).performTextInput("feather")
        rule.onNodeWithText("Cut out part of a photo").performClick()
        rule.onNodeWithText("Increase Feather", substring = true).assertExists()
        rule.onNodeWithText("All topics").performClick()
        rule.onNodeWithText("Cut out part of a photo").assertExists()
        rule.onNodeWithText("Build and arrange a layer stack").assertDoesNotExist()
    }
}
