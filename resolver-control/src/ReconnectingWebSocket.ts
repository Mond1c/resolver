export interface WebSocketCallbacks {
    onopen?: (event: Event) => void
    onmessage?: (event: MessageEvent<any>) => void
    onerror?: (event: Event) => void
    onclose?: (event: CloseEvent) => void
}

export class ReconnectingWebSocket {
    private readonly url: any;
    private reconnectDelay: number;
    private readonly maxReconnectDelay: number;
    private reconnectAttempts: number;
    private maxReconnectAttempts: number | null;
    private ws: WebSocket;
    private callbacks: WebSocketCallbacks

    constructor(url: string, callbacks: WebSocketCallbacks) {
        this.url = url;
        this.reconnectDelay = 1000;
        this.maxReconnectDelay = 30000;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = null;
        this.callbacks = callbacks
        this.connect();
    }

    connect() {
        this.ws = new WebSocket(this.url);

        this.ws.onopen = (event) => {
            this.reconnectDelay = 1000;
            this.reconnectAttempts = 0;
            this.callbacks.onopen?.(event);
        };

        this.ws.onmessage = (event) => {
            this.callbacks.onmessage?.(event);
        };

        this.ws.onerror = (event) => {
            this.callbacks.onerror?.(event);
        };

        this.ws.onclose = (event) => {
            this.callbacks.onclose?.(event);

            if (!event.wasClean && this.shouldReconnect()) {
                setTimeout(() => {
                    this.reconnectAttempts++;
                    this.reconnectDelay = Math.min(
                        this.reconnectDelay * 2,
                        this.maxReconnectDelay
                    );
                    this.connect();
                }, this.reconnectDelay);
            }
        };
    }

    shouldReconnect() {
        return (
            this.maxReconnectAttempts === null ||
            this.reconnectAttempts < this.maxReconnectAttempts
        );
    }

    send(data) {
        if (this.ws.readyState === WebSocket.OPEN) {
            this.ws.send(data);
        }
    }

    close(code = 1000, reason = '') {
        this.maxReconnectAttempts = 0;
        this.ws.close(code, reason);
    }
}