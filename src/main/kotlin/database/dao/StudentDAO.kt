package database.dao

import database.tables.StudentCards
import database.tables.StudentProfiles
import database.tables.Users
import models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction

object StudentCardDAO {

    fun findById(id: Int): StudentCard? = transaction {
        StudentCards.select { StudentCards.id eq id }.map { it.toCard() }.singleOrNull()
    }

    fun findByStudentUserId(studentUserId: Int): StudentCard? = transaction {
        StudentCards.select { StudentCards.studentUserId eq studentUserId }.map { it.toCard() }.singleOrNull()
    }

    fun findViewById(id: Int): StudentCardView? = transaction {
        StudentCards
            .join(Users, JoinType.INNER, StudentCards.studentUserId, Users.id)
            .select { StudentCards.id eq id }
            .map { StudentCardView(it.toCard(), it.toStudentUser()) }
            .singleOrNull()
    }

    fun findViewsByLogoped(logopedId: Int): List<StudentCardView> = transaction {
        StudentCards
            .join(Users, JoinType.INNER, StudentCards.studentUserId, Users.id)
            .select { StudentCards.logopedId eq logopedId }
            .orderBy(StudentCards.createdAt to SortOrder.DESC)
            .map { StudentCardView(it.toCard(), it.toStudentUser()) }
    }

    fun findAllViews(): List<StudentCardView> = transaction {
        StudentCards
            .join(Users, JoinType.INNER, StudentCards.studentUserId, Users.id)
            .selectAll()
            .orderBy(StudentCards.createdAt to SortOrder.DESC)
            .map { StudentCardView(it.toCard(), it.toStudentUser()) }
    }

    /** Проверяет наличие АКТИВНОЙ карточки для ученика (для блокировки повторного создания) */
    fun existsActiveForStudent(studentUserId: Int): Boolean = transaction {
        StudentCards.select {
            (StudentCards.studentUserId eq studentUserId) and
            (StudentCards.status eq StudentStatus.ACTIVE.name)
        }.count() > 0L
    }

    /** Все завершённые карточки всех логопедов — общий пул */
    fun findCompletedPoolViews(): List<StudentCardView> = transaction {
        StudentCards
            .join(Users, JoinType.INNER, StudentCards.studentUserId, Users.id)
            .select { StudentCards.status eq StudentStatus.COMPLETED.name }
            .orderBy(StudentCards.createdAt to SortOrder.DESC)
            .map { StudentCardView(it.toCard(), it.toStudentUser()) }
    }

    /** История работы с учеником: все карточки, кроме текущей, с именем логопеда */
    fun findCardHistoryForStudent(studentUserId: Int, excludeCardId: Int): List<CardHistoryEntry> = transaction {
        val logopedAlias = Users.alias("logoped_u")
        StudentCards
            .join(logopedAlias, JoinType.INNER, StudentCards.logopedId, logopedAlias[Users.id])
            .select { (StudentCards.studentUserId eq studentUserId) and (StudentCards.id neq excludeCardId) }
            .orderBy(StudentCards.createdAt to SortOrder.DESC)
            .map { row ->
                val card = row.toCard()
                val logopedName = listOfNotNull(
                    row[logopedAlias[Users.lastName]],
                    row[logopedAlias[Users.firstName]],
                    row[logopedAlias[Users.middleName]]
                ).filter { it.isNotBlank() }.joinToString(" ").ifBlank { "Логопед #${card.logopedId}" }
                CardHistoryEntry(card, logopedName)
            }
    }

    fun create(
        studentUserId: Int,
        logopedId: Int,
        conclusion: String?,
        correctionPlan: String?,
        notes: String?,
        status: StudentStatus
    ): Int = transaction {
        StudentCards.insert {
            it[StudentCards.studentUserId]  = studentUserId
            it[StudentCards.logopedId]      = logopedId
            it[StudentCards.conclusion]     = conclusion
            it[StudentCards.correctionPlan] = correctionPlan
            it[StudentCards.notes]          = notes
            it[StudentCards.status]         = status.name
            it[StudentCards.createdAt]      = System.currentTimeMillis()
        }[StudentCards.id]
    }

