import {TeamId} from "@shared/api";

export type ServerToControllerMessage =
    ServerToControllerMessage.VariantsToGoto

export interface VariantToGoto {
    stateIndex: number,
    problemsToResolveDisplayNames: string[]
}

export namespace ServerToControllerMessage {
    export enum Type {
        VariantsToGoto = "VariantsToGoto"
    }

    export interface VariantsToGoto {
        type: ServerToControllerMessage.Type.VariantsToGoto,
        teamId: TeamId,
        fullName: string,
        variants: VariantToGoto[]
    }
}