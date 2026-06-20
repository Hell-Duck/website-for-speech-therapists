package routes

import database.dao.HomeworkDAO
import database.dao.SessionDAO
import database.dao.StudentCardDAO
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import models.UserSession
import pages.*

fun Route.registerSessionRoutes() {

    val logopedAdmin = listOf("LOGOPED", "ADMIN")

    // ══ Занятия ═══════════════════════════════════════════════════════════════

    get("/sessions") {
        val session = call.sessions.get<UserSession>() ?: run { call.respondRedirect("/login"); return@get }
        if (session.role !in logopedAdmin) { call.respondRedirect("/dashboard"); return@get }

        val selectedStudentId = call.request.queryParameters["studentId"]?.toIntOrNull()
        val views = if (session.role == "ADMIN")
            SessionDAO.findAllViews(selectedStudentId)
        else
            SessionDAO.findViewsByLogoped(session.userId, selectedStudentId)

        val studentViews = if (session.role == "ADMIN") StudentCardDAO.findAllViews()
                           else StudentCardDAO.findViewsByLogoped(session.userId)

        call.respondText(sessionListPage(session, views, studentViews, selectedStudentId), ContentType.Text.Html)
    }

    get("/sessions/create") {
        val session = call.sessions.get<UserSession>() ?: run { call.respondRedirect("/login"); return@get }
        if (session.role !in logopedAdmin) { call.respondRedirect("/dashboard"); return@get }

        val preselected = call.request.queryParameters["studentId"]?.toIntOrNull()
        val studentViews = if (session.role == "ADMIN") StudentCardDAO.findAllViews()
                           else StudentCardDAO.findViewsByLogoped(session.userId)
        call.respondText(sessionFormPage(session, studentViews, preselectedStudentId = preselected), ContentType.Text.Html)
    }

    post("/sessions/create") {
        val session = call.sessions.get<UserSession>() ?: run { call.respondRedirect("/login"); return@post }
        if (session.role !in logopedAdmin) { call.respondRedirect("/dashboard"); return@post }

        val params         = call.receiveParameters()
        val studentUserId  = params["studentUserId"]?.toIntOrNull()
        val sessionDate    = params["sessionDate"]?.trim() ?: ""
        val exerciseKeys   = params.getAll("exercises") ?: emptyList()
        val comments       = params["comments"]?.trim()?.takeIf { it.isNotBlank() }
        val progressRating = params["progressRating"]?.toIntOrNull() ?: 3

        val studentViews = if (session.role == "ADMIN") StudentCardDAO.findAllViews()
                           else StudentCardDAO.findViewsByLogoped(session.userId)

        val error = when {
            studentUserId == null  -> "Выберите ученика."
            sessionDate.isBlank()  -> "Укажите дату занятия."
            else                   -> null
        }
        if (error != null) {
            call.respondText(sessionFormPage(session, studentViews, error = error), ContentType.Text.Html)
            return@post
        }

        val exercises = exerciseKeys.filter { it.isNotBlank() }.joinToString("|")
        val id = SessionDAO.create(studentUserId!!, session.userId, sessionDate, exercises, comments, progressRating)
        call.respondRedirect("/sessions/$id")
    }

    get("/sessions/{id}") {
        val session = call.sessions.get<UserSession>() ?: run { call.respondRedirect("/login"); return@get }
        if (session.role !in logopedAdmin) { call.respondRedirect("/dashboard"); return@get }
        val id = call.parameters["id"]?.toIntOrNull() ?: run { call.respondRedirect("/sessions"); return@get }
        val view = SessionDAO.findViewById(id) ?: run { call.respondRedirect("/sessions"); return@get }
        if (session.role != "ADMIN" && view.session.logopedId != session.userId) { call.respondRedirect("/sessions"); return@get }
        call.respondText(sessionViewPage(session, view), ContentType.Text.Html)
    }

    get("/sessions/{id}/edit") {
        val session = call.sessions.get<UserSession>() ?: run { call.respondRedirect("/login"); return@get }
        if (session.role !in logopedAdmin) { call.respondRedirect("/dashboard"); return@get }
        val id = call.parameters["id"]?.toIntOrNull() ?: run { call.respondRedirect("/sessions"); return@get }
        val view = SessionDAO.findViewById(id) ?: run { call.respondRedirect("/sessions"); return@get }
        if (session.role != "ADMIN" && view.session.logopedId != session.userId) { call.respondRedirect("/sessions"); return@get }
        val studentViews = if (session.role == "ADMIN") StudentCardDAO.findAllViews()
                           else StudentCardDAO.findViewsByLogoped(session.userId)
        call.respondText(sessionFormPage(session, studentViews, view.session), ContentType.Text.Html)
    }

    post("/sessions/{id}/edit") {
        val session = call.sessions.get<UserSession>() ?: run { call.respondRedirect("/login"); return@post }
        if (session.role !in logopedAdmin) { call.respondRedirect("/dashboard"); return@post }
        val id = call.parameters["id"]?.toIntOrNull() ?: run { call.respondRedirect("/sessions"); return@post }
        val existing = SessionDAO.findById(id) ?: run { call.respondRedirect("/sessions"); return@post }
        if (session.role != "ADMIN" && existing.logopedId != session.userId) { call.respondRedirect("/sessions"); return@post }

        val params         = call.receiveParameters()
        val sessionDate    = params["sessionDate"]?.trim() ?: ""
        val exerciseKeys   = params.getAll("exercises") ?: emptyList()
        val comments       = params["comments"]?.trim()?.takeIf { it.isNotBlank() }
        val progressRating = params["progressRating"]?.toIntOrNull() ?: 3

        val exercises = exerciseKeys.filter { it.isNotBlank() }.joinToString("|")
        SessionDAO.update(id, sessionDate, exercises, comments, progressRating)
        call.respondRedirect("/sessions/$id")
    }

    post("/sessions/{id}/delete") {
        val session = call.sessions.get<UserSession>() ?: run { call.respondRedirect("/login"); return@post }
        if (session.role !in logopedAdmin) { call.respondRedirect("/dashboard"); return@post }
        val id = call.parameters["id"]?.toIntOrNull() ?: run { call.respondRedirect("/sessions"); return@post }
        val existing = SessionDAO.findById(id) ?: run { call.respondRedirect("/sessions"); return@post }
        if (session.role != "ADMIN" && existing.logopedId != session.userId) { call.respondRedirect("/sessions"); return@post }
        SessionDAO.delete(id)
        call.respondRedirect("/sessions")
    }

    // ══ Домашние задания ══════════════════════════════════════════════════════

    get("/homework") {
        val session = call.sessions.get<UserSession>() ?: run { call.respondRedirect("/login"); return@get }
        if (session.role !in logopedAdmin) { call.respondRedirect("/dashboard"); return@get }

        val statusFilter = call.request.queryParameters["status"] ?: "ALL"
        val allViews = if (session.role == "ADMIN") HomeworkDAO.findAllViews()
                       else HomeworkDAO.findViewsByLogoped(session.userId)
        val views = if (statusFilter == "ALL") allViews
                    else allViews.filter { it.homework.effectiveStatus.name == statusFilter }
        call.respondText(homeworkListPage(session, views, statusFilter), ContentType.Text.Html)
    }

    get("/homework/create") {
        val session = call.sessions.get<UserSession>() ?: run { call.respondRedirect("/login"); return@get }
        if (session.role !in logopedAdmin) { call.respondRedirect("/dashboard"); return@get }

        val preselected = call.request.queryParameters["studentId"]?.toIntOrNull()
        val studentViews = if (session.role == "ADMIN") StudentCardDAO.findAllViews()
                           else StudentCardDAO.findViewsByLogoped(session.userId)
        call.respondText(homeworkFormPage(session, studentViews, preselected), ContentType.Text.Html)
    }

    post("/homework/create") {
        val session = call.sessions.get<UserSession>() ?: run { call.respondRedirect("/login"); return@post }
        if (session.role !in logopedAdmin) { call.respondRedirect("/dashboard"); return@post }

        val params        = call.receiveParameters()
        val studentUserId = params["studentUserId"]?.toIntOrNull()
        val dueDate       = params["dueDate"]?.trim() ?: ""
        val exerciseKeys  = params.getAll("exercises") ?: emptyList()
        val parentComment = params["parentComment"]?.trim()?.takeIf { it.isNotBlank() }

        val studentViews = if (session.role == "ADMIN") StudentCardDAO.findAllViews()
                           else StudentCardDAO.findViewsByLogoped(session.userId)

        val error = when {
            studentUserId == null        -> "Выберите ученика."
            dueDate.isBlank()            -> "Укажите срок выполнения."
            exerciseKeys.none { it.isNotBlank() } -> "Выберите хотя бы одно упражнение."
            else                         -> null
        }
        if (error != null) {
            call.respondText(homeworkFormPage(session, studentViews, studentUserId, error), ContentType.Text.Html)
            return@post
        }

        val exercises = exerciseKeys.filter { it.isNotBlank() }.joinToString("|")
        HomeworkDAO.create(studentUserId!!, session.userId, exercises, dueDate, parentComment)
        call.respondRedirect("/homework")
    }

    post("/homework/{id}/delete") {
        val session = call.sessions.get<UserSession>() ?: run { call.respondRedirect("/login"); return@post }
        if (session.role !in logopedAdmin) { call.respondRedirect("/dashboard"); return@post }
        val id = call.parameters["id"]?.toIntOrNull() ?: run { call.respondRedirect("/homework"); return@post }
        val hw = HomeworkDAO.findById(id) ?: run { call.respondRedirect("/homework"); return@post }
        if (session.role != "ADMIN" && hw.logopedId != session.userId) { call.respondRedirect("/homework"); return@post }
        HomeworkDAO.delete(id)
        call.respondRedirect("/homework")
    }

    // ══ Ученик: просмотр и выполнение ════════════════════════════════════════

    get("/student/homework") {
        val session = call.sessions.get<UserSession>() ?: run { call.respondRedirect("/login"); return@get }
        if (session.role != "STUDENT") { call.respondRedirect("/dashboard"); return@get }

        val statusFilter = call.request.queryParameters["status"] ?: "ALL"
        val all = HomeworkDAO.findByStudent(session.userId)
        val filtered = if (statusFilter == "ALL") all
                       else all.filter { it.effectiveStatus.name == statusFilter }
        call.respondText(studentHomeworkPage(session, filtered, statusFilter), ContentType.Text.Html)
    }

    post("/homework/{id}/complete") {
        val session = call.sessions.get<UserSession>() ?: run { call.respondRedirect("/login"); return@post }
        if (session.role != "STUDENT") { call.respondRedirect("/dashboard"); return@post }
        val id = call.parameters["id"]?.toIntOrNull() ?: run { call.respondRedirect("/student/homework"); return@post }
        val hw = HomeworkDAO.findById(id) ?: run { call.respondRedirect("/student/homework"); return@post }
        if (hw.studentUserId != session.userId) { call.respondRedirect("/student/homework"); return@post }

        val params         = call.receiveParameters()
        val studentComment = params["studentComment"]?.trim()?.takeIf { it.isNotBlank() }
        HomeworkDAO.markCompleted(id, studentComment)
        call.respondRedirect("/student/homework")
    }
}
