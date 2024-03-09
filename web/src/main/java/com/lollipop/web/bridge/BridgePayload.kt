package com.lollipop.web.bridge

class BridgePayload(
    val action: String,
    val params: Map<String, String>,
    val callback: String
)