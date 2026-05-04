import {useEffect, useRef} from "react";
import {ServerToControllerMessage} from "./models";
import {ReconnectingWebSocket} from "./ReconnectingWebSocket"

interface UseResolutionControlWebSocketParams {
    url: string,
    onMessage: (data: any) => void
}

export function useResolutionControlWebSocket(params: UseResolutionControlWebSocketParams) {
    const {url, onMessage} = params
    const wsRef = useRef<ReconnectingWebSocket | null>(null)
    const onMessageRef = useRef(onMessage)

    useEffect(() => {
        onMessageRef.current = onMessage
    }, [onMessage]);

    useEffect(() => {
        const socket = new ReconnectingWebSocket(url, {
            onmessage: (messageEvent) => {
                onMessageRef.current?.(JSON.parse(messageEvent.data) as ServerToControllerMessage)
            }
        })
        wsRef.current = socket

        return () => {
            socket.close(1000)
        }
    }, [url]);

    return {ws: wsRef.current}
}