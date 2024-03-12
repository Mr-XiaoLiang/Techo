# Web
沟通的Bridge，需要H5与原生双方的协议，因此这里记录对于本模块而言的默认事件总线。

## 接口定义
```javascript

// 这是Web端用于接收原生调用的事件总线
// @params action 这是事件名称，也可以是定义的方法名称，一般情况下，他是一个字符串类型，用于事件分发
// @params params 这是事件或函数携带的参数，它可能会空字符串，或者是字符串参数，也可能会是json。决定者是事件的约定
// @return 如果有必要，也可以提供一些返回内容。原生端接收时，将他转换为字符串传递。但是它的内容可以是字符串，或json，或者其他内容。
function lWebBridge(action, params)


// 这是原生端用于接收Web调用的事件总线
// @params action 这是事件名称，也可以是定义的方法名称，一般情况下，他是一个字符串类型，用于事件分发
// @params params 这是事件请求的参数。它可以为空、或者空字符串，或者为json，否则原生端将无法完成解析
// 同时，对于函数的返回值，接口将不会直接同步返回，因此调用的过程中会产生多次线程切换，无法同步返回，返回的结果将会出现在 { lWebBridge }的事件总线中，具体的分发细节，请查看关于 params 的部分
function nativeBridge(action, params)


```

## 接口参数
本部分的参数内容，主要针对`nativeBridge`接口的参数结构定义。
```json
{
    "data": {},
    "callback": ""
}
```
| 参数 | 类型 | 说明 |
| :-: | :-: | :-: |
| data | json | 参数的对象，内部参数会被真正的分发到函数处理逻辑中。可以为空或者不存在。 |
| callback | String | 回调函数的名称，用于回调函数在`lWebBridge`的`action`分发。可以为空或不存在。 |

## 范例
### getActionBarHeight
Web端请求调用
```javascript

function demo() {
    nativeBridge(
        "getActionBarHeight", 
        { callback: "onActionBarHeightResult" }
    );
}

function lWebBridge(action, params) {
    switch (action) {
        case "onActionBarHeightResult": {
            // 假定此处约定直接将高度值作为返回值参数
            let actionBarHeight = params;
        }
    }
}

```

## 事件总线实现范例
Web端
``` javascript

var bridgeCallbackMap = new Map();
var tempCallbackIndex = 0;

// 原有Web端接收器总线
function lWebBridge(action, params) {
    // 尝试检查是否为临时回调
    let tempCallback = bridgeCallbackMap.get(action);
    // 如果能查到，那么即消费
    if (tempCallback) {
        // 临时回调带有即时性，因此使用一次后就会释放
        bridgeCallbackMap.delete(action);
        // 触发回调函数完成流程
        tempCallback(params);
        return;
    }
    // 否则，我们认为是原生的通知行为
    onNativeCall(action, params);
}

// 新的包装后非临时回调的事件接收总线，建议在此处再次分发
function onNativeCall(action, params) {
    switch (action) {
        case "XXX": {
            // 可以在此处分发事件监听逻辑
        }
    }
}

// 包装后的Web端请求原生的函数，简化参数的设置与调用
// @params action 请求的函数名，与原始的函数对应
// @params params 参数内容，此处为协议函数本身
// @params callback 回调函数
function requestNative(action, params, callback) {
    let body = {};
    body.data = params;
    tempCallbackIndex++;
    let callbackName = "tempCallback" + tempCallbackIndex;
    bridgeCallbackMap.set(callbackName, callback);
    body.callback = callbackName;
    nativeBridge(action,body);
}

// 请求测试
function test() {
    requestNative(
        "getActionBarHeight", 
        null, 
        (result) => {
            // actionBarHeight
        }
    )
}

```


## 基础API
### getWebInsets
获取应用的系统元素占用尺寸，用于沉浸式的UI交互。
请求参数：无
返回值：
``` json
{
    "actionBarSize":0,
    "leftEdge":0,
    "topEdge":0,
    "rightEdge":0,
    "bottomEdge":0,
}
```
| 参数 | 类型 | 说明 |
| :-: | :-: | :-: |
| actionBarSize | int | ActionBar的高度，它可能会是0，单位为px |
| leftEdge | int | 左侧的系统占用尺寸，它一般会在横屏时不为0，单位为px |
| topEdge | int | 顶部的系统占用尺寸，它一般表示状态栏的高度，或者异形屏幕的顶部高度，单位为px |
| rightEdge | int | 右侧的系统占用尺寸，它一般会在横屏时不为0，单位为px |
| bottomEdge | int | 底部的系统占用尺寸，它一般会在虚拟导航栏存在时不为0，单位为px |


### fullScreen
用于设置状态栏与导航栏的状态。
此功能的效果受到具体实现业务的影响。
请求参数：
``` json
{
    "fixStatusBar":0,
    "fixNavigateBar":0
}
```

| 参数 |   类型    |                           说明                            |
| :-: |:-------:|:-------------------------------------------------------:|
| fixStatusBar | boolean | 当设置为true时，表示网页希望自行处理状态栏与ActionBar的空间，这需要与getWebInsets配合 |
| fixNavigateBar | boolean |   当设置为true时，表示网页希望自行处理导航栏（虚拟按键）的空间，这需要与getWebInsets配合   |

返回值：无