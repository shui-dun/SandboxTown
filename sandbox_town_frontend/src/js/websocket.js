import emitter from "./mitt";

// websocket连接
var ws = null;

let retryInterval = 1000; // 初始重试间隔为1秒

let maxRetryInterval = 30000; // 设置最大重试间隔为30秒，您可以根据实际需要进行调整

// 建立websocket连接
createWebSocket();

// 创建websocket，如果断开就使用二进制指数回退重连
function createWebSocket() {
    console.log("call createWebSocket");
    
    ws = new WebSocket((window.location.protocol === 'https:' ? 'wss:' : 'ws:') + "//" + window.location.host + "/websocket");
    ws.onmessage = websocketOnMessage;

    ws.onopen = function() {
        // 当连接成功时，重置重试间隔
        retryInterval = 1000;
    };

    ws.onclose = function () {
        setTimeout(createWebSocket, retryInterval);
        retryInterval *= 2; // 指数增长
        if (retryInterval > maxRetryInterval) {
            retryInterval = maxRetryInterval; // 限制最大重试间隔
        }
    }

    // 防止onerror和onclose同时触发重连
    // ws.onerror = handleWebSocketIssue;
}



async function websocketOnMessage(event) {
    let response = JSON.parse(event.data);
    emitter.emit(response.type, response.data);
}

export default ws;