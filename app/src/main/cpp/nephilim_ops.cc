#include <jni.h>
#include <cmath>

constexpr float NU_P=0.17259029f,PHI=1.6180339887f,F0=15.965f;

extern "C" JNIEXPORT jfloat JNICALL
Java_com_vesper_genesis_BraidBridge_braidEval(JNIEnv* env, jobject /* this */, jfloat a, jfloat b, jfloat cc) {
    float yz=fmodf(b*cc*PHI,1.0f), xz=fmodf(a*cc*PHI,1.0f);
    float ph=0.01f*sinf(2*M_PI*F0*0.0625f);
    return fmodf(xz+yz*NU_P+ph,1.0f);
}
