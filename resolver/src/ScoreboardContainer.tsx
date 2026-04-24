import styled from "styled-components";
import config from "./config";
import {useScoreboardData, useScoreboardRows} from "@/components/organisms/widgets/scoreboard/hooks/useScoreboardData";
import {useAnimatedScrollPos} from "@/components/organisms/widgets/scoreboard/hooks/useScoreboardScroll";
import {useAnimatingTeams} from "@/components/organisms/widgets/scoreboard/hooks/useScoreboardAnimation";
import React, {startTransition, useCallback, useEffect, useRef, useState} from "react";
import {useResizeObserver} from "usehooks-ts";
import {useTeams} from "@/components/organisms/widgets/scoreboard/ScoreboardContainer";
import {ScoreboardHeader, ScoreboardTableHeader} from "./ScoreboardHeader";
import {ResolverWidgetC, ScoreboardScrollDirection, ScoreboardSettings, Widget} from "./widgets";
import {AnimatedRow} from "./ScoreboardRow";

const ScoreboardWrap = styled.div`
    overflow: hidden;
    display: flex;
    flex-direction: column;
    gap: ${config.SCOREBOARD_GAP};

    box-sizing: border-box;
    width: 100%;
    height: 100%;
    padding: ${config.SCOREBOARD_PADDING_TOP} ${config.SCOREBOARD_PADDING_RIGHT} 0 ${config.SCOREBOARD_PADDING_LEFT};

    color: ${config.SCOREBOARD_TEXT_COLOR};

    background-color: ${config.SCOREBOARD_BACKGROUND_COLOR};
    border-radius: ${config.SCOREBOARD_BORDER_RADIUS};
`;

const ScoreboardContent = styled.div`
    display: flex;
    flex: 1 0 0;
    flex-direction: column;
    gap: ${config.SCOREBOARD_BETWEEN_HEADER_PADDING}px;
`;

const ScoreboardRowsWrap = styled.div<{ maxHeight: number }>`
    position: relative;

    overflow: hidden;
    flex: 1 0 0;

    height: auto;
    max-height: ${({maxHeight}) => `${maxHeight}px`};
`;

interface ScoreboardRowsProps {
    settings: ScoreboardSettings;
    onPage: number;
}

export function useScroller(
    totalRows: number,
    singleScreenRowCount: number,
    direction: ScoreboardScrollDirection | undefined,
) {
    const effectiveRowCount = Math.max(1, singleScreenRowCount);
    const showRows = totalRows;
    const numPages = Math.max(1, Math.ceil(showRows / effectiveRowCount));
    const singlePageRowCount = Math.ceil(showRows / numPages);

    const curPageRef = useRef(0);
    const [scrollPos, setScrollPos] = useState(0);

    const calcScrollPos = useCallback(
        (page: number) => {
            const pageEndRow = Math.min(
                (page + 1) * singlePageRowCount,
                totalRows,
            );
            return Math.max(0, pageEndRow - effectiveRowCount);
        },
        [singlePageRowCount, totalRows, effectiveRowCount],
    );

    useEffect(() => {
        if (direction === ScoreboardScrollDirection.FirstPage) {
            curPageRef.current = 0;
            startTransition(() => setScrollPos(calcScrollPos(0)));
        } else if (direction === ScoreboardScrollDirection.LastPage) {
            curPageRef.current = numPages - 1;
            startTransition(() => setScrollPos(calcScrollPos(numPages - 1)));
        } else if (direction === ScoreboardScrollDirection.Up) {
            const newPage = curPageRef.current - 1
            curPageRef.current = newPage
            startTransition(() => setScrollPos(calcScrollPos(newPage)))
        }
    }, [direction, numPages, calcScrollPos]);

    return scrollPos;
}

const ScoreboardRows = ({settings, onPage}: ScoreboardRowsProps) => {
    const rows = useScoreboardRows(settings.optimismLevel, settings.group);
    const rowHeight = config.SCOREBOARD_ROW_HEIGHT + config.SCOREBOARD_ROW_PADDING;

    const targetScrollPos = useScroller(
        rows.length,
        onPage,
        settings.scrollDirection
    );

    const {getScrollPos, subscribe} = useAnimatedScrollPos(targetScrollPos, config.SCOREBOARD_ROW_TRANSITION_TIME);
    const {scoreboardData, normalScoreboardData, contestData} =
        useScoreboardData(settings.optimismLevel);

    const animatingTeams = useAnimatingTeams(rows, config.SCOREBOARD_ROW_TRANSITION_TIME);

    const {teamsToRender} = useTeams({
        animatingTeams: animatingTeams,
        onPage: onPage,
        rows: rows,
        scoreboardRowTransitionTime: config.SCOREBOARD_ROW_TRANSITION_TIME,
        targetScrollPos: targetScrollPos
    });

    const effectiveOnPage = Math.max(1, onPage);

    return (
        <ScoreboardRowsWrap maxHeight={effectiveOnPage * rowHeight}>
            {teamsToRender.map(([teamId, position]) => (
                <AnimatedRow
                    key={teamId}
                    teamId={teamId}
                    targetPos={position}
                    animatingInfo={animatingTeams.get(teamId)}
                    rowHeight={rowHeight}
                    getScrollPos={getScrollPos}
                    subscribeScroll={subscribe}
                    zIndex={rows.length - position}
                    scoreboardRow={scoreboardData?.ids[teamId]}
                    rank={normalScoreboardData?.rankById[teamId]}
                    contestData={contestData}
                />
            ))}
        </ScoreboardRowsWrap>
    );
};

export const Scoreboard: ResolverWidgetC<Widget.ScoreboardWidget> = (
    {
        widgetData: {settings},
    }
) => {
    const ref = useRef<HTMLDivElement>(null);
    const {height = 0} = useResizeObserver({ref});
    const onPage = Math.floor(
        (height - config.SCOREBOARD_HEADER_HEIGHT) /
        (config.SCOREBOARD_ROW_HEIGHT + config.SCOREBOARD_ROW_PADDING),
    );

    return (
        <ScoreboardWrap>
            <ScoreboardHeader optimismLevel={settings.optimismLevel}/>
            <ScoreboardContent ref={ref}>
                <ScoreboardTableHeader/>
                <ScoreboardRows settings={settings} onPage={onPage}/>
            </ScoreboardContent>
        </ScoreboardWrap>
    );
};

export default Scoreboard;