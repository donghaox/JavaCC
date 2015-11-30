.class public ArrayTest
.super java/lang/Object

.field private static _runTimer LRunTimer;

.field private static X I

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
    putstatic	ArrayTest/_runTimer LRunTimer;

    ldc 10
    putstatic ArrayTest/X I
    getstatic java/lang/System/out Ljava/io/PrintStream;
    getstatic ArrayTest/X I
    invokevirtual java/io/PrintStream/print(I)V
    getstatic java/lang/System/out Ljava/io/PrintStream;
    getstatic ArrayTest/X I
    invokevirtual java/io/PrintStream/print(I)V
    getstatic java/lang/System/out Ljava/io/PrintStream;
    getstatic ArrayTest/X I
    invokevirtual java/io/PrintStream/println(I)V
    getstatic java/lang/System/out Ljava/io/PrintStream;
    getstatic ArrayTest/X I
    invokevirtual java/io/PrintStream/println(I)V

    getstatic	ArrayTest/_runTimer LRunTimer;
    invokevirtual	RunTimer.printElapsedTime()V

    return

.limit locals 2
.limit stack  16
.end method
