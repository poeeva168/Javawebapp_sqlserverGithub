package com.authority.pojo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 菜单类1.1
 * 
 * @author ...
 * @date 2011-10-25 下午9:53:59
 */
public class TreeCommonMenu {

	private List<CommonModules> list;
	private CommonModules root;

	public TreeCommonMenu(List<CommonModules> list) {
		this.list = list;
		this.root = new CommonModules();
	}

	/**
	 * 组合json
	 * 
	 * @param list
	 * @param node
	 */
	private Tree getNodeJson(List<CommonModules> list, CommonModules node) {
		Tree tree = new Tree();
		tree.setId("_common_" + node.getModuleId());
		tree.setText(node.getModuleName());
		tree.setIconCls(node.getIconCss());
		tree.setChildren(new ArrayList<Tree>());
		if (list == null) {
			// 防止没有权限菜单时
			return tree;
		}
		if (hasChild(list, node)) {
			List<Tree> lt = new ArrayList<Tree>();
			tree.setUrl(node.getModuleUrl());
			tree.setLeaf(node.getLeaf().equals("1") ? true : false);
			tree.setExpanded(node.getExpanded().equals("1") ? true : false);
			List<CommonModules> childList = getChildList(list, node);
			Iterator<CommonModules> it = childList.iterator();
			while (it.hasNext()) {
				CommonModules modules = (CommonModules) it.next();
				// 递归
				lt.add(getNodeJson(list, modules));
			}
			tree.setChildren(lt);
			// } else if ((node.getParentId() == root.getModuleId()) ||
			// node.getModuleUrl() == null) {
			// // 防止是主菜单,或者主菜单里面的url为空，但是下面没有子菜单的时候
			// tree.setUrl("");
			// tree.setLeaf(node.getLeaf() == 1 ? true : false);
			// tree.setExpanded(node.getExpanded() == 1 ? true : false);
		} else {
			tree.setUrl(node.getModuleUrl());
			tree.setLeaf(node.getLeaf().equals("1") ? true : false);
			tree.setExpanded(node.getExpanded().equals("1") ? true : false);
		}

		return tree;
	}

	/**
	 * 判断是否有子节点
	 */
	private boolean hasChild(List<CommonModules> list, CommonModules node) {
		return getChildList(list, node).size() > 0 ? true : false;
	}

	/**
	 * 得到子节点列表
	 */
	private List<CommonModules> getChildList(List<CommonModules> list, CommonModules modules) {
		List<CommonModules> li = new ArrayList<CommonModules>();
		Iterator<CommonModules> it = list.iterator();
		while (it.hasNext()) {
			CommonModules temp = (CommonModules) it.next();
			if (temp.getParentId().equals(modules.getModuleId())) {
				li.add(temp);
			}
		}
		return li;
	}

	public Tree getTreeJson() {
		// 父菜单的id为0
		this.root.setModuleId("0");
		this.root.setLeaf("0");
		this.root.setExpanded("0");
		return this.getNodeJson(this.list, this.root);
	}
}
