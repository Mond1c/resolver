import {useResolutionControlWebSocket} from "./useResolutionControlWebSocket";
import {ServerToControllerMessage, useAppDispatch, useAppSelector} from "./models";
import {useState} from "react";
import {useHandleMessage} from 'resolver/src/contract/useHandleMessage'
import {store} from 'resolver/src/redux/store';
import {OptimismLevel} from "@shared/api";
import {useResolutionWebSocket} from "@resolver/src/contract/useResolutionWebSocket";
import {widgetComponents} from "@resolver/src/widgets";
import {WidgetWrap} from "@resolver/src/scoreboard/ScoreboardContainer";
import config from "@resolver/src/config/config";
import VariantsToGoto = ServerToControllerMessage.VariantsToGoto;

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

    const [variantsToGoto, setVariantsToGoto] = useState<VariantsToGoto | null>(null)

    const controlWs = useResolutionControlWebSocket({
        onMessage: (data: ServerToControllerMessage) => {
            if (data.type === ServerToControllerMessage.Type.VariantsToGoto) {
                setVariantsToGoto(data)
            }
        },
        url: "ws://localhost:8080/control"
    })

    const [speedFactor, setSpeedFactor] = useState("")
    const [teamId, setTeamId] = useState("")
    const [stateIndex, setStateIndex] = useState<string>("")

    const handleApplySpeed = () => {
        controlWs.ws?.send("4 " + speedFactor)
        setSpeedFactor("")
    }

    const handleGetVariantsToGoto = () => {
        controlWs.ws?.send("6 " + teamId)
    }

    const handleGoto = () => {
        controlWs.ws?.send("7 " + stateIndex + " " + variantsToGoto.teamId)
    }

    return (
        <div style={{
            height: '100vh',
            display: 'flex',
            flexDirection: 'column',
            background: config.SCOREBOARD_BACKGROUND_COLOR
        }}>
            <div style={{
                flex: '0 0 75%',
                position: 'relative'
            }}>
                {Object.values(widgets).map((widget) => {
                    const Component = widgetComponents[widget.type]
                    return <WidgetWrap key={widget.widgetId}>
                        <Component widgetData={widget}></Component>
                    </WidgetWrap>
                })}
            </div>
            <div style={{
                flex: 1,
                display: 'flex',
                gap: '56px',
                paddingLeft: '15px',
                paddingTop: '15px',
                alignItems: 'flex-start'
            }}
            >
                <div style={{
                    display: 'flex',
                    flexDirection: 'column',
                    gap: '15px'
                }}>
                    <button onClick={() => controlWs.ws?.send("1")}
                            style={{
                                fontSize: '24px'
                            }}
                    >Start
                    </button>
                    <button onClick={() => controlWs.ws?.send("0")}
                            style={{
                                fontSize: '24px'
                            }}
                    >Stop
                    </button>
                    <button onClick={() => controlWs.ws?.send("5")}
                            style={{
                                fontSize: '24px'
                            }}
                    >Change direction
                    </button>
                </div>
                <div style={{
                    display: 'flex',
                    flexDirection: 'column',
                    gap: '15px'
                }}>
                    <button onClick={() => controlWs.ws?.send("2")}
                            style={{
                                fontSize: '24px'
                            }}
                    >Step up
                    </button>
                    <button onClick={() => controlWs.ws?.send("3")}
                            style={{
                                fontSize: '24px'
                            }}
                    >Step down
                    </button>
                </div>
                <div style={{
                    display: 'flex',
                    flexDirection: 'column',
                    gap: '15px'
                }}>
                    <input type="number"
                           placeholder="Speed factor"
                           value={speedFactor}
                           onChange={(ce) => {
                               const value = ce.target.value
                               if (value === '' || /^\d*\.?\d*$/.test(value)) {
                                   setSpeedFactor(ce.target.value)
                               }
                           }}
                           style={{
                               fontSize: '24px'
                           }}
                    >
                    </input>
                    <button onClick={handleApplySpeed}
                            style={{
                                fontSize: '24px'
                            }}
                    >Apply speed factor
                    </button>
                </div>
                <div style={{
                    display: 'flex',
                    flexDirection: 'column',
                    gap: '15px'
                }}>
                    <input type="text"
                           placeholder="team id"
                           value={teamId}
                           onChange={(ce) => {
                               const value = ce.target.value
                               setTeamId(value)
                           }}
                           style={{
                               fontSize: '24px'
                           }}
                    >

                    </input>
                    <button onClick={handleGetVariantsToGoto}
                            style={{
                                fontSize: '24px'
                            }}
                    >Get variants to go to
                    </button>
                    <button onClick={handleGoto}
                            style={{
                                fontSize: '24px'
                            }}
                    >Go to
                    </button>
                </div>
                <div style={{
                    display: 'flex',
                    flexDirection: 'column',
                    gap: '15px'
                }}>
                    {variantsToGoto && <div style={{
                        background: 'white',
                        fontSize: '18px',
                        overflowWrap: 'break-word'
                    }}>
                        {variantsToGoto.fullName}
                    </div>}
                    {variantsToGoto && <div>
                        <select
                            value={stateIndex}
                            onChange={(ce) => {
                                setStateIndex(ce.target.value)
                            }}
                        >
                            {variantsToGoto.variants.map((v) => (
                                <option key={v.stateIndex} value={v.stateIndex}>
                                    {v.stateIndex + ": " + v.problemsToResolveDisplayNames.join(", ")}
                                </option>
                            ))}
                        </select>
                    </div>}
                </div>
            </div>
        </div>
    )
}