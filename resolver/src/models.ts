import {ContestInfo, ScoreboardRow} from "@shared/api";

export type UiEvent =
    | UiEvent.Scoreboard
    | UiEvent.AcceptICPC
    | UiEvent.AcceptIOI
    | UiEvent.ChooseProblem
    | UiEvent.ChooseRow
    | UiEvent.HideGroupAwards
    | UiEvent.HideTeamAwards
    | UiEvent.NoOp
    | UiEvent.RejectICPC
    | UiEvent.RejectIOI
    | UiEvent.ReverseAcceptICPC
    | UiEvent.ReverseAcceptIOI
    | UiEvent.ReverseRejectICPC
    | UiEvent.ReverseRejectIOI
    | UiEvent.ShowGroupAwards
    | UiEvent.ShowTeamAwards
    | UiEvent.UnchooseProblem
    | UiEvent.UnchooseRow;

export namespace UiEvent {
    export enum Type {
        Scoreboard = "Scoreboard",
        AcceptICPC = "AcceptICPC",
        AcceptIOI = "AcceptIOI",
        ChooseProblem = "ChooseProblem",
        ChooseRow = "ChooseRow",
        HideGroupAwards = "HideGroupAwards",
        HideTeamAwards = "HideTeamAwards",
        NoOp = "NoOp",
        RejectICPC = "RejectICPC",
        RejectIOI = "RejectIOI",
        ReverseAcceptICPC = "ReverseAcceptICPC",
        ReverseAcceptIOI = "ReverseAcceptIOI",
        ReverseRejectICPC = "ReverseRejectICPC",
        ReverseRejectIOI = "ReverseRejectIOI",
        ShowGroupAwards = "ShowGroupAwards",
        ShowTeamAwards = "ShowTeamAwards",
        UnchooseProblem = "UnchooseProblem",
        UnchooseRow = "UnchooseRow",
    }

    export interface Scoreboard {
        type: UiEvent.Type.Scoreboard,
        teamIdToScoreboardRow: Record<TeamId, ScoreboardRow>,
        order: TeamId[],
        ranks: number[],
        contestInfo: ContestInfo
    }

    export interface AcceptICPC {
        type: UiEvent.Type.AcceptICPC;
        row: ScoreboardRow,
        ranks: number[],
        order: string[],
        teamId: TeamId;
        problemId: ProblemId;
        oldIndex: number;
        newIndex: number;
    }

    export interface AcceptIOI {
        type: UiEvent.Type.AcceptIOI;
        row: ScoreboardRow,
        ranks: number[],
        order: string[],
        teamId: TeamId;
        problemId: ProblemId;
        oldIndex: number;
        newIndex: number;
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

    export interface RejectICPC {
        type: UiEvent.Type.RejectICPC;
        row: ScoreboardRow,
        teamId: TeamId;
        problemId: ProblemId;
    }

    export interface RejectIOI {
        type: UiEvent.Type.RejectIOI;
        row: ScoreboardRow,
        teamId: TeamId;
        problemId: ProblemId;
    }

    export interface ReverseAcceptICPC {
        type: UiEvent.Type.ReverseAcceptICPC;
        teamId: TeamId;
        problemId: ProblemId;
        oldRanks: number[];
        oldOrder: TeamId[];
        oldRow: ScoreboardRow;
    }

    export interface ReverseAcceptIOI {
        type: UiEvent.Type.ReverseAcceptIOI;
        teamId: TeamId;
        problemId: ProblemId;
        oldRanks: number[];
        oldOrder: TeamId[];
        oldRow: ScoreboardRow;
    }

    export interface ReverseRejectICPC {
        type: UiEvent.Type.ReverseRejectICPC;
        teamId: TeamId;
        problemId: ProblemId;
        oldRow: ScoreboardRow;
    }

    export interface ReverseRejectIOI {
        type: UiEvent.Type.ReverseRejectIOI;
        teamId: TeamId;
        problemId: ProblemId;
        oldRow: ScoreboardRow;
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