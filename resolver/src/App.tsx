import {useAppDispatch, useAppSelector} from "./hooks";
import {OptimismLevel} from "@shared/api";
import {useCallback, useEffect} from "react";
import {ScoreboardScrollDirection, showWidget, Widget, widgetComponents} from "./widgets";
import {UiEvent} from "./models";
import {useWebSocket} from "./useWebSocket";
import {handleScoreboardDiff} from "@/redux/contest/scoreboard";
import {handleRow} from "./row";
import {handleProblem} from "./problem";
import config from "./config";
import {setInfo} from "@/redux/contest/contestInfo";

export function App() {
    const dispatch = useAppDispatch()
    const scoreboardData = useAppSelector(state => state.scoreboard[OptimismLevel.normal])
    const contestInfo = useAppSelector(state => state.contestInfo.info)
    const widgets = useAppSelector(state => state.widgets.widgets)

    useEffect(() => {
        dispatch(showWidget(
            {
                settings: {
                    scrollDirection: ScoreboardScrollDirection.LastPage,
                    group: "all",
                    optimismLevel: OptimismLevel.normal
                },
                statisticsId: "scoreboard",
                widgetId: "scoreboard",
                widgetLocationId: "scoreboard",
                type: Widget.Type.ScoreboardWidget
            }
        ))
    }, [dispatch]);

    const handleMessage = useCallback((data: UiEvent) => {
            switch (data.type) {
                case UiEvent.Type.NoOp:
                    break;
                case UiEvent.Type.Scoreboard: {
                    dispatch(handleScoreboardDiff(
                        {
                            optimism: OptimismLevel.normal,
                            diff: {
                                rows: data.teamIdToScoreboardRow,
                                order: data.order,
                                ranks: data.ranks,
                                awards: []
                            }
                        }
                    ))
                    dispatch(setInfo(data.contestInfo))
                    break;
                }
                case UiEvent.Type.AcceptICPC:
                    dispatch(handleScoreboardDiff(
                        {
                            optimism: OptimismLevel.normal,
                            diff: {
                                rows: {[data.teamId]: data.row},
                                order: data.order,
                                ranks: data.ranks,
                                awards: []
                            }
                        }
                    ))
                    break;
                case UiEvent.Type.AcceptIOI:
                    dispatch(handleScoreboardDiff(
                        {
                            optimism: OptimismLevel.normal,
                            diff: {
                                rows: {[data.teamId]: data.row},
                                order: data.order,
                                ranks: data.ranks,
                                awards: []
                            }
                        }
                    ))
                    break;
                case UiEvent.Type.ChooseProblem: {
                    dispatch(
                        handleProblem({
                            problem: {
                                teamId: data.teamId,
                                problemId: data.problemId,
                                chosen: true
                            }
                        })
                    )
                    break;
                }
                case UiEvent.Type.ChooseRow: {
                    dispatch(showWidget(
                        {
                            settings: {
                                scrollDirection: ScoreboardScrollDirection.Goto,
                                targetPos: scoreboardData.orderById[data.teamId],
                                group: "all",
                                optimismLevel: OptimismLevel.normal
                            },
                            statisticsId: "scoreboard",
                            widgetId: "scoreboard",
                            widgetLocationId: "scoreboard",
                            type: Widget.Type.ScoreboardWidget
                        }
                    ))
                    dispatch(
                        handleRow({
                            row: {
                                teamId: data.teamId,
                                chosen: true
                            }
                        })
                    )
                    break;
                }
                case UiEvent.Type.HideGroupAwards:
                    break;
                case UiEvent.Type.HideTeamAwards:
                    break;
                case UiEvent.Type.RejectICPC:
                    dispatch(handleScoreboardDiff(
                        {
                            optimism: OptimismLevel.normal,
                            diff: {
                                rows: {[data.teamId]: data.row},
                                order: scoreboardData.order,
                                ranks: scoreboardData.ranks,
                                awards: []
                            }
                        }
                    ))
                    break;
                case UiEvent.Type.RejectIOI:
                    dispatch(handleScoreboardDiff(
                        {
                            optimism: OptimismLevel.normal,
                            diff: {
                                rows: {[data.teamId]: data.row},
                                order: scoreboardData.order,
                                ranks: scoreboardData.ranks,
                                awards: []
                            }
                        }
                    ))
                    break;
                case UiEvent.Type.ReverseAcceptICPC:
                    dispatch(handleScoreboardDiff(
                        {
                            optimism: OptimismLevel.normal,
                            diff: {
                                rows: {[data.teamId]: data.oldRow},
                                order: data.oldOrder,
                                ranks: data.oldRanks,
                                awards: []
                            }
                        }
                    ));
                    break;
                case UiEvent.Type.ReverseAcceptIOI:
                    dispatch(handleScoreboardDiff(
                        {
                            optimism: OptimismLevel.normal,
                            diff: {
                                rows: {[data.teamId]: data.oldRow},
                                order: data.oldOrder,
                                ranks: data.oldRanks,
                                awards: []
                            }
                        }
                    ));
                    break;
                case UiEvent.Type.ReverseRejectICPC:
                    dispatch(handleScoreboardDiff(
                        {
                            optimism: OptimismLevel.normal,
                            diff: {
                                rows: {[data.teamId]: data.oldRow},
                                order: scoreboardData.order,
                                ranks: scoreboardData.ranks,
                                awards: []
                            }
                        }
                    ));
                    break;
                case UiEvent.Type.ReverseRejectIOI:
                    dispatch(handleScoreboardDiff(
                        {
                            optimism: OptimismLevel.normal,
                            diff: {
                                rows: {[data.teamId]: data.oldRow},
                                order: scoreboardData.order,
                                ranks: scoreboardData.ranks,
                                awards: []
                            }
                        }
                    ));
                    break;
                case UiEvent.Type.ShowGroupAwards:
                    break;
                case UiEvent.Type.ShowTeamAwards:
                    break;
                case UiEvent.Type.UnchooseProblem: {
                    dispatch(
                        handleProblem({
                            problem: {
                                teamId: data.teamId,
                                problemId: data.problemId,
                                chosen: false
                            }
                        })
                    )
                    break;
                }
                case UiEvent.Type.UnchooseRow: {
                    dispatch(
                        handleRow({
                            row: {
                                teamId: data.teamId,
                                chosen: false
                            }
                        })
                    )
                }
            }
        },
        [dispatch, scoreboardData, contestInfo]
    )

    useWebSocket({
        onMessage: handleMessage,
        url: config.BASE_URL_WS
    })

    return (
        <>
            {Object.values(widgets).map((widget) => {
                const Component = widgetComponents[widget.type]
                return <div
                    key={widget.widgetId}
                    style={{
                        position: "absolute",
                        top: 0,
                        left: 0,
                        width: "100%",
                        height: "100%"
                    }}
                >
                    <Component widgetData={widget}></Component>
                </div>
            })}
        </>
    )
}
