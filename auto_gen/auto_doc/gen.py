import re
import os

def parseSql(tableName, columnOfImgName=None, imgSize=120):
    """
    从 import.sql 中提取某个表的 INSERT 信息，返回一个 list[dict]。
    例如将
    INSERT INTO tableName (a, b, c) VALUES 
    (1, 2, 3),
    (4, 5, 6),
    (7, 8, 9);
    转化为
    [{a: 1, b: 2, c: 3}, {a: 4, b: 5, c: 6}, {a: 7, b: 8, c: 9}]
    columnOfImgName表示要生成 img 列的列名（如果需要），imgSize表示图片大小
    """
    with open('../sandbox_town_db/import.sql', encoding='utf-8') as f:
        sql = f.read()

    # 注意：要让 tableName 在正则中安全，需要先对其做 re.escape
    # 这个正则会捕捉:
    #   1) INSERT INTO tableName (a,b,c) VALUES (1,2,3),(4,5,6);
    #   2) 可以有多个 INSERT INTO 同一个表，程序会将他们全部捕捉并拼接
    # group(1) => 字段列表
    # group(2) => 所有的多行值 (1,2,3),(4,5,6)
    regex = r'INSERT INTO\s+{}\s*\(([^)]*)\)\s*VALUES\s*((?:\(.*?\)\s*,?\s*)+);'.format(re.escape(tableName))
    pattern = re.compile(regex, re.IGNORECASE | re.DOTALL)
    matches = pattern.findall(sql)

    all_rows = []

    for (fields_str, values_str) in matches:
        # 把字段名字拆开
        field_names = [fn.strip() for fn in fields_str.split(',')]

        # 处理 values_str 中的多组 ( ... ), ( ... )
        # 例如 (1,2,3),(4,5,6)
        # 用一个 split 或正则切分
        row_strings = re.split(r'\)\s*,\s*\(', values_str.strip())
        # 修整一下开头结尾的括号
        if row_strings:
            # 如果 row_strings[0] 以 '(' 开头，则去掉 '('
            if row_strings[0].startswith('('):
                row_strings[0] = row_strings[0][1:]
            # 如果 row_strings[-1] 以 ')' 结尾，则去掉 ')'
            if row_strings[-1].endswith(')'):
                row_strings[-1] = row_strings[-1][:-1]

        # 将每组数据解析为 dict
        for row_str in row_strings:
            # 以逗号切割，并去掉多余的引号
            values = [v.strip().strip("'") for v in row_str.split(',')]
            # 拉链成 key-value
            row_dict = dict(zip(field_names, values))
            all_rows.append(row_dict)

    # 如果指定了 columnOfImgName，则给每行增加一个 img 字段
    if columnOfImgName:
        for row in all_rows:
            # 例如 `<img src="../sandbox_town_frontend/src/assets/img/WOOD.png" width="120" />`
            row['img'] = f'<img src="../sandbox_town_frontend/src/assets/img/{row[columnOfImgName]}.png" width="{imgSize}" />'

    return all_rows

def genMdTable(head, data, columns):
    """
    data形如[{a: 1, b: 2, c: 3}, {a: 4, b: 5, c: 6}, {a: 7, b: 8, c: 9}]
    columns形如{'a': '嘻嘻', 'c': '呵呵'}
    return形如 
              | 嘻嘻 | 呵呵 |
              | --- | --- |
              | 1 | 3 |
              | 4 | 6 |
              | 7 | 9 |
    """
    # 生成标题
    title = '# ' + head + '\n\n'
    # 生成表头
    header = '|'
    for key in columns:
        header += ' ' + columns[key] + ' |'
    header += '\n'

    # 生成分隔符
    separator = '|'
    for key in columns:
        separator += ' --- |'
    separator += '\n'

    # 生成表格内容
    content = ''
    for item in data:
        line = '|'
        for key in columns:
            line += ' ' + str(item[key]) + ' |'
        line += '\n'
        content += line

    return title + header + separator + content

def genEffectDoc():
    with open('../doc/effect.md', 'w', encoding='utf-8') as f:
        f.write(
            genMdTable('效果列表', parseSql('effect', 'id'),
                    {'name': '名称', 'img': '图像', 'description': '描述'})
        )

def genItemDoc():
    with open('../doc/item.md', 'w', encoding='utf-8') as f:
        f.write(
            genMdTable('物品列表', parseSql('item_type', 'id'),
                    {'name': '名称', 'img': '图像', 'description': '描述'})
        )

