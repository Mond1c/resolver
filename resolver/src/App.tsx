import {useAppDispatch, useAppSelector} from "./redux/hooks";
import {hideWidget, widgetComponents} from "./widgets";
import {useResolutionWebSocket} from "./contract/useResolutionWebSocket";
import config from "./config/config";
import {useHandleMessage} from "./contract/useHandleMessage";
import {WidgetWrap} from "./scoreboard/ScoreboardContainer";
import {OptimismLevel} from "@shared/api";
import {store} from "./redux/store";
import {handleScoreboardDiff} from "@/redux/contest/scoreboard";

export function App() {
    const dispatch = useAppDispatch()
    const widgets = useAppSelector(state => state.widgets.widgets)
    const scoreboardData = useAppSelector(state => state.scoreboard[OptimismLevel.normal])
    const contestInfo = useAppSelector(state => state.contestInfo.info)

    const handleMessage = useHandleMessage(
        {
            contestInfo: contestInfo,
            dispatch: dispatch,
            getScoreboardData: () => store.getState().scoreboard[OptimismLevel.normal],
            scoreboardData: scoreboardData
        }
    )

    useResolutionWebSocket({
        onMessage: handleMessage,
        onClose: (code: number) => {
            dispatch(hideWidget("awards"))
            dispatch(handleScoreboardDiff(
                {
                    optimism: OptimismLevel.normal,
                    diff: {
                        rows: {},
                        order: [],
                        ranks: [],
                        awards: []
                    }
                }
            ))
        },
        url: config.BASE_URL_WS
    })

    return (
        <>
            {Object.values(widgets).map((widget) => {
                const Component = widgetComponents[widget.type]
                return <WidgetWrap key={widget.widgetId}>
                    <Component widgetData={widget}></Component>
                </WidgetWrap>
            })}
        </>
    )
}