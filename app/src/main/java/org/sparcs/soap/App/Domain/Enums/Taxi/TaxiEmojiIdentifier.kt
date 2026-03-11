package org.sparcs.soap.App.Domain.Enums.Taxi

enum class EmojiIdentifier(val id: String, val display: String) {
    APPLE("apple", "🍎"),
    ORANGE("orange", "🍊"),
    LEMON("lemon", "🍋"),
    WATERMELON("watermelon", "🍉"),
    GRAPE("grape", "🍇"),
    STRAWBERRY("strawberry", "🍓"),
    CHERRY("cherry", "🍒"),
    PINEAPPLE("pineapple", "🍍"),
    KIWI("kiwi", "🥝"),
    COCONUT("coconut", "🥥"),
    PEACH("peach", "🍑"),
    BANANA("banana", "🍌"),
    CARROT("carrot", "🥕"),
    CORN("corn", "🌽"),
    BROCCOLI("broccoli", "🥦"),
    MUSHROOM("mushroom", "🍄"),
    UNKNOWN("unknown", "❓");

    companion object {
        fun fromRawValue(rawValue: String?): EmojiIdentifier {
            return entries.find { it.id == rawValue } ?: UNKNOWN
        }
    }
}