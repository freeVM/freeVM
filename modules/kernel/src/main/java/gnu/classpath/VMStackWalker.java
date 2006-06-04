package gnu.classpath;

public class VMStackWalker
{
    //during bootup, jchevm specifically loads this class
    //this empty stub is all that is required for simple "hello world"
    //see jchevm/libjc/bootstrap.c
    //
    //_jc_object * _JC_JCNI_ATTR
    // JCNI_gnu_classpath_VMStackWalker_getCallingClassLoader(_jc_env *env)
    // _jc_object * _JC_JCNI_ATTR
    // JCNI_gnu_classpath_VMStackWalker_getCallingClass(_jc_env *env)
    // _jc_object_array * _JC_JCNI_ATTR
    // JCNI_gnu_classpath_VMStackWalker_getClassContext(_jc_env *env)
    // _jc_object * _JC_JCNI_ATTR
    // JCNI_gnu_classpath_VMStackWalker_getClassLoader(_jc_env *env, _jc_object
    // *clobj)
    //
    public static final native Class[] getClassContext();
    public static final native ClassLoader getClassLoader(Class clazz);
}
