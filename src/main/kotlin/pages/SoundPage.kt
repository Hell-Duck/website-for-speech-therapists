package pages

import data.GameData
import models.UserSession
import kotlinx.html.*
import kotlinx.html.stream.*

fun soundPage(sound: String, session: UserSession): String {
    val exercises = GameData.exercises[sound] ?: listOf(
        "Базовые упражнения" to "Повторение звука изолированно",
        "Слоги" to "Сочетание звука с гласными",
        "Слова" to "Слова с целевым звуком в разных позициях",
        "Фразы" to "Короткие фразы и предложения"
    )

    return createHTML().html {
        head {
            title { +"Упражнения для $sound" }
            style {
                unsafe {
                    +commonStyles()
                    +"""
                    .exercise-grid {
                        display: grid;
                        grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
                        gap: 20px;
                        margin-top: 24px;
                    }
                    .exercise-card {
                        background: white;
                        border: 1px solid #e0e0e0;
                        border-radius: 10px;
                        padding: 20px;
                        display: flex;
                        flex-direction: column;
                        justify-content: space-between;
                        min-height: 160px;
                        transition: transform 0.2s, box-shadow 0.2s;
                        scroll-margin-top: 80px;
                    }
                    .exercise-card:hover {
                        transform: translateY(-4px);
                        box-shadow: 0 6px 20px rgba(0,0,0,0.1);
                    }
                    .exercise-card:target {
                        animation: exercisePulse 2s ease-out forwards;
                        border-color: #4CAF50;
                    }
                    @keyframes exercisePulse {
                        0%   { box-shadow: 0 0 0 4px rgba(76,175,80,0.5); background: #f1f8e9; }
                        70%  { box-shadow: 0 0 0 2px rgba(76,175,80,0.2); background: #f9fbe7; }
                        100% { box-shadow: 0 6px 20px rgba(0,0,0,0.1); background: white; }
                    }
                    .exercise-title { color: #2c3e50; font-size: 16px; font-weight: bold; margin-bottom: 10px; }
                    .exercise-desc { color: #666; font-size: 14px; line-height: 1.6; flex-grow: 1; }
                    .sound-label { color: #4CAF50; font-weight: bold; }
                    """
                }
            }
        }
        body {
            navbar(session)
            div("container") {
                div {
                    attributes["style"] = "display: flex; align-items: center; gap: 16px; margin-bottom: 8px;"
                    a("/sounds") {
                        attributes["class"] = "btn btn-secondary btn-sm"
                        +"← Назад"
                    }
                    h2 {
                        attributes["style"] = "margin: 0; color: #333;"
                        +"Упражнения для звука "
                        span("sound-label") { +sound }
                    }
                }
                div("exercise-grid") {
                    exercises.forEachIndexed { index, (title, description) ->
                        div("exercise-card") {
                            id = "exercise-$sound-$index"
                            div("exercise-title") { +title }
                            p("exercise-desc") { +description }
                            if (sound == "P" && title.contains("Игра")) {
                                a("/game/word-find/r") {
                                    attributes["class"] = "btn btn-primary"
                                    attributes["style"] = "margin-top: 12px; align-self: flex-start;"
                                    +"Играть"
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
