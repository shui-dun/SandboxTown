import re

with open('a.txt') as f:
    lst = re.findall(r'\((-?\d+), (-?\d+)\)', f.read())
    for item in lst:
        print("{}, {},".format(int(item[0]) + 250, int(item[1]) + 250))
