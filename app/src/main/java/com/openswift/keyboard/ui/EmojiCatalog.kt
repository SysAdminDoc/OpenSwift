package com.openswift.keyboard.ui

data class EmojiEntry(
    val value: String,
    val category: String,
    val keywords: Set<String>
)

object EmojiCatalog {
    const val RECENTS = "Recent"
    const val FAVORITES = "Star"

    val categories = listOf(
        RECENTS,
        FAVORITES,
        "Smile",
        "Hand",
        "Heart",
        "Food",
        "Nature",
        "Travel",
        "Object",
        "Symbol"
    )

    val entries = listOf(
        e("😀", "Smile", "grin happy face smile"),
        e("😁", "Smile", "grin happy face teeth"),
        e("😂", "Smile", "laugh cry tears funny"),
        e("🤣", "Smile", "laugh rolling funny"),
        e("😊", "Smile", "smile blush happy"),
        e("😍", "Smile", "love eyes heart face"),
        e("😘", "Smile", "kiss love face"),
        e("😎", "Smile", "cool sunglasses face"),
        e("😢", "Smile", "sad cry tears face"),
        e("😡", "Smile", "angry mad face"),
        e("😴", "Smile", "sleep tired face"),
        e("🤔", "Smile", "think curious face"),
        e("👍", "Hand", "thumb up yes like hand"),
        e("👎", "Hand", "thumb down no dislike hand"),
        e("👌", "Hand", "ok hand"),
        e("👏", "Hand", "clap applause hand"),
        e("🙏", "Hand", "pray thanks please hand"),
        e("👋", "Hand", "wave hello bye hand"),
        e("✌️", "Hand", "peace victory hand"),
        e("🤝", "Hand", "handshake agree hand"),
        e("💪", "Hand", "strong flex arm"),
        e("🫶", "Hand", "heart hands love"),
        e("❤️", "Heart", "heart red love"),
        e("🧡", "Heart", "heart orange love"),
        e("💛", "Heart", "heart yellow love"),
        e("💚", "Heart", "heart green love"),
        e("💙", "Heart", "heart blue love"),
        e("💜", "Heart", "heart purple love"),
        e("🖤", "Heart", "heart black love"),
        e("🤍", "Heart", "heart white love"),
        e("💔", "Heart", "heart broken sad"),
        e("💕", "Heart", "heart two love"),
        e("🍎", "Food", "apple fruit food"),
        e("🍌", "Food", "banana fruit food"),
        e("🍓", "Food", "strawberry fruit food"),
        e("🍕", "Food", "pizza food"),
        e("🍔", "Food", "burger food"),
        e("🍟", "Food", "fries food"),
        e("🌮", "Food", "taco food"),
        e("🍣", "Food", "sushi food"),
        e("🍰", "Food", "cake dessert food"),
        e("☕", "Food", "coffee drink"),
        e("🐶", "Nature", "dog animal pet"),
        e("🐱", "Nature", "cat animal pet"),
        e("🦊", "Nature", "fox animal"),
        e("🐼", "Nature", "panda animal"),
        e("🌲", "Nature", "tree nature"),
        e("🌻", "Nature", "flower nature"),
        e("🔥", "Nature", "fire hot"),
        e("⭐", "Nature", "star favorite"),
        e("🌙", "Nature", "moon night"),
        e("☀️", "Nature", "sun sunny"),
        e("🚗", "Travel", "car travel"),
        e("🚕", "Travel", "taxi travel"),
        e("🚌", "Travel", "bus travel"),
        e("🚆", "Travel", "train travel"),
        e("✈️", "Travel", "plane travel"),
        e("🚀", "Travel", "rocket travel"),
        e("🏠", "Travel", "house home"),
        e("🏢", "Travel", "office building"),
        e("🌍", "Travel", "earth world globe"),
        e("🗺️", "Travel", "map travel"),
        e("📱", "Object", "phone mobile object"),
        e("💻", "Object", "laptop computer object"),
        e("⌨️", "Object", "keyboard object"),
        e("📷", "Object", "camera photo object"),
        e("🎁", "Object", "gift present object"),
        e("🎉", "Object", "party celebration object"),
        e("📌", "Object", "pin object"),
        e("📝", "Object", "note write object"),
        e("🔒", "Object", "lock private security"),
        e("🔑", "Object", "key security"),
        e("✅", "Symbol", "check yes done"),
        e("❌", "Symbol", "x no close"),
        e("⚠️", "Symbol", "warning alert"),
        e("❓", "Symbol", "question help"),
        e("❗", "Symbol", "exclamation important"),
        e("➕", "Symbol", "plus add"),
        e("➖", "Symbol", "minus remove"),
        e("➡️", "Symbol", "arrow right"),
        e("⬅️", "Symbol", "arrow left"),
        e("🔁", "Symbol", "repeat refresh")
    )

    val byValue = entries.associateBy { it.value }

    fun search(query: String): List<EmojiEntry> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return entries
        return entries.filter { entry ->
            entry.keywords.any { it.contains(q) } || entry.category.lowercase().contains(q)
        }
    }

    private fun e(value: String, category: String, keywords: String): EmojiEntry {
        return EmojiEntry(value, category, keywords.split(' ').filter { it.isNotBlank() }.toSet())
    }
}
