package data

object ExerciseHelper {

    val soundNames = linkedMapOf(
        "P"  to "Звук «Р»",
        "Sh" to "Звук «Ш»",
        "L"  to "Звук «Л»",
        "X"  to "Звук «Х»"
    )

    /** "P:0" → "Скороговорки с 'Р'" */
    fun resolveName(key: String): String {
        val p = key.split(":")
        if (p.size != 2) return key
        val index = p[1].toIntOrNull() ?: return key
        return GameData.exercises[p[0]]?.getOrNull(index)?.first ?: key
    }

    /** "P:0" → "Звук «Р»: Скороговорки с 'Р'" */
    fun resolveFull(key: String): String {
        val p = key.split(":")
        if (p.size != 2) return key
        val soundName = soundNames[p[0]] ?: p[0]
        return "$soundName: ${resolveName(key)}"
    }

    /** "2024-03-15" → "15.03.2024" */
    fun formatDate(iso: String): String {
        val p = iso.split("-")
        return if (p.size == 3) "${p[2]}.${p[1]}.${p[0]}" else iso
    }

    fun ratingLabel(r: Int) = when (r) {
        1 -> "Без прогресса"
        2 -> "Слабый прогресс"
        3 -> "Заметный прогресс"
        4 -> "Хороший прогресс"
        5 -> "Отличный прогресс"
        else -> "$r/5"
    }

    fun ratingEmoji(r: Int) = when (r) {
        1 -> "😔"; 2 -> "😐"; 3 -> "🙂"; 4 -> "😊"; 5 -> "🌟"; else -> "•"
    }

    /**
     * Возвращает URL упражнения для прямого перехода.
     * Для игр — страница игры, для остальных — якорь на карточке упражнения.
     */
    fun resolveUrl(key: String): String {
        val p = key.split(":")
        if (p.size != 2) return "#"
        val soundKey = p[0]
        val index    = p[1].toIntOrNull() ?: return "#"
        val title    = GameData.exercises[soundKey]?.getOrNull(index)?.first ?: return "#"
        return when {
            soundKey == "P"  && title.contains("Игра") -> "/game/word-find/r"
            else -> "/sound/$soundKey#exercise-$soundKey-$index"
        }
    }
}
