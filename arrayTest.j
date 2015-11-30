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

.method private static PassByReference(LIWrap;)V

    .var 0 is A LIWrap;

    getstatic java/lang/System/out Ljava/io/PrintStream;
    aload 0
    getfield IWrap/value I
    invokevirtual java/io/PrintStream/println(I)V
    aload 0
    ldc 5
    putfield IWrap/value I
    getstatic java/lang/System/out Ljava/io/PrintStream;
    aload 0
    getfield IWrap/value I
    invokevirtual java/io/PrintStream/println(I)V
    getstatic java/lang/System/out Ljava/io/PrintStream;
    getstatic ArrayTest/X I
    invokevirtual java/io/PrintStream/println(I)V

    return

.limit locals 1
.limit stack  16
.end method

.method public static main([Ljava/lang/String;)V

    new	 RunTimer
    dup
    invokenonvirtual	RunTimer/<init>()V
    putstatic	ArrayTest/_runTimer LRunTimer;

    ldc 10
    putstatic ArrayTest/X I
    new IWrap 
    dup
    getstatic ArrayTest/X I
    invokenonvirtual IWrap/<init>(I)V
    dup
    astore 1
    invokestatic  ArrayTest/PassByReference(LIWrap;)V
    aload 1
    getfield IWrap/value I
    putstatic ArrayTest/X I
    getstatic java/lang/System/out Ljava/io/PrintStream;
    getstatic ArrayTest/X I
    invokevirtual java/io/PrintStream/println(I)V

    getstatic	ArrayTest/_runTimer LRunTimer;
    invokevirtual	RunTimer.printElapsedTime()V

    return

.limit locals 2
.limit stack  16
.end method
