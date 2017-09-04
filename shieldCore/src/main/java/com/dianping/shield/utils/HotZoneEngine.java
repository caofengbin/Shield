package com.dianping.shield.utils;

import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.View;

import com.dianping.agentsdk.sectionrecycler.section.MergeSectionDividerAdapter;
import com.dianping.agentsdk.sectionrecycler.section.PieceAdapter;
import com.dianping.shield.entity.HotZoneYRange;
import com.dianping.shield.entity.ScrollDirection;
import com.dianping.shield.feature.HotZoneObserverInterface;
import com.dianping.shield.manager.LightAgentManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by hezhi on 2017/8/11.
 */

public class HotZoneEngine {
    public HotZoneObserverInterface hotZoneObserverInterface;
    HashMap<Integer, PieceAdapter> adaptersMap = new LinkedHashMap<>();//在热区的模块

    HotZoneYRange hotZoneYRange;
    Set<String> observerAgents;

    public void setHotZoneObserverInterface(HotZoneObserverInterface hotZoneObserverInterface, String prefix) {
        this.hotZoneObserverInterface = hotZoneObserverInterface;
        hotZoneYRange = hotZoneObserverInterface.defineHotZone();
        if (prefix != null) {
            Set<String> stringSet = new HashSet();
            for (String agentName : hotZoneObserverInterface.observerAgents()) {
                stringSet.add(prefix + LightAgentManager.AGENT_SEPARATE + agentName);
            }
            observerAgents = stringSet;
        } else {
            observerAgents = hotZoneObserverInterface.observerAgents();
        }
    }

    public void reset() {
        adaptersMap.clear();
    }

