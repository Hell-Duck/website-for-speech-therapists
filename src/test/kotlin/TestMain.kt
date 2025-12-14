package com.logoped

import io.ktor.server.testing.*
import kotlin.test.*
import io.ktor.http.*
import io.ktor.application.*
import module

class ApplicationTest {
    @Test
    fun testRootPage() {
        withTestApplication(Application::module) {
            // Тест 1: Проверка главной страницы
            handleRequest(HttpMethod.Get, "/").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(response.content!!.contains("Логопедический помощник"))
                assertTrue(response.content!!.contains("Выберите звук:"))
                assertTrue(response.content!!.contains("Звук 'Р'"))
                assertTrue(response.content!!.contains("Звук 'Ш'"))
                assertTrue(response.content!!.contains("Звук 'Л'"))
                assertTrue(response.content!!.contains("Звук 'Х'"))
            }
        }
    }

    @Test
    fun testSoundPageForP() {
        withTestApplication(Application::module) {
            // Тест 2: Проверка страницы звука "Р"
            handleRequest(HttpMethod.Get, "/sound/P").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(response.content!!.contains("Упражнения для P"))
                assertTrue(response.content!!.contains("Скороговорки с 'Р'"))
                assertTrue(response.content!!.contains("Слова с 'Р' в начале"))
                assertTrue(response.content!!.contains("Повторяйте: 'Рыба рыбачит, рак ракушку ищет'"))
            }
        }
    }

    @Test
    fun testSoundPageForSh() {
        withTestApplication(Application::module) {
            // Тест 3: Проверка страницы звука "Ш"
            handleRequest(HttpMethod.Get, "/sound/Sh").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(response.content!!.contains("Упражнения для Sh"))
                assertTrue(response.content!!.contains("Шипящие слова"))
                assertTrue(response.content!!.contains("Шла Саша по шоссе и сосала сушку"))
                assertTrue(response.content!!.contains("Игра 'Тихий ветер'"))
            }
        }
    }

    @Test
    fun testSoundPageForL() {
        withTestApplication(Application::module) {
            // Тест 4: Проверка страницы звука "Л"
            handleRequest(HttpMethod.Get, "/sound/L").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(response.content!!.contains("Упражнения для L"))
                assertTrue(response.content!!.contains("Ла-ла-ла"))
                assertTrue(response.content!!.contains("Слова с 'Л' в слогах"))
                assertTrue(response.content!!.contains("Прижмите язык к небу и произнесите 'Л'"))
            }
        }
    }

    @Test
    fun testSoundPageForX() {
        withTestApplication(Application::module) {
            // Тест 5: Проверка страницы звука "Х"
            handleRequest(HttpMethod.Get, "/sound/X").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(response.content!!.contains("Упражнения для X"))
                assertTrue(response.content!!.contains("Хлопки с 'Х'"))
                assertTrue(response.content!!.contains("Дыхательные упражнения"))
                assertTrue(response.content!!.contains("Хлеб, хомяк, халат, хижина"))
            }
        }
    }

    @Test
    fun testAllSoundPagesWithCorrectExercises() {
        withTestApplication(Application::module) {
            // Тест 6: Проверка что для каждого звука свои упражнения
            val soundExercises = mapOf(
                "P" to listOf("Скороговорки с 'Р'", "Слова с 'Р' в начале"),
                "Sh" to listOf("Шипящие слова", "Скороговорки с 'Ш'"),
                "L" to listOf("Ла-ла-ла", "Слова с 'Л' в слогах"),
                "X" to listOf("Хлопки с 'Х'", "Дыхательные упражнения")
            )

            soundExercises.forEach { (sound, expectedExercises) ->
                handleRequest(HttpMethod.Get, "/sound/$sound").apply {
                    assertEquals(HttpStatusCode.OK, response.status())
                    val content = response.content!!
                    expectedExercises.forEach { exercise ->
                        assertTrue(
                            content.contains(exercise),
                            "Страница звука $sound должна содержать упражнение '$exercise'"
                        )
                    }
                }
            }
        }
    }

    @Test
    fun testExerciseCardsStructure() {
        withTestApplication(Application::module) {
            // Тест 7: Проверка структуры карточек упражнений
            handleRequest(HttpMethod.Get, "/sound/P").apply {
                val html = response.content!!
                assertTrue(html.contains("exercise-card"), "Должны быть карточки упражнений")
                assertTrue(html.contains("exercise-title"), "Должны быть заголовки упражнений")
                assertTrue(html.contains("exercise-desc"), "Должны быть описания упражнений")
                assertTrue(html.contains("exercise-grid"), "Должна быть сетка упражнений")
            }
        }
    }

    @Test
    fun testBackButtonNavigation() {
        withTestApplication(Application::module) {
            // Тест 8: Проверка навигации назад
            handleRequest(HttpMethod.Get, "/sound/L").apply {
                val html = response.content!!
                assertTrue(html.contains("href=\"/\""), "Должна быть ссылка на главную страницу")
                assertTrue(html.contains("Назад к выбору звуков"), "Должна быть кнопка 'Назад'")
                assertTrue(html.contains("back-btn"), "Должна быть кнопка с классом 'back-btn'")
            }
        }
    }

    @Test
    fun testDefaultSoundPage() {
        withTestApplication(Application::module) {
            // Тест 9: Проверка страницы с неизвестным звуком
            handleRequest(HttpMethod.Get, "/sound/Unknown").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(response.content!!.contains("Упражнения для Unknown"))
                assertTrue(response.content!!.contains("Базовые упражнения"))
                assertTrue(response.content!!.contains("Слова с целевым звуком в разных позициях"))
            }
        }
    }

    @Test
    fun testSoundLabelStyling() {
        withTestApplication(Application::module) {
            // Тест 10: Проверка стилизации метки звука
            handleRequest(HttpMethod.Get, "/sound/Sh").apply {
                val html = response.content!!
                assertTrue(html.contains("sound-label"), "Должен быть стилизованный лейбл звука")
                assertTrue(html.contains("<span"), "Должен быть span для лейбла")
                assertTrue(html.contains("color: #4CAF50"), "Лейбл должен иметь зелёный цвет")
            }
        }
    }

    @Test
    fun testNotFoundPage() {
        withTestApplication(Application::module) {
            // Тест 11: Проверка несуществующей страницы
            handleRequest(HttpMethod.Get, "/notfound").apply {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }
        }
    }

    @Test
    fun testSoundPageHasFourExercises() {
        withTestApplication(Application::module) {
            listOf("P", "Sh", "L", "X").forEach { sound ->
                handleRequest(HttpMethod.Get, "/sound/$sound").apply {
                    val content = response.content!!
                    val pattern = "<div.*?class=.*?exercise-card.*?>"
                    val regex = Regex(pattern, RegexOption.IGNORE_CASE)
                    val matches = regex.findAll(content).toList()
                    val exerciseCount = matches.size

                    assertEquals(4, exerciseCount, "Страница звука $sound должна содержать 4 упражнения")
                }
            }
        }
    }
}