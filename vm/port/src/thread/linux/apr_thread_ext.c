#include "apr_thread_ext.h"
//#include "apr_arch_threadproc.h"
#include <pthread.h>
#include <semaphore.h>

int convert_priority(apr_int32_t priority);

APR_DECLARE(apr_status_t) apr_thread_set_priority(apr_thread_t *thread, 
                apr_int32_t priority) 
{
 /*   HANDLE *os_thread;
    apr_status_t status;
    
    if (status = apr_os_thread_get(&((apr_os_thread_t *)os_thread), thread)) {
        return status;
    }
    
    if (SetThreadPriority(os_thread, (int)convert_priority(priority))) {
        return APR_SUCCESS;
    } else {
        return apr_get_os_error();
    }
    */
    return APR_SUCCESS;
}

int convert_priority(apr_int32_t priority) {
    return (int)priority;
}



pthread_mutex_t yield_other_mutex = PTHREAD_MUTEX_INITIALIZER;
sem_t yield_other_sem;
int yield_other_init_flag = 0;

void yield_other_handler(int signum, siginfo_t* info, void* context) {
    if (yield_other_init_flag) {
        sem_post(&yield_other_sem);
    }
       
}

static void init_thread_yield_other () {
   struct sigaction sa;
   //init notification semaphore
   sem_init(&yield_other_sem,0,0);
   //set signal handler
    sigemptyset(&sa.sa_mask);
    sa.sa_flags = SA_SIGINFO | SA_RESTART;
    sa.sa_sigaction = yield_other_handler;
    sigaction(SIGUSR2, &sa, NULL);
    yield_other_init_flag = 1;
    
}
// touch thread to flash memory
APR_DECLARE(apr_status_t) apr_thread_yield_other(apr_thread_t* thread) {
    apr_status_t status;
    pthread_t *os_thread;

    pthread_mutex_lock(&yield_other_mutex);
    if (!yield_other_init_flag) {
        init_thread_yield_other ();
    }
    if (!thread 
         || (status = apr_os_thread_get((apr_os_thread_t**)&os_thread, thread)) !=APR_SUCCESS 
         || !*os_thread) {
        pthread_mutex_unlock(&yield_other_mutex);
        return status;
    }
    if(!(pthread_kill(*os_thread, SIGUSR2))) {
       sem_wait(&yield_other_sem);
    }
    pthread_mutex_unlock(&yield_other_mutex);    
    return APR_SUCCESS; 
}

APR_DECLARE(void) apr_memory_rw_barrier() {
    #ifdef _IPF_ 
        asm volatile ("mf" ::: "memory");
    #else
        __asm__("mfence");
    #endif    
}

APR_DECLARE(apr_status_t) apr_thread_times(apr_thread_t *thread, 
                                apr_time_t * kernel_time, apr_time_t * user_time){
/*    FILETIME creationTime;
    FILETIME exitTime;
    FILETIME kernelTime;
    FILETIME userTime;
    HANDLE hThread;
    SYSTEMTIME sysTime;
    int res;
    __int64 xx;
    __int32 * pp;

    res = GetThreadTimes(
        thread->td,
        &creationTime,
        &exitTime,
        &kernelTime,
        &userTime
    );
    
    printf( "Creation time = %08x %08x\n", creationTime.dwHighDateTime, creationTime.dwLowDateTime);
    printf( "Exit     time = %08x %08x\n", exitTime.dwHighDateTime, exitTime.dwLowDateTime);
    printf( "Kernrl   time = %08x %08x %08d\n", kernelTime.dwHighDateTime
                                      , kernelTime.dwLowDateTime, kernelTime.dwLowDateTime);
    printf( "User     time = %08x %08x %08d\n", userTime.dwHighDateTime
                                      , userTime.dwLowDateTime, userTime.dwLowDateTime);
    printf("%d\n", 
        ((unsigned)exitTime.dwLowDateTime - (unsigned)creationTime.dwLowDateTime)/10000000);
    
    FileTimeToSystemTime(&creationTime, &sysTime);
    printf("%d %d %d %d %d %d \n", sysTime.wYear, sysTime.wMonth,
        sysTime.wHour + 3, sysTime.wMinute, sysTime.wSecond, sysTime.wMilliseconds);
    
    pp = (int*)&xx;
    *pp = kernelTime.dwLowDateTime;
    *(pp + 1) = kernelTime.dwHighDateTime;
    *kernel_time = xx;
    pp = (int*)&xx;
    *pp = userTime.dwLowDateTime;
    *(pp + 1) = userTime.dwHighDateTime;
    *user_time = xx;*/

    return APR_SUCCESS; 
}

APR_DECLARE(apr_status_t) apr_thread_cancel(apr_thread_t *thread) {
    apr_os_thread_t *os_thread;
    apr_status_t status = apr_os_thread_get(&os_thread, thread);

    if (status ) {
        return status;
    }
    status = pthread_cancel(*(pthread_t *)os_thread);
    return status;
}
