import styled from "styled-components";
import {ReconnectingWebSocket} from "@resolver/src/websocket/ReconnectingWebSocket";
import React from "react";

export const AuthWrap = styled.div`
    flex: 1;
    display: flex;
    gap: 15px;
    padding-left: 15px;
    padding-right: 15px;
    padding-top: 15px;
    align-items: center;
    flex-direction: column;
`

export const LoginInputWrap = styled.input.attrs(
    {
        type: 'text',
        placeholder: 'Login'
    }
)<{ isError: boolean }>`
    font-size: 24px;
    border: 2px solid ${props => props.isError ? 'red' : 'initial'}
`

export const PasswordInputWrap = styled.input.attrs(
    {
        type: 'password',
        placeholder: 'Password'
    }
)<{ isError: boolean }>`
    font-size: 24px;
    border: 2px solid ${props => props.isError ? 'red' : 'initial'}
`

export type AuthProps = {
    ws: ReconnectingWebSocket | null
    login: string
    setLogin: React.Dispatch<React.SetStateAction<string>>
    password: string
    setPassword: React.Dispatch<React.SetStateAction<string>>
    isError: boolean
}

export const AuthButtonWrap = styled.button`
    font-size: 24px;
`

export const Auth = (
    {
        ws,
        login,
        isError,
        setLogin,
        password,
        setPassword
    }: AuthProps
) => {
    return <AuthWrap>
        <LoginInputWrap
            value={login}
            isError={isError}
            onChange={(ce) => {
                const value = ce.target.value
                setLogin(value)
            }}>
        </LoginInputWrap>
        <PasswordInputWrap
            value={password}
            isError={isError}
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