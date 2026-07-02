.class public Teste6
.super java/lang/Object

.method public static main([Ljava/lang/String;)V
    .limit stack 20
    .limit locals 10

    new java/util/Scanner
    dup
    getstatic java/lang/System/in Ljava/io/InputStream;
    invokespecial java/util/Scanner/<init>(Ljava/io/InputStream;)V
    astore 1

    getstatic java/lang/System/out Ljava/io/PrintStream;
    ldc "Digite um numero inteiro positivo: "
    invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V
    aload 1
    invokevirtual java/util/Scanner/nextInt()I
    istore 2
    iload 2
    iconst_0
    if_icmpgt L2
    iconst_0
    goto L3
L2:
    iconst_1
L3:
    ifeq L0
    iconst_0
    istore 4
    iconst_1
    istore 3
L4:
    iload 4
    iload 3
    iadd
    istore 4
    iload 3
    iconst_1
    iadd
    istore 3
    iload 3
    iload 2
    if_icmpgt L5
    iconst_0
    goto L6
L5:
    iconst_1
L6:
    ifeq L4
    getstatic java/lang/System/out Ljava/io/PrintStream;
    ldc "A soma de 1 ate "
    invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V
    getstatic java/lang/System/out Ljava/io/PrintStream;
    iload 2
    invokevirtual java/io/PrintStream/println(I)V
    getstatic java/lang/System/out Ljava/io/PrintStream;
    ldc " e: "
    invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V
    getstatic java/lang/System/out Ljava/io/PrintStream;
    iload 4
    invokevirtual java/io/PrintStream/println(I)V
    goto L1
L0:
    getstatic java/lang/System/out Ljava/io/PrintStream;
    ldc "Numero invalido. Digite um valor maior que zero."
    invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V
L1:

    return
.end method
