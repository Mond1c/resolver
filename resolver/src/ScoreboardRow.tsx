import React, {useRef} from "react";
import config from "./config";
import {useAnimatedRow} from "@/components/organisms/widgets/scoreboard/ScoreboardRow";
import {AnimatingTeam} from "@/components/organisms/widgets/scoreboard/hooks/useScoreboardAnimation";
import {ContestInfo, ProblemInfo, ScoreboardRow as APIScoreboardRow, TeamInfo} from "@shared/api";
import styled from "styled-components";
import {ShrinkingBox} from "@/components/atoms/ShrinkingBox";
import {RankLabel, TaskResultLabel} from "@/components/atoms/ContestLabels";
import {formatScore, useFormatPenalty, useNeedPenalty} from "@/services/displayUtils";

type ContestDataWithMaps = ContestInfo & {
    teamsId: Record<TeamInfo["id"], TeamInfo>;
    problemsId: Record<ProblemInfo["id"], ProblemInfo>;
};

const ScoreboardTableRowWrap = styled.div<{
    needPenalty: boolean;
    nProblems: number;
}>`
    display: grid;
    grid-template-columns:
        ${config.SCOREBOARD_CELL_PLACE_SIZE}
        ${config.SCOREBOARD_CELL_TEAMNAME_SIZE}
        ${config.SCOREBOARD_CELL_POINTS_SIZE}
        ${({needPenalty}) =>
                needPenalty ? config.SCOREBOARD_CELL_PENALTY_SIZE : ""}
        repeat(${(props) => props.nProblems}, 1fr);
    gap: ${config.SCOREBOARD_BETWEEN_HEADER_PADDING}px;

    box-sizing: border-box;

    background-color: ${config.SCOREBOARD_BACKGROUND_COLOR};
`;

const ScoreboardRowWrap = styled(ScoreboardTableRowWrap)`
    overflow: hidden;
    align-items: center;

    box-sizing: content-box;
    height: ${config.SCOREBOARD_ROW_HEIGHT}px;

    font-size: ${config.SCOREBOARD_ROW_FONT_SIZE};
    font-weight: ${config.SCOREBOARD_TABLE_ROW_FONT_WEIGHT};
    font-family: ${config.GLOBAL_DEFAULT_FONT_FAMILY};
    font-style: normal;

    border-top: ${config.SCOREBOARD_ROWS_DIVIDER_COLOR} solid 1px;
    border-bottom: ${config.SCOREBOARD_ROWS_DIVIDER_COLOR} solid 1px;
`;

const ScoreboardRowName = styled(ShrinkingBox)`
    padding: 0 ${config.SCOREBOARD_CELL_PADDING};
`;

const ScoreboardRankLabel = styled(RankLabel)`
    display: flex;
    align-items: center;
    align-self: stretch;
    justify-content: center;
`;

export const ScoreboardTaskResultLabel = styled(TaskResultLabel)`
    display: flex;
    align-items: center;
    align-self: stretch;
    justify-content: center;
    position: relative;
    overflow: hidden;
`;

const PositionedScoreboardRowDiv = styled.div`
    position: absolute;
    top: 0;
    right: 0;
    left: 0;

    width: 100%;
    height: ${config.SCOREBOARD_ROW_HEIGHT}px;

    will-change: transform;

    /* Performance optimization: isolate layout calculations */
    contain: layout style paint;
`;

interface ScoreboardTeamRowProps {
    scoreboardRow: APIScoreboardRow;
    teamId: string;
    needPenalty: boolean;
    contestData: ContestDataWithMaps;
}

