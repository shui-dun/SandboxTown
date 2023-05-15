import re

with open('a.txt') as f:
    # offset = 250
    offset = 512
    lst = re.findall(r'\((-?\d+), (-?\d+)\)', f.read())
    for item in lst:
        print("{}, {},".format(int(item[0]) + offset, int(item[1]) + offset))
