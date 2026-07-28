import {TeamId} from "@shared/api";

export type ServerToControllerMessage =
    ServerToControllerMessage.VariantsToGoto |
    ServerToControllerMessage.Settings

export interface VariantToGoto {
    stateIndex: number,
    problemsToResolveDisplayNames: string[]
}

export enum Direction {
    Up = 'Up',
    Down = 'Down'
}

export enum State {
    Process = 'Process',
    Stop = 'Stop'
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