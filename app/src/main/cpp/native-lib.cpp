#include <jni.h>
#include <pjsua2.hpp>
using namespace pj;

extern "C"
JNIEXPORT void JNICALL
Java_com_bnw_voip_VoipLib_init(JNIEnv* env, jclass clazz) {
// minimal example just to prove it's working
pj::Endpoint ep;
ep.libCreate();
EpConfig ep_cfg;
ep.libInit(ep_cfg);
}
