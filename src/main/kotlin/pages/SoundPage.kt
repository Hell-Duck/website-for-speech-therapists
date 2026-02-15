package pages

import data.GameData
import kotlinx.html.*
import kotlinx.html.stream.*

fun soundPage(sound: String): String {
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
                    +"""
                        body {
                            font-family: Arial, sans-serif;
                            margin: 0;
                            padding: 20px;
                            background-color: #f5f5f5;
                        }
                        .container {
                            max-width: 800px;
                            margin: 0 auto;
                            background: white;
                            padding: 30px;
                            border-radius: 10px;
                            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                        }
                        .back-btn {
                            background: #6c757d;
                            color: white;
                            padding: 10px 20px;
                            border: none;
                            border-radius: 5px;
                            cursor: pointer;
                            margin-bottom: 20px;
                            text-decoration: none;
                            display: inline-block;
                        }
                        .back-btn:hover {
                            background: #5a6268;
                        }
                        .exercise-grid {
                            display: grid;
                            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
                            gap: 20px;
                            margin-top: 30px;
                        }
                        .exercise-card {
                            background: #fff;
                            border: 1px solid #ddd;
                            border-radius: 8px;
                            padding: 20px;
                            text-align: center;
                            transition: transform 0.2s;
                            min-height: 180px;
                            display: flex;
                            flex-direction: column;
                            justify-content: space-between;
                        }
                        .exercise-card:hover {
                            transform: translateY(-5px);
                            box-shadow: 0 5px 15px rgba(0,0,0,0.1);
                        }
                        h1 {
                            color: #333;
                            text-align: center;
                        }
                        .exercise-title {
                            color: #2c3e50;
                            margin-bottom: 10px;
                        }
                        .exercise-desc {
                            color: #666;
                            font-size: 14px;
                            line-height: 1.5;
                        }
                        .sound-label {
                            color: #4CAF50;
                            font-weight: bold;
                            margin-left: 5px;
                        }
                        .play-btn {
                            background: #4CAF50;
                            color: white;
                            padding: 8px 16px;
                            border: none;
                            border-radius: 5px;
                            cursor: pointer;
                            margin-top: 10px;
                            text-decoration: none;
                            display: inline-block;
                        }
                        .play-btn:hover {
                            background: #45a049;
                        }
                    """
                }
            }
        }
        body {
            div("container") {
                a("/", classes = "back-btn") { +"Назад к выбору звуков" }

                h1 {
                    +"Игры и упражнения для автоматизации звука "
                    span("sound-label") { +sound }
                }

                div("exercise-grid") {
                    exercises.forEach { (title, description) ->
                        div("exercise-card") {
                            h3("exercise-title") { +title }
                            p("exercise-desc") { +description }

                            if (sound == "P" && title.contains("Игра 'Найди слово'")) {
                                a("/game/word-find/r", classes = "play-btn") { +"Играть" }
                            }
                        }
                    }
                }
            }
        }
    }
}