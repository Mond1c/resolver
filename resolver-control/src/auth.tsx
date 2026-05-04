import styled from "styled-components";
import {ReconnectingWebSocket} from "./ReconnectingWebSocket";
import React from "react";

export const AuthWrap = styled.div`
    flex: 1;
    display: flex;
    gap: 15px;
    padding-left: 15px;
    padding-right: 15px;
    padding-top: 15px;
    align-items: flex-start;
    flex-direction: column;
`

export const LoginInputWrap = styled.input.attrs(
    {
        type: 'text',
        placeholder: 'Login'
    }
)`
    font-size: 24px;
`

export const PasswordInputWrap = styled.input.attrs(
    {
        type: 'password',
        placeholder: 'Password'
    }
)`
    font-size: 24px;
`

export type AuthProps = {
    ws: ReconnectingWebSocket | null
    login: string
    setLogin: React.Dispatch<React.SetStateAction<string>>
    password: string
    setPassword: React.Dispatch<React.SetStateAction<string>>
}

export const AuthButtonWrap = styled.button`
    font-size: 24px;
`

export const Auth = (
    {
        ws,
        login,
        setLogin,
        password,
        setPassword
    }: AuthProps
) => {
    return <AuthWrap>
        <LoginInputWrap
            value={login}
            onChange={(ce) => {
                const value = ce.target.value
                setLogin(value)
            }}>
        </LoginInputWrap>
        <PasswordInputWrap
            value={password}
            onChange={(ce) => {
                const value = ce.target.value
                setPassword(value)
            }}>
        </PasswordInputWrap>
        <AuthButtonWrap
            onClick={() => ws.send(login + ' ' + password)}>
            Sign in
        </AuthButtonWrap>
    </AuthWrap>
}