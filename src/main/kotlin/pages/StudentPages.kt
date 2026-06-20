package pages

import models.*
import kotlinx.html.*
import kotlinx.html.stream.createHTML

fun studentListPage(
    session: UserSession,
    views: List<StudentCardView>,
    search: String = "",
    statusFilter: String = "ALL"
): String = createHTML().html {
    head {
        title { +"Ученики" }
        style { unsafe { +commonStyles() } }
    }
    body {
        navbar(session)
        div("container") {
            div {
                attributes["style"] = "display:flex; justify-content:space-between; align-items:center; margin-bottom:24px;"
                h2 { attributes["style"] = "margin:0; color:#333;"; +"Ученики" }
                div("actions") {
                    a("/dashboard") { attributes["class"] = "btn btn-secondary"; +"← Назад" }
                    a("/students/create") { attributes["class"] = "btn btn-primary"; +"+ Добавить карточку" }
                }
            }

            div("card") {
                attributes["style"] = "padding:20px;"
                form(method = FormMethod.get, action = "/students") {
                    div {
                        attributes["style"] = "display:flex; gap:12px; flex-wrap:wrap; align-items:flex-end;"
                        div("form-group") {
                            attributes["style"] = "margin:0; flex:1; min-width:200px;"
                            label { +"Поиск по ФИО" }
                            input(type = InputType.text, name = "search") {
                                attributes["class"] = "form-control"
                                attributes["placeholder"] = "Введите ФИО..."
                                attributes["value"] = search
                            }
                        }
                        div("form-group") {
                            attributes["style"] = "margin:0;"
                            label { +"Статус" }
                            select {
                                name = "status"
                                attributes["class"] = "form-control"
                                option { value = "ALL"; selected = statusFilter == "ALL"; +"Все" }
                                option { value = "ACTIVE"; selected = statusFilter == "ACTIVE"; +"Активные" }
                                option { value = "COMPLETED"; selected = statusFilter == "COMPLETED"; +"Завершённые" }
                            }
                        }
                        button(type = ButtonType.submit) { attributes["class"] = "btn btn-primary"; +"Найти" }
                        if (search.isNotBlank() || statusFilter != "ALL") {
                            a("/students") { attributes["class"] = "btn btn-secondary"; +"Сбросить" }
                        }
                    }
                }
            }

            div("card") {
                if (views.isEmpty()) {
                    p { attributes["style"] = "color:#888; text-align:center; padding:30px 0;"; +"Карточки не найдены." }
                } else {
                    table {
                        thead {
                            tr {
                                th { +"ФИО ученика" }
                                th { +"Email" }
                                th { +"Статус" }
                                th { +"Заключение" }
                                th { +"Действия" }
                            }
                        }
                        tbody {
                            for (v in views) {
                                // Своя карточка (любой статус) или admin → полные права
                                val canEdit = v.card.logopedId == session.userId || session.role == "ADMIN"
                                // Нет активной карточки → можно взять ученика
                                val canTake = v.card.status == StudentStatus.COMPLETED && session.role != "ADMIN"
                                tr {
                                    td { +v.studentUser.fullName }
                                    td { +v.studentUser.email }
                                    td {
                                        span {
                                            attributes["class"] = if (v.card.status == StudentStatus.ACTIVE)
                                                "badge badge-active" else "badge badge-blocked"
                                            +v.card.status.displayName
                                        }
                                    }
                                    td {
                                        attributes["style"] = "max-width:200px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;"
                                        val c = v.card.conclusion
                                        +(if (c.isNullOrBlank()) "—" else if (c.length > 60) c.take(60) + "…" else c)
                                    }
                                    td {
                                        div("actions") {
                                            a("/students/${v.card.id}") {
                                                attributes["class"] = "btn btn-sm btn-secondary"; +"Просмотр"
                                            }
                                            if (canEdit) {
                                                a("/students/${v.card.id}/edit") {
                                                    attributes["class"] = "btn btn-sm btn-warning"; +"Изменить"
                                                }
                                                form(method = FormMethod.post, action = "/students/${v.card.id}/delete") {
                                                    attributes["style"] = "display:inline;"
                                                    attributes["onsubmit"] = "return confirm('Удалить карточку ${v.studentUser.fullName}?')"
                                                    button(type = ButtonType.submit) {
                                                        attributes["class"] = "btn btn-sm btn-danger"; +"Удалить"
                                                    }
                                                }
                                            }
                                            if (canTake) {
                                                a("/students/create?studentId=${v.card.studentUserId}") {
                                                    attributes["class"] = "btn btn-sm btn-primary"
                                                    +"Взять ученика"
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun studentCreateSearchPage(
    session: UserSession,
    availableStudents: List<User>,
    search: String = ""
): String = createHTML().html {
    head {
        title { +"Новая карточка" }
        style { unsafe { +commonStyles() } }
    }
    body {
        navbar(session)
        div("container") {
            div {
                attributes["style"] = "display:flex; justify-content:space-between; align-items:center; margin-bottom:24px;"
                h2 { attributes["style"] = "margin:0; color:#333;"; +"Выбор ученика" }
                a("/students") { attributes["class"] = "btn btn-secondary"; +"Назад" }
            }

            div("card") {
                attributes["style"] = "padding:20px; margin-bottom:0;"
                form(method = FormMethod.get, action = "/students/create") {
                    div {
                        attributes["style"] = "display:flex; gap:12px; align-items:flex-end;"
                        div("form-group") {
                            attributes["style"] = "margin:0; flex:1;"
                            label { +"Поиск по ФИО" }
                            input(type = InputType.text, name = "search") {
                                attributes["class"] = "form-control"
                                attributes["placeholder"] = "Введите ФИО ученика..."
                                attributes["value"] = search
                            }
                        }
                        button(type = ButtonType.submit) { attributes["class"] = "btn btn-primary"; +"Найти" }
                    }
                }
            }

            div("card") {
                p {
                    attributes["style"] = "color:#888; font-size:13px; margin-bottom:16px;"
                    +"Показаны только ученики без карточки. Выберите ученика для создания карточки."
                }
                if (availableStudents.isEmpty()) {
                    p { attributes["style"] = "color:#888; text-align:center; padding:20px 0;"; +"Все ученики уже имеют карточки или ни одного не зарегистрировано." }
                } else {
                    table {
                        thead {
                            tr { th { +"ФИО" }; th { +"Email" }; th { +"" } }
                        }
                        tbody {
                            for (u in availableStudents) {
                                tr {
                                    td { +u.fullName }
                                    td { +u.email }
                                    td {
                                        a("/students/create?studentId=${u.id}") {
                                            attributes["class"] = "btn btn-sm btn-primary"
                                            +"Создать карточку"
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun studentCardFormPage(
    session: UserSession,
    studentUser: User,
    card: StudentCard? = null,
    error: String? = null
): String = createHTML().html {
    val isEdit = card != null
    val title  = if (isEdit) "Редактировать карточку" else "Новая карточка"
    val action = if (isEdit) "/students/${card!!.id}/edit" else "/students/create"

    head {
        title { +title }
        style { unsafe { +commonStyles() } }
    }
    body {
        navbar(session)
        div("container") {
            h2 { attributes["style"] = "margin-bottom:24px; color:#333;"; +title }
            if (error != null) { div("alert alert-error") { +error } }

            div("card") {
                div("section-title") { +"Ученик" }
                div {
                    attributes["style"] = "display:flex; align-items:center; gap:16px; padding:12px 0;"
                    div {
                        attributes["style"] = "font-size:18px; font-weight:bold; color:#333;"
                        +studentUser.fullName
                    }
                    span {
                        attributes["style"] = "color:#888; font-size:14px;"
                        +studentUser.email
                    }
                }
            }

            div("card") {
                form(method = FormMethod.post, action = action) {
                    if (!isEdit) {
                        input(type = InputType.hidden, name = "studentUserId") {
                            attributes["value"] = studentUser.id.toString()
                        }
                    }
                    div("section-title") { +"Заключение и план" }

                    div("form-group") {
                        label { +"Заключение" }
                        textArea {
                            name = "conclusion"
                            attributes["class"] = "form-control"
                            attributes["rows"] = "5"
                            +(card?.conclusion ?: "")
                        }
                    }
                    div("form-group") {
                        label { +"Коррекционный план" }
                        textArea {
                            name = "correctionPlan"
                            attributes["class"] = "form-control"
                            attributes["rows"] = "5"
                            +(card?.correctionPlan ?: "")
                        }
                    }
                    div("form-group") {
                        label { +"Примечания" }
                        textArea {
                            name = "notes"
                            attributes["class"] = "form-control"
                            attributes["rows"] = "3"
                            +(card?.notes ?: "")
                        }
                    }
                    div("form-group") {
                        label { +"Статус" }
                        select {
                            name = "status"
                            attributes["class"] = "form-control"
                            attributes["style"] = "max-width:250px;"
                            option {
                                value = StudentStatus.ACTIVE.name
                                selected = (card?.status ?: StudentStatus.ACTIVE) == StudentStatus.ACTIVE
                                +StudentStatus.ACTIVE.displayName
                            }
                            option {
                                value = StudentStatus.COMPLETED.name
                                selected = card?.status == StudentStatus.COMPLETED
                                +StudentStatus.COMPLETED.displayName
                            }
                        }
                    }

                    div {
                        attributes["style"] = "display:flex; gap:12px; margin-top:8px;"
                        button(type = ButtonType.submit) {
                            attributes["class"] = "btn btn-primary"
                            +if (isEdit) "Сохранить" else "Создать карточку"
                        }
                        a(if (isEdit) "/students/${card!!.id}" else "/students") {
                            attributes["class"] = "btn btn-secondary"
                            +"Отмена"
                        }
                    }
                }
            }
        }
    }
}

fun studentViewPage(
    session: UserSession,
    view: StudentCardView,
    profile: StudentProfile?,
    history: List<CardHistoryEntry> = emptyList()
): String = createHTML().html {
    head {
        title { +"Карточка: ${view.studentUser.fullName}" }
        style { unsafe { +commonStyles() } }
    }
    body {
        navbar(session)
        div("container") {
            div {
                attributes["style"] = "display:flex; justify-content:space-between; align-items:center; margin-bottom:24px;"
                div {
                    h2 { attributes["style"] = "margin:0; color:#333;"; +view.studentUser.fullName }
                    span { attributes["style"] = "color:#888; font-size:14px;"; +view.studentUser.email }
                }
                div("actions") {
                    val isOwnCard = view.card.logopedId == session.userId
                    if (isOwnCard || session.role == "ADMIN") {
                        a("/students/${view.card.id}/edit") { attributes["class"] = "btn btn-warning"; +"Изменить" }
                    }
                    if (!isOwnCard && view.card.status == StudentStatus.COMPLETED && session.role != "ADMIN") {
                        a("/students/create?studentId=${view.card.studentUserId}") {
                            attributes["class"] = "btn btn-primary"
                            +"Взять ученика"
                        }
                    }
                    a("/students") { attributes["class"] = "btn btn-secondary"; +"← Назад" }
                }
            }

            // Статус
            div("card") {
                attributes["style"] = "padding:16px 30px;"
                div {
                    attributes["style"] = "display:flex; align-items:center; gap:12px;"
                    span { attributes["style"] = "color:#555; font-size:14px;"; +"Статус:" }
                    span {
                        attributes["class"] = if (view.card.status == StudentStatus.ACTIVE) "badge badge-active" else "badge badge-blocked"
                        +view.card.status.displayName
                    }
                }
            }

            // Заключение логопеда
            div("card") {
                div("section-title") { +"Заключение логопеда" }
                viewBlock("Заключение", view.card.conclusion)
                viewBlock("Коррекционный план", view.card.correctionPlan)
                viewBlock("Примечания", view.card.notes)
            }

            // Анкета ребёнка
            div("card") {
                div("section-title") { +"Анкета ребёнка" }
                if (profile == null) {
                    p {
                        attributes["style"] = "color:#aaa; font-style:italic; padding:12px 0;"
                        +"Ученик ещё не заполнил анкету."
                    }
                } else {
                    // Родители
                    viewSection("Сведения о родителях") {
                        div("grid-2") {
                            viewField("Мать — ФИО", profile.motherFullName)
                            viewField("Мать — возраст", profile.motherAge)
                            viewField("Отец — ФИО", profile.fatherFullName)
                            viewField("Отец — возраст", profile.fatherAge)
                            viewField("Какой по счёту ребёнок и от каких родов", profile.pregnancyNumber)
                        }
                    }
                    // Перинатальный анамнез
                    viewSection("Перинатальный анамнез") {
                        div("grid-2") {
                            viewField("Характер беременности", profile.pregnancyCharacter)
                            viewField("Роды", profile.birthType)
                            viewField("Стимуляция", profile.birthStimulation)
                            viewField("Когда закричал", profile.firstCryTime)
                            viewField("Асфиксия", profile.asphyxia)
                            viewField("Резус-фактор (совместимость)", profile.rhesusFactor)
                            viewField("Вес при рождении", profile.birthWeight)
                            viewField("Рост при рождении", profile.birthHeight)
                        }
                    }
                    // Вскармливание
                    viewSection("Вскармливание и выписка") {
                        div("grid-2") {
                            viewField("Когда принесли кормить", profile.feedingStart)
                            viewField("Как взял грудь", profile.breastFeeding)
                            viewField("Как сосал", profile.suckingCharacter)
                            viewField("Срыгивания, поперхивания", profile.regurgitation)
                            viewField("День выписки из роддома", profile.dischargeDay)
                        }
                    }
                    // Психомоторное развитие
                    viewSection("Психомоторное развитие") {
                        div("grid-2") {
                            viewField("Голову держит с", profile.headsUp)
                            viewField("Сидит с", profile.sitting)
                            viewField("Стоит с", profile.standing)
                            viewField("Ходит с", profile.walking)
                            viewField("Первые зубы", profile.firstTeeth)
                        }
                    }
                    // Заболевания
                    viewSection("Перенесённые заболевания") {
                        div("grid-2") {
                            viewField("До года", profile.illnessesBeforeYear)
                            viewField("После года", profile.illnessesAfterYear)
                            viewField("Инфекции", profile.infections)
                            viewField("Ушибы, травмы головы", profile.headInjuries)
                            viewField("Судороги при высокой температуре", profile.convulsions)
                        }
                    }
                    // Речевое развитие
                    viewSection("Речевое развитие") {
                        div("grid-2") {
                            viewField("Гуление", profile.cooing)
                            viewField("Лепет", profile.babbling)
                            viewField("Первые слова", profile.firstWords)
                            viewField("Первые фразы", profile.firstPhrases)
                            viewField("Прерывалось ли речевое развитие", profile.speechInterruption)
                            viewField("Речевая среда", profile.speechEnvironment)
                            viewField("Занимался ли с логопедом раньше", profile.previousSpeechTherapy)
                            viewField("Отношение ребёнка к своей речи", profile.attitudeToSpeech)
                        }
                    }
                }
            }

            // ── История работы ─────────────────────────────────────────────────
            if (history.isNotEmpty()) {
                div("card") {
                    div("section-title") { +"История работы с учеником" }
                    for (entry in history) {
                        div {
                            attributes["style"] = "border-left: 3px solid #e0e0e0; padding: 12px 16px; margin-bottom: 16px; background: #fafafa; border-radius: 0 6px 6px 0;"
                            div {
                                attributes["style"] = "display:flex; align-items:center; gap:12px; margin-bottom:8px; flex-wrap:wrap;"
                                span {
                                    attributes["class"] = if (entry.card.status == StudentStatus.ACTIVE) "badge badge-active" else "badge badge-blocked"
                                    +entry.card.status.displayName
                                }
                                span {
                                    attributes["style"] = "font-size:13px; color:#555; font-weight:bold;"
                                    +entry.logopedName
                                }
                                span {
                                    attributes["style"] = "font-size:12px; color:#aaa;"
                                    val isoDate = java.time.Instant.ofEpochMilli(entry.card.createdAt)
                                        .atZone(java.time.ZoneId.systemDefault()).toLocalDate().toString()
                                    val dateParts = isoDate.split("-")
                                    val dateFormatted = if (dateParts.size == 3) "${dateParts[2]}.${dateParts[1]}.${dateParts[0]}" else isoDate
                                    +"с $dateFormatted"
                                }
                            }
                            if (!entry.card.conclusion.isNullOrBlank()) {
                                div {
                                    attributes["style"] = "font-size:13px; margin-bottom:6px;"
                                    span { attributes["style"] = "color:#888; font-weight:bold;"; +"Заключение: " }
                                    span { attributes["style"] = "color:#333;"; +entry.card.conclusion }
                                }
                            }
                            if (!entry.card.correctionPlan.isNullOrBlank()) {
                                div {
                                    attributes["style"] = "font-size:13px; margin-bottom:6px;"
                                    span { attributes["style"] = "color:#888; font-weight:bold;"; +"План коррекции: " }
                                    span { attributes["style"] = "color:#333;"; +entry.card.correctionPlan }
                                }
                            }
                            if (!entry.card.notes.isNullOrBlank()) {
                                div {
                                    attributes["style"] = "font-size:13px;"
                                    span { attributes["style"] = "color:#888; font-weight:bold;"; +"Примечания: " }
                                    span { attributes["style"] = "color:#333;"; +entry.card.notes }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun FlowContent.viewSection(title: String, content: FlowContent.() -> Unit) {
    div {
        attributes["style"] = "margin-bottom:20px;"
        div {
            attributes["style"] = "font-size:13px; font-weight:bold; color:#4CAF50; margin-bottom:12px; text-transform:uppercase; letter-spacing:0.5px;"
            +title
        }
        content()
    }
}

private fun FlowContent.viewField(label: String, value: String?) {
    div {
        attributes["style"] = "margin-bottom:14px;"
        div {
            attributes["style"] = "font-size:11px; font-weight:bold; color:#999; margin-bottom:3px; text-transform:uppercase; letter-spacing:0.5px;"
            +label
        }
        div {
            attributes["style"] = "font-size:14px; color:" + if (value.isNullOrBlank()) "#ccc" else "#333" + ";"
            +(if (value.isNullOrBlank()) "Не указано" else value)
        }
    }
}

private fun FlowContent.viewBlock(label: String, value: String?) {
    div {
        attributes["style"] = "margin-bottom:18px;"
        div {
            attributes["style"] = "font-size:12px; font-weight:bold; color:#888; margin-bottom:6px; text-transform:uppercase; letter-spacing:0.5px;"
            +label
        }
        if (value.isNullOrBlank()) {
            span { attributes["style"] = "color:#ccc; font-style:italic;"; +"Не указано" }
        } else {
            p { attributes["style"] = "font-size:15px; color:#333; white-space:pre-wrap; margin:0;"; +value }
        }
    }
}
