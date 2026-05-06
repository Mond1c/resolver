import {useResolutionControlWebSocket} from "./useResolutionControlWebSocket";
import {ServerToControllerMessage} from "./models";
import {useState} from "react";
import {useHandleMessage} from '@resolver/src/contract/useHandleMessage'
import {store} from '@resolver/src/redux/store';
import {OptimismLevel} from "@shared/api";
import {useResolutionWebSocket} from "@resolver/src/contract/useResolutionWebSocket";
import {hideWidget, widgetComponents} from "@resolver/src/widgets";
import {WidgetWrap} from "@resolver/src/scoreboard/ScoreboardContainer";
import config from "@resolver/src/config/config";
import {useAppDispatch, useAppSelector} from "@resolver/src/redux/hooks";
import {Controller, ScoreboardWithControllerWrap, ScoreboardWrap} from "./controller";
import {Auth} from "./auth";
import {handleScoreboardDiff} from "@overlay/src/redux/contest/scoreboard";
import VariantsToGoto = ServerToControllerMessage.VariantsToGoto;
import Settings = ServerToControllerMessage.Settings;

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

    const [variantsToGoto, setVariantsToGoto] = useState<VariantsToGoto | null>(null)
    const [speedFactor, setSpeedFactor] = useState('')
    const [teamId, setTeamId] = useState('')
    const [stateIndex, setStateIndex] = useState<string>('')
    const [isAuthenticated, setIsAuthenticated] = useState(false)
    const [settings, setSettings] = useState<Settings | null>(null)
    const [login, setLogin] = useState<string>('')
    const [password, setPassword] = useState<string>('')
    const [isError, setIsError] = useState<boolean>(false)

    const controlWs = useResolutionControlWebSocket({
        onMessage: (data: ServerToControllerMessage) => {
            if (data.type === ServerToControllerMessage.Type.VariantsToGoto) {
                setVariantsToGoto(data)
                setStateIndex('')
            } else if (data.type === ServerToControllerMessage.Type.Settings) {
                setIsError(false)
                if (!isAuthenticated) {
                    setIsAuthenticated(true)
                }
                setSettings(data)
            }
        },
        onClose: (code: number) => {
            if (code !== 1008) {
                setLogin('')
                setPassword('')
            }
            setIsAuthenticated(false)
            if (code === 1008) {
                setIsError(true)
            }
        },
        url: 'ws://localhost:8080/control'
    })

    return (
        <ScoreboardWithControllerWrap>
            <ScoreboardWrap>
                {Object.values(widgets).map((widget) => {
                    const Component = widgetComponents[widget.type]
                    return <WidgetWrap key={widget.widgetId}>
                        <Component widgetData={widget}></Component>
                    </WidgetWrap>
                })}
            </ScoreboardWrap>
            {isAuthenticated && (<Controller
                ws={controlWs.ws}
                speedFactor={speedFactor}
                setSpeedFactor={setSpeedFactor}
                teamId={teamId}
                setTeamId={setTeamId}
                stateIndex={stateIndex}
                setStateIndex={setStateIndex}
                variantsToGoto={variantsToGoto}
                settings={settings}>
            </Controller>)}
            {!isAuthenticated && (<Auth
                    ws={controlWs.ws}
                    isError={isError}
                    login={login}
                    setLogin={setLogin}
                    password={password}
                    setPassword={setPassword}>
                </Auth>
            )}
        </ScoreboardWithControllerWrap>
    )
}