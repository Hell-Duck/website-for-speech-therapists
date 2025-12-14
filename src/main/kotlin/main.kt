import io.ktor.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.routing.*
import io.ktor.html.*
import kotlinx.html.*

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    routing {
        get("/") {
            call.respondHtml {
                head {
                    title { +"Логопедический помощник" }
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
                                .welcome {
                                    text-align: center;
                                    margin-bottom: 30px;
                                }
                                .sound-list {
                                    list-style: none;
                                    padding: 0;
                                }
                                .sound-list li {
                                    margin: 10px 0;
                                }
                                .sound-list a {
                                    display: block;
                                    padding: 15px;
                                    background: #4CAF50;
                                    color: white;
                                    text-decoration: none;
                                    border-radius: 5px;
                                    text-align: center;
                                    font-size: 18px;
                                }
                                .sound-list a:hover {
                                    background: #45a049;
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
                                }
                                .exercise-card:hover {
                                    transform: translateY(-5px);
                                    box-shadow: 0 5px 15px rgba(0,0,0,0.1);
                                }
                            """
                        }
                    }
                }
                body {
                    div("container") {
                        div("welcome") {
                            h1 { +"Здравствуйте!" }
                            p { +"Я Ваш помощник по автоматизации звуков!" }
                            p { +"Выберите звук:" }

                            ul("sound-list") {
                                li {
                                    a("/sound/P") { +"Звук 'Р'" }
                                }
                                li {
                                    a("/sound/Sh") { +"Звук 'Ш'" }
                                }
                                li {
                                    a("/sound/L") { +"Звук 'Л'" }
                                }
                                li {
                                    a("/sound/X") { +"Звук 'Х'" }
                                }
                            }
                        }
                    }
                }
            }
        }

        get("/sound/{name}") {
            val sound = call.parameters["name"] ?: "P"

            // Определяем упражнения в зависимости от выбранного звука
            val exercises = when (sound) {
                "P" -> listOf(
                    "Скороговорки с 'Р'" to "Повторяйте: 'Рыба рыбачит, рак ракушку ищет'",
                    "Слова с 'Р' в начале" to "Произносите: рак, роза, рука, река",
                    "Слоги с 'Р'" to "Тренируйте: ра-ро-ру-ры, ар-ор-ур-ыр",
                    "Игра 'Найди слово'" to "Найдите все предметы в комнате, где есть звук 'Р'"
                )
                "Sh" -> listOf(
                    "Шипящие слова" to "Произносите: шар, шуба, машина, каштан",
                    "Скороговорки с 'Ш'" to "'Шла Саша по шоссе и сосала сушку'",
                    "Шепотом и громко" to "Произнесите слова с 'Ш' шепотом и громко",
                    "Игра 'Тихий ветер'" to "Произносите 'Ш-ш-ш' как тихий ветер"
                )
                "L" -> listOf(
                    "Ла-ла-ла" to "Пропевайте: ла-ло-лу-лы, ал-ол-ул-ыл",
                    "Слова с 'Л' в слогах" to "Произносите: лампа, лодка, луна, ложка",
                    "Скороговорки с 'Л'" to "'Лавина ласково ласкала лаванду'",
                    "Игра 'Веселый язычок'" to "Прижмите язык к небу и произнесите 'Л'"
                )
                "X" -> listOf(
                    "Хлопки с 'Х'" to "На каждый 'Х' в слове делайте хлопок",
                    "Дыхательные упражнения" to "Произносите 'Ха-ха-ха' с сильным выдохом",
                    "Слова с 'Х'" to "Хлеб, хомяк, халат, хижина",
                    "Игра 'Смешной смех'" to "Смейтесь с разной интонацией: 'Хи-хи-хи', 'Ха-ха-ха'"
                )
                else -> listOf(
                    "Базовые упражнения" to "Повторение звука изолированно",
                    "Слоги" to "Сочетание звука с гласными",
                    "Слова" to "Слова с целевым звуком в разных позициях",
                    "Фразы" to "Короткие фразы и предложения"
                )
            }

            call.respondHtml {
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
                            """
                        }
                    }
                }
                body {
                    div("container") {
                        a("/", classes = "back-btn") {
                            +"Назад к выбору звуков"
                        }

                        h1 {
                            +"Игры и упражнения для автоматизации звука "
                            span("sound-label") { +"$sound" }
                        }

                        div("exercise-grid") {
                            exercises.forEach { (title, description) ->
                                div("exercise-card") {
                                    h3("exercise-title") { +title }
                                    p("exercise-desc") { +description }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}