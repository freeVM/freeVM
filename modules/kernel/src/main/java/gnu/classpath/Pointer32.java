package gnu.classpath;

public class Pointer32 extends Pointer
{
    //during bootup, jchevm specifically loads this class
    //this empty stub is all this is required for simple "hello world"
    //see jchevm/libjc/bootstrap.c
    int data;
}
