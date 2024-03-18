# Techo
关于项目
> 它是一个笔记类的项目。它的目的在于提供更全面更丰富的内容来记录生活。
> 当然，因为作者的一些业余精力原因，更新会比较慢。同时，也会在里面尝试一些新的东西。
> 如果你是一位用户，那么它未来可能会满足你几乎所有的记录生活的需求。
> 如果你是一位开发者，那么也许它的源码中有一些能帮助到你的代码，欢迎取用。

## 功能
它在计划中，会包含以下功能
#### 富文本
它会尽可能多的实现富文本的样式，目前已经实现的有
* 无序列表
* 有序列表
* 复选框
* 单选框（还有些小问题）
* 文字特效（颜色、下划线、删除线、上角标、下角标等）
#### 多媒体
多媒体是智能时代笔记的必备，多元化的记录方式，会让回忆更加丰满。
* 录音与播放（已经实现）
* 视频录制与播放（还未实现）
* 照片与图片插入（拍照未实现，图片插入实现中）
* 支出与收入记录（实现中）
* 内嵌式浏览器（实现中）
* 二维码扫描与生成（已实现，暂时表现为独立的APP）

## 代码
如果你是一位开发者，那么可能你会寻找一些可用的代码组件来简化你的工作，那么你可以看看我的这些代码模块。

* **base** 它会提供常用并且必须的代码工具，比如ViewBinding的inline工具，它是符合代码规范，并且按照官方最新范例实现的。
* **gallery** 这是一个符合官方要求的相册工具，主要用于读取相册或者存储图片到相册。它兼容Android 13与Android 14的权限调整。
* **palette** 它是一个调色盘的UI组件，通过将RGB的颜色转化为HSV，并且在屏幕上呈现出所有色相，让用户能所见即所得的选择自己需要的颜色。
* **maskGuide** 这是一个用于用户引导的组件，它能帮助计算屏幕组件的位置，以此来放置相应的提示气泡引导用户操作。它会让用户就像在游戏中一样，一步步的学习操作。
* **app** 这是笔记项目的主模块，有些不是那么独立的功能都会在里面，如果你发现其他模块中找不到那个功能，那么可能是在主模块中
* **bigBoom** 它叫大爆炸，模仿锤子OS的大爆炸分词的组件，但是因为个人技术原因，没有完成实现。
* **web** 这是一个希望实现动态切换内核的浏览器模块，但是还没有完全达到目标。
* **browser** 这是一个独立的浏览器应用模块，它基于**web**模块，但是我发现一个完整的浏览器并不是那么简单，所以它被暂时搁置了。
* **recorder** 这是录音相关代码的模块，它包含从声音录制，保存到文件，读取文件并播放，频谱的获取等一系列工具，但是为了避免不必要冗余，所以它没有完整的功能实现，UI的绘制与交互，都在**app**模块中
* **renderScript** 这是Google提供的用来代替过时API的计算模块，这里我主要用它来计算高斯模糊。
* **pigment** 这是我自己设计并完成的应用主题模块，它能让应用在符合开发规范的情况下动态更新主题内容，同时不会产生额外的消耗与稳定性问题。我还加入了获取系统壁纸主题的功能。在我的**lQR**模块已经得到了实践。
* **ltabview** 这是一个很有创意的Tab组件，它来源于某个设计网站上很有想象力的设计师，我把它实现了出来。它的每个Tab在选中之前都是一个图标，但是选中之后，会有灵活的展开动画，会有颜色的变化，同时展示相应的名称。
* **qr** 这是一个二维码模块，它主要是服务于QR二维码，为QR码做了额外的处理。它的生成部分来源于ZXing。它的识别部分来自于MlKit，这是谷歌的机器学习模块，能更快的识别二维码，同时能同时识别多个二维码。
* **clip** 这是一个视图剪裁组件，能方便在UI中对特定的视图做圆角剪裁或者按照矢量图形剪裁。它主要出现在对于内容不确定，或者图片可能变化的场景，简化视图裁切的工作量。
* **fragmentHelper** 它是一个Android中Fragment的辅助工具，提供ViewPager2与ViewGroup两种模式的管理，主要是为了简化Fragment的生命周期、回收与重新创建、检索与回调等相对繁琐但是相似的工作。
* **colorRes** 这只是一个我为了统一所有相关应用风格的颜色模块
* **verticalPage** 这是一个垂直分页的UI组件，主要是为了特定场景下，需要上下翻页的交互。
* **stitch** 它是一个创意UI模块，它的作用是在一个区域内，随机将面积分成指定的数量，并且每一块的面积都是一样的，它就像是在制作一种特殊的拼图效果。很有意思。
* **fileChooser** 这是一个文件选择的模块，它的目的在于简化选择文件的过程，并且在符合开发规范的情况下，更轻松的请求并选择文件。
* **lQR** 这是我对于**qr**模块的应用实现，它包括了二维码的扫描，二维码内容的分析、复制、分享。还包括特定格式的二维码生成，比如日程、通讯录、WiFi等。同时还有相对丰富的二维码样式定制。我在2023年花了很多的时间来完成它，目前认为它是一个可用的状态了，所以重心又回到了主模块中。
* **widget** 这是一个UI小组件的模块，里面包含一些和业务没有什么关系，但是又不知道放哪里的小组件。
* **privacy** 这是一个隐私协议展示的功能模块，它能让使用它的应用通过简单的声明，就可以组合出一个完整的隐私。
* **faceIcon** 这是一个可爱的组件，它是通过特定的一些方式绘制出笑脸和沮丧的脸，并且变化的过程带有动画。它主要用于一些场景对用户表示结果的提示。目前它用在**lQR**的二维码生成中，当用户生成的二维码可能不利于识别时，会通过沮丧的表情来提示。
* **lBus** 这是一个基于Android的应用内广播来实现的事件总线，它的目的在于替代**EventBus**和**RxBus**，因为我不太喜欢他们的使用方式以及实现方式。
* **dotsCanvas** 这是一个风格化的图片转换工具，可以把图片转换为由纯色小圆点组成的图案
* **insets** 这是一个关于Android的system window insets的包装组件，简化了关于系统窗口内容缩进相关的业务逻辑



