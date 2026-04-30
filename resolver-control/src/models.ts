import {TeamId} from "@shared/api";
import {configureStore} from "@reduxjs/toolkit";
import {widgetsReducer} from "@resolver/src/widgets";
import scoreboardReducer from "@overlay/src/redux/contest/scoreboard";
import {contestInfoReducer} from "@overlay/src/redux/contest/contestInfo";
import rowReducer from "@resolver/src/redux/row";
import problemReducer from "@resolver/src/redux/problem";
import {useDispatch, useSelector} from "react-redux";

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

// export const store = configureStore({
//     reducer: {
//         widgets: widgetsReducer,
//         scoreboard: scoreboardReducer,
//         contestInfo: contestInfoReducer,
//         row: rowReducer,
//         problem: problemReducer
//     },
//     middleware: (getDefaultMiddleware) => {
//         return getDefaultMiddleware({
//             immutableCheck: false,
//             serializableCheck: false,
//         });
//     },
//     devTools: import.meta.env.DEV,
// });
//
// export type RootState = ReturnType<typeof store.getState>;
//
// export type AppDispatch = typeof store.dispatch;
//
// export const useAppDispatch = useDispatch.withTypes<AppDispatch>();
// export const useAppSelector = useSelector.withTypes<RootState>();