package java.nio;

class DirectByteBufferImpl
{
    public static void junk()
    {
        //below is testing code that should ultimately be removed
        DirectByteBufferImpl db = new DirectByteBufferImpl();
        db.doit();
    }

    public void doit()
    {
        new ReadWrite();
    }

    static final class ReadWrite extends DirectByteBufferImpl
    {
        public ReadWrite ()
        {
        }

        //(Ljava/lang/Object;Lgnu/classpath/Pointer;III)V
        public ReadWrite(Object obj1, gnu.classpath.Pointer ptr1, int i1, int i2, int i3)
        {

        }
        public ReadWrite(int i1)
        {

        }
        public ReadWrite(Object obj1)
        {

        }
        public ReadWrite(gnu.classpath.Pointer ptr1)
        {

        }
    }
}

