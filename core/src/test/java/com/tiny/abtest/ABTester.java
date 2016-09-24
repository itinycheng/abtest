package com.tiny.abtest;

import com.tiny.abtest.model.ABTestTree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 16072453 on 2016/9/23.
 */
public class ABTester {

    public static void main(String args[]){
        ABTestManager manager = new ABTestManager();
        ABTestTree model = manager.getABTestModel("xxx");
        Map<ABTestTree.TreeNodeType, Object> map = new HashMap<>();
        map.put(ABTestTree.TreeNodeType.CATEGORY,60);
        map.put(ABTestTree.TreeNodeType.CITY,20);
        map.put(ABTestTree.TreeNodeType.ID,10);
        List<String> decisions = model.mkDecision(map);
        for (String dec : decisions) {
            System.out.println(dec);
        }
    }

}
