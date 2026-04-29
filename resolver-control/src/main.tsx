import {StrictMode} from 'react'
import {createRoot} from 'react-dom/client'
import {App} from './App'
import {Provider} from "react-redux";
import {createGlobalStyle} from "styled-components";
import {configureStore} from "@reduxjs/toolkit";
import {widgetsReducer} from "@resolver/src/widgets";
import scoreboardReducer from "@overlay/src/redux/contest/scoreboard";
import {contestInfoReducer} from "@overlay/src/redux/contest/contestInfo";
import rowReducer from "@resolver/src/redux/row";
import problemReducer from "@resolver/src/redux/problem";
import {store} from "./models";

const container = document.getElementById("root");
const root = createRoot(container);

export const GlobalStyle = createGlobalStyle`
    body {
        margin: 0;
        padding: 0;

        font-family: Helvetica, sans-serif;

        /* height: 100vh;
        width: 100vw; */

        -webkit-font-smoothing: antialiased;
        -moz-osx-font-smoothing: grayscale;
    }

    * {
        scrollbar-width: none;

        -ms-overflow-style: none;
    }

    *::-webkit-scrollbar {
        display: none;
    }
`;

root.render(
    <StrictMode>
        <Provider store={store}>
            <GlobalStyle/>
            <App/>
        </Provider>
    </StrictMode>
)
