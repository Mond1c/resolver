import {configureStore} from "@reduxjs/toolkit";
import scoreboardReducer from "@/redux/contest/scoreboard";
import {contestInfoReducer} from "@/redux/contest/contestInfo";
import {widgetsReducer} from "../widgets";
import problemReducer from "./problem"
import rowReducer from "./row"

export const store = configureStore({
    reducer: {
        widgets: widgetsReducer,
        scoreboard: scoreboardReducer,
        contestInfo: contestInfoReducer,
        row: rowReducer,
        problem: problemReducer
    },
    middleware: (getDefaultMiddleware) => {
        return getDefaultMiddleware({
            immutableCheck: false,
            serializableCheck: false,
        });
    },
    devTools: import.meta.env.DEV,
});

export type RootState = ReturnType<typeof store.getState>;

export type AppDispatch = typeof store.dispatch;