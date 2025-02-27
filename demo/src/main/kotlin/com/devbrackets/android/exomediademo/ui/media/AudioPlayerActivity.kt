package com.devbrackets.android.exomediademo.ui.media

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.SeekBar
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.devbrackets.android.exomedia.util.millisToFormattedDuration
import com.devbrackets.android.exomediademo.App
import com.devbrackets.android.exomediademo.R
import com.devbrackets.android.exomediademo.data.MediaItem
import com.devbrackets.android.exomediademo.data.Samples
import com.devbrackets.android.exomediademo.databinding.AudioPlayerActivityBinding
import com.devbrackets.android.exomediademo.playlist.manager.PlaylistManager
import com.devbrackets.android.exomediademo.ui.support.BindingActivity
import com.devbrackets.android.playlistcore.data.MediaProgress
import com.devbrackets.android.playlistcore.data.PlaybackState
import com.devbrackets.android.playlistcore.listener.PlaylistListener
import com.devbrackets.android.playlistcore.listener.ProgressListener

/**
 * An example activity to show how to implement and audio UI
 * that interacts with the [com.devbrackets.android.playlistcore.service.BasePlaylistService]
 * and [com.devbrackets.android.playlistcore.manager.ListPlaylistManager]
 * classes.
 */
class AudioPlayerActivity : BindingActivity<AudioPlayerActivityBinding>(),
    PlaylistListener<MediaItem>, ProgressListener {
    companion object {
        const val EXTRA_INDEX = "EXTRA_INDEX"
        const val PLAYLIST_ID = 4 //Arbitrary, for the example

        fun intent(context: Context, sample: Samples.Sample): Intent {
            // NOTE:
            // We pass the index of the sample for simplicity, however you will likely
            // want to pass an ID for both the selected playlist (audio/video in this demo)
            // and the selected media item
            val index = Samples.audio.indexOf(sample)

            return Intent(context, AudioPlayerActivity::class.java).apply {
                putExtra(EXTRA_INDEX, index)
            }
        }
    }

    private var shouldSetDuration: Boolean = false
    private var userInteracting: Boolean = false

    private lateinit var playlistManager: PlaylistManager
    private val selectedPosition by lazy { intent.extras?.getInt(EXTRA_INDEX, 0) ?: 0 }

    private val glide: RequestManager by lazy { Glide.with(this) }

    override fun inflateBinding(layoutInflater: LayoutInflater): AudioPlayerActivityBinding {
        return AudioPlayerActivityBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        init()
    }

    override fun onPause() {
        super.onPause()
        playlistManager.unRegisterPlaylistListener(this)
        playlistManager.unRegisterProgressListener(this)
    }

    override fun onResume() {
        super.onResume()
        playlistManager = (applicationContext as App).playlistManager
        playlistManager.registerPlaylistListener(this)
        playlistManager.registerProgressListener(this)

        //Makes sure to retrieve the current playback information
        updateCurrentPlaybackInformation()
    }

    override fun onPlaylistItemChanged(
        currentItem: MediaItem?,
        hasNext: Boolean,
        hasPrevious: Boolean
    ): Boolean {
        shouldSetDuration = true

        //Updates the button states
        binding.nextButton.isEnabled = hasNext
        binding.previousButton.isEnabled = hasPrevious

        //Loads the new image
        currentItem?.let {
            glide.load(it.artworkUrl).into(binding.artworkView)
        }

        return true
    }

    override fun onPlaybackStateChanged(playbackState: PlaybackState): Boolean {
        when (playbackState) {
            PlaybackState.STOPPED -> finish()
            PlaybackState.RETRIEVING, PlaybackState.PREPARING, PlaybackState.SEEKING -> restartLoading()
            PlaybackState.PLAYING -> doneLoading(true)
            PlaybackState.PAUSED -> doneLoading(false)
            else -> {}
        }

        return true
    }

    override fun onProgressUpdated(mediaProgress: MediaProgress): Boolean {
        if (shouldSetDuration && mediaProgress.duration > 0) {
            shouldSetDuration = false
            setDuration(mediaProgress.duration)
        }

        if (!userInteracting) {
            binding.seekBar.secondaryProgress =
                (mediaProgress.duration * mediaProgress.bufferPercentFloat).toInt()
            binding.seekBar.progress = mediaProgress.position.toInt()
            binding.currentPositionView.text = mediaProgress.position.millisToFormattedDuration()
        }

        return true
    }

    /**
     * Makes sure to update the UI to the current playback item.
     */
    private fun updateCurrentPlaybackInformation() {
        playlistManager.currentItemChange?.let {
            onPlaylistItemChanged(it.currentItem, it.hasNext, it.hasPrevious)
        }

        if (playlistManager.currentPlaybackState != PlaybackState.STOPPED) {
            onPlaybackStateChanged(playlistManager.currentPlaybackState)
        }

        playlistManager.currentProgress?.let {
            onProgressUpdated(it)
        }
    }

    /**
     * Performs the initialization of the views and any other
     * general setup
     */
    private fun init() {
        setupListeners()
        startPlayback(setupPlaylistManager())
    }


    /**
     * Called when we receive a notification that the current item is
     * done loading.  This will then update the view visibilities and
     * states accordingly.
     *
     * @param isPlaying True if the audio item is currently playing
     */
    private fun doneLoading(isPlaying: Boolean) {
        loadCompleted()
        updatePlayPauseImage(isPlaying)
    }

    /**
     * Updates the Play/Pause image to represent the correct playback state
     *
     * @param isPlaying True if the audio item is currently playing
     */
    private fun updatePlayPauseImage(isPlaying: Boolean) {
        val resId =
            if (isPlaying) R.drawable.playlistcore_ic_pause_black else R.drawable.playlistcore_ic_play_arrow_black
        binding.playPauseButton.setImageResource(resId)
    }

    /**
     * Used to inform the controls to finalize their setup.  This
     * means replacing the loading animation with the PlayPause button
     */
    private fun loadCompleted() {
        binding.playPauseButton.visibility = View.VISIBLE
        binding.previousButton.visibility = View.VISIBLE
        binding.nextButton.visibility = View.VISIBLE

        binding.loadingBar.visibility = View.INVISIBLE
    }

    /**
     * Used to inform the controls to return to the loading stage.
     * This is the opposite of [.loadCompleted]
     */
    private fun restartLoading() {
        binding.playPauseButton.visibility = View.INVISIBLE
        binding.previousButton.visibility = View.INVISIBLE
        binding.nextButton.visibility = View.INVISIBLE

        binding.loadingBar.visibility = View.VISIBLE
    }

    /**
     * Sets the [.seekBar]s max and updates the duration text
     *
     * @param duration The duration of the media item in milliseconds
     */
    private fun setDuration(duration: Long) {
        binding.seekBar.max = duration.toInt()
        binding.durationView.text = duration.millisToFormattedDuration()
    }

    /**
     * Retrieves the playlist instance and performs any generation
     * of content if it hasn't already been performed.
     *
     * @return True if the content was generated
     */
    private fun setupPlaylistManager(): Boolean {
        playlistManager = (applicationContext as App).playlistManager

        //There is nothing to do if the currently playing values are the same
        if (playlistManager.id == PLAYLIST_ID.toLong()) {
            return false
        }

        val mediaItems = Samples.audio.map {
            MediaItem(it, true)
        }

        playlistManager.setParameters(mediaItems, selectedPosition)
        playlistManager.id = PLAYLIST_ID.toLong()

        return true
    }

    /**
     * Links the SeekBarChanged to the [.seekBar] and
     * onClickListeners to the media buttons that call the appropriate
     * invoke methods in the [.playlistManager]
     */
    private fun setupListeners() {
        binding.seekBar.setOnSeekBarChangeListener(SeekBarChanged())
        binding.previousButton.setOnClickListener { playlistManager.invokePrevious() }
        binding.playPauseButton.setOnClickListener { playlistManager.invokePausePlay() }
        binding.nextButton.setOnClickListener { playlistManager.invokeNext() }
    }

    /**
     * Starts the audio playback if necessary.
     *
     * @param forceStart True if the audio should be started from the beginning even if it is currently playing
     */
    private fun startPlayback(forceStart: Boolean) {
        //If we are changing audio files, or we haven't played before then start the playback
        if (forceStart || playlistManager.currentPosition != selectedPosition) {
            playlistManager.currentPosition = selectedPosition
            playlistManager.play(0, false)
        }
    }

    /**
     * Listens to the seek bar change events and correctly handles the changes
     */
    private inner class SeekBarChanged : SeekBar.OnSeekBarChangeListener {
        private var seekPosition = -1

        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            if (!fromUser) {
                return
            }

            seekPosition = progress
            binding.currentPositionView.text = progress.toLong().millisToFormattedDuration()
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {
            userInteracting = true

            seekPosition = seekBar.progress
            playlistManager.invokeSeekStarted()
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            userInteracting = false

            playlistManager.invokeSeekEnded(seekPosition.toLong())
            seekPosition = -1
        }
    }
}
