from auto_doc.gen import genAll as gen_auto_doc
from physics_editor_helper.gen import genAll as gen_physics_editor_helper

def genAll():
    gen_auto_doc()
    gen_physics_editor_helper()

if __name__ == '__main__':
    genAll()