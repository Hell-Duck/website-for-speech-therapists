package pages

import data.ExerciseHelper
import data.GameData
import models.*
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import java.time.LocalDate

// ── Общие стили для занятий и заданий ─────────────────────────────────────────
private val sessionStyles = """
    .ep-group        { margin-bottom: 18px; }
    .ep-group-label  { font-size: 12px; font-weight: bold; color: #4CAF50; margin-bottom: 8px; text-transform: uppercase; letter-spacing: 0.5px; }
    .ep-items        { display: grid; grid-template-columns: repeat(auto-fill, minmax(230px, 1fr)); gap: 6px; }
    .ep-item         { display: flex; align-items: center; gap: 8px; padding: 8px 12px; border: 1px solid #e0e0e0; border-radius: 6px; cursor: pointer; font-size: 14px; transition: background .15s; }
    .ep-item:hover   { background: #f5f5f5; }
    .ep-item:has(input:checked) { background: #e8f5e9; border-color: #4CAF50; }
    .ep-item input   { flex-shrink: 0; accent-color: #4CAF50; width: 16px; height: 16px; }
    .ep-empty        { color: #f44336; font-size: 13px; margin-top: 6px; }
    .rating-stars    { display: flex; gap: 6px; flex-wrap: wrap; }
    .rating-opt      { display: flex; align-items: center; gap: 6px; padding: 8px 14px; border: 1px solid #e0e0e0; border-radius: 6px; cursor: pointer; font-size: 13px; transition: background .15s; }
    .rating-opt:hover { background: #f5f5f5; }
    .rating-opt:has(input:checked) { background: #e8f5e9; border-color: #4CAF50; font-weight: bold; }
    .rating-opt input { accent-color: #4CAF50; }
    .badge-active    { background: #e3f2fd; color: #1565c0; }
    .badge-overdue   { background: #ffebee; color: #c62828; }
    .badge-completed { background: #e8f5e9; color: #2e7d32; }
    .hw-card         { background: white; border-radius: 10px; border: 1px solid #e0e0e0; padding: 20px; margin-bottom: 14px; }
    .hw-card.overdue { border-left: 4px solid #f44336; }
    .hw-card.active  { border-left: 4px solid #2196F3; }
    .hw-card.completed { border-left: 4px solid #4CAF50; opacity: .85; }
    .ex-list         { list-style: none; padding: 0; margin: 10px 0; }
    .ex-list li      { padding: 5px 0 5px 16px; font-size: 14px; color: #444; border-bottom: 1px solid #f5f5f5; position: relative; }
    .ex-list li::before { content: "•"; position: absolute; left: 0; color: #4CAF50; }
    .filter-tabs     { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 20px; }
    .filter-tab      { padding: 6px 16px; border-radius: 20px; font-size: 13px; text-decoration: none; background: #f0f0f0; color: #555; transition: all .15s; }
    .filter-tab.active { background: #4CAF50; color: white; }
    .filter-tab:hover  { background: #e0e0e0; }
    .filter-tab.active:hover { background: #388E3C; }
    .complete-form   { margin-top: 14px; padding-top: 14px; border-top: 1px dashed #e0e0e0; }
    .ex-link         { color: #1565c0; text-decoration: none; }
    .ex-link:hover   { text-decoration: underline; color: #0d47a1; }
"""

// ── Общий компонент: выбор упражнений ────────────────────────────────────────
fun FlowContent.exercisePicker(selectedKeys: List<String> = emptyList()) {
    div {
        GameData.exercises.forEach { (soundKey, exercises) ->
            val soundName = ExerciseHelper.soundNames[soundKey] ?: soundKey
            div("ep-group") {
                div("ep-group-label") { +soundName }
                div("ep-items") {
                    exercises.forEachIndexed { index, (title, _) ->
                        val key = "$soundKey:$index"
                        label("ep-item") {
                            input(type = InputType.checkBox, name = "exercises") {
                                value = key
                                checked = key in selectedKeys
                            }
                            span { +title }
                        }
                    }
                }
            }
        }
    }
}

