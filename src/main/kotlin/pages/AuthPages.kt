package pages

import models.User
import models.UserRole
import models.UserSession
import kotlinx.html.*
import kotlinx.html.stream.createHTML

fun commonStyles() = """
    * { box-sizing: border-box; }
    body { font-family: Arial, sans-serif; background: #f5f5f5; min-height: 100vh; margin: 0; }
    .navbar {
        background: linear-gradient(135deg, #4CAF50 0%, #388E3C 100%);
        color: white;
        box-shadow: 0 2px 8px rgba(0,0,0,0.2);
    }
    .navbar-inner {
        max-width: 1000px;
        margin: 0 auto;
        padding: 14px 20px;
        display: flex;
        justify-content: space-between;
        align-items: center;
    }
    .nav-brand { color: white; text-decoration: none; font-size: 18px; font-weight: bold; }
    .nav-links a {
        color: white;
        text-decoration: none;
        margin-left: 18px;
        font-size: 14px;
        opacity: 0.9;
    }
    .nav-links a:hover { opacity: 1; text-decoration: underline; }
    .container { max-width: 1000px; margin: 0 auto; padding: 30px 20px; }
    .card {
        background: white;
        border-radius: 12px;
        padding: 30px;
        box-shadow: 0 2px 10px rgba(0,0,0,0.08);
        margin-bottom: 20px;
    }
    .card h2 { margin-bottom: 20px; color: #333; font-size: 20px; }
    .btn {
        padding: 10px 20px;
        border: none;
        border-radius: 6px;
        cursor: pointer;
        font-size: 14px;
        text-decoration: none;
        display: inline-block;
        transition: all 0.2s;
        font-family: Arial, sans-serif;
    }
    .btn-primary { background: #4CAF50; color: white; }
    .btn-primary:hover { background: #388E3C; }
    .btn-danger { background: #f44336; color: white; }
    .btn-danger:hover { background: #c62828; }
    .btn-secondary { background: #6c757d; color: white; }
    .btn-secondary:hover { background: #5a6268; }
    .btn-warning { background: #ff9800; color: white; }
    .btn-warning:hover { background: #e65100; }
    .btn-sm { padding: 6px 12px; font-size: 12px; }
    .form-group { margin-bottom: 18px; }
    label { display: block; margin-bottom: 6px; font-weight: bold; color: #555; font-size: 14px; }
    .form-control {
        width: 100%;
        padding: 10px 14px;
        border: 1px solid #ddd;
        border-radius: 6px;
        font-size: 14px;
        font-family: Arial, sans-serif;
    }
    .form-control:focus { outline: none; border-color: #4CAF50; box-shadow: 0 0 0 2px rgba(76,175,80,0.2); }
    select.form-control { background: white; }
    .alert { padding: 12px 16px; border-radius: 6px; margin-bottom: 20px; font-size: 14px; }
    .alert-error { background: #ffebee; color: #c62828; border-left: 4px solid #f44336; }
    .alert-success { background: #e8f5e9; color: #2e7d32; border-left: 4px solid #4CAF50; }
    .badge { padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: bold; }
    .badge-admin { background: #9c27b0; color: white; }
    .badge-logoped { background: #2196F3; color: white; }
    .badge-student { background: #ff9800; color: white; }
    .badge-active { background: #4CAF50; color: white; }
    .badge-blocked { background: #f44336; color: white; }
    table { width: 100%; border-collapse: collapse; }
    th { text-align: left; padding: 12px 16px; background: #f8f9fa; color: #555; font-size: 13px; border-bottom: 2px solid #dee2e6; }
    td { padding: 12px 16px; border-bottom: 1px solid #eee; font-size: 14px; vertical-align: middle; }
    tr:hover td { background: #fafafa; }
    .grid-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
    @media (max-width: 640px) { .grid-2 { grid-template-columns: 1fr; } }
    .section-title { font-size: 15px; font-weight: bold; color: #555; margin-bottom: 16px; padding-bottom: 8px; border-bottom: 2px solid #e0e0e0; }
    .actions { display: flex; gap: 8px; flex-wrap: wrap; }
"""

