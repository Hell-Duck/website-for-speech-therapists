package database.tables

import org.jetbrains.exposed.sql.Table

object StudentCards : Table("student_cards") {
    val id            = integer("id").autoIncrement()
    val studentUserId = integer("student_user_id").references(Users.id)
    val logopedId     = integer("logoped_id").references(Users.id)
    val conclusion    = text("conclusion").nullable()
    val correctionPlan = text("correction_plan").nullable()
    val notes         = text("notes").nullable()
    val status        = varchar("status", 20).default("ACTIVE")
    val createdAt     = long("created_at")

    override val primaryKey = PrimaryKey(id)
}

object StudentProfiles : Table("student_profiles") {
    val userId               = integer("user_id").references(Users.id)
    val motherFullName       = varchar("mother_full_name", 200).nullable()
    val motherAge            = varchar("mother_age", 50).nullable()
    val fatherFullName       = varchar("father_full_name", 200).nullable()
    val fatherAge            = varchar("father_age", 50).nullable()
    val pregnancyNumber      = varchar("pregnancy_number", 100).nullable()
    val pregnancyCharacter   = text("pregnancy_character").nullable()
    val birthType            = varchar("birth_type", 200).nullable()
    val birthStimulation     = varchar("birth_stimulation", 200).nullable()
    val firstCryTime         = varchar("first_cry_time", 100).nullable()
    val asphyxia             = varchar("asphyxia", 100).nullable()
    val rhesusFactor         = varchar("rhesus_factor", 100).nullable()
    val birthWeight          = varchar("birth_weight", 50).nullable()
    val birthHeight          = varchar("birth_height", 50).nullable()
    val feedingStart         = text("feeding_start").nullable()
    val breastFeeding        = varchar("breast_feeding", 200).nullable()
    val suckingCharacter     = varchar("sucking_character", 200).nullable()
    val regurgitation        = varchar("regurgitation", 200).nullable()
    val dischargeDay         = varchar("discharge_day", 200).nullable()
    val headsUp              = varchar("heads_up", 50).nullable()
    val sitting              = varchar("sitting", 50).nullable()
    val standing             = varchar("standing", 50).nullable()
    val walking              = varchar("walking", 50).nullable()
    val firstTeeth           = varchar("first_teeth", 50).nullable()
    val illnessesBeforeYear  = text("illnesses_before_year").nullable()
    val illnessesAfterYear   = text("illnesses_after_year").nullable()
    val infections           = text("infections").nullable()
    val headInjuries         = text("head_injuries").nullable()
    val convulsions          = varchar("convulsions", 200).nullable()
    val cooing               = varchar("cooing", 50).nullable()
    val babbling             = varchar("babbling", 50).nullable()
    val firstWords           = varchar("first_words", 50).nullable()
    val firstPhrases         = varchar("first_phrases", 50).nullable()
    val speechInterruption   = text("speech_interruption").nullable()
    val speechEnvironment    = text("speech_environment").nullable()
    val previousSpeechTherapy = text("previous_speech_therapy").nullable()
    val attitudeToSpeech     = text("attitude_to_speech").nullable()
    val updatedAt            = long("updated_at")

    override val primaryKey = PrimaryKey(userId)
}
