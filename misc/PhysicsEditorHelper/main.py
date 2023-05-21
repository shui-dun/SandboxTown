import re
import json

def genCollapseShapes():
    """生成collapseShapes.json：只取下面一部分的凸多面体列表（用于碰撞检测，以及放置建筑时的碰撞检测）"""
    with open("data/a.json", encoding='utf8') as f:
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
    with open("output/collapseShapes.json", 'w', encoding='utf8') as f:
        # json.dump(jsonData, f, ensure_ascii=False, indent=4)
        json.dump(jsonData, f, ensure_ascii=False)
        
        # for item in jsonData[key]:
        #     for i in range(len(item)):
        #         item[i] = [int(item[i][0]), int(item[i][1])]
        

def genClickShape():
    """生成clickShapes.json：完整凹多面体（用于点击） """
    with open('data/a.txt') as f:
        s = f.read()
        nameList = re.findall(r'Name:\s+(\S+)', s)
        sizeList = re.findall(r'Size:\s+\{\s*(\S+),(\S+)\s*\}', s)
        sizeList = [(int(float(item[0])), int(float(item[1]))) for item in sizeList]
        polygonList = re.findall(r'Hull polygon:\s+((\((-?\d+(\.\d+)?), (-?\d+(\.\d+)?)\)\s+,?\s+)+)Convex sub polygons', s, re.S)
        polygonList = [re.findall(r'\((-?\d+(?:\.\d+)?), (-?\d+(?:\.\d+)?)\)', item[0]) for item in polygonList]
        polygonList = [[(int(float(item[0])), int(float(item[1]))) for item in polygon] for polygon in polygonList]
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
        with open("output/clickShapes.json", 'w', encoding='utf8') as f:
            # json.dump(jsonData, f, ensure_ascii=False, indent=4)
            json.dump(jsonData, f, ensure_ascii=False)




"""生成bitmap.json：只取下面一部分的凹多面体（用于寻路算法，需要配合下面要将的算法）"""

if __name__ == "__main__":
    # genCollapseShapes()
    genClickShape()
