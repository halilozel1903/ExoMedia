/*
 * Copyright (C) 2016 - 2017 ExoMedia Contributors,
 * Copyright (C) 2014 The Android Open Source Project
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

package com.devbrackets.android.exomedia.core.listener

import com.google.android.exoplayer2.metadata.Metadata

/**
 * A repeatListener for receiving ID3 metadata parsed from the media stream.
 */
interface MetadataListener {
    /**
     * Called each time there is a metadata associated with current playback time.
     *
     * @param metadata The metadata.
     */
    fun onMetadata(metadata: Metadata)
}
