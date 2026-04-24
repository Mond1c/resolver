import React from "react";
import styled from "styled-components";
import config from "./config";
import {ProblemLabel} from "@/components/atoms/ProblemLabel";
import {useNeedPenalty} from "@/services/displayUtils";
import {useAppSelector} from "@/redux/hooks";
import {OptimismLevel} from "@shared/api";
import {ShrinkingBox} from "@/components/atoms/ShrinkingBox";

const ScoreboardHeaderWrap = styled.div`
    display: flex;
    flex-direction: row;

    width: 100%;
    padding-top: ${config.SCOREBOARD_HEADER_PADDING_TOP};

    font-size: ${config.SCOREBOARD_CAPTION_FONT_SIZE};
    font-weight: ${config.GLOBAL_DEFAULT_FONT_WEIGHT_BOLD};
    font-family: ${config.GLOBAL_DEFAULT_FONT_FAMILY};
    font-style: normal;
`;

const ScoreboardTitle = styled.div`
    flex: 1 0 0;
`;

const ScoreboardTableRowWrap = styled.div<{
    needPenalty: boolean;
    nProblems: number;
}>`
    display: grid;
    grid-template-columns:
        ${config.SCOREBOARD_CELL_PLACE_SIZE}
        ${config.SCOREBOARD_CELL_TEAMNAME_SIZE}
        repeat(${(props) => props.nProblems}, 1fr)
        ${({needPenalty}) =>
                needPenalty ? config.SCOREBOARD_CELL_PENALTY_SIZE : ""}
        ${config.SCOREBOARD_CELL_POINTS_SIZE};
    gap: ${config.SCOREBOARD_BETWEEN_HEADER_PADDING}px;

    box-sizing: border-box;

    background-color: ${config.SCOREBOARD_BACKGROUND_COLOR};
`;

const ScoreboardTableHeaderWrap = styled(ScoreboardTableRowWrap)`
    overflow: hidden;

    height: ${config.SCOREBOARD_HEADER_HEIGHT}px;

    font-size: ${config.SCOREBOARD_HEADER_FONT_SIZE};
    font-weight: ${config.SCOREBOARD_HEADER_FONT_WEIGHT};
    font-style: normal;
    line-height: ${config.SCOREBOARD_HEADER_HEIGHT}px;

    border-radius: ${config.SCOREBOARD_HEADER_BORDER_RADIUS_TOP_LEFT} ${config.SCOREBOARD_HEADER_BORDER_RADIUS_TOP_RIGHT} 0 0;
`;

const ScoreboardTableHeaderCell = styled.div`
    padding: 0 ${config.SCOREBOARD_CELL_PADDING};
    text-align: center;
    font-family: ${config.GLOBAL_DEFAULT_FONT_FAMILY};
    background-color: ${config.SCOREBOARD_HEADER_BACKGROUND_COLOR};
`;

const ScoreboardTableHeaderNameCell = styled(ScoreboardTableHeaderCell)`
    text-align: left;
    font-family: ${config.GLOBAL_DEFAULT_FONT_FAMILY};
`;

const ScoreboardProblemLabel = styled(ProblemLabel)`
    width: unset;
    font-family: ${config.GLOBAL_DEFAULT_FONT_FAMILY};
`;

export const nameTable = {
    normal: config.SCOREBOARD_NORMAL_NAME,
    optimistic: config.SCOREBOARD_OPTIMISTIC_NAME,
    pessimistic: config.SCOREBOARD_PESSIMISTIC_NAME,
};

interface ScoreboardHeaderProps {
    optimismLevel: OptimismLevel;
}

export function ScoreboardTableHeader() {
    const problems = useAppSelector(
        (state) => state.contestInfo.info?.problems,
    );
    const needPenalty = useNeedPenalty();

    return (
        <ScoreboardTableHeaderWrap
            nProblems={Math.max(problems?.length ?? 0, 1)}
            needPenalty={needPenalty}
        >
            <ScoreboardTableHeaderCell>#</ScoreboardTableHeaderCell>
            <ScoreboardTableHeaderNameCell>Name</ScoreboardTableHeaderNameCell>
            {problems &&
                problems.map((probData) => (
                    <ScoreboardProblemLabel
                        key={probData.name}
                        letter={probData.letter}
                        problemColor={probData.color}
                    />
                ))}
            {needPenalty && (
                <ScoreboardTableHeaderCell>
                    <ShrinkingBox text={"Penalty"}/>
                </ScoreboardTableHeaderCell>
            )}
            <ScoreboardTableHeaderCell>Σ</ScoreboardTableHeaderCell>
        </ScoreboardTableHeaderWrap>
    );
}

export function ScoreboardHeader({optimismLevel}: ScoreboardHeaderProps) {
    return (
        <ScoreboardHeaderWrap>
            <ScoreboardTitle>
                {nameTable[optimismLevel] ?? config.SCOREBOARD_UNDEFINED_NAME}{" "}
                {config.SCOREBOARD_STANDINGS_NAME}
            </ScoreboardTitle>
        </ScoreboardHeaderWrap>
    );
}
