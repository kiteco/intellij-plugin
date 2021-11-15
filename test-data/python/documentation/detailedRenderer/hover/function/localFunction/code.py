# myFunction prints the values it receives
def myFunction(a, b, *myVararg, **myKwarg):
    print()
    print(a)
    print(b)
    print(myVararg)
    print(myKwarg)
    return 123

myFunction(123, "abc")
myFunction("abc", True)
myFunction("abc", True, 123, 234, myKwArg1=42)
myFunction("abc", True, 123, 234, myKwArg1=123)
myFunction("abc", True, 123, 234, myKwArg1="7", myKwarg2=123)
