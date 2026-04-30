package com.resolver.resolution_logic_impl

import com.resolver.resolution_logic_api.AwardBehaviour
import com.resolver.resolution_logic_api.AwardsHandler
import com.resolver.resolution_logic_api.ResolutionStep
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
            if (!award.teams.contains(teamId) || awardIdToAwardBehaviour[award.id] == AwardBehaviour.IGNORE) {
                continue
            }
            (awardIdToTeamIds[award.id] ?: TODO("Unexpected null")).remove(teamId)
            if (
                award.teams.size == 1 ||
                awardIdToAwardBehaviour[award.id] == AwardBehaviour.AFTER_EACH ||
                awardIdToAwardBehaviour[award.id] == null
            ) {
                teamAwardsToShow.add(award)
            } else if (awardIdToTeamIds[award.id]?.isEmpty() ?: TODO("Unexpected null")) {
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