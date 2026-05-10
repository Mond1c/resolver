import config from "@resolver/src/config/config";
import styled from "styled-components";
import React from "react";
import {
    Direction,
    ServerToControllerMessage,
    State
} from "./models";
import {ReconnectingWebSocket} from "@resolver/src/websocket/ReconnectingWebSocket";
import {
    SIG_APPLY_FACTOR,
    SIG_CHANGE_DIRECTION,
    SIG_DOWN,
    SIG_GET_VARIANTS_TO_GOTO,
    SIG_GOTO,
    SIG_START,
    SIG_STOP,
    SIG_UP
} from "./constants";

export const ScoreboardWithControllerWrap = styled.div`
    height: 100vh;
    display: flex;
    flex-direction: column;
    background: ${config.SCOREBOARD_BACKGROUND_COLOR};
`

export const ScoreboardWrap = styled.div`
    flex: 0 0 75%;
    position: relative;
`

export const ControllerWrap = styled.div`
    flex: 1;
    display: flex;
    gap: 56px;
    padding-left: 15px;
    padding-right: 15px;
    padding-top: 15px;
    align-items: flex-start;
`

export const ControllerColumnWrap = styled.div`
    display: flex;
    flex-direction: column;
    gap: 15px;
`

export const SettingsWrap = styled.div`
    display: flex;
    flex-direction: row;
    gap: 15px;
    justify-content: center;
`

export const TeamFullNameWrap = styled.div`
    background: white;
    font-size: 18px;
    overflow-wrap: break-word;
`
export const ControllerButtonWrap = styled.button`
    font-size: 24px;
`

export const SelectVariantsToGotoWrap = styled.select`
    font-size: 24px;
`

export const CurrentSpeedFactorWrap = styled.div`
    background: white;
    font-size: 24px;
    text-align: center;
`

export const StateWrap = styled.div<{ colour: string }>`
    background: ${({colour}) => colour};
    width: 28px;
    height: 28px;
`

export const DirectionWrap = styled.div`
    background: white;
    font-size: 28px;
`

export const SpeedFactorInputWrap = styled.input.attrs(
    {
        type: 'number',
        placeholder: 'Speed factor'
    }
)`
    font-size: 24px;
`

export const TeamIdInputWrap = styled.input.attrs(
    {
        type: 'text',
        placeholder: 'Team id'
    }
)`
    font-size: 24px;
`

export type ControllerProps = {
    ws: ReconnectingWebSocket | null
    speedFactor: string
    setSpeedFactor: React.Dispatch<React.SetStateAction<string>>
    teamId: string
    setTeamId: React.Dispatch<React.SetStateAction<string>>
    stateIndex: string
    setStateIndex: React.Dispatch<React.SetStateAction<string>>
    variantsToGoto: ServerToControllerMessage.VariantsToGoto | null
    settings: ServerToControllerMessage.Settings | null
}

export const Controller = (
    {
        ws,
        speedFactor,
        setSpeedFactor,
        teamId,
        setTeamId,
        stateIndex,
        setStateIndex,
        variantsToGoto,
        settings
    }: ControllerProps
) => {
    const handleApplySpeed = () => {
        ws?.send(SIG_APPLY_FACTOR + ' ' + speedFactor)
        setSpeedFactor('')
    }

    const handleGetVariantsToGoto = () => {
        ws?.send(SIG_GET_VARIANTS_TO_GOTO + ' ' + teamId)
    }

    const handleGoto = () => {
        ws?.send(SIG_GOTO + ' ' + stateIndex + ' ' + variantsToGoto.teamId)
    }

    return <ControllerWrap>
        <ControllerColumnWrap>
            <ControllerButtonWrap onClick={() => ws?.send(SIG_START)}>
                Start
            </ControllerButtonWrap>
            <ControllerButtonWrap onClick={() => ws?.send(SIG_STOP)}>
                Stop
            </ControllerButtonWrap>
            <ControllerButtonWrap onClick={() => ws?.send(SIG_CHANGE_DIRECTION)}>
                Change direction
            </ControllerButtonWrap>
        </ControllerColumnWrap>
        <ControllerColumnWrap>
            <ControllerButtonWrap onClick={() => ws?.send(SIG_UP)}>
                Step up
            </ControllerButtonWrap>
            <ControllerButtonWrap onClick={() => ws?.send(SIG_DOWN)}>
                Step down
            </ControllerButtonWrap>
        </ControllerColumnWrap>
        <ControllerColumnWrap>
            <SpeedFactorInputWrap
                value={speedFactor}
                onChange={(ce) => {
                    const value = ce.target.value
                    if (value === '' || /^\d*\.?\d*$/.test(value)) {
                        setSpeedFactor(ce.target.value)
                    }
                }}>
            </SpeedFactorInputWrap>
            <ControllerButtonWrap onClick={handleApplySpeed}>
                Apply speed factor
            </ControllerButtonWrap>
            <CurrentSpeedFactorWrap>
                Current speed factor: {settings.speedFactor}
            </CurrentSpeedFactorWrap>
        </ControllerColumnWrap>
        {settings?.isGotoEnabled && (
            <><ControllerColumnWrap>
                <TeamIdInputWrap
                    value={teamId}
                    onChange={(ce) => {
                        const value = ce.target.value;
                        setTeamId(value);
                    }}>
                </TeamIdInputWrap>
                <ControllerButtonWrap onClick={handleGetVariantsToGoto}>
                    Get variants to go to
                </ControllerButtonWrap>
                <ControllerButtonWrap onClick={handleGoto}>
                    Go to
                </ControllerButtonWrap>
            </ControllerColumnWrap><ControllerColumnWrap>
                {variantsToGoto && <TeamFullNameWrap>
                    {variantsToGoto.fullName}
                </TeamFullNameWrap>}
                {variantsToGoto && <div>
                    <SelectVariantsToGotoWrap
                        value={stateIndex}
                        onChange={(ce) => {
                            setStateIndex(ce.target.value);
                        }}>
                        <option value='' disabled>Choose variant</option>
                        {variantsToGoto.variants.map((v) => (
                            <option key={v.stateIndex} value={v.stateIndex}>
                                {v.stateIndex + ': ' + v.problemsToResolveDisplayNames.join(', ')}
                            </option>
                        ))}
                    </SelectVariantsToGotoWrap>
                </div>}
                <SettingsWrap>
                    {settings?.direction === Direction.Up && (
                        <DirectionWrap>↑</DirectionWrap>
                    )}
                    {settings?.direction === Direction.Down && (
                        <DirectionWrap>↓</DirectionWrap>
                    )}
                    {settings?.state === State.Process && (
                        <StateWrap colour={'green'}/>)}
                    {settings?.state === State.Stop && (
                        <StateWrap colour={'red'}/>)}
                </SettingsWrap>
            </ControllerColumnWrap></>
        )}
    </ControllerWrap>
}