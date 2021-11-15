class Counter(object):
    """This is a counter"""
    def __init__(self, n=0, name="counter"):
        self.n = n
        self.name = name

    def increment(self, n):
        return self.n

def foo(bar) -> int | str | bool:
    return bar;

# Caret between . and increment
Counter.increment

x = True
x = 1
x = 2
print(x)