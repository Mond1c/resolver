import {ProblemId, TeamId} from "@shared/api";
import {createSlice, PayloadAction} from "@reduxjs/toolkit";

export type ProblemData = {
    teamId: TeamId | null,
    problemId: ProblemId | null,
    chosen: boolean | null
};

const initialState: ProblemData =
    {
        teamId: null,
        problemId: null,
        chosen: null
    };

const problemSlice = createSlice({
    name: "problem",
    initialState,
    reducers: {
        handleProblem(
            state,
            action: PayloadAction<{
                problem: ProblemData;
            }>,
        ) {
            const {teamId, problemId, chosen} = action.payload.problem;
            state.teamId = teamId
            state.problemId = problemId
            state.chosen = chosen
        },
    },
});

export const {handleProblem} = problemSlice.actions;

export default problemSlice.reducer;