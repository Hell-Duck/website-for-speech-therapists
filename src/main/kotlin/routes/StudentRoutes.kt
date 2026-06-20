package routes

import database.dao.StudentCardDAO
import database.dao.StudentProfileDAO
import database.dao.StudentProfileData
import database.dao.UserDAO
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import models.StudentStatus
import models.UserRole
import models.UserSession
import pages.*

fun Route.registerStudentRoutes() {

    // ── Список карточек (логопед / админ) ─────────────────────────────────
    get("/students") {
        val session = call.sessions.get<UserSession>() ?: run {
            call.respondRedirect("/login"); return@get
        }
        if (session.role !in listOf("LOGOPED", "ADMIN")) {
            call.respondRedirect("/dashboard"); return@get
        }
        val search       = call.request.queryParameters["search"]?.trim() ?: ""
        val statusFilter = call.request.queryParameters["status"] ?: "ALL"

        // Собираем все видимые карточки
        val allVisible = if (session.role == "ADMIN") {
            StudentCardDAO.findAllViews()
        } else {
            // Свои карточки + пул завершённых от всех логопедов
            val own  = StudentCardDAO.findViewsByLogoped(session.userId)
            val pool = StudentCardDAO.findCompletedPoolViews()
            (own + pool).distinctBy { it.card.id }
        }

        // Дедупликация: один ученик — одна строка.
        // Приоритет: своя активная карточка > любая активная > самая свежая
        val byStudent = allVisible
            .groupBy { it.card.studentUserId }
            .values
            .map { cards ->
                cards.firstOrNull { it.card.logopedId == session.userId && it.card.status == StudentStatus.ACTIVE }
                    ?: cards.firstOrNull { it.card.status == StudentStatus.ACTIVE }
                    ?: cards.maxByOrNull { it.card.createdAt }
                    ?: cards.first()
            }
            .sortedByDescending { it.card.createdAt }

        // Применяем фильтр по статусу и поиск
        val filtered = byStudent
            .let { if (statusFilter != "ALL") it.filter { v -> v.card.status.name == statusFilter } else it }
            .let { if (search.isNotBlank()) it.filter { v -> v.studentUser.fullName.contains(search, ignoreCase = true) } else it }

        call.respondText(studentListPage(session, filtered, search, statusFilter), ContentType.Text.Html)
    }

    // ── Создание карточки: шаг 1 — выбор ученика ──────────────────────────
    get("/students/create") {
        val session = call.sessions.get<UserSession>() ?: run {
            call.respondRedirect("/login"); return@get
        }
        if (session.role !in listOf("LOGOPED", "ADMIN")) {
            call.respondRedirect("/dashboard"); return@get
        }

        val studentId = call.request.queryParameters["studentId"]?.toIntOrNull()
        if (studentId != null) {
            val studentUser = UserDAO.findById(studentId)
            if (studentUser == null || studentUser.role != UserRole.STUDENT) {
                call.respondRedirect("/students/create"); return@get
            }
            call.respondText(studentCardFormPage(session, studentUser), ContentType.Text.Html)
            return@get
        }

        val search = call.request.queryParameters["search"]?.trim() ?: ""
        val allStudents = UserDAO.findAll().filter { it.role == UserRole.STUDENT && !it.isBlocked }
        val available = allStudents
            .filter { !StudentCardDAO.existsActiveForStudent(it.id) }
            .let { if (search.isNotBlank()) it.filter { u -> u.fullName.contains(search, ignoreCase = true) } else it }

        call.respondText(studentCreateSearchPage(session, available, search), ContentType.Text.Html)
    }

    // ── Создание карточки: шаг 2 — сохранение ─────────────────────────────
    post("/students/create") {
        val session = call.sessions.get<UserSession>() ?: run {
            call.respondRedirect("/login"); return@post
        }
        if (session.role !in listOf("LOGOPED", "ADMIN")) {
            call.respondRedirect("/dashboard"); return@post
        }

        val params        = call.receiveParameters()
        val studentUserId = params["studentUserId"]?.toIntOrNull() ?: run {
            call.respondRedirect("/students/create"); return@post
        }
        val studentUser = UserDAO.findById(studentUserId) ?: run {
            call.respondRedirect("/students/create"); return@post
        }

        if (StudentCardDAO.existsActiveForStudent(studentUserId)) {
            call.respondText(
                studentCardFormPage(session, studentUser, error = "У ученика уже есть активная карточка."),
                ContentType.Text.Html
            )
            return@post
        }

        val conclusion     = params["conclusion"]?.trim()?.takeIf { it.isNotBlank() }
        val correctionPlan = params["correctionPlan"]?.trim()?.takeIf { it.isNotBlank() }
        val notes          = params["notes"]?.trim()?.takeIf { it.isNotBlank() }
        val status         = StudentStatus.valueOf(params["status"] ?: StudentStatus.ACTIVE.name)

        val id = StudentCardDAO.create(studentUserId, session.userId, conclusion, correctionPlan, notes, status)
        call.respondRedirect("/students/$id")
    }

    // ── Просмотр карточки ──────────────────────────────────────────────────
    get("/students/{id}") {
        val session = call.sessions.get<UserSession>() ?: run {
            call.respondRedirect("/login"); return@get
        }
        if (session.role !in listOf("LOGOPED", "ADMIN")) {
            call.respondRedirect("/dashboard"); return@get
        }
        val id = call.parameters["id"]?.toIntOrNull() ?: run {
            call.respondRedirect("/students"); return@get
        }
        val view = StudentCardDAO.findViewById(id) ?: run {
            call.respondRedirect("/students"); return@get
        }
        // Карточки из пула (завершённые) видны всем логопедам; активные — только своему логопеду
        val canView = session.role == "ADMIN" ||
                      view.card.logopedId == session.userId ||
                      view.card.status == StudentStatus.COMPLETED
        if (!canView) { call.respondRedirect("/students"); return@get }
        val profile = StudentProfileDAO.findByUserId(view.card.studentUserId)
        val history = StudentCardDAO.findCardHistoryForStudent(view.card.studentUserId, view.card.id)
        call.respondText(studentViewPage(session, view, profile, history), ContentType.Text.Html)
    }

    // ── Редактирование карточки ────────────────────────────────────────────
    get("/students/{id}/edit") {
        val session = call.sessions.get<UserSession>() ?: run {
            call.respondRedirect("/login"); return@get
        }
        if (session.role !in listOf("LOGOPED", "ADMIN")) {
            call.respondRedirect("/dashboard"); return@get
        }
        val id = call.parameters["id"]?.toIntOrNull() ?: run {
            call.respondRedirect("/students"); return@get
        }
        val view = StudentCardDAO.findViewById(id) ?: run {
            call.respondRedirect("/students"); return@get
        }
        if (session.role != "ADMIN" && view.card.logopedId != session.userId) {
            call.respondRedirect("/students"); return@get
        }
        call.respondText(studentCardFormPage(session, view.studentUser, view.card), ContentType.Text.Html)
    }

    post("/students/{id}/edit") {
        val session = call.sessions.get<UserSession>() ?: run {
            call.respondRedirect("/login"); return@post
        }
        if (session.role !in listOf("LOGOPED", "ADMIN")) {
            call.respondRedirect("/dashboard"); return@post
        }
        val id = call.parameters["id"]?.toIntOrNull() ?: run {
            call.respondRedirect("/students"); return@post
        }
        val view = StudentCardDAO.findViewById(id) ?: run {
            call.respondRedirect("/students"); return@post
        }
        if (session.role != "ADMIN" && view.card.logopedId != session.userId) {
            call.respondRedirect("/students"); return@post
        }

        val params         = call.receiveParameters()
        val conclusion     = params["conclusion"]?.trim()?.takeIf { it.isNotBlank() }
        val correctionPlan = params["correctionPlan"]?.trim()?.takeIf { it.isNotBlank() }
        val notes          = params["notes"]?.trim()?.takeIf { it.isNotBlank() }
        val status         = StudentStatus.valueOf(params["status"] ?: StudentStatus.ACTIVE.name)

        StudentCardDAO.update(id, conclusion, correctionPlan, notes, status)
        call.respondRedirect("/students/$id")
    }

    // ── Удаление карточки ──────────────────────────────────────────────────
    post("/students/{id}/delete") {
        val session = call.sessions.get<UserSession>() ?: run {
            call.respondRedirect("/login"); return@post
        }
        if (session.role !in listOf("LOGOPED", "ADMIN")) {
            call.respondRedirect("/dashboard"); return@post
        }
        val id = call.parameters["id"]?.toIntOrNull() ?: run {
            call.respondRedirect("/students"); return@post
        }
        val card = StudentCardDAO.findById(id) ?: run {
            call.respondRedirect("/students"); return@post
        }
        if (session.role != "ADMIN" && card.logopedId != session.userId) {
            call.respondRedirect("/students"); return@post
        }
        StudentCardDAO.delete(id)
        call.respondRedirect("/students")
    }

    // ── Анкета ученика (заполняет сам ученик / родитель) ──────────────────
    get("/student/anamnesis") {
        val session = call.sessions.get<UserSession>() ?: run {
            call.respondRedirect("/login"); return@get
        }
        if (session.role != "STUDENT") {
            call.respondRedirect("/dashboard"); return@get
        }
        val user    = UserDAO.findById(session.userId) ?: run {
            call.respondRedirect("/login"); return@get
        }
        val profile = StudentProfileDAO.findByUserId(session.userId)
        val success = call.request.queryParameters["saved"] == "1"
        call.respondText(studentAnamnesisPage(session, user, profile, success), ContentType.Text.Html)
    }

    post("/student/anamnesis") {
        val session = call.sessions.get<UserSession>() ?: run {
            call.respondRedirect("/login"); return@post
        }
        if (session.role != "STUDENT") {
            call.respondRedirect("/dashboard"); return@post
        }
        val user = UserDAO.findById(session.userId) ?: run {
            call.respondRedirect("/login"); return@post
        }

        val p = call.receiveParameters()
        fun field(name: String) = p[name]?.trim()?.takeIf { it.isNotBlank() }

        val data = StudentProfileData(
            motherFullName       = field("motherFullName"),
            motherAge            = field("motherAge"),
            fatherFullName       = field("fatherFullName"),
            fatherAge            = field("fatherAge"),
            pregnancyNumber      = field("pregnancyNumber"),
            pregnancyCharacter   = field("pregnancyCharacter"),
            birthType            = field("birthType"),
            birthStimulation     = field("birthStimulation"),
            firstCryTime         = field("firstCryTime"),
            asphyxia             = field("asphyxia"),
            rhesusFactor         = field("rhesusFactor"),
            birthWeight          = field("birthWeight"),
            birthHeight          = field("birthHeight"),
            feedingStart         = field("feedingStart"),
            breastFeeding        = field("breastFeeding"),
            suckingCharacter     = field("suckingCharacter"),
            regurgitation        = field("regurgitation"),
            dischargeDay         = field("dischargeDay"),
            headsUp              = field("headsUp"),
            sitting              = field("sitting"),
            standing             = field("standing"),
            walking              = field("walking"),
            firstTeeth           = field("firstTeeth"),
            illnessesBeforeYear  = field("illnessesBeforeYear"),
            illnessesAfterYear   = field("illnessesAfterYear"),
            infections           = field("infections"),
            headInjuries         = field("headInjuries"),
            convulsions          = field("convulsions"),
            cooing               = field("cooing"),
            babbling             = field("babbling"),
            firstWords           = field("firstWords"),
            firstPhrases         = field("firstPhrases"),
            speechInterruption   = field("speechInterruption"),
            speechEnvironment    = field("speechEnvironment"),
            previousSpeechTherapy = field("previousSpeechTherapy"),
            attitudeToSpeech     = field("attitudeToSpeech")
        )

        StudentProfileDAO.upsert(session.userId, data)
        call.respondRedirect("/student/anamnesis?saved=1")
    }
}