fun FlowContent.navbar(session: UserSession) {
    nav {
        attributes["class"] = "navbar"
        div {
            attributes["class"] = "navbar-inner"
            a("/dashboard") {
                attributes["class"] = "nav-brand"
                +"Логопедический помощник"
            }
            div {
                attributes["class"] = "nav-links"
                when (session.role) {
                    "ADMIN" -> {
                        a("/admin/users") { +"Пользователи" }
                        a("/students") { +"Ученики" }
                        a("/sessions") { +"Занятия" }
                        a("/homework") { +"Задания" }
                        a("/sounds") { +"Упражнения" }
                    }
                    "LOGOPED" -> {
                        a("/students") { +"Ученики" }
                        a("/sessions") { +"Занятия" }
                        a("/homework") { +"Задания" }
                        a("/sounds") { +"Упражнения" }
                    }
                    "STUDENT" -> {
                        a("/student/homework") { +"Задания" }
                        a("/sounds") { +"Упражнения" }
                    }
                }
                a("/profile") { +"${session.firstName}" }
                a("/logout") { +"Выйти" }
            }
        }
    }
}

fun loginPage(error: String? = null): String = createHTML().html {
    head {
        title { +"Вход — Логопедический помощник" }
        style {
            unsafe {
                +commonStyles()
                +"""
                .auth-wrap {
                    min-height: 100vh;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    background: linear-gradient(135deg, #e8f5e9 0%, #f3e5f5 100%);
                    padding: 20px;
                }
                .auth-card {
                    background: white;
                    border-radius: 16px;
                    padding: 40px;
                    box-shadow: 0 8px 32px rgba(0,0,0,0.12);
                    width: 100%;
                    max-width: 400px;
                }
                .auth-logo { text-align: center; margin-bottom: 28px; }
                .auth-logo h1 { font-size: 22px; color: #333; margin-bottom: 6px; }
                .auth-logo p { color: #888; font-size: 14px; }
                .auth-footer { text-align: center; margin-top: 20px; font-size: 14px; color: #888; }
                .auth-footer a { color: #4CAF50; text-decoration: none; }
                .auth-footer a:hover { text-decoration: underline; }
                """
            }
        }
    }
    body {
        div("auth-wrap") {
            div("auth-card") {
                div("auth-logo") {
                    h1 { +"Логопедический помощник" }
                    p { +"Войдите в свой аккаунт" }
                }
                if (error != null) {
                    div("alert alert-error") { +error }
                }
                form(method = FormMethod.post, action = "/login") {
                    div("form-group") {
                        label { +"Email" }
                        input(type = InputType.email, name = "email") {
                            attributes["class"] = "form-control"
                            attributes["required"] = "true"
                            attributes["placeholder"] = "your@email.com"
                            attributes["autofocus"] = "true"
                        }
                    }
                    div("form-group") {
                        label { +"Пароль" }
                        input(type = InputType.password, name = "password") {
                            attributes["class"] = "form-control"
                            attributes["required"] = "true"
                            attributes["placeholder"] = "Введите пароль"
                        }
                    }
                    button(type = ButtonType.submit) {
                        attributes["class"] = "btn btn-primary"
                        attributes["style"] = "width: 100%; padding: 12px; font-size: 15px;"
                        +"Войти"
                    }
                }
                p("auth-footer") {
                    +"Нет аккаунта? "
                    a("/register") { +"Зарегистрироваться" }
                }
            }
        }
    }
}

