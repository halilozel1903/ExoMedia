/*
 * Copyright (C) 2017 - 2021 ExoMedia Contributors
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

package com.devbrackets.android.exomedia.core.source.builder

import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource

class SsMediaSourceBuilder : MediaSourceBuilder() {
    override fun build(attributes: MediaSourceAttributes): MediaSource {
        val factoryAttributes = attributes.copy(
            transferListener = null
        )

        val dataSourceFactory = buildDataSourceFactory(factoryAttributes)
        val meteredDataSourceFactory = buildDataSourceFactory(attributes)
        val mediaItem = MediaItem.Builder().setUri(attributes.uri).build()

        return SsMediaSource.Factory(
            DefaultSsChunkSource.Factory(meteredDataSourceFactory),
            dataSourceFactory
        )
            .setDrmSessionManagerProvider(attributes.drmSessionManagerProvider)
            .createMediaSource(mediaItem)
    }
}
