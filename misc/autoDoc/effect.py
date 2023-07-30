from minxin import *

with open('../../doc/effect.md', 'w', encoding='utf-8') as f:
    f.write(
        genMdTable('效果列表', parseSql('effect', 'id'),
                   {'name': '名称', 'img': '图像', 'description': '描述'})
    )
