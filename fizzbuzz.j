.class public Fizzbuzz
.super java/lang/Object

.field private static _runTimer LRunTimer;

.field private static I I

.method public <init>()V

	aload_0
	invokenonvirtual	java/lang/Object/<init>()V
	return

.limit locals 1
.limit stack 1
.end method

.method public static main([Ljava/lang/String;)V

    new	 RunTimer
    dup
    invokenonvirtual	RunTimer/<init>()V
    putstatic	Fizzbuzz/_runTimer LRunTimer;

    ldc 5
    putstatic Fizzbuzz/I I
    getstatic Fizzbuzz/I I
    i2f
    ldc 5
    i2f
    fcmpg
    ifne label1
    ldc 6
    putstatic label2/I I
    goto label2
label1:
label2:

    getstatic	Fizzbuzz/_runTimer LRunTimer;
    invokevirtual	RunTimer.printElapsedTime()V

    return

.limit locals 2
.limit stack  16
.end method