import yaml
import pprint

if __name__ == '__main__':
    with open("../tests/resource/out/JavaParser.java.yaml") as f:
        lines = f.readlines()
        yamlStr = "".join(lines)

    pp = pprint.PrettyPrinter()
    for d in yaml.load_all(yamlStr):
        n = d['root(Type=MethodDeclaration)']['name(Type=SimpleName)']['identifier']
        print(n)
