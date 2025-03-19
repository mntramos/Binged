package com.app.binged.navigation

sealed class Route(val path: String) {

    data object ShowList : Route("show_list")

    data object ShowDetail : Route("show_detail/{showId}") {
        fun createRoute(showId: Int) = "show_detail/$showId"
    }

    data object LogEpisode : Route("log_episode/{showId}") {
        fun createRoute(showId: Int) = "log_episode/$showId"
    }

    data object Search : Route("search")
}