    public void scroll(ScrollDirection scrollDirection, RecyclerView recyclerView, MergeSectionDividerAdapter mergeRecyclerAdapter) {
        if (mergeRecyclerAdapter == null || scrollDirection == null
                || !(recyclerView.getLayoutManager() instanceof LinearLayoutManager)
                || hotZoneObserverInterface == null) {
            return;
        }

        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        int firstPosition = layoutManager.findFirstVisibleItemPosition();
        int lastPostion = layoutManager.findLastVisibleItemPosition();
        // 为 dianping 的 PullToRefreshRecyclerView 提供兼容，
        // 考虑到其中添加的一个 header view，调整相应的position
        boolean hasExtraHeader = false;
        if (recyclerView != null && recyclerView.getAdapter() != null) {
            RecyclerView.Adapter adapter = recyclerView.getAdapter();
            hasExtraHeader = "HeaderViewRecyclerAdapter".equals(adapter.getClass().getSimpleName());
        }

        if (hasExtraHeader) {
            firstPosition--;
            lastPostion--;
        }


        if (scrollDirection == ScrollDirection.UP) {
            Pair<Integer, Integer> temSectionInfo = mergeRecyclerAdapter.getSectionIndex(firstPosition);
            if (temSectionInfo != null) {
                MergeSectionDividerAdapter.DetailSectionPositionInfo sectionInfo
                        = mergeRecyclerAdapter.getDetailSectionPositionInfo(temSectionInfo.first, temSectionInfo.second);
                if (sectionInfo != null) {
                    HashMap<Integer, PieceAdapter> tempMap = (HashMap<Integer, PieceAdapter>) adaptersMap.clone();
                    for (Map.Entry<Integer, PieceAdapter> entry : tempMap.entrySet()) {
                        if (entry.getKey() < sectionInfo.adapterIndex) {
                            hotZoneObserverInterface.scrollOut(adaptersMap.get(entry.getKey()).getAgentInterface().getHostName(), scrollDirection);
                            adaptersMap.remove(entry.getKey());
                        }
                    }
                }
            }
        } else if (scrollDirection == ScrollDirection.DOWN) {
            Pair<Integer, Integer> temSectionInfo = mergeRecyclerAdapter.getSectionIndex(lastPostion);
            if (temSectionInfo != null) {
                MergeSectionDividerAdapter.DetailSectionPositionInfo sectionInfo
                        = mergeRecyclerAdapter.getDetailSectionPositionInfo(temSectionInfo.first, temSectionInfo.second);
                if (sectionInfo != null) {
                    HashMap<Integer, PieceAdapter> tempMap = (HashMap<Integer, PieceAdapter>) adaptersMap.clone();
                    for (Map.Entry<Integer, PieceAdapter> entry : tempMap.entrySet()) {
                        if (entry.getKey() > sectionInfo.adapterIndex) {
                            hotZoneObserverInterface.scrollOut(adaptersMap.get(entry.getKey()).getAgentInterface().getHostName(), scrollDirection);
                            adaptersMap.remove(entry.getKey());
                        }
                    }
                }
            }
        }


        for (int i = firstPosition; i <= lastPostion; i++) {
            Pair<Integer, Integer> temSectionInfo = mergeRecyclerAdapter.getSectionIndex(i);
            if (temSectionInfo == null) {
                continue;
            }
            MergeSectionDividerAdapter.DetailSectionPositionInfo sectionInfo
                    = mergeRecyclerAdapter.getDetailSectionPositionInfo(temSectionInfo.first, temSectionInfo.second);
            if (sectionInfo == null) {
                continue;
            }

            PieceAdapter adapterOwner = mergeRecyclerAdapter.getPieceAdapter(sectionInfo.adapterIndex);

            if (adapterOwner == null) {
                continue;
            }

            //不是关心的模块不看
            String agentName = adapterOwner.getAgentInterface().getHostName();
            if (!observerAgents.contains(agentName)) {
                continue;
            }

            //只判断Adapter的第一个和最后一个
            if (!(isFirstItem(sectionInfo.adapterSectionIndex, sectionInfo.adapterSectionPosition, adapterOwner)
                    || isLastItem(sectionInfo.adapterSectionIndex, sectionInfo.adapterSectionPosition, adapterOwner))) {
                continue;
            }

            if (hotZoneYRange == null) {
                continue;
            }
            View itemView = null;
            int viewIndex = hasExtraHeader ? i + 1 : i;
            for (int index = 0; index < recyclerView.getChildCount(); index++) {
                if (recyclerView.getChildAdapterPosition(recyclerView.getChildAt(index)) == viewIndex) {
                    itemView = recyclerView.getChildAt(index);
                }
            }

            if (itemView == null) continue;

            Rect itemRect = new Rect();
            itemView.getHitRect(itemRect);
            //往上第一个item的top边缘进入热区
            if (isFirstItem(sectionInfo.adapterSectionIndex, sectionInfo.adapterSectionPosition, adapterOwner)
                    && scrollDirection == ScrollDirection.UP && itemRect.top <= hotZoneYRange.endY
                    && itemRect.bottom > hotZoneYRange.startY && (!adaptersMap.containsValue(adapterOwner))) {
                adaptersMap.put(sectionInfo.adapterIndex, adapterOwner);
                hotZoneObserverInterface.scrollReach(adapterOwner.getAgentInterface().getHostName(), scrollDirection);
            } else if (isFirstItem(sectionInfo.adapterSectionIndex, sectionInfo.adapterSectionPosition, adapterOwner)
                    && scrollDirection == ScrollDirection.DOWN && itemRect.top > hotZoneYRange.endY
                    && adaptersMap.containsValue(adapterOwner)) {
                adaptersMap.remove(sectionInfo.adapterIndex);
                hotZoneObserverInterface.scrollOut(adapterOwner.getAgentInterface().getHostName(), scrollDirection);
            } else if (isLastItem(sectionInfo.adapterSectionIndex, sectionInfo.adapterSectionPosition, adapterOwner)
                    && scrollDirection == ScrollDirection.UP && itemRect.bottom <= hotZoneYRange.startY
                    && adaptersMap.containsValue(adapterOwner)) {
                adaptersMap.remove(sectionInfo.adapterIndex);
                hotZoneObserverInterface.scrollOut(adapterOwner.getAgentInterface().getHostName(), scrollDirection);
            } else if (isLastItem(sectionInfo.adapterSectionIndex, sectionInfo.adapterSectionPosition, adapterOwner)
                    && scrollDirection == ScrollDirection.DOWN && itemRect.bottom > hotZoneYRange.startY
                    && itemRect.top < hotZoneYRange.endY && (!adaptersMap.containsValue(adapterOwner))) {
                adaptersMap.put(sectionInfo.adapterIndex, adapterOwner);
                hotZoneObserverInterface.scrollReach(adapterOwner.getAgentInterface().getHostName(), scrollDirection);
            }
        }
//        Log.e("HotZone", "ScrollEnd:" + (System.currentTimeMillis() - time));
    }

    private boolean isFirstItem(int section, int row, PieceAdapter adapter) {
        if (section == 0 && row == 0) {
            return true;
        }
        return false;
    }

    private boolean isLastItem(int section, int row, PieceAdapter adapter) {
        if (section == adapter.getSectionCount() - 1
                && row == adapter.getRowCount(section) - 1) {
            return true;
        }
        return false;
    }
}
