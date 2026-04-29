import {useAppDispatch, useAppSelector} from "./redux/hooks";
import {widgetComponents} from "./widgets";
import {useResolutionWebSocket} from "./contract/useResolutionWebSocket";
import config from "./config/config";
import {useHandleMessage} from "./contract/useHandleMessage";
import {WidgetWrap} from "./scoreboard/ScoreboardContainer";
import {OptimismLevel} from "@shared/api";
import {store} from "./redux/store";

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