import re
import os

def parseSql(tableName, columnOfImgName=None):
    # 读取import.sql
    with open('../sandbox_town_db/import.sql', encoding='utf-8') as f:
        sql = f.read()


    # 从中找到tableName表的数据（忽略大小写）
    # INSERT INTO tableName (a, b, c) VALUES 
    # (1, 2, 3),
    # (4, 5, 6),
    # (7, 8, 9);
    regex = r'INSERT INTO {} \((.*?)\)(.*?)\);'.format(tableName)
    pattern = re.compile(regex, re.IGNORECASE | re.MULTILINE | re.DOTALL)
    matched = pattern.search(sql).group()

    # 将其转化为python的数据结构: [{a: 1, b: 2, c: 3}, {a: 4, b: 5, c: 6}, {a: 7, b: 8, c: 9}]
    def convert_sql_to_dict(sql_string):
        # Splitting the string to extract field names and values
        field_names_string = sql_string[sql_string.find("(")+1:sql_string.find(")")]
        field_names = [name.strip() for name in field_names_string.split(",")]

        values_string = sql_string[sql_string.find("VALUES")+7:sql_string.rfind(");")]
        values_lists = values_string.split("),")

        dict_list = []

        # Looping through each set of values and associating them with field names
        for values in values_lists:
            values = values[values.find("(")+1:].strip()
            # Stripping quotes from values
            values = [value.strip().strip("'") for value in values.split(",")]
            dict_list.append(dict(zip(field_names, values)))

        return dict_list

    dict_list = convert_sql_to_dict(matched)

    # 添加图片路径
    if columnOfImgName:
        for item in dict_list:
            item['img'] = '<img src="../sandbox_town_frontend/src/assets/img/{}.png" width="120" />'.format(item[columnOfImgName])

    return dict_list


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

def genAll():
    genEffectDoc()
    genItemDoc()
    genEnum()

if __name__ == '__main__':
    genAll()