fun registerPage(error: String? = null): String = createHTML().html {
    head {
        title { +"Регистрация — Логопедический помощник" }
        style {
            unsafe {
                +commonStyles()
                +"""
                .auth-wrap {
                    min-height: 100vh;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    background: linear-gradient(135deg, #e8f5e9 0%, #f3e5f5 100%);
                    padding: 30px 20px;
                }
                .auth-card {
                    background: white;
                    border-radius: 16px;
                    padding: 40px;
                    box-shadow: 0 8px 32px rgba(0,0,0,0.12);
                    width: 100%;
                    max-width: 500px;
                }
                .auth-logo { text-align: center; margin-bottom: 28px; }
                .auth-logo h1 { font-size: 22px; color: #333; }
                .auth-footer { text-align: center; margin-top: 20px; font-size: 14px; color: #888; }
                .auth-footer a { color: #4CAF50; text-decoration: none; }
                .auth-footer a:hover { text-decoration: underline; }
                """
            }
        }
    }
    body {
        div("auth-wrap") {
            div("auth-card") {
                div("auth-logo") {
                    h1 { +"Создать аккаунт" }
                }
                if (error != null) {
                    div("alert alert-error") { +error }
                }
                form(method = FormMethod.post, action = "/register") {
                    div("grid-2") {
                        div("form-group") {
                            label { +"Фамилия *" }
                            input(type = InputType.text, name = "lastName") {
                                attributes["class"] = "form-control"
                                attributes["required"] = "true"
                            }
                        }
                        div("form-group") {
                            label { +"Имя *" }
                            input(type = InputType.text, name = "firstName") {
                                attributes["class"] = "form-control"
                                attributes["required"] = "true"
                            }
                        }
                    }
                    div("form-group") {
                        label { +"Отчество" }
                        input(type = InputType.text, name = "middleName") {
                            attributes["class"] = "form-control"
                        }
                    }
                    div("form-group") {
                        label { +"Email *" }
                        input(type = InputType.email, name = "email") {
                            attributes["class"] = "form-control"
                            attributes["required"] = "true"
                        }
                    }
                    div("form-group") {
                        label { +"Пароль * (минимум 6 символов)" }
                        input(type = InputType.password, name = "password") {
                            attributes["class"] = "form-control"
                            attributes["required"] = "true"
                            attributes["minlength"] = "6"
                        }
                    }
                    div("form-group") {
                        label { +"Повторите пароль *" }
                        input(type = InputType.password, name = "confirmPassword") {
                            attributes["class"] = "form-control"
                            attributes["required"] = "true"
                        }
                    }
                    div("form-group") {
                        label { +"Роль *" }
                        select {
                            name = "role"
                            attributes["class"] = "form-control"
                            attributes["required"] = "true"
                            option {
                                value = ""
                                +"-- Выберите роль --"
                            }
                            option {
                                value = "LOGOPED"
                                +"Логопед"
                            }
                            option {
                                value = "STUDENT"
                                +"Ученик"
                            }
                        }
                    }
                    button(type = ButtonType.submit) {
                        attributes["class"] = "btn btn-primary"
                        attributes["style"] = "width: 100%; padding: 12px; font-size: 15px;"
                        +"Зарегистрироваться"
                    }
                }
                p("auth-footer") {
                    +"Уже есть аккаунт? "
                    a("/login") { +"Войти" }
                }
            }
        }
    }
}

fun dashboardPage(session: UserSession, user: User): String = createHTML().html {
    head {
        title { +"Личный кабинет" }
        style { unsafe { +commonStyles() } }
    }
    body {
        navbar(session)
        div("container") {
            h2 {
                attributes["style"] = "margin-bottom: 6px; color: #333;"
                +"Добро пожаловать, ${user.firstName}!"
            }
            p {
                attributes["style"] = "color: #888; margin-bottom: 28px; font-size: 14px;"
                +"Роль: "
                span("badge badge-${user.role.name.lowercase()}") { +user.role.displayName }
            }
            when (user.role) {
                UserRole.ADMIN -> adminDashboardContent()
                UserRole.LOGOPED -> logopedDashboardContent()
                UserRole.STUDENT -> studentDashboardContent()
            }
        }
    }
}

private fun FlowContent.adminDashboardContent() {
    div("grid-2") {
        dashboardCard(
            "Управление пользователями",
            "Создание, блокировка и удаление аккаунтов логопедов и учеников.",
            "/admin/users", "Открыть", false
        )
        dashboardCard(
            "Библиотека упражнений",
            "Просмотр упражнений по звукам и категориям.",
            "/sounds", "Открыть", false
        )
    }
}

private fun FlowContent.logopedDashboardContent() {
    div("grid-2") {
        dashboardCard("Библиотека упражнений", "Упражнения для работы с учениками.", "/sounds", "Открыть", false)
        dashboardCard("Ученики", "Управление карточками учеников.", "/students", "Открыть", false)
        dashboardCard("Занятия", "Запись проведённых занятий и история прогресса.", "/sessions", "Открыть", false)
        dashboardCard("Домашние задания", "Назначение и контроль выполнения заданий.", "/homework", "Открыть", false)
        dashboardCard("Диагностика", "Протоколы диагностики и планы коррекции.", "#", "Скоро", true)
    }
}

private fun FlowContent.studentDashboardContent() {
    div("grid-2") {
        dashboardCard("Упражнения", "Упражнения по автоматизации звуков.", "/sounds", "Открыть", false)
        dashboardCard("Анкета ребёнка", "Заполните данные для логопеда: анамнез, развитие, заболевания.", "/student/anamnesis", "Заполнить", false)
        dashboardCard("Домашние задания", "Задания, назначенные логопедом.", "/student/homework", "Открыть", false)
    }
}

