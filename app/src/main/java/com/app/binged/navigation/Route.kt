package com.app.binged.navigation

import android.net.Uri

sealed class Route(val path: String) {

    data object ShowList : Route("show_list")

    data object ShowDetail : Route("show_detail/{showId}?fromSearch={fromSearch}") {
        fun createRoute(showId: Int, fromSearch: Boolean = false) =
            "show_detail/$showId?fromSearch=$fromSearch"
    }

    data object LogEpisode : Route("log_episode/{showId}") {
        fun createRoute(showId: Int) = "log_episode/$showId"
    }

    data object Search : Route("search")

    data object Diary : Route("diary")

    data object EpisodeDetail : Route("episode_detail/{showId}/{season}/{episode}?showName={showName}") {
        fun createRoute(showId: Int, season: Int, episode: Int, showName: String = "") =
            "episode_detail/$showId/$season/$episode?showName=${Uri.encode(showName)}"
    }

    data object Settings : Route("settings")

    data object Statistics : Route("statistics")

    data object Tutorial : Route("tutorial")
}
