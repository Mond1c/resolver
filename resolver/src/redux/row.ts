import {TeamId} from "@shared/api";
import {createSlice, PayloadAction} from "@reduxjs/toolkit";

export type RowData = {
    teamId: TeamId | null,
    chosen: boolean | null
};

const initialState: RowData =
    {
        teamId: null,
        chosen: null
    };

const rowSlice = createSlice({
    name: "row",
    initialState,
    reducers: {
        handleRow(
            state,
            action: PayloadAction<{
                row: RowData;
            }>,
        ) {
            const {teamId, chosen} = action.payload.row;
            state.teamId = teamId
            state.chosen = chosen
        },
    },
});

export const {handleRow} = rowSlice.actions;

export default rowSlice.reducer;