private fun FlowContent.dashboardCard(title: String, description: String, href: String, btnText: String, disabled: Boolean) {
    div("card") {
        div("section-title") { +title }
        p {
            attributes["style"] = "color: #666; font-size: 14px; margin-bottom: 20px; line-height: 1.6;"
            +description
        }
        a(href) {
            attributes["class"] = if (disabled) "btn btn-secondary" else "btn btn-primary"
            +btnText
        }
    }
}

fun profilePage(user: User, session: UserSession, success: Boolean = false, error: String? = null): String = createHTML().html {
    head {
        title { +"Профиль" }
        style { unsafe { +commonStyles() } }
    }
    body {
        navbar(session)
        div("container") {
            h2 {
                attributes["style"] = "margin-bottom: 24px; color: #333;"
                +"Мой профиль"
            }
            if (success) {
                div("alert alert-success") { +"Данные успешно сохранены." }
            }
            if (error != null) {
                div("alert alert-error") { +error }
            }
            div("card") {
                div("section-title") { +"Личные данные" }
                form(method = FormMethod.post, action = "/profile") {
                    div("grid-2") {
                        div("form-group") {
                            label { +"Фамилия *" }
                            input(type = InputType.text, name = "lastName") {
                                attributes["class"] = "form-control"
                                attributes["required"] = "true"
                                attributes["value"] = user.lastName
                            }
                        }
                        div("form-group") {
                            label { +"Имя *" }
                            input(type = InputType.text, name = "firstName") {
                                attributes["class"] = "form-control"
                                attributes["required"] = "true"
                                attributes["value"] = user.firstName
                            }
                        }
                    }
                    div("form-group") {
                        label { +"Отчество" }
                        input(type = InputType.text, name = "middleName") {
                            attributes["class"] = "form-control"
                            attributes["value"] = user.middleName ?: ""
                        }
                    }
                    div("form-group") {
                        label { +"Email (изменить нельзя)" }
                        input(type = InputType.email) {
                            attributes["class"] = "form-control"
                            attributes["value"] = user.email
                            attributes["disabled"] = "true"
                        }
                    }
                    div("form-group") {
                        label { +"Телефон" }
                        input(type = InputType.tel, name = "phone") {
                            attributes["class"] = "form-control"
                            attributes["value"] = user.phone ?: ""
                            attributes["placeholder"] = "+7 (___) ___-__-__"
                        }
                    }
                    button(type = ButtonType.submit) {
                        attributes["class"] = "btn btn-primary"
                        +"Сохранить изменения"
                    }
                }
            }
            div("card") {
                div("section-title") { +"Смена пароля" }
                form(method = FormMethod.post, action = "/profile/password") {
                    div("form-group") {
                        label { +"Текущий пароль" }
                        input(type = InputType.password, name = "currentPassword") {
                            attributes["class"] = "form-control"
                            attributes["required"] = "true"
                        }
                    }
                    div("form-group") {
                        label { +"Новый пароль (минимум 6 символов)" }
                        input(type = InputType.password, name = "newPassword") {
                            attributes["class"] = "form-control"
                            attributes["required"] = "true"
                            attributes["minlength"] = "6"
                        }
                    }
                    div("form-group") {
                        label { +"Повторите новый пароль" }
                        input(type = InputType.password, name = "confirmPassword") {
                            attributes["class"] = "form-control"
                            attributes["required"] = "true"
                        }
                    }
                    button(type = ButtonType.submit) {
                        attributes["class"] = "btn btn-warning"
                        +"Изменить пароль"
                    }
                }
            }
        }
    }
}

