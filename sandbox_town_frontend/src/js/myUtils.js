const myUtils = {
    // 显示提示框
    fadeInfoShow: null,

    async myPOST(url, body, callback) {
        let result = null;
        await fetch(url, {
            method: 'POST',
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
                    this.fadeInfoShow(data.msg);
                }
            }).catch(error => {
                this.fadeInfoShow(`请求出错: ${error}`);
            });
        return result;
    },

    async myGET(url, queryParams, callback) {
        let result = null;

        // 将查询参数添加到URL中
        const urlWithParams = new URL(url, window.location.origin);
        if (queryParams != null) {
            for (const [key, value] of queryParams.entries()) {
                urlWithParams.searchParams.append(key, value);
            }
        }

        await fetch(urlWithParams, {
            method: 'GET',
        }).then(response => response.json())
            .then(data => {
                if (data.code === 0) {
                    result = data.data;
                    if (callback) {
                        callback(data.data);
                    }
                } else {
                    this.fadeInfoShow(data.msg);
                }
            }).catch(error => {
                this.fadeInfoShow(`请求出错: ${error}`);
            });
        return result;
    }
}

export default myUtils;