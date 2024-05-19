#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_run_piece_dev_App_isCheckDebugToolJNI(
        JNIEnv* env,
        jobject /* this */) {
    int TPid;
    char buf[512];
    const char *str = "TracerPid:";
    size_t strSize = strlen(str);
    std::string strDebugging = "NONE";
    FILE* file = fopen("/proc/self/status", "r");

    while (fgets(buf, 512, file)) {
        if (!strncmp(buf, str, strSize)) {
            sscanf(buf, "TracerPid: %d", &TPid);
            if (TPid != 0) {
                strDebugging = buf;
                fclose(file);
                return env->NewStringUTF(strDebugging.c_str());
            }
        }
    }

    fclose(file);
    return env->NewStringUTF(strDebugging.c_str());
}