fun adminUsersPage(session: UserSession, users: List<User>): String = createHTML().html {
    head {
        title { +"Пользователи" }
        style { unsafe { +commonStyles() } }
    }
    body {
        navbar(session)
        div("container") {
            div {
                attributes["style"] = "display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;"
                h2 {
                    attributes["style"] = "color: #333; margin: 0;"
                    +"Управление пользователями"
                }
                a("/admin/users/create") {
                    attributes["class"] = "btn btn-primary"
                    +"+ Создать пользователя"
                }
            }
            div("card") {
                val manageable = users.filter { it.email != "admin@logoped.ru" }
                if (manageable.isEmpty()) {
                    p {
                        attributes["style"] = "color: #888; text-align: center; padding: 30px;"
                        +"Пользователей пока нет."
                    }
                } else {
                    table {
                        thead {
                            tr {
                                th { +"ФИО" }
                                th { +"Email" }
                                th { +"Роль" }
                                th { +"Статус" }
                                th { +"Действия" }
                            }
                        }
                        tbody {
                            manageable.forEach { u ->
                                tr {
                                    td { +u.fullName }
                                    td { +u.email }
                                    td {
                                        span("badge badge-${u.role.name.lowercase()}") {
                                            +u.role.displayName
                                        }
                                    }
                                    td {
                                        if (u.isBlocked) {
                                            span("badge badge-blocked") { +"Заблокирован" }
                                        } else {
                                            span("badge badge-active") { +"Активен" }
                                        }
                                    }
                                    td {
                                        div("actions") {
                                            if (u.isBlocked) {
                                                form(method = FormMethod.post, action = "/admin/users/${u.id}/unblock") {
                                                    button(type = ButtonType.submit) {
                                                        attributes["class"] = "btn btn-primary btn-sm"
                                                        +"Разблокировать"
                                                    }
                                                }
                                            } else {
                                                form(method = FormMethod.post, action = "/admin/users/${u.id}/block") {
                                                    button(type = ButtonType.submit) {
                                                        attributes["class"] = "btn btn-warning btn-sm"
                                                        +"Блокировать"
                                                    }
                                                }
                                            }
                                            form(method = FormMethod.post, action = "/admin/users/${u.id}/delete") {
                                                button(type = ButtonType.submit) {
                                                    attributes["class"] = "btn btn-danger btn-sm"
                                                    attributes["onclick"] = "return confirm('Удалить ${u.fullName}?')"
                                                    +"Удалить"
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

fun adminCreateUserPage(session: UserSession, error: String? = null): String = createHTML().html {
    head {
        title { +"Создать пользователя" }
        style { unsafe { +commonStyles() } }
    }
    body {
        navbar(session)
        div("container") {
            div {
                attributes["style"] = "display: flex; align-items: center; gap: 16px; margin-bottom: 24px;"
                a("/admin/users") {
                    attributes["class"] = "btn btn-secondary btn-sm"
                    +"← Назад"
                }
                h2 {
                    attributes["style"] = "color: #333; margin: 0;"
                    +"Создать пользователя"
                }
            }
            div("card") {
                attributes["style"] = "max-width: 500px;"
                if (error != null) {
                    div("alert alert-error") { +error }
                }
                form(method = FormMethod.post, action = "/admin/users/create") {
                    div("grid-2") {
                        div("form-group") {
                            label { +"Фамилия *" }
                            input(type = InputType.text, name = "lastName") {
                                attributes["class"] = "form-control"
                                attributes["required"] = "true"
                            }
                        }
                        div("form-group") {
                            label { +"Имя *" }
                            input(type = InputType.text, name = "firstName") {
                                attributes["class"] = "form-control"
                                attributes["required"] = "true"
                            }
                        }
                    }
                    div("form-group") {
                        label { +"Отчество" }
                        input(type = InputType.text, name = "middleName") {
                            attributes["class"] = "form-control"
                        }
                    }
                    div("form-group") {
                        label { +"Email *" }
                        input(type = InputType.email, name = "email") {
                            attributes["class"] = "form-control"
                            attributes["required"] = "true"
                        }
                    }
                    div("form-group") {
                        label { +"Пароль *" }
                        input(type = InputType.password, name = "password") {
                            attributes["class"] = "form-control"
                            attributes["required"] = "true"
                            attributes["minlength"] = "6"
                        }
                    }
                    div("form-group") {
                        label { +"Роль *" }
                        select {
                            name = "role"
                            attributes["class"] = "form-control"
                            option {
                                value = ""
                                +"-- Выберите роль --"
                            }
                            option {
                                value = "ADMIN"
                                +"Администратор"
                            }
                            option {
                                value = "LOGOPED"
                                +"Логопед"
                            }
                            option {
                                value = "STUDENT"
                                +"Ученик"
                            }
                        }
                    }
                    button(type = ButtonType.submit) {
                        attributes["class"] = "btn btn-primary"
                        +"Создать аккаунт"
                    }
                }
            }
        }
    }
}
