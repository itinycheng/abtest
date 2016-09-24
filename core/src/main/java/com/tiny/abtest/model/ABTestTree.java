package com.tiny.abtest.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tiny.abtest.utils.ScriptEngineUtils;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.util.*;

/**
 * abTest Tree model
 * basic class for AB Test
 */
public final class ABTestTree {

    private TreeNode treeNode;

    private Map<TreeEdge, Object> children;

    // only existed in root tree
    private List<TreeNodeType> nodeTypeList;

    public TreeNode getTreeNode() {
        return treeNode;
    }

    public void setTreeNode(TreeNode treeNode) {
        this.treeNode = treeNode;
    }

    public Map<TreeEdge, Object> getChildren() {
        return children;
    }

    public void setChildren(Map<TreeEdge, Object> children) {
        this.children = children;
    }

    public List<TreeNodeType> getNodeTypeList() {
        return nodeTypeList;
    }

    public void setNodeTypeList(List<TreeNodeType> nodeTypeList) {
        this.nodeTypeList = nodeTypeList;
    }

    public void setChild(TreeEdge edge, Object object) {
        if (null == this.children) {
            this.children = new HashMap<>();
        }
        this.children.put(edge, object);
    }

    public List<String> mkDecision(Map<TreeNodeType,Object> dimensions) throws IllegalArgumentException {
        for (TreeNodeType nodeType : this.nodeTypeList) {
            if (null == dimensions.get(nodeType))
                throw new IllegalArgumentException("TreeNodeType[" + nodeType + "]'s value is null.");
        }
        return doDecision(dimensions, this);
    }

    private List<String> doDecision(Map<TreeNodeType,Object> dimensions, ABTestTree abTestTree) {
        List<String> leaves = new ArrayList<String>();
        TreeNode treeNode = abTestTree.getTreeNode();
        Map<TreeEdge, Object> children = abTestTree.getChildren();
        TreeNodeType nodeType = treeNode.getType();
        if (null == nodeType) {
            throw new IllegalStateException("TreeNodeType is null");
        }
        Object nodeVal = dimensions.get(nodeType);
        if(null == nodeVal){
            throw new IllegalStateException("dimensions[" + nodeType + "]'s value is null");
        }
        // whether enter "other" edge
        boolean isOther = true;
        for (Map.Entry<TreeEdge, Object> child : children.entrySet()) {
            TreeEdge treeEdge = child.getKey();
            Object subObj = child.getValue();
            if(TreeEdgeType.JSEXPRESSION.equals(treeEdge.getType())){
                Bindings bindings = new SimpleBindings();
                bindings.put("x", nodeVal);
                try {
                    Object isTrue = ((CompiledScript) treeEdge.getValue()).eval(bindings);
                    if(!((boolean) isTrue)){
                        continue;
                    }
                    // not enter other edge
                    isOther = false;
                    if(subObj instanceof ABTestTree){
                        leaves.addAll(doDecision(dimensions, (ABTestTree) subObj));
                    } else {
                        leaves.add((String) subObj);
                    }
                } catch (ScriptException e) {
                    e.printStackTrace();
                }
            }else if(isOther && TreeEdgeType.OTHER.equals(treeEdge.getType())){
                if(subObj instanceof ABTestTree){
                    leaves.addAll(doDecision(dimensions, (ABTestTree) subObj));
                } else {
                    leaves.add((String) subObj);
                }
            }
        }
        return leaves;
    }

    public static class TreeEdge {

        private TreeEdgeType type;

        private Object value;

        public TreeEdgeType getType() {
            return type;
        }

        public void setType(TreeEdgeType type) {
            this.type = type;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }
    }

    public static class TreeNode {

        private TreeNodeType type;

        private String name;

        public TreeNodeType getType() {
            return type;
        }

        public void setType(TreeNodeType type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public enum TreeNodeType {
        ID, UUID, PERSON, CITY, CATEGORY;

        public static TreeNodeType getEnumByName(String name) {
            TreeNodeType[] values = TreeNodeType.values();
            for (TreeNodeType value : values) {
                if (value.name().equalsIgnoreCase(name))
                    return value;
            }
            return null;
        }
    }

    public enum TreeEdgeType {
        OTHER, JSEXPRESSION
    }

    public static class Builder {

        private long id;

        private String code;

        private String name;

        private String config;

        private Set<TreeNodeType> nodeTypes;

        public Builder setConfig(String config) {
            this.config = config;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setId(long id) {
            this.id = id;
            return this;
        }

        public Builder setCode(String code) {
            this.code = code;
            return this;
        }

        public ABTestTree build() {
            JSONObject jsonObject = JSON.parseObject(config);
            ABTestTree tree = this.generateTree(jsonObject);
            tree.setNodeTypeList(new ArrayList<>(this.nodeTypes));
            return tree;
        }

        /**
         * generate abtest tree model
         *
         * @param jsonTree
         * @return
         */
        private ABTestTree generateTree(Object jsonTree) {
            if (jsonTree instanceof JSONObject) {
                JSONObject jsonObject = (JSONObject) jsonTree;
                JSONObject node = jsonObject.getJSONObject("node");
                JSONObject edges = jsonObject.getJSONObject("edges");
                ABTestTree tree = new ABTestTree();
                TreeNode treeNode = this.generateTreeNode(node);
                tree.setTreeNode(treeNode);
                if (null == nodeTypes)
                    nodeTypes = new HashSet<>();
                nodeTypes.add(treeNode.getType());
                for (String edge : edges.keySet()) {
                    Object child = edges.get(edge);
                    TreeEdge treeEdge = this.generateTreeEdge(edge);
                    if (child instanceof String) {
                        tree.setChild(treeEdge, child);
                    } else {
                        tree.setChild(treeEdge, this.generateTree(child));
                    }
                }
                return tree;
            }
            return null;
        }

        /**
         * generate tree node
         *
         * @param node
         * @return
         */
        private TreeNode generateTreeNode(JSONObject node) {
            if (null != node) {
                String nodeName = node.getString("name");
                String typeStr = node.getString("type");
                TreeNodeType nodeType = TreeNodeType.getEnumByName(typeStr);
                if (null == nodeType) {
                    throw new IllegalStateException("illegal type[" + typeStr + "]");
                }
                TreeNode treeNode = new TreeNode();
                treeNode.setName(nodeName);
                treeNode.setType(nodeType);
                return treeNode;
            } else {
                throw new IllegalStateException("node is null");
            }
        }

        /**
         * generate tree edge
         *
         * @param edge
         * @return
         */
        private TreeEdge generateTreeEdge(String edge) {
            Object edgeValue;
            TreeEdgeType edgeType;
            if ("other".equalsIgnoreCase(edge)) {
                edgeValue = edge;
                edgeType = TreeEdgeType.OTHER;
            } else {
                //TODO more script support
                try {
                    edgeType = TreeEdgeType.JSEXPRESSION;
                    edgeValue = ScriptEngineUtils.compileJSScript(edge);
                } catch (Exception e) {
                    throw new IllegalStateException("illegal js expr[" + edge + "]");
                }
            }
            TreeEdge treeEdge = new TreeEdge();
            treeEdge.setType(edgeType);
            treeEdge.setValue(edgeValue);
            return treeEdge;
        }
    }
}
