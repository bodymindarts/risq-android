#![cfg(target_os = "android")]
#![allow(non_snake_case)]

use android_logger::Config;
use jni::{
    objects::{JObject, JString},
    sys::{jint, jstring},
    JNIEnv,
};
use log::{debug, Level};
use risq;
use std::{
    ffi::{CStr, CString},
    path::PathBuf,
};

#[no_mangle]
pub unsafe extern "C" fn Java_risq_android_TorService_startRisq(
    env: JNIEnv,
    _: JObject,
    risq_home: JString,
) {
    android_logger::init_once(Config::default().with_min_level(Level::Debug));

    let risq_home: PathBuf =
        CString::from(CStr::from_ptr(env.get_string(risq_home).unwrap().as_ptr()))
            .into_string()
            .expect("get risq_home")
            .into();
    debug!("Starting risq daemon");

    risq::run(risq::DaemonConfig {
        api_port: 7477,
        server_port: 5000,
        network: risq::BaseCurrencyNetwork::BtcMainnet,
        risq_home,
        tor_control_port: Some(9051),
        tor_proxy_port: Some(9050),
        hidden_service_port: Some(9999),
    })
}

// fn log_level(level: Cstr) {
//     match level {
//         "info"
//     }
// }
