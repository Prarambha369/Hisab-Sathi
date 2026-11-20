package com.prarambha.hisabsathi.presentation.ui.features.settings.about

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.prarambha.hisabsathi.presentation.effects.BlurredAnimatedVisibility
import com.prarambha.hisabsathi.presentation.effects.overScrollVertical
import com.prarambha.hisabsathi.presentation.effects.rememberOverscrollFlingBehavior
import com.prarambha.hisabsathi.presentation.ui.components.CustomTitleTopAppBar
import com.prarambha.hisabsathi.presentation.ui.features.categories.NavigationContent
import com.prarambha.hisabsathi.presentation.ui.features.settings.SettingsViewModel
import com.prarambha.hisabsathi.presentation.ui.theme.Dimensions
import com.prarambha.hisabsathi.presentation.ui.theme.Spacing
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesScreen(
    onNavigateBack: () -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scrollBehaviorSmall = TopAppBarDefaults.pinnedScrollBehavior()
    val hazeState = remember { HazeState() }
    
    var selectedLibrary by remember { mutableStateOf<Library?>(null) }

    val libraries = remember {
        listOf(
            Library("Jetpack Compose", 1, "" +
                    "Apache License 2.0" +
                    "  \nCopyright [yyyy] [name of copyright owner]\n" +
                    "\n" +
                    "   Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                    "   you may not use this file except in compliance with the License.\n" +
                    "   You may obtain a copy of the License at\n" +
                    "\n" +
                    "       http://www.apache.org/licenses/LICENSE-2.0\n" +
                    "\n" +
                    "   Unless required by applicable law or agreed to in writing, software\n" +
                    "   distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                    "   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                    "   See the License for the specific language governing permissions and\n" +
                    "   limitations under the License.\n" +
                    "\n"),

            Library("Hilt", 1,  "" +
                    "Apache License 2.0" +
                    "  \nCopyright [yyyy] [name of copyright owner]\n" +
                    "\n" +
                    "   Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                    "   you may not use this file except in compliance with the License.\n" +
                    "   You may obtain a copy of the License at\n" +
                    "\n" +
                    "       http://www.apache.org/licenses/LICENSE-2.0\n" +
                    "\n" +
                    "   Unless required by applicable law or agreed to in writing, software\n" +
                    "   distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                    "   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                    "   See the License for the specific language governing permissions and\n" +
                    "   limitations under the License.\n" +
                    "\n"),

            Library("Room Database", 1,  "" +
                    "Apache License 2.0" +
                    "  \nCopyright [yyyy] [name of copyright owner]\n" +
                    "\n" +
                    "   Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                    "   you may not use this file except in compliance with the License.\n" +
                    "   You may obtain a copy of the License at\n" +
                    "\n" +
                    "       http://www.apache.org/licenses/LICENSE-2.0\n" +
                    "\n" +
                    "   Unless required by applicable law or agreed to in writing, software\n" +
                    "   distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                    "   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                    "   See the License for the specific language governing permissions and\n" +
                    "   limitations under the License.\n" +
                    "\n"),

            Library("Haze", 1,  "" +
                    "Apache License 2.0" +
                    "  \nCopyright [yyyy] [name of copyright owner]\n" +
                    "\n" +
                    "   Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                    "   you may not use this file except in compliance with the License.\n" +
                    "   You may obtain a copy of the License at\n" +
                    "\n" +
                    "       http://www.apache.org/licenses/LICENSE-2.0\n" +
                    "\n" +
                    "   Unless required by applicable law or agreed to in writing, software\n" +
                    "   distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                    "   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                    "   See the License for the specific language governing permissions and\n" +
                    "   limitations under the License.\n" +
                    "\n"),

            Library("MediaPipe GenAI", 1, "" +
                    "Apache License 2.0" +
                    "  \nCopyright [yyyy] [name of copyright owner]\n" +
                    "\n" +
                    "   Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                    "   you may not use this file except in compliance with the License.\n" +
                    "   You may obtain a copy of the License at\n" +
                    "\n" +
                    "       http://www.apache.org/licenses/LICENSE-2.0\n" +
                    "\n" +
                    "   Unless required by applicable law or agreed to in writing, software\n" +
                    "   distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                    "   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                    "   See the License for the specific language governing permissions and\n" +
                    "   limitations under the License.\n" +
                    "\n" +
                    "===========================================================================\n" +
                    "For files under tasks/cc/text/language_detector/custom_ops/utils/utf/\n" +
                    "===========================================================================\n" +
                    "/*\n" +
                    " * The authors of this software are Rob Pike and Ken Thompson.\n" +
                    " *              \nCopyright (c) 2002 by Lucent Technologies.\n" +
                    " * Permission to use, copy, modify, and distribute this software for any\n" +
                    " * purpose without fee is hereby granted, provided that this entire notice\n" +
                    " * is included in all copies of any software which is or includes a copy\n" +
                    " * or modification of this software and in all copies of the supporting\n" +
                    " * documentation for such software.\n" +
                    " * THIS SOFTWARE IS BEING PROVIDED \"AS IS\", WITHOUT ANY EXPRESS OR IMPLIED\n" +
                    " * WARRANTY.  IN PARTICULAR, NEITHER THE AUTHORS NOR LUCENT TECHNOLOGIES MAKE ANY\n" +
                    " * REPRESENTATION OR WARRANTY OF ANY KIND CONCERNING THE MERCHANTABILITY\n" +
                    " * OF THIS SOFTWARE OR ITS FITNESS FOR ANY PARTICULAR PURPOSE.\n" +
                    " */"),

            Library("Ktor", 1, "" +
                    "Apache License 2.0" +
                    "\nCopyright 2000-2023 JetBrains s.r.o.\n" +
                    "\n" +
                    "   Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                    "   you may not use this file except in compliance with the License.\n" +
                    "   You may obtain a copy of the License at\n" +
                    "\n" +
                    "       http://www.apache.org/licenses/LICENSE-2.0\n" +
                    "\n" +
                    "   Unless required by applicable law or agreed to in writing, software\n" +
                    "   distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                    "   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                    "   See the License for the specific language governing permissions and\n" +
                    "   limitations under the License."),

            Library("Coil", 1, "" +
                    "Apache License 2.0" +
                    "\nCopyright 2025 Coil Contributors\n" +
                    "\n" +
                    "   Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                    "   you may not use this file except in compliance with the License.\n" +
                    "   You may obtain a copy of the License at\n" +
                    "\n" +
                    "       http://www.apache.org/licenses/LICENSE-2.0\n" +
                    "\n" +
                    "   Unless required by applicable law or agreed to in writing, software\n" +
                    "   distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                    "   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                    "   See the License for the specific language governing permissions and\n" +
                    "   limitations under the License."),

            Library("Kotlinx Serialization", 1,  "" +
                    "Apache License 2.0" +
                    "  \nCopyright [yyyy] [name of copyright owner]\n" +
                    "\n" +
                    "   Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                    "   you may not use this file except in compliance with the License.\n" +
                    "   You may obtain a copy of the License at\n" +
                    "\n" +
                    "       http://www.apache.org/licenses/LICENSE-2.0\n" +
                    "\n" +
                    "   Unless required by applicable law or agreed to in writing, software\n" +
                    "   distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                    "   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                    "   See the License for the specific language governing permissions and\n" +
                    "   limitations under the License.\n" +
                    "\n"),

            Library("DataStore", 1,  "" +
                    "Apache License 2.0" +
                    "  \nCopyright [yyyy] [name of copyright owner]\n" +
                    "\n" +
                    "   Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                    "   you may not use this file except in compliance with the License.\n" +
                    "   You may obtain a copy of the License at\n" +
                    "\n" +
                    "       http://www.apache.org/licenses/LICENSE-2.0\n" +
                    "\n" +
                    "   Unless required by applicable law or agreed to in writing, software\n" +
                    "   distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                    "   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                    "   See the License for the specific language governing permissions and\n" +
                    "   limitations under the License.\n" +
                    "\n"),
            Library("Biometric Auth", 1,  "" +
                    "Apache License 2.0" +
                    "  \nCopyright [yyyy] [name of copyright owner]\n" +
                    "\n" +
                    "   Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                    "   you may not use this file except in compliance with the License.\n" +
                    "   You may obtain a copy of the License at\n" +
                    "\n" +
                    "       http://www.apache.org/licenses/LICENSE-2.0\n" +
                    "\n" +
                    "   Unless required by applicable law or agreed to in writing, software\n" +
                    "   distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                    "   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                    "   See the License for the specific language governing permissions and\n" +
                    "   limitations under the License.\n" +
                    "\n"),
            Library("WorkManager", 1,  "" +
                    "Apache License 2.0" +
                    "  \nCopyright [yyyy] [name of copyright owner]\n" +
                    "\n" +
                    "   Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                    "   you may not use this file except in compliance with the License.\n" +
                    "   You may obtain a copy of the License at\n" +
                    "\n" +
                    "       http://www.apache.org/licenses/LICENSE-2.0\n" +
                    "\n" +
                    "   Unless required by applicable law or agreed to in writing, software\n" +
                    "   distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                    "   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                    "   See the License for the specific language governing permissions and\n" +
                    "   limitations under the License.\n" +
                    "\n"),
            Library("Compose Charts", 1,  "" +
                    "Apache License 2.0" +
                    "  \nCopyright [yyyy] [name of copyright owner]\n" +
                    "\n" +
                    "   Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                    "   you may not use this file except in compliance with the License.\n" +
                    "   You may obtain a copy of the License at\n" +
                    "\n" +
                    "       http://www.apache.org/licenses/LICENSE-2.0\n" +
                    "\n" +
                    "   Unless required by applicable law or agreed to in writing, software\n" +
                    "   distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                    "   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                    "   See the License for the specific language governing permissions and\n" +
                    "   limitations under the License.\n" +
                    "\n"),
            Library("Reorderable", 1,  "" +
                    "Apache License 2.0" +
                    "  \nCopyright [yyyy] [name of copyright owner]\n" +
                    "\n" +
                    "   Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                    "   you may not use this file except in compliance with the License.\n" +
                    "   You may obtain a copy of the License at\n" +
                    "\n" +
                    "       http://www.apache.org/licenses/LICENSE-2.0\n" +
                    "\n" +
                    "   Unless required by applicable law or agreed to in writing, software\n" +
                    "   distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                    "   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                    "   See the License for the specific language governing permissions and\n" +
                    "   limitations under the License.\n" +
                    "\n"),
            Library("OpenCSV", 1,  "" +
                    "Apache License 2.0" +
                    "  \nCopyright [yyyy] [name of copyright owner]\n" +
                    "\n" +
                    "   Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                    "   you may not use this file except in compliance with the License.\n" +
                    "   You may obtain a copy of the License at\n" +
                    "\n" +
                    "       http://www.apache.org/licenses/LICENSE-2.0\n" +
                    "\n" +
                    "   Unless required by applicable law or agreed to in writing, software\n" +
                    "   distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                    "   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                    "   See the License for the specific language governing permissions and\n" +
                    "   limitations under the License.\n" +
                    "\n"),
            Library("Markdown Support", 1,  "" +
                    "Apache License 2.0" +
                    "\nCopyright 2014 FriendCode Inc.\n" +
                    "\n" +
                    "   Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                    "   you may not use this file except in compliance with the License.\n" +
                    "   You may obtain a copy of the License at\n" +
                    "\n" +
                    "       http://www.apache.org/licenses/LICENSE-2.0\n" +
                    "\n" +
                    "   Unless required by applicable law or agreed to in writing, software\n" +
                    "   distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                    "   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                    "   See the License for the specific language governing permissions and\n" +
                    "   limitations under the License."),
            Library("Gson", 1, apache2License("Copyright 2008 Google Inc.")),
            Library("ColorPicker Compose", 1, apache2License("Copyright 2022 skydoves (Jaewoong Eum)")),
            Library("On-device AI models", 1, "" +
                    "Downloaded separately from Hugging Face (litert-community) only when you choose one; " +
                    "not bundled with the app.\n\n" +
                    "- Qwen 2.5 0.5B / 1.5B Instruct: Apache License 2.0, Copyright Alibaba Cloud (Qwen team)\n" +
                    "- DeepSeek R1 Distill Qwen 1.5B: MIT License, Copyright DeepSeek\n" +
                    "- Phi-4 mini instruct: MIT License, Copyright Microsoft Corporation\n" +
                    "- SmolLM 135M Instruct: Apache License 2.0, Copyright Hugging Face\n"),
        )
    }


    val listState = rememberLazyListState()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CustomTitleTopAppBar(
                title = selectedLibrary?.name ?: "Licenses",
                scrollBehaviorSmall = scrollBehaviorSmall,
                scrollBehaviorLarge = scrollBehavior,
                hazeState = hazeState,
                hasBackButton = true,
                navigationContent = { 
                    NavigationContent { 
                        if (selectedLibrary != null) {
                            selectedLibrary = null
                        } else {
                            onNavigateBack()
                        }
                    } 
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState)
                .overScrollVertical(),
            flingBehavior = rememberOverscrollFlingBehavior { listState },
            contentPadding =
                PaddingValues(
                    top = Dimensions.Padding.content + paddingValues.calculateTopPadding(),
                    start = Dimensions.Padding.content,
                    end = Dimensions.Padding.content,
                    bottom = 0.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            item {
                BlurredAnimatedVisibility(
                    visible = selectedLibrary == null,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Spacer(modifier = Modifier.height(Spacing.xl))

                        Text(
                            text = "Hisab Sathi",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "v${com.prarambha.hisabsathi.BuildConfig.VERSION_NAME}, db-v${settingsViewModel.databaseVersion}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(Spacing.xl))

                        Text(
                            text = "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(Spacing.lg))

                        Text(
                            text = "The exchange rates displayed within this app are for informational purposes only and should not be used for investment decisions. These rates are estimates and may not reflect actual rates. By using this app, you acknowledge that you understand and accept these limitations and that you assume full responsibility for any decisions made based on the information provided within the app.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(Spacing.lg))

                        Text(
                            text = "Powered by Kotlin Compose",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(Spacing.xxl))
                    }
                }
            }
            items(libraries) { library ->
                BlurredAnimatedVisibility(
                    visible = selectedLibrary == null,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedLibrary = library }
                            .padding(vertical = Spacing.md),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = library.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${library.licenseCount} license${if (library.licenseCount > 1) "s" else ""}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            item {
                BlurredAnimatedVisibility(
                    visible = selectedLibrary != null,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
                ) {
                    Text(
                        text = selectedLibrary?.licenseText ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.md)
                    )
                }
            }
        }
    }
}


data class Library(
    val name: String,
    val licenseCount: Int,
    val licenseText: String
)

private fun apache2License(copyright: String): String =
    "Apache License 2.0\n$copyright\n\n" +
        "   Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
        "   you may not use this file except in compliance with the License.\n" +
        "   You may obtain a copy of the License at\n\n" +
        "       http://www.apache.org/licenses/LICENSE-2.0\n\n" +
        "   Unless required by applicable law or agreed to in writing, software\n" +
        "   distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
        "   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
        "   See the License for the specific language governing permissions and\n" +
        "   limitations under the License."
