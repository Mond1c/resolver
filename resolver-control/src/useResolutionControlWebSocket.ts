import {useEffect, useRef} from "react";
import {ServerToControllerMessage} from "./models";

interface UseResolutionControlWebSocketParams {
    url: string,
    onMessage: (data: any) => void
}

export function useResolutionControlWebSocket(params: UseResolutionControlWebSocketParams) {
    const {url, onMessage} = params
    const wsRef = useRef<WebSocket | null>(null)
    const onMessageRef = useRef(onMessage)

    useEffect(() => {
        onMessageRef.current = onMessage
    }, [onMessage]);

    useEffect(() => {
        const socket = new WebSocket(url)
        wsRef.current = socket

        socket.onmessage = (messageEvent) => {
            onMessageRef.current?.(JSON.parse(messageEvent.data) as ServerToControllerMessage)
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