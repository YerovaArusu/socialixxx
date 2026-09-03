package at.yerova.socialixxx

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform