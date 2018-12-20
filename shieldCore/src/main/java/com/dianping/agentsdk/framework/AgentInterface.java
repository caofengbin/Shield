package com.dianping.agentsdk.framework;

import android.content.Intent;
import android.os.Bundle;

/**
 * 在Shield框架里，页面是由一个个模块（Agent）组成的。
 * 模块是页面中粗粒度的抽象组件，包含部分页面UI展示和与之相关的业务逻辑。
 * 这些模块按线性的方式排布在页面中，可以很灵活地调换位置且互不影响。
 * 每个模块都有自己独立的生命周期，可以单独通过网络获取数据、渲染视图等等。
 * Created by hezhi on 16/1/26.
 */
public interface AgentInterface {

    void onCreate(Bundle savedInstanceState);

    void onStart();

    void onResume();

    void onPause();

    void onStop();

    void onDestroy();

    Bundle saveInstanceState();

    @Deprecated
    void onAgentChanged(Bundle data);

    void onActivityResult(int requestCode, int resultCode, Intent data);

    String getIndex();

    void setIndex(String index);

    String getHostName();

    void setHostName(String hostName);

    SectionCellInterface getSectionCellInterface();

    String getAgentCellName();
}
