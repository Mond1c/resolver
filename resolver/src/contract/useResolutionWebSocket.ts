import {useEffect, useRef} from "react";
import {UiEvent} from "./models";

interface UseResolutionWebSocketParams {
    url: string,
    onMessage: (data: any) => void
}

export function useResolutionWebSocket(params: UseResolutionWebSocketParams) {
    const {url, onMessage} = params
    const wsRef = useRef<WebSocket | null>(null)
    const onMessageRef = useRef(onMessage)
    const bufferRef = useRef<UiEvent[]>([])
    const isScoreboardLoaded = useRef<boolean>(false)

    useEffect(() => {
        onMessageRef.current = onMessage
    }, [onMessage]);

    useEffect(() => {
        const socket = new WebSocket(url)
        wsRef.current = socket

        socket.onmessage = (messageEvent) => {
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
        }

        socket.onerror = () => {
            console.log("Error in web socket occurred.")
        }

        return () => {
            socket.close(1000)
        }
    }, [url]);

    return {ws: wsRef.current}
}