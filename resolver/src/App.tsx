import {useAppSelector} from "./redux/hooks";
import {widgetComponents} from "./widgets";
import {useWebSocket} from "./contract/useWebSocket";
import config from "./config/config";
import {useHandleMessage} from "./contract/useHandleMessage";
import {WidgetWrap} from "./scoreboard/ScoreboardContainer";

export function App() {
    const widgets = useAppSelector(state => state.widgets.widgets)

    const handleMessage = useHandleMessage()

    useWebSocket({
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