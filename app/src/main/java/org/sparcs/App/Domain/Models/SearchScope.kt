package org.sparcs.App.Domain.Models

import androidx.annotation.StringRes
import org.sparcs.R

enum class SearchScope(val id: String, @StringRes val labelRes: Int) {
    All("All", R.string.all),
    Courses("Courses", R.string.courses),
    Posts("Posts", R.string.posts),
    Rides("Rides", R.string.rides);

    companion object {
        fun allScopes(): Array<SearchScope> = entries.toTypedArray()
    }
}
