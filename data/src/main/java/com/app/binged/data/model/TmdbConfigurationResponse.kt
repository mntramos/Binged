package com.app.binged.data.model

import com.google.gson.annotations.SerializedName

/**
 * Only used to prove that a credential is accepted by TMDB. Gson ignores the
 * rest of the payload, and the endpoint itself is cheap.
 */
data class TmdbConfigurationResponse(
    @SerializedName("change_keys")
    val changeKeys: List<String> = emptyList()
)
