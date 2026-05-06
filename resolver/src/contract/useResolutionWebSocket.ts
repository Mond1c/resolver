import {useEffect, useRef} from "react";
import {UiEvent} from "./models";
import {ReconnectingWebSocket} from "../websocket/ReconnectingWebSocket";

interface UseResolutionWebSocketParams {
    url: string,
    onMessage: (data: any) => void,
    onClose: (code: number) => void
}

export function useResolutionWebSocket(params: UseResolutionWebSocketParams) {
    const {url, onMessage, onClose} = params
    const wsRef = useRef<ReconnectingWebSocket | null>(null)
    const onMessageRef = useRef(onMessage)
    const bufferRef = useRef<UiEvent[]>([])
    const isScoreboardLoaded = useRef<boolean>(false)
    const onCloseRef = useRef(onClose)

    useEffect(() => {
        onMessageRef.current = onMessage
    }, [onMessage]);

    useEffect(() => {
        onCloseRef.current = onClose
    }, [onClose]);

    useEffect(() => {
        const socket = new ReconnectingWebSocket(url, {
            onmessage: (messageEvent) => {
                const uiEvent = JSON.parse(messageEvent.data) as UiEvent
                if (isScoreboardLoaded.current) {
                    onMessageRef.current?.(uiEvent)
                } else if (uiEvent.type === UiEvent.Type.Scoreboard) {
                    isScoreboardLoaded.current = true
                    onMessageRef.current?.(uiEvent)
                    for (const event of bufferRef.current) {
                        onMessageRef.current?.(event)
                    }
                    bufferRef.current = []
                } else {
                    bufferRef.current.push(uiEvent)
                }
            },
            onclose: (event: CloseEvent) => {
                onCloseRef.current?.(event.code)
            }
        })
        wsRef.current = socket

        return () => {
            socket.close(1000)
        }
    }, [url]);

    return {ws: wsRef.current}
}