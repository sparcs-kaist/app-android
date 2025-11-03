package com.example.soap.Domain.Models

enum class SearchScope(val id: String) {
    All("All") {
        override val description: String
            get() = "All"
    },
    Courses("Courses") {
        override val description: String
            get() = "Courses"
    },
    Posts("Posts") {
        override val description: String
            get() = "Posts"
    },
    Rides("Rides") {
        override val description: String
            get() = "Rides"
    };

    abstract val description: String

    companion object {
        fun allScopes(): Array<SearchScope> = values()
    }
}
