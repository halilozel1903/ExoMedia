/*
 * Copyright (C) 2016 ExoMedia Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.devbrackets.android.exomedia.ui.listener

/**
 * Interface definition for a callback to be invoked when the visibility
 * of the [com.devbrackets.android.exomedia.ui.widget.DefaultVideoControls] changes
 */
interface VideoControlsVisibilityListener {
    /**
     * Occurs when the DefaultControls becomes visible
     */
    fun onControlsShown()

    /**
     * Occurs when the DefaultControls becomes hidden
     */
    fun onControlsHidden()
}