def genEnum():
    """
    读取 import.sql 中的四个表:
    1. building_type   => BuildingTypeEnum.java (取字段 id)
    2. effect          => EffectEnum.java        (取字段 id)
    3. item_type       => ItemTypeEnum.java      (取字段 id)
    4. sprite_type     => SpriteTypeEnum.java    (取字段 type)
    """
    base_dir = r"../sandbox_town_backend/src/main/java/com/shuidun/sandbox_town_backend/enumeration"

    # 包名
    package_name = "com.shuidun.sandbox_town_backend.enumeration"

    # 小工具：写枚举文件
    def write_enum_file(filename, enum_name, values):
        """
        在 base_dir 下创建一个名为 filename 的文件，并写入 enum 内容。
        values 为一个字符串列表，形如 ["STORE", "TREE", ...]
        """
        # 如果目录不存在，可自行添加：
        if not os.path.exists(base_dir):
            os.makedirs(base_dir)

        path = os.path.join(base_dir, filename)
        with open(path, 'w', encoding='utf-8') as f:
            f.write(f"package {package_name};\n\n")
            f.write(f"public enum {enum_name} {{\n")
            for i, v in enumerate(values):
                # 最后一个枚举常量后面不加逗号
                suffix = "," if i < len(values) - 1 else ""
                f.write(f"    {v}{suffix}\n")
            f.write("}\n")

    # 1) 生成 BuildingTypeEnum
    building_data = parseSql('building_type')
    # 取出 id 列表
    building_ids = [row['id'] for row in building_data]
    write_enum_file('BuildingTypeEnum.java', 'BuildingTypeEnum', building_ids)

    # 2) 生成 EffectEnum
    effect_data = parseSql('effect')
    effect_ids = [row['id'] for row in effect_data]
    write_enum_file('EffectEnum.java', 'EffectEnum', effect_ids)

    # 3) 生成 ItemTypeEnum
    item_data = parseSql('item_type')
    item_ids = [row['id'] for row in item_data]
    write_enum_file('ItemTypeEnum.java', 'ItemTypeEnum', item_ids)

    # 4) 生成 SpriteTypeEnum
    sprite_data = parseSql('sprite_type')
    sprite_ids = [row['type'] for row in sprite_data]
    write_enum_file('SpriteTypeEnum.java', 'SpriteTypeEnum', sprite_ids)

def genFusionDoc():
    """生成物品合成文档 fusion.md"""
    # 1) 先获取所有带图片的物品信息：每个 item 包含 {id, name, description, img, ...}
    items = parseSql('item_type', 'id', 50)
    item_info = {item['id']: item for item in items}

    # 2) 获取所有融合结果 (fusion 表) 和 材料表 (fusion_material)
    fusions = parseSql('fusion')
    materials = parseSql('fusion_material')

    # 3) 按 fusion_id 对材料分组
    #   material_groups[fusion_id] = [ "木头<img> x3", "石头<img> x2", ... ]
    material_groups = {}
    for mat in materials:
        fid = mat['fusion_id']
        if fid not in material_groups:
            material_groups[fid] = []

        mat_item = item_info.get(mat['item_name'], {})
        mat_name = mat_item.get('name', mat['item_name'])  # 如果没找到就用原 id
        mat_img = mat_item.get('img', '')                  # 图片可能为空
        mat_qty = mat['quantity']

        # 这里既显示名称也显示图片
        mat_str = f"{mat_name}{mat_img} ×{mat_qty}"
        material_groups[fid].append(mat_str)

    # 4) 组装合成表
    #    每一行形如 {'result': 'xxx<img>', 'materials': '原料1<img> x1, 原料2<img> x2', 'description': 'xxx'}
    fusion_data = []
    for fusion in fusions:
        fid = fusion['id']
        res_item_id = fusion['result_item_id']
        res_item = item_info.get(res_item_id, {})
        res_name = res_item.get('name', res_item_id)
        res_img = res_item.get('img', '')
        res_desc = res_item.get('description', '')

        # “合成结果”列，包含名称和图片
        result_str = f"{res_name}{res_img}"
        # “所需材料”列
        materials_str = ', '.join(material_groups.get(fid, []))

        fusion_data.append({
            'result': result_str,
            'materials': materials_str,
            'description': res_desc
        })

    # 5) 写入 ../doc/fusion.md
    with open('../doc/fusion.md', 'w', encoding='utf-8') as f:
        f.write(genMdTable(
            '物品合成表',
            fusion_data,
            {
                'result': '合成结果',
                'materials': '所需材料',
                'description': '物品描述'
            }
        ))

def genAll():
    genEffectDoc()
    genItemDoc()
    genEnum()
    genFusionDoc()

if __name__ == '__main__':
    genAll()