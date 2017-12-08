# Shield

[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](https://github.com/Meituan-Dianping/Shield/pulls)
[![License](https://img.shields.io/badge/MIT-Apache%202.0-blue.svg)](https://github.com/Meituan-Dianping/Shield/blob/master/LICENSE)

## 概述

`Shield`是一个模块化UI界面解决方案，它不仅仅是一个Native（Android&iOS）的UI开发框架，而是美团点评到店综合团队基于自身复杂的业务场景沉淀出来的UI开发最佳实践，它不但具备高可复用性，协同开发等特性，还包括后端动态配置，动态模块等一系列解决方案。


## 设计思想

### 1.模块独立

模块具备完整的页面能力（生命周期，Context等），模块可以在不同的页面中自由组合，模块与页面高度解耦，模块不关心具体在哪个页面中运行。模块之间需要解耦，模块只关心自己涉及的状态和数据。

### 2.数据驱动
模块的表现只取决于所依赖的数据，而与具体的行为无关。

### 3.接口
整个模块化框架通过抽象接口进行交互，通过接口规范行为，通过接口的不同实现达到多态的目的

## 整体框架

一个典型的模块化页面主要由页面和多个模块构成。页面通过模块配置（Config）确定加载哪些模块构成页面。模块又分为业务逻辑（Agent）和视图逻辑部分（SectionCellInterface， 包括 Section 和 Row）。
页面包含两个管理器，模块管理器（AgentManager）和视图管理器（CellManager）。其中模块管理器决定了如何创建，更新，恢复及销毁模块以及如何将模块添加到页面中；
视图管理器则决定了页面使用何种视图容器管理视图以及模块中的视图组件（SectionCellInterface）如何添加到视图容器中。
除此之外，页面还持有一个支持数据订阅和通知的白板（WhiteBoard），用于模块与页面，模块与模块之间的数据交互。

## 接入
请参照Sample中的com.example.shield.fragments.AgentManagerFragment

另外：

1. 在onCreateView中构建页面容器ContentView。
2. 在onActivityCreated方法中调用setAgentContainerView方法设置模块容器。
3. 在getCellManager方法中指定页面使用何种CellManager（默认为SectionRecyclerCellManager,与RecyclerView容器配合使用）。
4. 在getAgentManager方法中指定页面使用何种AgentManager（默认为LightAgentManager，一般无需定制）。
5. 通过generaterDefaultConfigAgentList方法指定模块配置。

## 技术支持
- 通过提交issue来寻求帮助
- 联系我们寻求帮助

## 贡献代码
- 欢迎提交issue
- 欢迎提交PR

## LICENSE

```
Copyright (c) 2017 美团点评

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```