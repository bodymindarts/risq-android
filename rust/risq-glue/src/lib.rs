#![cfg(target_os = "android")]
#![allow(non_snake_case)]

use android_logger::Config;
use jni::{
    objects::{JObject, JString},
    sys::jint,
    JNIEnv,
};
use log::{debug, Level};
use risq;
use std::{
    ffi::{CStr, CString},
    path::PathBuf,
    str::FromStr,
};

#[no_mangle]
pub unsafe extern "C" fn Java_risq_android_TorService_startRisq(
    env: JNIEnv,
    _: JObject,
    j_risq_home: JString,
    tc_port: jint,
    socks_port: jint,
    j_network: JString,
    j_debug_level: JString,
) {
    let debug_level = match CStr::from_ptr(env.get_string(j_debug_level).unwrap().as_ptr()).to_str()
    {
        Ok("INFO") => Level::Info,
        Ok("WARN") => Level::Warn,
        Ok("ERROR") => Level::Error,
        Ok("DEBUG") => Level::Debug,
        Ok("VERBOSE") => Level::Trace,
        _ => Level::Info,
    };

    let network: risq::BaseCurrencyNetwork =
        CStr::from_ptr(env.get_string(j_network).unwrap().as_ptr())
            .to_str()
            .ok()
            .map(risq::BaseCurrencyNetwork::from_str)
            .and_then(Result::ok)
            .unwrap_or(risq::BaseCurrencyNetwork::BtcMainnet);

    android_logger::init_once(Config::default().with_min_level(debug_level));

    let risq_home: PathBuf = CString::from(CStr::from_ptr(
        env.get_string(j_risq_home).unwrap().as_ptr(),
    ))
    .into_string()
    .expect("get risq_home")
    .into();

    info!("Starting risq daemon");

    risq::run(risq::DaemonConfig {
        api_port: 7477,
        server_port: 5000,
        network,
        risq_home,
        tor_control_port: Some(tc_port as u16),
        tor_proxy_port: Some(socks_port as u16),
        hidden_service_port: Some(9999),
    })
}
