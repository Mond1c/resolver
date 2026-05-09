import styled from "styled-components";
import config from "../config/config";
import {ResolverWidgetC, Widget} from "../widgets";
import {Award, ContestInfo, TeamId, TeamMediaType} from "@shared/api";
import {useAppSelector} from "../redux/hooks";
import {VerticalMarquee} from "./marquee";
import {useMemo} from "react";

const AwardsWrap = styled.div`
    overflow: hidden;
    display: flex;
    flex-direction: column;
    gap: ${config.AWARDS_GAP};

    top: 0;
    left: 0;
    position: absolute;
    box-sizing: border-box;
    width: 100%;
    height: 100%;
    padding: ${config.AWARDS_PADDING_TOP} ${config.AWARDS_PADDING_RIGHT} 0 ${config.AWARDS_PADDING_LEFT};

    color: white;
    z-index: 1000;

    background-color: ${config.AWARDS_BACKGROUND_COLOR};
    border-radius: ${config.AWARDS_BORDER_RADIUS};
`;

const ImageContainer = styled.div`
    position: relative;
    width: 100%;
    height: 100%;
    overflow: hidden;
`

const Image = styled.img`
    width: 100%;
    height: 100%;
    object-fit: contain;
    display: block;
`

const OrganizationLogo = styled.img`
    height: 50%;
    width: auto;
    object-fit: contain;
`

const InfoLabel = styled.div`
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 30%;
    background: linear-gradient(
            to top,
            rgba(0, 0, 0, 0.99) 0%,
            rgba(0, 0, 0, 0.29) 100%
    );
    display: flex;
    align-items: flex-start;
    gap: 16px;
    padding: 12px 16px;
    box-sizing: border-box;
    color: #fff;
    font-size: 24px;
    line-height: 1.3;
);
`

const TextInfoColumn = styled.div`
    display: flex;
    flex-direction: column;
    gap: 4px;
    overflow-wrap: break-word;
    word-break: break-word;
    flex: 1;
`

const GroupAwardWrap = styled.div`
    display: flex;
    flex-direction: column;
    width: 100%;
    height: 80%;
    align-items: center;
`

const CitationWrap = styled.div`
    font-size: 24px;
    text-align: center;
    padding-bottom: 8px;
    margin-bottom: 8px;
    border-bottom: 2px solid white;
`

const VerticalMarqueeWrap = styled.div`
    padding-top: 15px;
`

const AwardRowWrap = styled.div`
    display: flex;
    justify-content: center;
    font-size: 20px;
    font-weight: bold;
    text-align: center;
    padding: 10px 0;
    align-items: center;
`

export function getTeamAwardHeader(
    contestInfo: ContestInfo,
    teamId: TeamId
) {
    const teamInfo = contestInfo.teams.find((team) => team.id === teamId)
    return contestInfo.organizations.find(
        (org) => org.id === teamInfo.organizationId
    )?.displayName ?? teamInfo.shortName
}

export function getTeamAwardCitations(
    awards: Array<Award>
) {
    return awards.map(award => award.citation).join(", ")
}

export function getTeamPhotoUrl(
    contestInfo: ContestInfo,
    teamId: TeamId
) {
    const teamInfo = contestInfo.teams.find((team) => team.id === teamId)
    return teamInfo.medias[TeamMediaType.photo]?.at(0)?.url
}

export function getOrganizationLogoUrl(
    contestInfo: ContestInfo,
    teamId: TeamId
) {
    const organizationId = contestInfo.teams.find((team) => team.id === teamId).organizationId
    return contestInfo.organizations.find(org => org.id === organizationId)?.logo[TeamMediaType.photo]?.url
}

export type TeamAwardsProps = {
    teamId: TeamId
    awards: Array<Award>
}

export const TeamAwards = (
    {
        teamId,
        awards
    }: TeamAwardsProps
) => {
    const contestInfo = useAppSelector(state => state.contestInfo.info)
    return <ImageContainer>
        <Image
            src={getTeamPhotoUrl(contestInfo, teamId)}>
        </Image>
        <InfoLabel>
            <OrganizationLogo
                src={getOrganizationLogoUrl(contestInfo, teamId)}>
            </OrganizationLogo>
            <TextInfoColumn>
                <span>
                    {getTeamAwardHeader(contestInfo, teamId)}
                </span>
                <span>
                    {getTeamAwardCitations(awards)}
                </span>
            </TextInfoColumn>
        </InfoLabel>
    </ImageContainer>
}

export type GroupAwardsProps = {
    awards: Array<Award>
}

export const GroupAwards = (
    {
        awards
    }: GroupAwardsProps
) => {
    const contestInfo = useAppSelector(state => state.contestInfo.info)
    const teamIdToTeamInfo = useMemo(() =>
            new Map(contestInfo.teams.map(teamInfo => [teamInfo.id, teamInfo])),
        [contestInfo.teams]
    )
    return <GroupAwardWrap>
        <CitationWrap>{awards.at(0).citation}</CitationWrap>
        <VerticalMarqueeWrap>
            <VerticalMarquee speed={20}>
                {awards.at(0).teams.map((teamId, index) => {
                    return (
                        <AwardRowWrap key={index}>
                            {teamIdToTeamInfo.get(teamId).shortName}
                        </AwardRowWrap>
                    )
                })}
            </VerticalMarquee>
        </VerticalMarqueeWrap>
    </GroupAwardWrap>
}

export const Awards: ResolverWidgetC<Widget.AwardsWidget> = (
    {
        widgetData: {settings},
    }
) => {
    return (
        <AwardsWrap>
            {settings.teamId && (
                <TeamAwards
                    teamId={settings.teamId}
                    awards={settings.awards}/>
            )}
            {!settings.teamId && (
                <GroupAwards awards={settings.awards}/>
            )}
        </AwardsWrap>
    );
};

export default Awards;