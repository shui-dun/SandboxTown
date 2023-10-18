import emitter from "./mitt";

// websocket连接
var connection = null;

let retryInterval = 1000; // 初始重试间隔为1秒

let maxRetryInterval = 30000; // 设置最大重试间隔为30秒，您可以根据实际需要进行调整

// 建立websocket连接
createWebSocket();

// 创建websocket，如果断开就使用二进制指数回退重连
function createWebSocket() {
    console.log("call createWebSocket");
    
    connection = new WebSocket((window.location.protocol === 'https:' ? 'wss:' : 'ws:') + "//" + window.location.host + "/websocket");
    connection.onmessage = websocketOnMessage;

    connection.onopen = function() {
        // 当连接成功时，重置重试间隔
        retryInterval = 1000;
    };

    connection.onclose = function () {
        setTimeout(createWebSocket, retryInterval);
        retryInterval *= 2; // 指数增长
        if (retryInterval > maxRetryInterval) {
            retryInterval = maxRetryInterval; // 限制最大重试间隔
        }
    }

    // 为防止onerror和onclose同时触发重连，注释掉onerror
    // connection.onerror = function () { xxx }
}



async function websocketOnMessage(event) {
    let response = JSON.parse(event.data);
    // 发送事件（发布订阅模式）
    emitter.emit(response.type, response.data);
}

// 当使用 export default connection; 导出 connection 时
// 导出的是 connection 当时的值，而不是引用
// 这意味着，当 connection 重新赋值（例如，在重连时）时，其他模块导入的 connection 值不会自动更新
// 解决办法1：导出一个对象，该对象包含 connection 的引用
// const state = {
//     connection: null
// };
// export default state;
// // 在其他模块中
// import wsState from "./websocket";
// 解决办法2：导出一个获取函数（目前的解决办法）
function ws() {
    return connection;
}
export default ws;
