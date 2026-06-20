package routes

import database.dao.UserDAO
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import models.UserRole
import models.UserSession
import org.mindrot.jbcrypt.BCrypt
import pages.*

fun Route.registerAuthRoutes() {

    get("/") {
        val session = call.sessions.get<UserSession>()
        call.respondRedirect(if (session != null) "/dashboard" else "/login")
    }

    // ── Вход ──────────────────────────────────────────────────────────────
    get("/login") {
        if (call.sessions.get<UserSession>() != null) {
            call.respondRedirect("/dashboard"); return@get
        }
        val errorMsg = when (call.request.queryParameters["error"]) {
            "invalid"  -> "Неверный email или пароль."
            "blocked"  -> "Ваш аккаунт заблокирован. Обратитесь к администратору."
            else       -> null
        }
        call.respondText(loginPage(errorMsg), ContentType.Text.Html)
    }

    post("/login") {
        val params = call.receiveParameters()
        val email    = params["email"]?.trim() ?: ""
        val password = params["password"] ?: ""

        val user = UserDAO.findByEmail(email)
        when {
            user == null || !BCrypt.checkpw(password, user.passwordHash) ->
                call.respondRedirect("/login?error=invalid")
            user.isBlocked ->
                call.respondRedirect("/login?error=blocked")
            else -> {
                call.sessions.set(UserSession(user.id, user.email, user.role.name, user.firstName))
                call.respondRedirect("/dashboard")
            }
        }
    }

    // ── Регистрация ────────────────────────────────────────────────────────
    get("/register") {
        if (call.sessions.get<UserSession>() != null) {
            call.respondRedirect("/dashboard"); return@get
        }
        call.respondText(registerPage(), ContentType.Text.Html)
    }

    post("/register") {
        val params          = call.receiveParameters()
        val email           = params["email"]?.trim() ?: ""
        val password        = params["password"] ?: ""
        val confirmPassword = params["confirmPassword"] ?: ""
        val firstName       = params["firstName"]?.trim() ?: ""
        val lastName        = params["lastName"]?.trim() ?: ""
        val middleName      = params["middleName"]?.trim()?.takeIf { it.isNotBlank() }
        val roleStr         = params["role"] ?: ""

        val error = when {
            lastName.isBlank()                          -> "Введите фамилию."
            firstName.isBlank()                         -> "Введите имя."
            email.isBlank() || !email.contains("@")     -> "Введите корректный email."
            password.length < 6                         -> "Пароль должен быть не менее 6 символов."
            password != confirmPassword                 -> "Пароли не совпадают."
            roleStr !in listOf("LOGOPED", "STUDENT")   -> "Выберите роль."
            UserDAO.emailExists(email)                  -> "Пользователь с таким email уже существует."
            else                                        -> null
        }

        if (error != null) {
            call.respondText(registerPage(error), ContentType.Text.Html); return@post
        }

        val id = UserDAO.create(
            email, BCrypt.hashpw(password, BCrypt.gensalt()),
            UserRole.valueOf(roleStr), firstName, lastName, middleName, null, null
        )
        val user = UserDAO.findById(id)!!
        call.sessions.set(UserSession(user.id, user.email, user.role.name, user.firstName))
        call.respondRedirect("/dashboard")
    }

    // ── Выход ─────────────────────────────────────────────────────────────
    get("/logout") {
        call.sessions.clear<UserSession>()
        call.respondRedirect("/login")
    }

    // ── Личный кабинет ────────────────────────────────────────────────────
    get("/dashboard") {
        val session = call.sessions.get<UserSession>() ?: run {
            call.respondRedirect("/login"); return@get
        }
        val user = UserDAO.findById(session.userId) ?: run {
            call.sessions.clear<UserSession>(); call.respondRedirect("/login"); return@get
        }
        if (user.isBlocked) {
            call.sessions.clear<UserSession>(); call.respondRedirect("/login?error=blocked"); return@get
        }
        call.respondText(dashboardPage(session, user), ContentType.Text.Html)
    }

    // ── Профиль ───────────────────────────────────────────────────────────
    get("/profile") {
        val session = call.sessions.get<UserSession>() ?: run {
            call.respondRedirect("/login"); return@get
        }
        val user = UserDAO.findById(session.userId) ?: run {
            call.sessions.clear<UserSession>(); call.respondRedirect("/login"); return@get
        }
        val success = call.request.queryParameters["saved"] == "1"
        val error = when (call.request.queryParameters["error"]) {
            "wrong_password"    -> "Неверный текущий пароль."
            "password_mismatch" -> "Новые пароли не совпадают."
            "password_short"    -> "Новый пароль должен быть не менее 6 символов."
            else                -> null
        }
        call.respondText(profilePage(user, session, success, error), ContentType.Text.Html)
    }

    post("/profile") {
        val session = call.sessions.get<UserSession>() ?: run {
            call.respondRedirect("/login"); return@post
        }
        val params        = call.receiveParameters()
        val firstName     = params["firstName"]?.trim() ?: ""
        val lastName      = params["lastName"]?.trim() ?: ""
        val middleName    = params["middleName"]?.trim()?.takeIf { it.isNotBlank() }
        val phone         = params["phone"]?.trim()?.takeIf { it.isNotBlank() }
        val specialization = params["specialization"]?.trim()?.takeIf { it.isNotBlank() }

        if (firstName.isBlank() || lastName.isBlank()) {
            val user = UserDAO.findById(session.userId)!!
            call.respondText(profilePage(user, session, false, "Имя и фамилия обязательны."), ContentType.Text.Html)
            return@post
        }

        UserDAO.updateProfile(session.userId, firstName, lastName, middleName, phone, specialization)
        call.sessions.set(session.copy(firstName = firstName))
        call.respondRedirect("/profile?saved=1")
    }

    post("/profile/password") {
        val session = call.sessions.get<UserSession>() ?: run {
            call.respondRedirect("/login"); return@post
        }
        val params          = call.receiveParameters()
        val currentPassword = params["currentPassword"] ?: ""
        val newPassword     = params["newPassword"] ?: ""
        val confirmPassword = params["confirmPassword"] ?: ""

        val user = UserDAO.findById(session.userId)!!
        val errorParam = when {
            !BCrypt.checkpw(currentPassword, user.passwordHash) -> "wrong_password"
            newPassword.length < 6                              -> "password_short"
            newPassword != confirmPassword                      -> "password_mismatch"
            else                                                -> null
        }
        if (errorParam != null) {
            call.respondRedirect("/profile?error=$errorParam"); return@post
        }

        UserDAO.updatePassword(session.userId, BCrypt.hashpw(newPassword, BCrypt.gensalt()))
        call.respondRedirect("/profile?saved=1")
    }

    // ── Администрирование пользователей ───────────────────────────────────
    get("/admin/users") {
        val session = call.sessions.get<UserSession>() ?: run {
            call.respondRedirect("/login"); return@get
        }
        if (session.role != "ADMIN") { call.respondRedirect("/dashboard"); return@get }
        call.respondText(adminUsersPage(session, UserDAO.findAll()), ContentType.Text.Html)
    }

    get("/admin/users/create") {
        val session = call.sessions.get<UserSession>() ?: run {
            call.respondRedirect("/login"); return@get
        }
        if (session.role != "ADMIN") { call.respondRedirect("/dashboard"); return@get }
        call.respondText(adminCreateUserPage(session), ContentType.Text.Html)
    }

    post("/admin/users/create") {
        val session = call.sessions.get<UserSession>() ?: run {
            call.respondRedirect("/login"); return@post
        }
        if (session.role != "ADMIN") { call.respondRedirect("/dashboard"); return@post }

        val params     = call.receiveParameters()
        val email      = params["email"]?.trim() ?: ""
        val password   = params["password"] ?: ""
        val firstName  = params["firstName"]?.trim() ?: ""
        val lastName   = params["lastName"]?.trim() ?: ""
        val middleName = params["middleName"]?.trim()?.takeIf { it.isNotBlank() }
        val roleStr    = params["role"] ?: ""

        val error = when {
            lastName.isBlank()                        -> "Введите фамилию."
            firstName.isBlank()                       -> "Введите имя."
            email.isBlank() || !email.contains("@")   -> "Введите корректный email."
            password.length < 6                       -> "Пароль должен быть не менее 6 символов."
            roleStr !in listOf("ADMIN", "LOGOPED", "STUDENT") -> "Выберите роль."
            UserDAO.emailExists(email)                       -> "Пользователь с таким email уже существует."
            else                                             -> null
        }
        if (error != null) {
            call.respondText(adminCreateUserPage(session, error), ContentType.Text.Html); return@post
        }

        UserDAO.create(
            email, BCrypt.hashpw(password, BCrypt.gensalt()),
            UserRole.valueOf(roleStr), firstName, lastName, middleName, null, null
        )
        call.respondRedirect("/admin/users")
    }

    post("/admin/users/{id}/block") {
        val session = call.sessions.get<UserSession>() ?: run {
            call.respondRedirect("/login"); return@post
        }
        if (session.role != "ADMIN") { call.respondRedirect("/dashboard"); return@post }
        val id = call.parameters["id"]?.toIntOrNull() ?: run {
            call.respondRedirect("/admin/users"); return@post
        }
        UserDAO.setBlocked(id, true)
        call.respondRedirect("/admin/users")
    }

    post("/admin/users/{id}/unblock") {
        val session = call.sessions.get<UserSession>() ?: run {
            call.respondRedirect("/login"); return@post
        }
        if (session.role != "ADMIN") { call.respondRedirect("/dashboard"); return@post }
        val id = call.parameters["id"]?.toIntOrNull() ?: run {
            call.respondRedirect("/admin/users"); return@post
        }
        UserDAO.setBlocked(id, false)
        call.respondRedirect("/admin/users")
    }

    post("/admin/users/{id}/delete") {
        val session = call.sessions.get<UserSession>() ?: run {
            call.respondRedirect("/login"); return@post
        }
        if (session.role != "ADMIN") { call.respondRedirect("/dashboard"); return@post }
        val id = call.parameters["id"]?.toIntOrNull() ?: run {
            call.respondRedirect("/admin/users"); return@post
        }
        UserDAO.delete(id)
        call.respondRedirect("/admin/users")
    }
}
