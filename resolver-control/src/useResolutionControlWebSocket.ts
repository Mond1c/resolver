import {useEffect, useRef} from "react";
import {ServerToControllerMessage} from "./models";
import {ReconnectingWebSocket} from "@resolver/src/websocket/ReconnectingWebSocket"

interface UseResolutionControlWebSocketParams {
    url: string,
    onMessage: (data: any) => void,
    onClose: (code: number) => void
}

export function useResolutionControlWebSocket(params: UseResolutionControlWebSocketParams) {
    const {url, onMessage, onClose} = params
    const wsRef = useRef<ReconnectingWebSocket | null>(null)
    const onMessageRef = useRef(onMessage)
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
                onMessageRef.current?.(JSON.parse(messageEvent.data) as ServerToControllerMessage)
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