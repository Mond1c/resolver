import {useCallback} from "react";
import {UiEvent} from "./models";
import {handleScoreboardDiff, ScoreboardData} from "@/redux/contest/scoreboard";
import {OptimismLevel} from "@shared/api";
import {setInfo} from "@/redux/contest/contestInfo";
import {AppDispatch} from "../redux/store";
import {hideWidget, ScoreboardScrollDirection, showWidget, Widget} from "../widgets";
import {handleRow} from "../redux/row";
import {handleProblem} from "../redux/problem";
import {AWARDS, SCOREBOARD} from "../constants";

interface UseHandleMessageParams {
    dispatch: AppDispatch
    scoreboardData: ScoreboardData
    getScoreboardData: () => ScoreboardData
}

export function useHandleMessage(
    {
        dispatch,
        scoreboardData,
        getScoreboardData
    }: UseHandleMessageParams
) {
    return useCallback((data: UiEvent) => {
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
                    const actualScoreboardData = getScoreboardData()
                    dispatch(showWidget(
                        {
                            settings: {
                                scrollDirection: ScoreboardScrollDirection.Goto,
                                targetPos: data.teamOfLastChosenRow ?
                                    actualScoreboardData.orderById[data.teamOfLastChosenRow] : actualScoreboardData.order.length - 1,
                                group: "all",
                                optimismLevel: OptimismLevel.normal
                            },
                            statisticsId: SCOREBOARD,
                            widgetId: SCOREBOARD,
                            widgetLocationId: SCOREBOARD,
                            type: Widget.Type.ScoreboardWidget
                        }
                    ))
                    dispatch(
                        handleRow({
                            row: {
                                teamId: data.teamOfLastChosenRow,
                                chosen: data.isLastChosenRowChosenNow
                            }
                        })
                    )
                    break;
                }
                case UiEvent.Type.Accept:
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
                    const actual = getScoreboardData()
                    dispatch(showWidget(
                        {
                            settings: {
                                scrollDirection: ScoreboardScrollDirection.Goto,
                                targetPos: actual.orderById[data.teamId],
                                group: "all",
                                optimismLevel: OptimismLevel.normal
                            },
                            statisticsId: SCOREBOARD,
                            widgetId: SCOREBOARD,
                            widgetLocationId: SCOREBOARD,
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
                    dispatch(hideWidget(AWARDS))
                    break;
                case UiEvent.Type.HideTeamAwards:
                    dispatch(hideWidget(AWARDS))
                    break;
                case UiEvent.Type.Reject:
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
                case UiEvent.Type.ReverseAccept:
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
                    ));
                    break;
                case UiEvent.Type.ReverseReject:
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
                    ));
                    break;
                case UiEvent.Type.ShowGroupAwards:
                    dispatch(showWidget(
                        {
                            settings: {
                                awards: data.awards
                            },
                            statisticsId: AWARDS,
                            widgetId: AWARDS,
                            widgetLocationId: AWARDS,
                            type: Widget.Type.AwardsWidget
                        }
                    ))
                    break;
                case UiEvent.Type.ShowTeamAwards:
                    dispatch(showWidget(
                        {
                            settings: {
                                teamId: data.teamId,
                                awards: data.awards
                            },
                            statisticsId: AWARDS,
                            widgetId: AWARDS,
                            widgetLocationId: AWARDS,
                            type: Widget.Type.AwardsWidget
                        }
                    ))
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
        [dispatch, scoreboardData, getScoreboardData]
    )
}