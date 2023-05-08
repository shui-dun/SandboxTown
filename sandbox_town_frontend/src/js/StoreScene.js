const storeScene = {
    key: 'store',
    preload: function () {},
    create: function () {
        const item = this.add.text(100, 100, "hello,store");
        item.setInteractive();

        item.on('pointerdown', () => {
            console.log(this);
            clearScene(this);
            this.game.scene.start('main');
            this.game.events.emit('itemClicked');
        });
    },
    update: function () {},
};

function clearScene(scene) {
    // 遍历场景中的所有游戏对象
    scene.children.each(function (gameObject) {
        // 移除游戏对象上的所有事件监听器
        gameObject.removeAllListeners();
    });

    // 清空场景的游戏对象和事件
    scene.children.removeAll();
    scene.events.off();
}




export default storeScene;