    fun update(
        id: Int,
        conclusion: String?,
        correctionPlan: String?,
        notes: String?,
        status: StudentStatus
    ) = transaction {
        StudentCards.update({ StudentCards.id eq id }) {
            it[StudentCards.conclusion]     = conclusion
            it[StudentCards.correctionPlan] = correctionPlan
            it[StudentCards.notes]          = notes
            it[StudentCards.status]         = status.name
        }
    }

    fun delete(cardId: Int) = transaction {
        StudentCards.deleteWhere { with(it) { StudentCards.id eq cardId } }
    }

    private fun ResultRow.toCard() = StudentCard(
        id            = this[StudentCards.id],
        studentUserId = this[StudentCards.studentUserId],
        logopedId     = this[StudentCards.logopedId],
        conclusion    = this[StudentCards.conclusion],
        correctionPlan = this[StudentCards.correctionPlan],
        notes         = this[StudentCards.notes],
        status        = StudentStatus.valueOf(this[StudentCards.status]),
        createdAt     = this[StudentCards.createdAt]
    )

    private fun ResultRow.toStudentUser() = User(
        id             = this[Users.id],
        email          = this[Users.email],
        passwordHash   = this[Users.passwordHash],
        role           = UserRole.valueOf(this[Users.role]),
        firstName      = this[Users.firstName],
        lastName       = this[Users.lastName],
        middleName     = this[Users.middleName],
        phone          = this[Users.phone],
        specialization = this[Users.specialization],
        isBlocked      = this[Users.isBlocked],
        createdAt      = this[Users.createdAt]
    )
}

object StudentProfileDAO {

    fun findByUserId(userId: Int): StudentProfile? = transaction {
        StudentProfiles.select { StudentProfiles.userId eq userId }.map { it.toProfile() }.singleOrNull()
    }

    fun upsert(userId: Int, data: StudentProfileData) = transaction {
        val exists = StudentProfiles.select { StudentProfiles.userId eq userId }.count() > 0L
        val now = System.currentTimeMillis()
        if (exists) {
            StudentProfiles.update({ StudentProfiles.userId eq userId }) { applyData(it, userId, data, now) }
        } else {
            StudentProfiles.insert { applyData(it, userId, data, now) }
        }
    }

    private fun applyData(it: UpdateBuilder<*>, userId: Int, d: StudentProfileData, now: Long) {
        it[StudentProfiles.userId]               = userId
        it[StudentProfiles.motherFullName]        = d.motherFullName
        it[StudentProfiles.motherAge]             = d.motherAge
        it[StudentProfiles.fatherFullName]        = d.fatherFullName
        it[StudentProfiles.fatherAge]             = d.fatherAge
        it[StudentProfiles.pregnancyNumber]       = d.pregnancyNumber
        it[StudentProfiles.pregnancyCharacter]    = d.pregnancyCharacter
        it[StudentProfiles.birthType]             = d.birthType
        it[StudentProfiles.birthStimulation]      = d.birthStimulation
        it[StudentProfiles.firstCryTime]          = d.firstCryTime
        it[StudentProfiles.asphyxia]              = d.asphyxia
        it[StudentProfiles.rhesusFactor]          = d.rhesusFactor
        it[StudentProfiles.birthWeight]           = d.birthWeight
        it[StudentProfiles.birthHeight]           = d.birthHeight
        it[StudentProfiles.feedingStart]          = d.feedingStart
        it[StudentProfiles.breastFeeding]         = d.breastFeeding
        it[StudentProfiles.suckingCharacter]      = d.suckingCharacter
        it[StudentProfiles.regurgitation]         = d.regurgitation
        it[StudentProfiles.dischargeDay]          = d.dischargeDay
        it[StudentProfiles.headsUp]               = d.headsUp
        it[StudentProfiles.sitting]               = d.sitting
        it[StudentProfiles.standing]              = d.standing
        it[StudentProfiles.walking]               = d.walking
        it[StudentProfiles.firstTeeth]            = d.firstTeeth
        it[StudentProfiles.illnessesBeforeYear]   = d.illnessesBeforeYear
        it[StudentProfiles.illnessesAfterYear]    = d.illnessesAfterYear
        it[StudentProfiles.infections]            = d.infections
        it[StudentProfiles.headInjuries]          = d.headInjuries
        it[StudentProfiles.convulsions]           = d.convulsions
        it[StudentProfiles.cooing]                = d.cooing
        it[StudentProfiles.babbling]              = d.babbling
        it[StudentProfiles.firstWords]            = d.firstWords
        it[StudentProfiles.firstPhrases]          = d.firstPhrases
        it[StudentProfiles.speechInterruption]    = d.speechInterruption
        it[StudentProfiles.speechEnvironment]     = d.speechEnvironment
        it[StudentProfiles.previousSpeechTherapy] = d.previousSpeechTherapy
        it[StudentProfiles.attitudeToSpeech]      = d.attitudeToSpeech
        it[StudentProfiles.updatedAt]             = now
    }

