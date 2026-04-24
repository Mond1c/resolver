import {useEffect, useRef} from "react";

interface UseWebSocketParams {
    url: string,
    onMessage: (data: any) => void
}

export function useWebSocket(params: UseWebSocketParams) {
    const {url, onMessage} = params
    const wsRef = useRef<WebSocket | null>(null)
    const onMessageRef = useRef(onMessage)

    useEffect(() => {
        onMessageRef.current = onMessage
    }, [onMessage]);

    useEffect(() => {
        const socket = new WebSocket(url)
        wsRef.current = socket

        socket.onmessage = (event) => {
            onMessageRef.current?.(JSON.parse(event.data))
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