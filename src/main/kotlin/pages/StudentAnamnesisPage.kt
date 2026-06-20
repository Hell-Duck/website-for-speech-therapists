package pages

import models.StudentProfile
import models.User
import models.UserSession
import kotlinx.html.*
import kotlinx.html.stream.createHTML

fun studentAnamnesisPage(
    session: UserSession,
    user: User,
    profile: StudentProfile? = null,
    success: Boolean = false,
    error: String? = null
): String = createHTML().html {
    head {
        title { +"Анкета ребёнка" }
        style { unsafe { +commonStyles() } }
    }
    body {
        navbar(session)
        div("container") {
            div {
                attributes["style"] = "display:flex; justify-content:space-between; align-items:center; margin-bottom:8px;"
                h2 { attributes["style"] = "margin:0; color:#333;"; +"Анкета ребёнка" }
                a("/dashboard") { attributes["class"] = "btn btn-secondary"; +"← Назад" }
            }
            p {
                attributes["style"] = "color:#888; font-size:14px; margin-bottom:24px;"
                +"Данные анкеты используются логопедом при составлении плана коррекции."
            }

            if (success) { div("alert alert-success") { +"Анкета сохранена." } }
            if (error != null) { div("alert alert-error") { +error } }

            form(method = FormMethod.post, action = "/student/anamnesis") {

                // Раздел I
                anamnesisSection("I. Сведения о родителях") {
                    div("grid-2") {
                        anamnesisField("Мать — ФИО", "motherFullName", profile?.motherFullName)
                        anamnesisField("Мать — возраст", "motherAge", profile?.motherAge)
                        anamnesisField("Отец — ФИО", "fatherFullName", profile?.fatherFullName)
                        anamnesisField("Отец — возраст", "fatherAge", profile?.fatherAge)
                    }
                    anamnesisField(
                        "Какой по счёту ребёнок и от каких родов?",
                        "pregnancyNumber", profile?.pregnancyNumber
                    )
                }

                // Раздел II
                anamnesisSection("II. Перинатальный анамнез") {
                    anamnesisField(
                        "2. Характер беременности (токсикоз, падения, хронические заболевания, инфекционные заболевания)",
                        "pregnancyCharacter", profile?.pregnancyCharacter, textarea = true
                    )
                    anamnesisField(
                        "3. Роды — досрочные, срочные, стремительные, обезвоженные",
                        "birthType", profile?.birthType
                    )
                    anamnesisField(
                        "4. Стимуляция — механическая, химическая, электростимуляция",
                        "birthStimulation", profile?.birthStimulation
                    )
                    anamnesisField("5. Когда закричал", "firstCryTime", profile?.firstCryTime)
                    anamnesisField("6. Асфиксия — белая, синяя", "asphyxia", profile?.asphyxia)
                    anamnesisField("7. Резус-фактор (совместимость)", "rhesusFactor", profile?.rhesusFactor)
                    div("grid-2") {
                        anamnesisField("8. Вес при рождении", "birthWeight", profile?.birthWeight)
                        anamnesisField("Рост при рождении", "birthHeight", profile?.birthHeight)
                    }
                }

                // Раздел III
                anamnesisSection("III. Вскармливание и ранний период") {
                    anamnesisField(
                        "9а. Когда принесли кормить — сразу, через несколько дней...",
                        "feedingStart", profile?.feedingStart, textarea = true
                    )
                    anamnesisField("9б. Как взял грудь", "breastFeeding", profile?.breastFeeding)
                    anamnesisField("9в. Как сосал", "suckingCharacter", profile?.suckingCharacter)
                    anamnesisField(
                        "9г. Наблюдались ли срыгивания, поперхивания",
                        "regurgitation", profile?.regurgitation
                    )
                    anamnesisField(
                        "10. На какой день выписался из роддома; если задержался — почему",
                        "dischargeDay", profile?.dischargeDay
                    )
                }

                // Раздел IV
                anamnesisSection("IV. Психомоторное развитие") {
                    div("grid-2") {
                        anamnesisField("11. Голову держит с", "headsUp", profile?.headsUp)
                        anamnesisField("12. Сидит с", "sitting", profile?.sitting)
                        anamnesisField("13. Стоит с", "standing", profile?.standing)
                        anamnesisField("14. Ходит с", "walking", profile?.walking)
                        anamnesisField("15. Первые зубы", "firstTeeth", profile?.firstTeeth)
                    }
                }

                // Раздел V
                anamnesisSection("V. Перенесённые заболевания") {
                    anamnesisField("16а. До года", "illnessesBeforeYear", profile?.illnessesBeforeYear, textarea = true)
                    anamnesisField("16б. После года", "illnessesAfterYear", profile?.illnessesAfterYear, textarea = true)
                    anamnesisField("16в. Инфекции", "infections", profile?.infections, textarea = true)
                    anamnesisField("16г. Ушибы, травмы головы", "headInjuries", profile?.headInjuries)
                    anamnesisField("Судороги при высокой температуре", "convulsions", profile?.convulsions)
                }

                // Раздел VI
                anamnesisSection("VI. Речевое развитие") {
                    p {
                        attributes["style"] = "color:#888; font-size:13px; margin-bottom:12px;"
                        +"17. Во сколько месяцев у ребёнка начали появляться:"
                    }
                    div("grid-2") {
                        anamnesisField("17а. Гуление", "cooing", profile?.cooing)
                        anamnesisField("17б. Лепет", "babbling", profile?.babbling)
                        anamnesisField("17в. Первые слова", "firstWords", profile?.firstWords)
                        anamnesisField("17г. Первые фразы", "firstPhrases", profile?.firstPhrases)
                    }
                    anamnesisField("18. Прерывалось ли речевое развитие", "speechInterruption", profile?.speechInterruption, textarea = true)
                    anamnesisField("19. Речевая среда", "speechEnvironment", profile?.speechEnvironment, textarea = true)
                    anamnesisField("20. Занимался ли с логопедом ребёнок раньше", "previousSpeechTherapy", profile?.previousSpeechTherapy)
                    anamnesisField("21. Отношение ребёнка к своей речи", "attitudeToSpeech", profile?.attitudeToSpeech)
                }

                div {
                    attributes["style"] = "margin-top:8px;"
                    button(type = ButtonType.submit) {
                        attributes["class"] = "btn btn-primary"
                        +"Сохранить анкету"
                    }
                }
            }
        }
    }
}

private fun FlowContent.anamnesisSection(title: String, content: FlowContent.() -> Unit) {
    div("card") {
        div("section-title") { +title }
        content()
    }
}

private fun FlowContent.anamnesisField(
    label: String,
    name: String,
    value: String?,
    textarea: Boolean = false
) {
    div("form-group") {
        label { +label }
        if (textarea) {
            textArea {
                this.name = name
                attributes["class"] = "form-control"
                attributes["rows"] = "3"
                +(value ?: "")
            }
        } else {
            input(type = InputType.text, name = name) {
                attributes["class"] = "form-control"
                attributes["value"] = value ?: ""
            }
        }
    }
}