    private fun ResultRow.toProfile() = StudentProfile(
        userId               = this[StudentProfiles.userId],
        motherFullName       = this[StudentProfiles.motherFullName],
        motherAge            = this[StudentProfiles.motherAge],
        fatherFullName       = this[StudentProfiles.fatherFullName],
        fatherAge            = this[StudentProfiles.fatherAge],
        pregnancyNumber      = this[StudentProfiles.pregnancyNumber],
        pregnancyCharacter   = this[StudentProfiles.pregnancyCharacter],
        birthType            = this[StudentProfiles.birthType],
        birthStimulation     = this[StudentProfiles.birthStimulation],
        firstCryTime         = this[StudentProfiles.firstCryTime],
        asphyxia             = this[StudentProfiles.asphyxia],
        rhesusFactor         = this[StudentProfiles.rhesusFactor],
        birthWeight          = this[StudentProfiles.birthWeight],
        birthHeight          = this[StudentProfiles.birthHeight],
        feedingStart         = this[StudentProfiles.feedingStart],
        breastFeeding        = this[StudentProfiles.breastFeeding],
        suckingCharacter     = this[StudentProfiles.suckingCharacter],
        regurgitation        = this[StudentProfiles.regurgitation],
        dischargeDay         = this[StudentProfiles.dischargeDay],
        headsUp              = this[StudentProfiles.headsUp],
        sitting              = this[StudentProfiles.sitting],
        standing             = this[StudentProfiles.standing],
        walking              = this[StudentProfiles.walking],
        firstTeeth           = this[StudentProfiles.firstTeeth],
        illnessesBeforeYear  = this[StudentProfiles.illnessesBeforeYear],
        illnessesAfterYear   = this[StudentProfiles.illnessesAfterYear],
        infections           = this[StudentProfiles.infections],
        headInjuries         = this[StudentProfiles.headInjuries],
        convulsions          = this[StudentProfiles.convulsions],
        cooing               = this[StudentProfiles.cooing],
        babbling             = this[StudentProfiles.babbling],
        firstWords           = this[StudentProfiles.firstWords],
        firstPhrases         = this[StudentProfiles.firstPhrases],
        speechInterruption   = this[StudentProfiles.speechInterruption],
        speechEnvironment    = this[StudentProfiles.speechEnvironment],
        previousSpeechTherapy = this[StudentProfiles.previousSpeechTherapy],
        attitudeToSpeech     = this[StudentProfiles.attitudeToSpeech],
        updatedAt            = this[StudentProfiles.updatedAt]
    )
}

data class StudentProfileData(
    val motherFullName: String?,
    val motherAge: String?,
    val fatherFullName: String?,
    val fatherAge: String?,
    val pregnancyNumber: String?,
    val pregnancyCharacter: String?,
    val birthType: String?,
    val birthStimulation: String?,
    val firstCryTime: String?,
    val asphyxia: String?,
    val rhesusFactor: String?,
    val birthWeight: String?,
    val birthHeight: String?,
    val feedingStart: String?,
    val breastFeeding: String?,
    val suckingCharacter: String?,
    val regurgitation: String?,
    val dischargeDay: String?,
    val headsUp: String?,
    val sitting: String?,
    val standing: String?,
    val walking: String?,
    val firstTeeth: String?,
    val illnessesBeforeYear: String?,
    val illnessesAfterYear: String?,
    val infections: String?,
    val headInjuries: String?,
    val convulsions: String?,
    val cooing: String?,
    val babbling: String?,
    val firstWords: String?,
    val firstPhrases: String?,
    val speechInterruption: String?,
    val speechEnvironment: String?,
    val previousSpeechTherapy: String?,
    val attitudeToSpeech: String?
)
