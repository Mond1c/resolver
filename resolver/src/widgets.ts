import {OptimismLevel} from "@shared/api";
import React, {FC} from "react";
import Scoreboard from "./ScoreboardContainer";
import _ from "lodash";

export interface AwardsSettings {
}

export interface ScoreboardSettings {
    scrollDirection?: ScoreboardScrollDirection
    lastVisible?: number
    optimismLevel?: OptimismLevel
    group?: string
}

export enum ScoreboardScrollDirection {
    FirstPage = "FirstPage",
    Up = "Up",
    Down = "Down",
    LastPage = "LastPage",
}

export type Widget =
    Widget.ScoreboardWidget |
    Widget.AwardsWidget

export namespace Widget {
    export enum Type {
        ScoreboardWidget = "ScoreboardWidget",
        AwardsWidget = "AwardsWidget"
    }

    export interface AwardsWidget {
        type: Widget.Type.AwardsWidget;
        widgetId: string;
        widgetLocationId: string;
        statisticsId: string;
        advertisement: AwardsSettings;
    }

    export interface ScoreboardWidget {
        type: Widget.Type.ScoreboardWidget;
        widgetId: string;
        widgetLocationId: string;
        statisticsId: string;
        settings: ScoreboardSettings;
    }
}

export const widgetComponents: Record<Widget["type"], React.ComponentType<any>> = {
    [Widget.Type.ScoreboardWidget]: Scoreboard,
    [Widget.Type.AwardsWidget]: undefined
}

export type ResolverWidgetProps<W extends Widget> = {
    widgetData: W;
    transitionState: string;
};

export type ResolverWidgetC<W extends Widget> = FC<ResolverWidgetProps<W>> & {
    ignoreAnimation?: boolean;
    overrideTimeout?: number;
};

const ActionTypes = {
    SHOW_WIDGET: "SHOW_WIDGET",
    HIDE_WIDGET: "HIDE_WIDGET",
    SET_WIDGETS: "SET_WIDGETS",
};

type WidgetsState = {
    widgets: Record<Widget["widgetId"], Widget>;
};

const initialState: WidgetsState = {
    widgets: {},
};

export const showWidget = (widgetData: Widget) => {
    return async (dispatch: (arg0: { type: string; payload: { newWidget: Widget; }; }) => void) => {
        dispatch({
            type: ActionTypes.SHOW_WIDGET,
            payload: {
                newWidget: widgetData,
            },
        });
    };
};

export const hideWidget = (widgetId: string) => {
    return async (dispatch: (arg0: { type: string; payload: { widgetId: string; }; }) => void) => {
        dispatch({
            type: ActionTypes.HIDE_WIDGET,
            payload: {
                widgetId,
            },
        });
    };
};

export const setWidgets = (widgets: Widget[]) => {
    return async (dispatch: (arg0: { type: string; payload: { widgets: Widget[]; }; }) => void) => {
        dispatch({
            type: ActionTypes.SET_WIDGETS,
            payload: {
                widgets,
            },
        });
    };
};

export function widgetsReducer(state = initialState, action: {
    type: any;
    payload: { newWidget: { widgetId: any; }; widgetId: any; widgets: any; };
}): WidgetsState {
    switch (action.type) {
        case ActionTypes.SHOW_WIDGET:
            return {
                widgets: {
                    ...state.widgets,
                    [action.payload.newWidget.widgetId]:
                    action.payload.newWidget,
                },
            };
        case ActionTypes.HIDE_WIDGET:
            return {
                widgets: _.omit(state.widgets, action.payload.widgetId),
            };
        case ActionTypes.SET_WIDGETS:
            return {
                widgets: _.keyBy(action.payload.widgets, "widgetId"),
            };
        default:
            return state;
    }
}
