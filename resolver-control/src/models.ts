import {TeamId} from "@shared/api";

export type ServerToControllerMessage =
    ServerToControllerMessage.VariantsToGoto |
    ServerToControllerMessage.Settings

export const SIG_STOP = '0'
export const SIG_START = '1'
export const SIG_UP = '2'
export const SIG_DOWN = '3'
export const SIG_APPLY_FACTOR = '4'
export const SIG_CHANGE_DIRECTION = '5'
export const SIG_GET_VARIANTS_TO_GOTO = '6'
export const SIG_GOTO = '7'

export interface VariantToGoto {
    stateIndex: number,
    problemsToResolveDisplayNames: string[]
}

export enum Direction {
    Up, Down
}

export enum State {
    Process, Stop
}

export namespace ServerToControllerMessage {
    export enum Type {
        VariantsToGoto = "VariantsToGoto",
        Settings = "Settings"
    }

    export interface VariantsToGoto {
        type: ServerToControllerMessage.Type.VariantsToGoto,
        teamId: TeamId,
        fullName: string,
        variants: VariantToGoto[]
    }

    export interface Settings {
        type: ServerToControllerMessage.Type.Settings,
        speedFactor: number,
        direction: Direction,
        state: State,
        isGotoEnabled: boolean
    }
}