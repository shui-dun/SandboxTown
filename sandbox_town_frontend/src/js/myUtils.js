let fadeInfoShow = null;

// 设置全局的工具类里面的fadeInfoShow方法，因为有些地方需要显示提示框
function setFadeInfoShow(fadeInfoShowFunc) {
    fadeInfoShow = fadeInfoShowFunc;
}

async function myFetch(url, method, body, callback) {
    let result = null;
    await fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: body,
    }).then(response => response.json())
        .then(data => {
            if (data.code === 0) {
                result = data.data;
                if (callback) {
                    callback(data.data);
                }
            } else {
                fadeInfoShow(data.msg);
            }
        }).catch(error => {
            fadeInfoShow(`请求出错: ${error}`);
        });
    return result;
}

export default { setFadeInfoShow, myFetch };