.class public org/apache/harmony/drlvm/tests/regression/h3382/Test
.super junit/framework/TestCase
.method public <init>()V
    aload_0
    invokespecial junit/framework/TestCase/<init>()V
    return
.end method

.method public static main([Ljava/lang/String;)V
    .limit stack 10
    .limit locals 10
    new org/apache/harmony/drlvm/tests/regression/h3382/Test
    dup
    invokespecial org/apache/harmony/drlvm/tests/regression/h3382/Test/<init>()V
    invokevirtual org/apache/harmony/drlvm/tests/regression/h3382/Test/test()V
    return
.end method

.method public test()V
   invokestatic org/apache/harmony/drlvm/tests/regression/h3382/Test/test()Ljava/lang/Object;
   return
.end method

.method public static test()Ljava/lang/Object;
    .limit stack 10
    .limit locals 10
	.catch java/lang/Exception from L1 to L2 using L2
L0:
	getstatic java/lang/System/out Ljava/io/PrintStream;
    ldc "SUCCESS"
    invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V
	aconst_null     
	areturn
L1:
	astore_0        
	goto L0
L2:
	astore_1        
	getstatic java/lang/System/out Ljava/io/PrintStream;
    ldc "SUCCESS"
    invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V
	aload_1         
	athrow
	nop
.end method
