package com.app.binged.navigation

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
}
