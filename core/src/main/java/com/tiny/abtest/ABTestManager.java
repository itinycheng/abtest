package com.tiny.abtest;

import com.tiny.abtest.model.ABTestConfig;
import com.tiny.abtest.model.ABTestTree;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by 16072453 on 2016/9/19.
 */
public final class ABTestManager {

    private static final Map<String, ABTestTree> abTestTreePool = new HashMap<>();

    private static final ABTestTree AB_TEST_TREE_NULL = new ABTestTree();

    /**
     * get AB test tree
     * generate if not exist
     * @param treeCode
     * @return
     */
    public ABTestTree getABTestModel(String treeCode){
        if(StringUtils.isEmpty(treeCode)){
            return null;
        }
        ABTestTree treeModel = abTestTreePool.get(treeCode);
        if (null == treeModel){
            synchronized (ABTestManager.class){
                treeModel = abTestTreePool.get(treeCode);
                if (null == treeModel){
                    ABTestConfig conf = queryABTestConfigFromDB(treeCode);
                    treeModel = new ABTestTree.Builder()
                            .setId(conf.getId())
                            .setName(conf.getName())
                            .setCode(conf.getCode())
                            .setConfig(conf.getConfig())
                            .build();
                    abTestTreePool.put(treeCode, null == treeModel ? AB_TEST_TREE_NULL : treeModel);
                }
            }
        }
        if (AB_TEST_TREE_NULL.equals(treeModel))
            return null;
        return treeModel;
    }

    /**
     * TODO
     * @param modelCode
     */
    public void refreshABTestModel(String modelCode){

    }

    /**
     * TODO
     * query json data from db by tree's code
     * @param modelCode
     * @return
     */
    private static ABTestConfig queryABTestConfigFromDB(String modelCode){
        ABTestConfig conf = new ABTestConfig();
        String confStr = "{"
                + "node : {type:\"CATEGORY\",name:\"类目\"},"
                + "edges : {"
                + "\"x>50\" : {node : {type:\"ID\",name:\"ID\"},edges : {\"x>10\" : \"value1\",\"x<10\" : \"value2\",\"other\" : \"value3\"}}, "
                + "\"x<50\" : {node : {type:\"CITY\",name:\"城市\"},edges : {\"x>10\":\"value4\",\"x<10\":\"value5\"}},"
                + "\"other\" : {node : {type:\"CITY\",name:\"城市\"},edges : {\"x>10\":\"value6\",\"x<10\":\"value7\"}}"
                + "}}";
        conf.setId(1);
        conf.setName("首页AB测试");
        conf.setCode("xxxx-wwww");
        conf.setConfig(confStr);
        return conf;
    }
}
