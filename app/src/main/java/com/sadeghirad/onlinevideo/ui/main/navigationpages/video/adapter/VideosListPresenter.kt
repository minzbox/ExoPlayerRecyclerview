package com.sadeghirad.onlinevideo.ui.main.navigationpages.video.adapter

import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.sadeghirad.onlinevideo.http.apimodel.Clip
import com.sadeghirad.onlinevideo.http.apimodel.Video
import com.sadeghirad.onlinevideo.player.ExoPlayerViewManager


class VideosListPresenter(private var videos: Video?) :
    VideosListMVP.Presenter {

    private lateinit var currentClip: Clip
    private lateinit var currentHolder: VideosListAdapter.VideosViewHolder
    private var currentPosition = 0
    private var currentFirstVisibleItemPosition = 0
    private var currentLastVisibleItemPosition = 0

    override fun setView(holder: VideosListAdapter.VideosViewHolder) {
//        this.holder = holder
    }

    override fun handleItemViewClick(holder: VideosListAdapter.VideosViewHolder, position: Int) {
        val url = getVideoUrlByPosition(position)
        val clip = getClipModelByPosition(position)

        if (url != null) {
            holder.makeVideoPlayerInstance(clip)
            setCurrentHolderAndClip(clip, holder)
            play(holder, clip, position)
        }
    }

    override fun handlePlayerFullScreenClick(
        holder: VideosListAdapter.VideosViewHolder,
        position: Int
    ) {
        val url = getVideoUrlByPosition(position)
        val clip = getClipModelByPosition(position)
        currentPosition = position

        if (url != null) {
            setCurrentHolderAndClip(clip, holder)
            holder.showFullScreenActivity(clip.source!!)
        }
    }

    override fun deactivate() {
    }

    override fun play(holder: VideosListAdapter.VideosViewHolder, clip: Clip, position: Int) {

        val instance = ExoPlayerViewManager.getInstance(clip.source!!)
        instance?.play((object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    ExoPlayer.STATE_READY -> {
                        clip.showLoading = false
                        clip.showVideoAndHideThumbnail = true
                    }

                    ExoPlayer.STATE_ENDED -> {
                        clip.showVideoAndHideThumbnail = false
                    }

                    ExoPlayer.STATE_BUFFERING -> {
                        clip.showLoading = true
                    }
                }

                onBindVideoRow(holder, position)
            }

            override fun onPlayerError(error: ExoPlaybackException?) {

            }
        }))

    }

    override fun resumeCurrentVideoOnBackFromFullScreen() {
        if (::currentHolder.isInitialized && ::currentClip.isInitialized)
            currentHolder.makeVideoPlayerInstance(currentClip)
    }

    override fun releaseAllPlayers() {
        val instances = ExoPlayerViewManager.instances
        for (url in instances.keys)
            instances[url]?.releaseVideoPlayer()
    }

    override fun pauseAllPlayers() {
        val instances = ExoPlayerViewManager.instances
        for (url in instances.keys)
            instances[url]?.pausePlayer()
    }


    override fun onBindVideoRow(holder: VideosListAdapter.VideosViewHolder, position: Int) {
        val currentClip = getClipModelByPosition(position)
        holder.setThumbnailImage(currentClip.thumb!!)
        holder.setVideoTitle(currentClip.title!!)


        if (currentClip.showVideoAndHideThumbnail)
            holder.showVideoAndHideThumbnail()
        else
            holder.showThumbnailAndHideVideo()


        if (currentClip.showLoading)
            holder.showLoading()
        else
            holder.hideLoading()
    }

    override fun getItemCount(): Int {
        return videos?.clips!!.size
    }

    override fun setAdapterData(videos: Video?) {
        this.videos = videos!!
    }

    override fun setClip(clipModel: Clip) {
        currentClip = clipModel
    }

    override fun setHolder(holder: VideosListAdapter.VideosViewHolder) {
        currentHolder = holder
    }

    override fun setCurrentHolderAndClip(
        clipModel: Clip,
        holder: VideosListAdapter.VideosViewHolder
    ) {
        setClip(clipModel)
        setHolder(holder)
    }

    override fun getClipModelByPosition(position: Int): Clip {
        return videos?.clips?.get(position)!!
    }

    override fun getVideoUrlByPosition(position: Int): String? {
        return getClipModelByPosition(position).source
    }

    override fun handlePauseCondition(
        dy: Int,
        firstVisiblePosition: Int,
        lastVisiblePosition: Int
    ) {
        val isScrollingUp = dy > 0
        if (isScrollingUp) {
            if (firstVisiblePosition != currentFirstVisibleItemPosition) {
                pauseOutOfScreenVideo(currentFirstVisibleItemPosition)
            }
        } else {
            if (lastVisiblePosition != currentLastVisibleItemPosition) {
                pauseOutOfScreenVideo(currentLastVisibleItemPosition)
            }
        }
        currentFirstVisibleItemPosition = firstVisiblePosition
        currentLastVisibleItemPosition = lastVisiblePosition
    }

    override fun pauseOutOfScreenVideo(position: Int) {
        val url = getVideoUrlByPosition(position)
        if (url != null) {
            val currentInstance = ExoPlayerViewManager.getInstance(url)
            if (currentInstance != null)
                if (currentInstance.isPlaying())
                    currentInstance.pausePlayer()
        }
    }
}
