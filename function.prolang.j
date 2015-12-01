.class public Function
.super java/lang/Object

.field private static _runTimer LRunTimer;

.field private static A I

.method public <init>()V

	aload_0
	invokenonvirtual	java/lang/Object/<init>()V
	return

.limit locals 1
.limit stack 1
.end method

.method private static Add_five(LIWrap;)V

    .var 0 is A LIWrap;

    aload 0
    aload 0
    getfield IWrap/value I
    ldc 5
    iadd
    putfield IWrap/value I
    getstatic java/lang/System/out Ljava/io/PrintStream;
    aload 0
    getfield IWrap/value I
    invokevirtual java/io/PrintStream/println(I)V

    return

.limit locals 1
.limit stack  16
.end method

.method public static main([Ljava/lang/String;)V

    new	 RunTimer
    dup
    invokenonvirtual	RunTimer/<init>()V
    putstatic	Function/_runTimer LRunTimer;

    ldc 5
    putstatic Function/A I
    new IWrap 
    dup
    getstatic Function/A I
    invokenonvirtual IWrap/<init>(I)V
    dup
    astore 1
    invokestatic  Function/Add_five(LIWrap;)V
    aload 1
    getfield IWrap/value I
    putstatic Function/A I
    getstatic java/lang/System/out Ljava/io/PrintStream;
    getstatic Function/A I
    invokevirtual java/io/PrintStream/print(I)V

    getstatic	Function/_runTimer LRunTimer;
    invokevirtual	RunTimer.printElapsedTime()V

    return

.limit locals 2
.limit stack  16
.end method
