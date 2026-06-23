package org.icpclive.resolver

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlin.jvm.JvmInline
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Serializable
@JvmInline
value class TeamId(val value: String)

@Serializable
@JvmInline
value class ProblemId(val value: String)

@Serializable
@JvmInline
value class GroupId(val value: String)

@Serializable
@JvmInline
value class OrganizationId(val value: String)

internal fun DurationUnit.asSerializer() = Long.serializer().map(
    "Duration/${this}",
    onSerialize = { it.toLong(this) },
    onDeserialize = { it.toDuration(this) }
)

object DurationInMillisecondsSerializer :
    KSerializer<Duration> by DurationUnit.MILLISECONDS.asSerializer()

object DurationInSecondsSerializer :
    KSerializer<Duration> by DurationUnit.SECONDS.asSerializer()

@Serializable
sealed class ProblemResult {
    abstract val lastSubmitTime: Duration?
}

@Serializable
@SerialName("ICPC")
data class ICPCProblemResult(
    val wrongAttempts: Int,
    val pendingAttempts: Int,
    val isSolved: Boolean,
    val isFirstToSolve: Boolean,
    @SerialName("lastSubmitTimeMs")
    @Serializable(with = DurationInMillisecondsSerializer::class)
    override val lastSubmitTime: Duration?,
) : ProblemResult()

@Serializable
@SerialName("IOI")
data class IOIProblemResult(
    val score: Double?,
    @SerialName("lastSubmitTimeMs")
    @Serializable(with = DurationInMillisecondsSerializer::class)
    override val lastSubmitTime: Duration?,
    val isFirstBest: Boolean,
    val pendingAttempts: Int,
    val totalAttempts: Int,
) : ProblemResult()

@Serializable
data class ScoreboardRow(
    val totalScore: Double,
    @Serializable(
        with =

            DurationInSecondsSerializer::class
    )
    val penalty: Duration,
    @Serializable(with = DurationInMillisecondsSerializer::class)
    @SerialName("lastAcceptedMs")
    val lastAccepted: Duration,
    val problemResults: List<ProblemResult>,
)

@Serializable
sealed class Award {
    abstract val id: String
    abstract val citation: String
    abstract val teams: Set<TeamId>

    @Serializable
    @SerialName("winner")
    data class Winner(
        override val id: String,
        override val citation: String,
        override val teams: Set<TeamId>,
    ) : Award()

    @Serializable
    @SerialName("medal")
    data class Medal(
        override val id: String,
        override val citation: String,
        override val teams: Set<TeamId>,
    ) : Award() {
        enum class MedalColor {
            GOLD, SILVER, BRONZE;
        }
    }

    @Serializable
    @SerialName("group_champion")
    data class GroupChampion(
        override val id: String,
        override val citation: String,
        val groupId: GroupId,
        override val teams: Set<TeamId>,
    ) : Award()

    @Serializable
    @SerialName("custom")
    data class Custom(
        override val id: String,
        override val citation: String,
        override val teams: Set<TeamId>,
    ) : Award()
}

@Serializable
enum class ContestResultType {
    ICPC, IOI
}

@Serializable
data class ContestInfo(
    val name: String,
    val resultType: ContestResultType,
    @SerialName("problems") val problemList: List<ProblemInfo>,
    @SerialName("teams") val teamList: List<TeamInfo>,
    @SerialName("organizations") val organizationList: List<OrganizationInfo>,
) {
    val teams: Map<TeamId, TeamInfo> by lazy { teamList.associateBy { it.id } }
    val organizations: Map<OrganizationId, OrganizationInfo> by lazy { organizationList.associateBy { it.id } }
    val problems: Map<ProblemId, ProblemInfo> by lazy { problemList.associateBy { it.id } }
    val scoreboardProblems: List<ProblemInfo> by lazy {
        problemList.sortedBy { it.ordinal }.filterNot { it.isHidden }
    }
}

@Serializable
data class ProblemInfo(
    val id: ProblemId,
    @SerialName("letter") val displayName: String,
    @SerialName("name") val fullName: String,
    val ordinal: Int,
    @Required val isHidden: Boolean = false,
)

@Serializable
data class TeamInfo(
    val id: TeamId,
    @SerialName("name") val fullName: String,
    @SerialName("shortName") val displayName: String,
    val groups: List<GroupId>,
    val hashTag: String?,
//    val medias: Map<TeamMediaType, List<MediaType>>,
    val organizationId: OrganizationId?,
)

@Serializable
data class OrganizationInfo(
    val id: OrganizationId,
    val displayName: String,
    val fullName: String,
)