// ── Список занятий ────────────────────────────────────────────────────────────
fun sessionListPage(
    session: UserSession,
    views: List<SessionView>,
    studentViews: List<StudentCardView>,
    selectedStudentId: Int?
): String = createHTML().html {
    head {
        title { +"Занятия" }
        style { unsafe { +commonStyles(); +sessionStyles } }
    }
    body {
        navbar(session)
        div("container") {
            div {
                attributes["style"] = "display:flex; justify-content:space-between; align-items:center; margin-bottom:24px;"
                h2 { attributes["style"] = "margin:0; color:#333;"; +"Занятия" }
                div("actions") {
                    a("/dashboard") { attributes["class"] = "btn btn-secondary"; +"← Назад" }
                    a("/sessions/create") { attributes["class"] = "btn btn-primary"; +"+ Записать занятие" }
                }
            }

            // Фильтр по ученику
            if (studentViews.isNotEmpty()) {
                div("card") {
                    attributes["style"] = "padding:16px 20px;"
                    form(method = FormMethod.get, action = "/sessions") {
                        div {
                            attributes["style"] = "display:flex; gap:12px; align-items:flex-end; flex-wrap:wrap;"
                            div("form-group") {
                                attributes["style"] = "margin:0; flex:1; min-width:220px;"
                                label { +"Ученик" }
                                select {
                                    name = "studentId"
                                    attributes["class"] = "form-control"
                                    option { value = ""; +"— Все ученики —" }
                                    for (v in studentViews) {
                                        option {
                                            value = v.studentUser.id.toString()
                                            selected = v.studentUser.id == selectedStudentId
                                            +v.studentUser.fullName
                                        }
                                    }
                                }
                            }
                            button(type = ButtonType.submit) { attributes["class"] = "btn btn-primary"; +"Применить" }
                            if (selectedStudentId != null) {
                                a("/sessions") { attributes["class"] = "btn btn-secondary"; +"Сбросить" }
                            }
                        }
                    }
                }
            }

            div("card") {
                if (views.isEmpty()) {
                    p { attributes["style"] = "color:#888; text-align:center; padding:30px 0;"; +"Занятия не найдены." }
                } else {
                    table {
                        thead {
                            tr {
                                th { +"Дата" }
                                th { +"Ученик" }
                                th { +"Упражнения" }
                                th { +"Прогресс" }
                                th { +"Действия" }
                            }
                        }
                        tbody {
                            for (v in views) {
                                tr {
                                    td { +ExerciseHelper.formatDate(v.session.sessionDate) }
                                    td { +v.studentUser.fullName }
                                    td {
                                        attributes["style"] = "font-size:13px; color:#666;"
                                        val count = v.session.exerciseKeys.size
                                        +(if (count == 0) "—" else "$count упр.")
                                    }
                                    td {
                                        val r = v.session.progressRating
                                        span {
                                            attributes["title"] = ExerciseHelper.ratingLabel(r)
                                            +ExerciseHelper.ratingEmoji(r)
                                        }
                                    }
                                    td {
                                        div("actions") {
                                            a("/sessions/${v.session.id}") { attributes["class"] = "btn btn-sm btn-secondary"; +"Просмотр" }
                                            a("/sessions/${v.session.id}/edit") { attributes["class"] = "btn btn-sm btn-warning"; +"Изменить" }
                                            form(method = FormMethod.post, action = "/sessions/${v.session.id}/delete") {
                                                attributes["style"] = "display:inline;"
                                                attributes["onsubmit"] = "return confirm('Удалить запись о занятии?')"
                                                button(type = ButtonType.submit) { attributes["class"] = "btn btn-sm btn-danger"; +"Удалить" }
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

// ── Форма занятия (создать / редактировать) ───────────────────────────────────
fun sessionFormPage(
    session: UserSession,
    studentViews: List<StudentCardView>,
    current: Session? = null,
    preselectedStudentId: Int? = null,
    error: String? = null
): String = createHTML().html {
    val isEdit = current != null
    head {
        title { +if (isEdit) "Редактировать занятие" else "Записать занятие" }
        style { unsafe { +commonStyles(); +sessionStyles } }
    }
    body {
        navbar(session)
        div("container") {
            h2 { attributes["style"] = "margin-bottom:24px; color:#333;"; +if (isEdit) "Редактировать занятие" else "Записать занятие" }
            if (error != null) { div("alert alert-error") { +error } }

            if (studentViews.isEmpty()) {
                div("card") {
                    div("alert alert-error") { attributes["style"] = "margin:0;" ; +"У вас нет учеников. Сначала " }
                    a("/students/create") { attributes["class"] = "btn btn-primary"; attributes["style"] = "margin-top:12px;"; +"создайте карточку ученика" }
                }
            } else {
                div("card") {
                    form(method = FormMethod.post, action = if (isEdit) "/sessions/${current!!.id}/edit" else "/sessions/create") {
                        div("grid-2") {
                            div("form-group") {
                                label { +"Ученик *" }
                                select {
                                    name = "studentUserId"
                                    attributes["class"] = "form-control"
                                    attributes["required"] = "true"
                                    if (!isEdit) option { value = ""; +"— Выберите ученика —" }
                                    for (v in studentViews) {
                                        val sel = when {
                                            isEdit      -> v.studentUser.id == current!!.studentUserId
                                            preselectedStudentId != null -> v.studentUser.id == preselectedStudentId
                                            else        -> false
                                        }
                                        option { value = v.studentUser.id.toString(); selected = sel; +v.studentUser.fullName }
                                    }
                                }
                            }
                            div("form-group") {
                                label { +"Дата занятия *" }
                                input(type = InputType.date, name = "sessionDate") {
                                    attributes["class"] = "form-control"
                                    attributes["required"] = "true"
                                    attributes["value"] = current?.sessionDate ?: LocalDate.now().toString()
                                }
                            }
                        }

                        div("form-group") {
                            div("section-title") { +"Выполненные упражнения" }
                            exercisePicker(current?.exerciseKeys ?: emptyList())
                        }

                        div("form-group") {
                            label { +"Оценка прогресса *" }
                            div("rating-stars") {
                                for (r in 1..5) {
                                    label("rating-opt") {
                                        input(type = InputType.radio, name = "progressRating") {
                                            value = r.toString()
                                            checked = (current?.progressRating ?: 3) == r
                                        }
                                        +"${ExerciseHelper.ratingEmoji(r)} ${ExerciseHelper.ratingLabel(r)}"
                                    }
                                }
                            }
                        }

                        div("form-group") {
                            label { +"Комментарии к занятию" }
                            textArea {
                                name = "comments"
                                attributes["class"] = "form-control"
                                attributes["rows"] = "4"
                                attributes["placeholder"] = "Что отработали, что вызвало затруднения, рекомендации..."
                                +(current?.comments ?: "")
                            }
                        }

                        div {
                            attributes["style"] = "display:flex; gap:12px;"
                            button(type = ButtonType.submit) { attributes["class"] = "btn btn-primary"; +if (isEdit) "Сохранить" else "Записать занятие" }
                            a(if (isEdit) "/sessions/${current!!.id}" else "/sessions") { attributes["class"] = "btn btn-secondary"; +"Отмена" }
                        }
                    }
                }
            }
        }
    }
}

// ── Просмотр занятия ──────────────────────────────────────────────────────────
fun sessionViewPage(session: UserSession, view: SessionView): String = createHTML().html {
    val s = view.session
    head {
        title { +"Занятие ${ExerciseHelper.formatDate(s.sessionDate)}" }
        style { unsafe { +commonStyles(); +sessionStyles } }
    }
    body {
        navbar(session)
        div("container") {
            div {
                attributes["style"] = "display:flex; justify-content:space-between; align-items:center; margin-bottom:24px;"
                div {
                    h2 { attributes["style"] = "margin:0; color:#333;"; +"Занятие ${ExerciseHelper.formatDate(s.sessionDate)}" }
                    span { attributes["style"] = "color:#888; font-size:14px;"; +view.studentUser.fullName }
                }
                div("actions") {
                    a("/sessions/${s.id}/edit") { attributes["class"] = "btn btn-warning"; +"Изменить" }
                    a("/sessions") { attributes["class"] = "btn btn-secondary"; +"Назад" }
                }
            }

            div("card") {
                div("section-title") { +"Сведения о занятии" }
                div("grid-2") {
                    svField("Ученик", view.studentUser.fullName)
                    svField("Дата", ExerciseHelper.formatDate(s.sessionDate))
                    svField("Прогресс", "${ExerciseHelper.ratingEmoji(s.progressRating)} ${ExerciseHelper.ratingLabel(s.progressRating)}")
                }
            }

            div("card") {
                div("section-title") { +"Выполненные упражнения" }
                if (s.exerciseKeys.isEmpty()) {
                    p { attributes["style"] = "color:#aaa; font-style:italic;"; +"Не указаны" }
                } else {
                    ul("ex-list") {
                        s.exerciseKeys.forEach { key ->
                            li { +ExerciseHelper.resolveFull(key) }
                        }
                    }
                }
            }

            div("card") {
                div("section-title") { +"Комментарии" }
                if (s.comments.isNullOrBlank()) {
                    p { attributes["style"] = "color:#aaa; font-style:italic;"; +"Не указаны" }
                } else {
                    p { attributes["style"] = "white-space:pre-wrap; color:#333;"; +s.comments }
                }
            }
        }
    }
}

private fun FlowContent.svField(label: String, value: String) {
    div { attributes["style"] = "margin-bottom:14px;"
        div { attributes["style"] = "font-size:11px; font-weight:bold; color:#999; text-transform:uppercase; letter-spacing:.5px; margin-bottom:3px;"; +label }
        div { attributes["style"] = "font-size:15px; color:#333;"; +value }
    }
}

// ── Список домашних заданий (логопед) ─────────────────────────────────────────
fun homeworkListPage(
    session: UserSession,
    views: List<HomeworkView>,
    statusFilter: String
): String = createHTML().html {
    head {
        title { +"Домашние задания" }
        style { unsafe { +commonStyles(); +sessionStyles } }
    }
    body {
        navbar(session)
        div("container") {
            div {
                attributes["style"] = "display:flex; justify-content:space-between; align-items:center; margin-bottom:24px;"
                h2 { attributes["style"] = "margin:0; color:#333;"; +"Домашние задания" }
                div("actions") {
                    a("/dashboard") { attributes["class"] = "btn btn-secondary"; +"← Назад" }
                    a("/homework/create") { attributes["class"] = "btn btn-primary"; +"+ Назначить задание" }
                }
            }

            div("filter-tabs") {
                listOf("ALL" to "Все", "ACTIVE" to "Активные", "OVERDUE" to "Просроченные", "COMPLETED" to "Выполненные").forEach { (val_, label) ->
                    a("/homework?status=$val_") {
                        attributes["class"] = "filter-tab" + if (statusFilter == val_) " active" else ""
                        +label
                    }
                }
            }

            if (views.isEmpty()) {
                div("card") {
                    p { attributes["style"] = "color:#888; text-align:center; padding:30px 0;"; +"Заданий не найдено." }
                }
            } else {
                for (v in views) {
                    val eff = v.homework.effectiveStatus
                    val cardCls = when (eff) { HomeworkStatus.ACTIVE -> "active"; HomeworkStatus.OVERDUE -> "overdue"; HomeworkStatus.COMPLETED -> "completed" }
                    div("hw-card $cardCls") {
                        div {
                            attributes["style"] = "display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:10px;"
                            div {
                                span {
                                    attributes["style"] = "font-weight:bold; font-size:15px; color:#333;"
                                    +v.studentUser.fullName
                                }
                                span {
                                    attributes["style"] = "margin-left:10px;"
                                    attributes["class"] = "badge " + when (eff) { HomeworkStatus.ACTIVE -> "badge-active"; HomeworkStatus.OVERDUE -> "badge-overdue"; HomeworkStatus.COMPLETED -> "badge-completed" }
                                    +eff.displayName
                                }
                            }
                            span { attributes["style"] = "font-size:13px; color:#888;"; +"до ${ExerciseHelper.formatDate(v.homework.dueDate)}" }
                        }
                        ul("ex-list") {
                            v.homework.exerciseKeys.take(3).forEach { key -> li { +ExerciseHelper.resolveFull(key) } }
                            if (v.homework.exerciseKeys.size > 3) li { attributes["style"] = "color:#aaa;"; +"ещё ${v.homework.exerciseKeys.size - 3}..." }
                        }
                        if (!v.homework.parentComment.isNullOrBlank()) {
                            p { attributes["style"] = "font-size:13px; color:#666; margin:6px 0 0;"; +"💬 ${v.homework.parentComment}" }
                        }
                        if (eff == HomeworkStatus.COMPLETED && !v.homework.studentComment.isNullOrBlank()) {
                            p { attributes["style"] = "font-size:13px; color:#2e7d32; margin:6px 0 0;"; +"✅ ${v.homework.studentComment}" }
                        }
                        div {
                            attributes["style"] = "margin-top:12px; display:flex; gap:8px;"
                            form(method = FormMethod.post, action = "/homework/${v.homework.id}/delete") {
                                attributes["onsubmit"] = "return confirm('Удалить задание?')"
                                button(type = ButtonType.submit) { attributes["class"] = "btn btn-sm btn-danger"; +"Удалить" }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Форма назначения домашнего задания ────────────────────────────────────────
fun homeworkFormPage(
    session: UserSession,
    studentViews: List<StudentCardView>,
    preselectedStudentId: Int? = null,
    error: String? = null
): String = createHTML().html {
    head {
        title { +"Назначить задание" }
        style { unsafe { +commonStyles(); +sessionStyles } }
    }
    body {
        navbar(session)
        div("container") {
            h2 { attributes["style"] = "margin-bottom:24px; color:#333;"; +"Назначить домашнее задание" }
            if (error != null) { div("alert alert-error") { +error } }

            if (studentViews.isEmpty()) {
                div("card") {
                    p { attributes["style"] = "color:#888;"; +"У вас нет учеников. Сначала создайте карточку ученика." }
                    a("/students/create") { attributes["class"] = "btn btn-primary"; +"Добавить ученика" }
                }
            } else {
                div("card") {
                    form(method = FormMethod.post, action = "/homework/create") {
                        div("grid-2") {
                            div("form-group") {
                                label { +"Ученик *" }
                                select {
                                    name = "studentUserId"
                                    attributes["class"] = "form-control"
                                    attributes["required"] = "true"
                                    option { value = ""; +"— Выберите ученика —" }
                                    for (v in studentViews) {
                                        option {
                                            value = v.studentUser.id.toString()
                                            selected = v.studentUser.id == preselectedStudentId
                                            +v.studentUser.fullName
                                        }
                                    }
                                }
                            }
                            div("form-group") {
                                label { +"Срок выполнения *" }
                                input(type = InputType.date, name = "dueDate") {
                                    attributes["class"] = "form-control"
                                    attributes["required"] = "true"
                                    attributes["min"] = LocalDate.now().toString()
                                    attributes["value"] = LocalDate.now().plusDays(7).toString()
                                }
                            }
                        }

                        div("form-group") {
                            div("section-title") { +"Упражнения *" }
                            exercisePicker()
                            div("ep-empty") { id = "ex-hint"; attributes["style"] = "display:none;"; +"Выберите хотя бы одно упражнение" }
                        }

                        div("form-group") {
                            label { +"Комментарий для родителя" }
                            textArea {
                                name = "parentComment"
                                attributes["class"] = "form-control"
                                attributes["rows"] = "3"
                                attributes["placeholder"] = "Пожелания, инструкции, что важно отработать..."
                            }
                        }

                        div {
                            attributes["style"] = "display:flex; gap:12px;"
                            button(type = ButtonType.submit) { attributes["class"] = "btn btn-primary"; +"Назначить задание" }
                            a("/homework") { attributes["class"] = "btn btn-secondary"; +"Отмена" }
                        }
                    }
                }
            }
        }
        script {
            unsafe {
                +"""
document.querySelector('form')&&document.querySelector('form').addEventListener('submit',function(e){
    var cbs=document.querySelectorAll('[name=exercises]:checked');
    if(!cbs.length){e.preventDefault();document.getElementById('ex-hint').style.display='block';}
});
"""
            }
        }
    }
}

// ── Домашние задания ученика ──────────────────────────────────────────────────
fun studentHomeworkPage(
    session: UserSession,
    homeworks: List<Homework>,
    statusFilter: String
): String = createHTML().html {
    head {
        title { +"Мои задания" }
        style { unsafe { +commonStyles(); +sessionStyles } }
    }
    body {
        navbar(session)
        div("container") {
            div {
                attributes["style"] = "display:flex; justify-content:space-between; align-items:center; margin-bottom:20px;"
                h2 { attributes["style"] = "margin:0; color:#333;"; +"Домашние задания" }
                a("/dashboard") { attributes["class"] = "btn btn-secondary"; +"← Назад" }
            }

            div("filter-tabs") {
                listOf("ALL" to "Все", "ACTIVE" to "Активные", "OVERDUE" to "Просроченные", "COMPLETED" to "Выполненные").forEach { (val_, label) ->
                    a("/student/homework?status=$val_") {
                        attributes["class"] = "filter-tab" + if (statusFilter == val_) " active" else ""
                        +label
                    }
                }
            }

            if (homeworks.isEmpty()) {
                div("card") {
                    p { attributes["style"] = "color:#888; text-align:center; padding:30px 0;"; +"Заданий нет." }
                }
            } else {
                for (hw in homeworks) {
                    val eff = hw.effectiveStatus
                    val cardCls = when (eff) { HomeworkStatus.ACTIVE -> "active"; HomeworkStatus.OVERDUE -> "overdue"; HomeworkStatus.COMPLETED -> "completed" }
                    div("hw-card $cardCls") {
                        // Шапка карточки
                        div {
                            attributes["style"] = "display:flex; justify-content:space-between; align-items:center; margin-bottom:12px;"
                            span {
                                attributes["class"] = "badge " + when (eff) { HomeworkStatus.ACTIVE -> "badge-active"; HomeworkStatus.OVERDUE -> "badge-overdue"; HomeworkStatus.COMPLETED -> "badge-completed" }
                                +eff.displayName
                            }
                            span { attributes["style"] = "font-size:13px; color:#888;"; +"Срок: ${ExerciseHelper.formatDate(hw.dueDate)}" }
                        }

                        // Упражнения
                        div { attributes["style"] = "font-size:13px; font-weight:bold; color:#555; margin-bottom:6px;"; +"Упражнения:" }
                        ul("ex-list") {
                            hw.exerciseKeys.forEach { key ->
                                li {
                                    val url = ExerciseHelper.resolveUrl(key)
                                    if (url != "#") {
                                        a(href = url, classes = "ex-link") {
                                            attributes["target"] = "_blank"
                                            +ExerciseHelper.resolveFull(key)
                                        }
                                    } else {
                                        +ExerciseHelper.resolveFull(key)
                                    }
                                }
                            }
                        }

                        // Комментарий логопеда
                        if (!hw.parentComment.isNullOrBlank()) {
                            div {
                                attributes["style"] = "background:#f8f9fa; border-radius:6px; padding:10px 12px; margin-top:10px; font-size:13px; color:#555;"
                                span { attributes["style"] = "font-weight:bold;"; +"Комментарий логопеда: " }
                                +hw.parentComment
                            }
                        }

                        // Отметка о выполнении (только если не выполнено)
                        if (eff != HomeworkStatus.COMPLETED) {
                            div("complete-form") {
                                p { attributes["style"] = "font-size:13px; color:#555; margin-bottom:8px;"; +"Отметить как выполненное:" }
                                form(method = FormMethod.post, action = "/homework/${hw.id}/complete") {
                                    textArea {
                                        name = "studentComment"
                                        attributes["class"] = "form-control"
                                        attributes["rows"] = "2"
                                        attributes["placeholder"] = "Комментарий (необязательно): что получилось, что вызвало затруднения..."
                                        attributes["style"] = "margin-bottom:8px;"
                                    }
                                    button(type = ButtonType.submit) {
                                        attributes["class"] = "btn btn-primary"
                                        +"✓ Выполнено"
                                    }
                                }
                            }
                        } else {
                            // Показываем комментарий ученика при выполнении
                            if (!hw.studentComment.isNullOrBlank()) {
                                div {
                                    attributes["style"] = "margin-top:10px; font-size:13px; color:#2e7d32;"
                                    +"✅ Ваш комментарий: ${hw.studentComment}"
                                }
                            }
                            if (hw.completedAt != null) {
                                div {
                                    attributes["style"] = "margin-top:6px; font-size:12px; color:#aaa;"
                                    val date = java.time.Instant.ofEpochMilli(hw.completedAt)
                                        .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                                    +"Выполнено: ${ExerciseHelper.formatDate(date.toString())}"
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
