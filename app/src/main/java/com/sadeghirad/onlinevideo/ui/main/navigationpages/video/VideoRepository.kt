package com.sadeghirad.onlinevideo.ui.main.navigationpages.video

import com.sadeghirad.onlinevideo.http.IApi
import com.sadeghirad.onlinevideo.http.apimodel.Video
import io.reactivex.Maybe



class VideoRepository(private val apiService: IApi) :
    IVideoRepository {
    override fun getShortClips(): Maybe<Video> {

        return apiService.getShortClips()

    }

}