const ScoreboardTeamRow = React.memo(
    ({
         scoreboardRow,
         teamId,
         needPenalty,
         contestData,
     }: ScoreboardTeamRowProps) => {
        const teamData = contestData?.teamsId[teamId];
        const formatPenalty = useFormatPenalty();

        const teamName = teamData?.shortName ?? "??";
        const scoreText =
            scoreboardRow === null
                ? "??"
                : formatScore(scoreboardRow?.totalScore ?? 0.0, 1);
        const penaltyText = formatPenalty(scoreboardRow?.penalty);

        return (
            <>
                <ScoreboardRowName
                    align={config.SCOREBOARD_CELL_TEAMNANE_ALIGN}
                    text={teamName}
                />
                {scoreboardRow?.problemResults.map((result, i) => (
                    <ScoreboardTaskResultLabel
                        key={i}
                        problemResult={result}
                        problemColor={contestData?.problems[i]?.color}
                        minScore={contestData?.problems[i]?.minScore}
                        maxScore={contestData?.problems[i]?.maxScore}
                    />
                ))}
                {needPenalty && (
                    <ShrinkingBox
                        align={config.SCOREBOARD_CELL_PENALTY_ALIGN}
                        text={penaltyText}
                        fontFamily={config.GLOBAL_DEFAULT_FONT_FAMILY}
                        fontSize={config.GLOBAL_DEFAULT_FONT_SIZE}
                    />
                )}
                <ShrinkingBox
                    align={config.SCOREBOARD_CELL_POINTS_ALIGN}
                    text={scoreText}
                    fontFamily={config.GLOBAL_DEFAULT_FONT_FAMILY}
                    fontSize={config.GLOBAL_DEFAULT_FONT_SIZE}
                />
            </>
        );
    },
    (prevProps: ScoreboardTeamRowProps, nextProps: ScoreboardTeamRowProps) => {
        return (
            prevProps.teamId === nextProps.teamId &&
            prevProps.scoreboardRow === nextProps.scoreboardRow &&
            prevProps.needPenalty === nextProps.needPenalty &&
            prevProps.contestData === nextProps.contestData
        );
    },
);

interface ScoreboardRowProps {
    scoreboardRow: APIScoreboardRow;
    rank: number;
    teamId: string;
    contestData: ContestDataWithMaps;
}

export const ScoreboardRow = React.memo(
    ({
         scoreboardRow,
         rank,
         teamId,
         contestData,
     }: ScoreboardRowProps) => {
        const needPenalty = useNeedPenalty();

        return (
            <ScoreboardRowWrap
                nProblems={Math.max(contestData?.problems?.length ?? 0, 1)}
                needPenalty={needPenalty}
            >
                <ScoreboardRankLabel
                    rank={rank}
                    effects={[]}
                />
                <ScoreboardTeamRow
                    scoreboardRow={scoreboardRow}
                    teamId={teamId}
                    needPenalty={needPenalty}
                    contestData={contestData}
                />
            </ScoreboardRowWrap>
        );
    },
);

interface AnimatedRowProps {
    teamId: string;
    targetPos: number;
    animatingInfo: AnimatingTeam | undefined;
    rowHeight: number;
    getScrollPos: () => number;
    subscribeScroll: (cb: () => void) => () => void;
    zIndex: number;
    scoreboardRow: APIScoreboardRow;
    rank: number;
    contestData: ContestDataWithMaps;
}

export const AnimatedRow = React.memo(
    ({
         teamId,
         targetPos,
         animatingInfo,
         rowHeight,
         getScrollPos,
         subscribeScroll,
         zIndex,
         scoreboardRow,
         rank,
         contestData,
     }: AnimatedRowProps) => {
        const rowRef = useRef<HTMLDivElement>(null);
        useAnimatedRow(
            config.SCOREBOARD_ROW_PADDING,
            config.SCOREBOARD_ROW_TRANSITION_TIME,
            targetPos,
            rowHeight,
            animatingInfo,
            getScrollPos,
            subscribeScroll,
            rowRef
        );

        return (
            <PositionedScoreboardRowDiv
                ref={rowRef}
                style={{
                    zIndex,
                }}
            >
                <ScoreboardRow
                    scoreboardRow={scoreboardRow}
                    rank={rank}
                    teamId={teamId}
                    contestData={contestData}
                />
            </PositionedScoreboardRowDiv>
        );
    },
    (prevProps: AnimatedRowProps, nextProps: AnimatedRowProps) => {
        return (
            prevProps.teamId === nextProps.teamId &&
            prevProps.targetPos === nextProps.targetPos &&
            prevProps.animatingInfo === nextProps.animatingInfo &&
            prevProps.rowHeight === nextProps.rowHeight &&
            prevProps.zIndex === nextProps.zIndex &&
            prevProps.scoreboardRow === nextProps.scoreboardRow &&
            prevProps.rank === nextProps.rank &&
            prevProps.contestData === nextProps.contestData &&
            prevProps.getScrollPos === nextProps.getScrollPos &&
            prevProps.subscribeScroll === nextProps.subscribeScroll
        );
    },
);