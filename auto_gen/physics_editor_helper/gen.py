import re
import json

"""
a.txt和a.json都是PhysicsEditor生成的
其中，a.txt包含Hull polygon（凹多边形）的信息，可用于点击和寻路算法
而a.json包含凸多边形列表信息，可以直接被matter.js使用，用于碰撞检测

两个文件里面，store、tree这种代表物体的完整形状
而store-2、tree-2这种代表物体只取下面一部分的形状（玩家只与建筑只有下半部分会碰撞，可直接穿过建筑上半部分，显得有立体感）
"""

def genCollapseShapes():
    """生成collapseShapes.json：只取下面一部分的凸多面体列表（用于碰撞检测，以及放置建筑时的碰撞检测）"""
    with open("physics_editor_helper/data/a.json", encoding='utf8') as f:
        jsonData = json.load(f)
    needToModify = []
    for key in jsonData.keys():
        if (key[-2:] == '-2'):
            needToModify.append(key)
    for key in needToModify:
        originKey = key[:-2]
        jsonData[originKey] = jsonData[key]
        jsonData[originKey]["label"] = originKey
        del jsonData[key]
    with open("../sandbox_town_frontend/src/assets/json/collapseShapes.json", 'w', encoding='utf8') as f:
        # json.dump(jsonData, f, ensure_ascii=False, indent=4)
        json.dump(jsonData, f, ensure_ascii=False)

def genClickShape():
    """生成clickShapes.json：完整凹多面体（用于点击） """
    with open('physics_editor_helper/data/a.txt') as f:
        s = f.read()
        nameList = re.findall(r'Name:\s+(\S+)', s)
        sizeList = re.findall(r'Size:\s+\{\s*(\S+),(\S+)\s*\}', s)
        sizeList = [(int(float(item[0])), int(float(item[1])))
                    for item in sizeList]
        polygonList = re.findall(
            r'Hull polygon:[\s\n]+(.+)[\s\n]+Convex sub polygons', s)
        polygonList = [re.findall(
            r'\((-?\d+(?:\.\d+)?), (-?\d+(?:\.\d+)?)\)', item) for item in polygonList]
        polygonList = [[(int(float(item[0])), int(float(item[1])))
                        for item in polygon] for polygon in polygonList]
        jsonData = dict()
        for i in range(len(nameList)):
            name = nameList[i]
            if name[-2:] == '-2':
                continue
            size = sizeList[i]
            polygon = polygonList[i]
            jsonData[name] = []
            for axis in polygon:
                # 也不知道size[0]和size[1]写反了没有，因为目前是正方形
                jsonData[name].append(axis[0] + int(size[0] / 2))
                jsonData[name].append(axis[1] + int(size[1] / 2))
        with open("../sandbox_town_frontend/src/assets/json/clickShapes.json", 'w', encoding='utf8') as f:
            # json.dump(jsonData, f, ensure_ascii=False, indent=4)
            json.dump(jsonData, f, ensure_ascii=False)

def genBitmap():
    """生成bitmap.json：只取下面一部分的凹多面体（用于寻路算法，需要配合下面要将的算法）"""

    def foo(width, height, points, path):
        """将凹多边形转换成bitmap"""
        from PIL import Image, ImageDraw

        # 创建一个大小为(width, height)的bitmap
        bitmapImg = Image.new('1', (width, height), color=1)

        # 创建一个绘图对象
        draw = ImageDraw.Draw(bitmapImg)

        # 绘制凹多边形
        draw.polygon(points, fill=0)

        # 将bitmap保存图片
        bitmapImg.save(path)

    with open("physics_editor_helper/data/a.txt", encoding='utf8') as f:
        s = f.read()
        nameList = re.findall(r'Name:\s+(\S+)', s)
        sizeList = re.findall(r'Size:\s+\{\s*(\S+),(\S+)\s*\}', s)
        sizeList = [(int(float(item[0])), int(float(item[1])))
                    for item in sizeList]
        polygonList = re.findall(
            r'Hull polygon:[\s\n]+(.+)[\s\n]+Convex sub polygons', s)
        polygonList = [re.findall(
            r'\((-?\d+(?:\.\d+)?), (-?\d+(?:\.\d+)?)\)', item) for item in polygonList]
        polygonList = [[(int(float(item[0])), int(float(item[1])))
                        for item in polygon] for polygon in polygonList]
        # 原点在中央，需要转换成左上角
        polygonList = [[(item[0] + int(sizeList[i][0] / 2), item[1] + int(sizeList[i][1] / 2)) for item in polygon] for i, polygon in enumerate(polygonList)]
        nameSet = set(nameList)
        for i in range(len(nameList)):
            name = nameList[i]
            appendName = name + '-2'
            if appendName in nameSet:
                continue
            if name[-2:] == '-2':
                # 删掉后缀
                name = name[:-2]
            size = sizeList[i]
            polygon = polygonList[i]
            # 生成bitmap
            foo(size[0], size[1], polygon, '../sandbox_town_backend/src/main/resources/static/bitmap/' + name + '.png')

def genAll():
    genCollapseShapes()
    genClickShape()
    genBitmap()

if __name__ == '__main__':
    genAll()