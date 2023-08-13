import emitter from "./mitt";

// websocket连接
var ws = null;

// 上一次创建ws的时间
var lastCreateWsTime = null;

// 建立websocket连接
createWebSocket();

// 创建websocket，如果断开就重连
function createWebSocket() {
    console.log("call createWebSocket");
    // 如果上次调用该函数的时间距离现在小于1秒，就等待1秒再调用
    if (lastCreateWsTime != null && new Date().getTime() - lastCreateWsTime < 1000) {
        setTimeout(createWebSocket, 1000);
        return;
    }
    console.log("createWebSocket");
    ws = new WebSocket((window.location.protocol === 'https:' ? 'wss:' : 'ws:') + "//" + window.location.host + "/websocket");
    ws.onmessage = websocketOnMessage;
    ws.onclose = createWebSocket;
    ws.onerror = createWebSocket;
    lastCreateWsTime = new Date().getTime();
}

async function websocketOnMessage(event) {
    let response = JSON.parse(event.data);
    emitter.emit(response.type, response.data);
}

export default ws;