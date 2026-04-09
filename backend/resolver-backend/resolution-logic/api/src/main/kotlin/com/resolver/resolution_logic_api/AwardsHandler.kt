package com.resolver.resolution_logic_api

import org.icpclive.cds.api.Award
import org.icpclive.cds.api.TeamId

interface AwardsHandler {
    fun handleAwards(
        steps: MutableList<ResolutionStep>,
        awards: List<Award>,
        awardIdToAwardBehaviour: Map<String, AwardBehaviour>,
        awardIdToTeamIds: HashMap<String, HashSet<TeamId>>,
        teamId: TeamId
    )
}