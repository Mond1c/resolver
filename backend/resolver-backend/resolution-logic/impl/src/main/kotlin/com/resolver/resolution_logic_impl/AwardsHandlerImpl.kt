package com.resolver.resolution_logic_impl

import com.resolver.resolution_logic_api.AwardBehaviour
import com.resolver.resolution_logic_api.AwardsHandler
import com.resolver.resolution_logic_api.ResolutionStep
import com.resolver.util_api.exception.CoreExceptions
import org.icpclive.cds.api.Award
import org.icpclive.cds.api.TeamId

object AwardsHandlerImpl : AwardsHandler {
    override fun handleAwards(
        steps: MutableList<ResolutionStep>,
        awards: List<Award>,
        awardIdToAwardBehaviour: Map<String, AwardBehaviour>,
        awardIdToTeamIds: HashMap<String, HashSet<TeamId>>,
        teamId: TeamId,
        teamIndex: Int
    ) {
        val teamAwardsToShow = mutableListOf<Award>()
        val groupAwardsToShow = mutableListOf<Award>()
        for (award in awards) {
            val behaviour = awardIdToAwardBehaviour[award.id]
            if (!award.teams.contains(teamId) || behaviour == AwardBehaviour.IGNORE) {
                continue
            }
            val teamIds = awardIdToTeamIds[award.id] ?: throw CoreExceptions.awardNotFoundException
            teamIds.remove(teamId)
            if (
                award.teams.size == 1 ||
                behaviour == AwardBehaviour.AFTER_EACH ||
                behaviour == null
            ) {
                teamAwardsToShow.add(award)
            } else if (teamIds.isEmpty()) {
                groupAwardsToShow.add(award)
            }
        }
        if (teamAwardsToShow.isNotEmpty()) {
            steps.add(
                ResolutionStep.WithTeamId.TeamAwardsResolutionStep(
                    teamId = teamId,
                    awards = teamAwardsToShow,
                    teamIndex = teamIndex
                )
            )
        } else {
            val prevTeamId = when (val step = steps.lastOrNull()) {
                is ResolutionStep.WithTeamId -> step.teamId
                else -> null
            }
            if (prevTeamId != teamId) {
                steps.add(ResolutionStep.WithTeamId.NoResolvedProblemsForTeam(teamId, teamIndex))
            }
        }
        if (groupAwardsToShow.isNotEmpty()) {
            steps.add(
                ResolutionStep.GroupAwardsResolutionStep(
                    awards = groupAwardsToShow
                )
            )
        }
    }
}