import {ContestInfo, ScoreboardRow} from "@shared/api";

export type UiEvent =
    | UiEvent.Scoreboard
    | UiEvent.Accept
    | UiEvent.ChooseProblem
    | UiEvent.ChooseRow
    | UiEvent.HideGroupAwards
    | UiEvent.HideTeamAwards
    | UiEvent.NoOp
    | UiEvent.Reject
    | UiEvent.ReverseAccept
    | UiEvent.ReverseReject
    | UiEvent.ShowGroupAwards
    | UiEvent.ShowTeamAwards
    | UiEvent.UnchooseProblem
    | UiEvent.UnchooseRow;

export namespace UiEvent {
    export enum Type {
        Scoreboard = "Scoreboard",
        Accept = "Accept",
        ChooseProblem = "ChooseProblem",
        ChooseRow = "ChooseRow",
        HideGroupAwards = "HideGroupAwards",
        HideTeamAwards = "HideTeamAwards",
        NoOp = "NoOp",
        Reject = "Reject",
        ReverseAccept = "ReverseAccept",
        ReverseReject = "ReverseReject",
        ShowGroupAwards = "ShowGroupAwards",
        ShowTeamAwards = "ShowTeamAwards",
        UnchooseProblem = "UnchooseProblem",
        UnchooseRow = "UnchooseRow"
    }

    export interface Scoreboard {
        type: UiEvent.Type.Scoreboard,
        teamIdToScoreboardRow: Record<TeamId, ScoreboardRow>,
        order: TeamId[],
        ranks: number[],
        contestInfo: ContestInfo,
        indexOfLastChosenRow: number,
        teamOfLastChosenRow: TeamId,
        isLastChosenRowChosenNow: boolean
    }

    export interface Accept {
        type: UiEvent.Type.Accept;
        row: ScoreboardRow,
        ranks: number[],
        order: string[],
        teamId: TeamId;
        problemId: ProblemId;
    }

    export interface ChooseProblem {
        type: UiEvent.Type.ChooseProblem;
        index: number;
        teamId: TeamId,
        problemId: ProblemId;
    }

    export interface ChooseRow {
        type: UiEvent.Type.ChooseRow;
        teamId: TeamId,
        index: number;
    }

    export interface HideGroupAwards {
        type: UiEvent.Type.HideGroupAwards;
        awards: Award[];
    }

    export interface HideTeamAwards {
        type: UiEvent.Type.HideTeamAwards;
        teamId: TeamId;
        awards: Award[];
    }

    export interface NoOp {
        type: UiEvent.Type.NoOp;
    }

    export interface Reject {
        type: UiEvent.Type.Reject;
        row: ScoreboardRow,
        teamId: TeamId;
        problemId: ProblemId;
    }

    export interface ReverseAccept {
        type: UiEvent.Type.ReverseAccept;
        teamId: TeamId;
        problemId: ProblemId;
        ranks: number[];
        order: TeamId[];
        row: ScoreboardRow;
    }

    export interface ReverseReject {
        type: UiEvent.Type.ReverseReject;
        teamId: TeamId;
        problemId: ProblemId;
        row: ScoreboardRow;
    }

    export interface ShowGroupAwards {
        type: UiEvent.Type.ShowGroupAwards;
        awards: Award[];
    }

    export interface ShowTeamAwards {
        type: UiEvent.Type.ShowTeamAwards;
        teamId: TeamId;
        awards: Award[];
    }

    export interface UnchooseProblem {
        type: UiEvent.Type.UnchooseProblem;
        index: number;
        teamId: TeamId,
        problemId: ProblemId;
    }

    export interface UnchooseRow {
        type: UiEvent.Type.UnchooseRow;
        teamId: TeamId,
        index: number;
    }
}

export type TeamId = string;

export type ProblemId = string;

export type Award =
    | Award.custom
    | Award.group_champion
    | Award.medal
    | Award.winner;

export namespace Award {
    export enum Type {
        custom = "custom",
        group_champion = "group_champion",
        medal = "medal",
        winner = "winner",
    }

    export interface custom {
        type: Award.Type.custom;
        id: string;
        citation: string;
        teams: TeamId[];
    }

    export interface group_champion {
        type: Award.Type.group_champion;
        id: string;
        citation: string;
        groupId: GroupId;
        teams: TeamId[];
    }

    export interface medal {
        type: Award.Type.medal;
        id: string;
        citation: string;
        medalColor: MedalColor | null;
        teams: TeamId[];
    }

    export interface winner {
        type: Award.Type.winner;
        id: string;
        citation: string;
        teams: TeamId[];
    }
}

export type GroupId = string;

export enum MedalColor {
    GOLD = "GOLD",
    SILVER = "SILVER",
    BRONZE = "BRONZE",
}