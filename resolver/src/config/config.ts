import {ResolverConfig} from "./config.interface";

export type EvaluatableTo<T> = {
    [K in keyof T]: T[K] | ((T) => T[K]);
};

export function evaluateConfig(evaluatable: EvaluatableTo<ResolverConfig>) {
    const evaluated = {}
    for (const key in evaluatable) {
        const value = evaluatable[key as keyof typeof evaluatable]
        evaluated[key] = typeof value === 'function' ? value(evaluated as ResolverConfig) : value
    }
    return evaluated as ResolverConfig
}

export const config = evaluateConfig(getDefaultConfig())

function getDefaultConfig(): EvaluatableTo<ResolverConfig> {
    return {
        AWARDS_BACKGROUND_COLOR: "#242425",
        AWARDS_BORDER_RADIUS: "0px",
        AWARDS_GAP: "14px",
        AWARDS_PADDING_LEFT: "16px",
        AWARDS_PADDING_RIGHT: "16px",
        AWARDS_PADDING_TOP: "7px",
        AWARDS_TEXT_COLOR: (cfg: ResolverConfig) => cfg.GLOBAL_TEXT_COLOR,

        CONTEST_COLOR: "#4C83C3",
        CONTEST_CAPTION: "",
        BASE_URL_WS: "ws://localhost:8080/resolution",

        SCOREBOARD_RESOLVED_ROWS_BELOW: 5,
        GLOBAL_DEFAULT_FONT_FAMILY: "Helvetica, serif", // css-property
        GLOBAL_DEFAULT_FONT_SIZE: "22px", // css-property
        GLOBAL_DEFAULT_FONT_WEIGHT: 400, // css-property
        GLOBAL_DEFAULT_FONT_WEIGHT_BOLD: 700, // css-property
        GLOBAL_DEFAULT_FONT: (cfg: ResolverConfig) =>
            cfg.GLOBAL_DEFAULT_FONT_SIZE + " " + cfg.GLOBAL_DEFAULT_FONT_FAMILY,
        GLOBAL_BACKGROUND_COLOR: "#242425",
        GLOBAL_TEXT_COLOR: "#FFF",
        GLOBAL_BORDER_RADIUS: "0px",

        VERDICT_OK: "#3bba6b",
        VERDICT_NOK: "#CB2E28",
        VERDICT_UNKNOWN: "#F3BE4B",

        VERDICT_LABEL_FONT_SIZE: "14px",
        VERDICT_CELL_TRANSITION_TIME: 250, // ms
        VERDICT_CELL_BRODER_RADIUS: (cfg: ResolverConfig) =>
            cfg.GLOBAL_BORDER_RADIUS,

        SCOREBOARD_ROW_TRANSITION_TIME: 1000,
        SCOREBOARD_SCROLL_INTERVAL: 20000,
        SCOREBOARD_CHOSEN_ROW_COLOR: "green",
        SCOREBOARD_BACKGROUND_COLOR: (cfg: ResolverConfig) =>
            cfg.GLOBAL_BACKGROUND_COLOR,
        SCOREBOARD_BORDER_RADIUS: (cfg: ResolverConfig) =>
            cfg.GLOBAL_BORDER_RADIUS,
        SCOREBOARD_TEXT_COLOR: (cfg: ResolverConfig) => cfg.GLOBAL_TEXT_COLOR,
        SCOREBOARD_CAPTION_FONT_SIZE: "32px", // css value
        SCOREBOARD_HEADER_BACKGROUND_COLOR: (cfg: ResolverConfig) =>
            cfg.CONTEST_COLOR,
        SCOREBOARD_HEADER_DIVIDER_COLOR: (cfg: ResolverConfig) =>
            cfg.SCOREBOARD_BACKGROUND_COLOR,
        SCOREBOARD_HEADER_FONT_SIZE: (cfg: ResolverConfig) =>
            cfg.GLOBAL_DEFAULT_FONT_SIZE,
        SCOREBOARD_HEADER_FONT_WEIGHT: (cfg: ResolverConfig) =>
            cfg.GLOBAL_DEFAULT_FONT_WEIGHT,
        SCOREBOARD_HEADER_HEIGHT: 38,
        SCOREBOARD_ROWS_DIVIDER_COLOR: (cfg: ResolverConfig) =>
            cfg.CONTEST_COLOR,
        SCOREBOARD_ROW_HEIGHT: 32, // px
        SCOREBOARD_ROW_PADDING: 1, // px
        SCOREBOARD_BETWEEN_HEADER_PADDING: 3, //px
        SCOREBOARD_ROW_FONT_SIZE: (cfg: ResolverConfig) =>
            cfg.GLOBAL_DEFAULT_FONT_SIZE,
        SCOREBOARD_TABLE_ROW_FONT_WEIGHT: (cfg: ResolverConfig) =>
            cfg.GLOBAL_DEFAULT_FONT_WEIGHT,

        SCOREBOARD_CELL_PLACE_SIZE: "73px",
        SCOREBOARD_CELL_TEAMNAME_SIZE: "304px",
        SCOREBOARD_CELL_TEAMNANE_ALIGN: "left",
        SCOREBOARD_CELL_POINTS_SIZE: "81px",
        SCOREBOARD_CELL_POINTS_ALIGN: "center",
        SCOREBOARD_CELL_PENALTY_SIZE: "92px",
        SCOREBOARD_CELL_PENALTY_ALIGN: "center",

        SCOREBOARD_NORMAL_NAME: "Current",
        SCOREBOARD_OPTIMISTIC_NAME: "Optimistic",
        SCOREBOARD_PESSIMISTIC_NAME: "Pessimistic",
        SCOREBOARD_UNDEFINED_NAME: "??",
        SCOREBOARD_STANDINGS_NAME: "standings",

        SCOREBOARD_GAP: "14px",
        SCOREBOARD_PADDING_TOP: "7px",
        SCOREBOARD_PADDING_RIGHT: "16px",
        SCOREBOARD_PADDING_LEFT: "16px",
        SCOREBOARD_HEADER_PADDING_TOP: "0.3em",
        SCOREBOARD_CELL_PADDING: "8px",
        SCOREBOARD_HEADER_BORDER_RADIUS_TOP_LEFT: "16px",
        SCOREBOARD_HEADER_BORDER_RADIUS_TOP_RIGHT: "16px"
    };
}

export default config