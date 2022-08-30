package liang.lollipop.ltabview

/**
 * @date: 2019/04/17 19:43
 * @author: lollipop
 * Tab的item
 */
interface LTabItem {

    val miniSize: Int

    /**
     * 0：收起
     * 1：展开
     * Item展开状态控制
     */
    fun schedule(progress: Float)

    /**
     * 点击事件
     */
    fun onTabClick(listener: (LTabItem) -> Unit)

}