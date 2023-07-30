from minxin import *

with open('../../doc/item.md', 'w', encoding='utf-8') as f:
    f.write(
        genMdTable('物品列表', parseSql('item_type', 'id'),
                   {'name': '名称', 'img': '图像', 'description': '描述'})
    )
