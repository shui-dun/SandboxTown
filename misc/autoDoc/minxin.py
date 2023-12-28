import re

def parseSql(tableName, columnOfImgName=None):
    # 读取import.sql
    with open('../../sandbox_town_db/import.sql', encoding='utf-8') as f:
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
