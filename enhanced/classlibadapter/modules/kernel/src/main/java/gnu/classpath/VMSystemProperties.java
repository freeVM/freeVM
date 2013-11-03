package gnu.classpath;

import java.util.Properties;

public class VMSystemProperties
{
    // jchevm system properties exported specifically to this class
    // Following native method defined:
    //   void JCNI_gnu_classpath_VMSystemProperties_preInit(
    //        _jc_env *env, _jc_object *props)
    //
    // Properties set:
    // { "java.version",			"1.4.2" },
    // { "java.vendor",			"JC virtual machine project" },
    // { "java.vendor.url",			"http://jcvm.sourceforge.net/" },
    // { "java.home",				_AC_DATADIR "/jc" },
    // { "java.vm.name",			"JC virtual machine" },
    // { "java.vm.vendor",			"JC virtual machine project" },
    // { "java.vm.version",			PACKAGE_VERSION },
    // { "java.vm.specification.name",		"Java Virtual Machine Specification" },
    // { "java.vm.specification.vendor",	"Sun Microsystems Inc." },
    // { "java.vm.specification.version",	"1.0" },
    // { "java.specification.name",		"Java Platform API Specification" },
    // { "java.specification.vendor",		"Sun Microsystems Inc." },
    // { "java.specification.version",		"1.4" },
    // { "java.class.version",			"46.0" },
    // { "java.io.tmpdir",			_JC_TEMP_DIR },
    // { "file.separator",			_JC_FILE_SEPARATOR },
    // { "line.separator",			_JC_LINE_SEPARATOR },
    // { "path.separator",			_JC_PATH_SEPARATOR },
    // { "java.library.path",			_JC_LIBRARY_PATH },
    // { "java.boot.class.path",		_JC_BOOT_CLASS_PATH },
    // #if _JC_BIG_ENDIAN
    // { "gnu.cpu.endian",			"big" },
    // #else
    // { "gnu.cpu.endian",			"little" },
    // #endif
    // #if _JC_THREAD_LOCAL_SUPPORT
    // { "jc.thread.local",			"true" },
    // #else
    // { "jc.thread.local",			"false" },
    // #endif
    // { "jc.stack.minimum",			_JC_STACK_MINIMUM },
    // { "jc.stack.maximum",			_JC_STACK_MAXIMUM },
    // { "jc.stack.default",			_JC_STACK_DEFAULT },
    // { "jc.java.stack.size",			_JC_JAVA_STACK_DEFAULT },
    // { "jc.heap.size",			_JC_DEFAULT_HEAP_SIZE },
    // { "jc.loader.size",			_JC_DEFAULT_LOADER_SIZE },
    // { "jc.heap.granularity",		_JC_DEFAULT_HEAP_GRANULARITY },

    private static native void preInit(Properties props);

    static {
        Properties props = new Properties();
        preInit(props);
        systemProperties = props;
    }

    private static Properties systemProperties;
    private VMSystemProperties() {}

    // FIXME: security checks needed
    public static Properties getSystemProperties() {
        return systemProperties;
    }
}
