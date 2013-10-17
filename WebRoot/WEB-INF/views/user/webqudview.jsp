<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<div id="${param.id}"></div>
<iframe id='xls' name='xls' style='display:none'></iframe>
<script type="text/javascript" >
yepnope({
	load : [
	 	 "${ctx}/resources/extjs/ux/treegrid/treegrid.css",
         "${ctx}/resources/extjs/ux/treegrid/TreeGridSorter.js",
         "${ctx}/resources/extjs/ux/treegrid/TreeGridColumnResizer.js",
         "${ctx}/resources/extjs/ux/treegrid/TreeGridNodeUI.js",
         "${ctx}/resources/extjs/ux/treegrid/TreeGridLoader.js",
         "${ctx}/resources/extjs/ux/treegrid/TreeGridColumns.js",
         "${ctx}/resources/extjs/ux/treegrid/TreeGrid.js",         
         //Ext.ux.grid.RowEditor
         "${ctx}/resources/extjs/ux/css/RowEditor.css",
         "${ctx}/resources/extjs/ux/RowEditor.js"
         
		],
	complete : function() {
			
	Ext.ns("Ext.Authority.qudview"); // 自定义一个命名空间
	qudview = Ext.Authority.qudview; // 定义命名空间的别名
	qudview = {			
		allmodules : ctx + '/qudview/allmodules',// 加载所有 json 数据 Tree
		child_list : ctx + '/qudview/child_list',// 加载所有 List 数据 tree's 节点信息	
		child_save : ctx + '/qudview/child_save',// 保存信息
		child_del : ctx + '/qudview/child_del',// 删除某一节点信息		
		report :	ctx + '/qudview/Report_maic_count',   //报表打印
		isdisplay : eval('(${fields.isdisplay==null?"{}":fields.isdisplay})'),
		role : eval('(${fields.role==null?"{}":fields.role})'),
		user_role : "${sessionScope.CURRENT_USER_ROLE}",
		pagesizes:eval('(${fields.pagesizes==null?"{}":fields.pagesizes})'),
		pageSize : 20 // 每页显示的记录数
		
	};
	

	qudview.modulename = new Ext.form.TextField({  
         width:150,  
         emptyText:'快速检索',  
         enableKeyEvents: true,  
         listeners:{  
            
             specialkey: function(node, e){
                    // e.HOME, e.END, e.PAGE_UP, e.PAGE_DOWN,
                    // e.TAB, e.ESC, arrow keys: e.LEFT, e.RIGHT, e.UP, e.DOWN
                    if (e.getKey() == e.ENTER) {
                       findByKeyWordFiler(node, e); 
                    }
              },  
             scope: this  
         }  
     });	
	
            
	qudview.refresh = new Ext.Action({
			text : '检索',
			iconCls : 'circle',
			handler : function() {
				
				/*qudview.treePanel.getLoader().baseParams.modulename=qudview.modulename.getValue();
				qudview.treePanel.root.reload();*/				
			//	qudview.treePanel.expandAll();
				
				
								
				/*var node=qudview.treePanel.getRootNode();
		        qudview.removenoChildNodes(node);*/  //开始递归
					
			}
		});
	
	qudview.removenoChildNodes= function (node){
	
         var length_child=node.childNodes.length;       
         for(var i=0;i<length_child;){  //从节点中取出子节点依次遍历
            //节点被删除时,重新计算  length_child , 未被删除时 i++;            
             var rootnode = node.childNodes[i];
                          
             if(rootnode.childNodes.length>0){               
               qudview.removenoChildNodes(rootnode);
               i++;
             }
             else if(!rootnode.isLeaf()){
             	
             	rootnode.parentNode.removeChild(rootnode,true);             	
             		 
             	length_child = node.childNodes.length;
             }
             else
              	i++;  
         
         }         
     };	
		
		/** 父节点的选择 */
	qudview.checkParentFun = function(treeNode) {
		var i;
		var check = false;
		var nocheck = false;
		if (treeNode.hasChildNodes()) {
			for (i = 0; i < treeNode.childNodes.length; i++) {
				if (treeNode.childNodes[i].getUI().checkbox.checked) {
					check = true;
				} else {
					nocheck = true;
				}
			}
		}
		if (check == true && nocheck == false) {// 可以全选
			treeNode.getUI().checkbox.checked = true;
		} else if (check == true && nocheck == true) {// 半选
			treeNode.getUI().checkbox.checked = true;
			// treeNode.getUI().iconNode.src =
			// '${ctx}/resources/extjs/ux/images/part.gif'
		} else if (check == false && nocheck == true) {// 全不选
			treeNode.getUI().checkbox.checked = false;
		}
	};
	/** 先清空已选择的状态 */
	qudview.clearTreeNodeCheckFun = function(treeNode, checked) {
		var i;
		if (treeNode.hasChildNodes()) {
			for (i = 0; i < treeNode.childNodes.length; i++) {
				if (treeNode.childNodes[i].getUI().checkbox) {
					treeNode.childNodes[i].getUI().checkbox.checked = checked;
				}
			}
			for (i = 0; i < treeNode.childNodes.length; i++) {
				qudview.clearTreeNodeCheckFun(treeNode.childNodes[i], checked);
			}
		}
	};
	qudview.visitAllTreeNodeFun = function(treeNode) {
		var i;
		if (treeNode.hasChildNodes()) {
			for (i = 0; i < treeNode.childNodes.length; i++) {
				if (treeNode.childNodes[i].getUI().checkbox) {
					if (treeNode.childNodes[i].getUI().checkbox.checked) {
						// 去除前缀
						qudview.childNodes += treeNode.childNodes[i].id.replace("_common_", "") + ',';
					}
				}
			}
			for (i = 0; i < treeNode.childNodes.length; i++) {
				qudview.visitAllTreeNodeFun(treeNode.childNodes[i]);
			}
		}
	};
	
	
	qudview.treePanel = new Ext.tree.TreePanel({
			title : '渠道信息',
			region : 'west',
			split : true,
			minSize : 200,
			maxSize : 900,
			useArrows : true,
			autoScroll : true,
			width : '30%',
			tbar : [qudview.modulename],
			animate : true,
			enableDD : true,
			containerScroll : true,
			rootVisible : false,
			buttonAlign : 'left',
			frame : false,
			disabled : false,
			root : {
				nodeType : 'async'
			},
	//		dataUrl : qudview.allmodules,
			loader: new Ext.tree.TreeLoader({
                      dataUrl:qudview.allmodules,
                      baseParams: {}
                  }),
			listeners : {
				'checkchange' : function(node, checked) {
					// 保存按钮生效
			//		qudview.saveMudulesAction.enable();

					
					//子节点选择
					if (node.hasChildNodes()) {
                            node.eachChild(function(child) {
                                child.ui.toggleCheck(checked);
                                child.attributes.checked = checked;
                                child.fireEvent('checkchange', child, checked); //递归调用
                            });
                    }
					/*if (node.childNodes.length > 0) {
						for (var i = 0; i < node.childNodes.length; i++) {
							if (node.childNodes[i].getUI().checkbox) {
								node.childNodes[i].getUI().checkbox.checked = checked;
							}
						}
					}*/  //只影响直接子节点状态
					
					//父节点选择
					if (node.parentNode && node.parentNode.getUI().checkbox != null) {
						qudview.checkParentFun(node.parentNode);
					}
					
				//	alert(node.id);  _common_00
					
				}
			}
		});
	
	/**  ------------快速搜索-----------begin */
	var timeOutId = null;  
  
    var treeFilter = new Ext.tree.TreeFilter(qudview.treePanel, {  
        clearBlank : true,  
        autoClear : true  
    });  

    // 保存上次隐藏的空节点  
    var hiddenPkgs = [];  
    var findByKeyWordFiler = function(node, event) {  
        clearTimeout(timeOutId);// 清除timeOutId  
        qudview.treePanel.expandAll();// 展开树节点  
        // 为了避免重复的访问后台，给服务器造成的压力，采用timeOutId进行控制，如果采用treeFilter也可以造成重复的keyup  
        timeOutId = setTimeout(function() {  
            // 获取输入框的值  
            var text = node.getValue();  
            // 根据输入制作一个正则表达式，'i'代表不区分大小写  
            var re = new RegExp(Ext.escapeRe(text), 'i');  
            // 先要显示上次隐藏掉的节点  
            Ext.each(hiddenPkgs, function(n) {  
                n.ui.show();  
            });  
            hiddenPkgs = [];  
            if (text != "") {
            	
                treeFilter.filterBy(function(n) {  
                    // 只过滤叶子节点，这样省去枝干被过滤的时候，底下的叶子都无法显示  
                    return !n.isLeaf() || re.test(n.text);  
                });  
                // 如果这个节点不是叶子，而且下面没有子节点，就应该隐藏掉  
                qudview.treePanel.root.cascade(function(n) {  
                    if(n.id!='0'){  
                        if(!n.isLeaf() &&judge(n,re)==false&& !re.test(n.text)){  
                            hiddenPkgs.push(n);  
                            n.ui.hide();  
                        }  
                    }  
                });  
            } else {  
            //    treeFilter.clear();  
            	treeFilter.filterBy(function(n) {  
                    // 只过滤叶子节点，这样省去枝干被过滤的时候，底下的叶子都无法显示  
                    return !n.isLeaf() || re.test(n.text);  
                });
                qudview.treePanel.collapseAll();
                return;  
            }  
        }, 500);  
    };
    // 过滤不匹配的非叶子节点或者是叶子节点  
    var judge =function(n,re){  
        var str=false;  
        n.cascade(function(n1){  
            if(n1.isLeaf()){  
                if(re.test(n1.text)){ str=true;return; }  
            } else {  
                if(re.test(n1.text)){ str=true;return; }  
            }  
        });  
        return str;  
    };
	
	/**  ------------快速搜索-----------end */
	
	/**  ------------右击菜单-----------begin*/

	qudview.tree_info = new Ext.Action({
			text : '节点信息',
			iconCls : 'info',
			disabled : false,
			handler : function() {
				var node=qudview.treePanel.getSelectionModel().getSelectedNode();
				var id=node.id;
				var text=node.text;				
				var child_num=node.childNodes.length;
				if(node.isLeaf()){
					Ext.Msg.show({
					   title:'节点信息--叶子节点',
					   msg: 'ID:'+id+' NAME:'+text+' CHILD_NUM:'+child_num,
					   buttons: Ext.Msg.YESNOCANCEL,
					   animEl: 'elId',
					   icon: Ext.MessageBox.QUESTION
					});
				}
				else{
					Ext.Msg.show({
					   title:'节点信息',
					   msg: 'ID:'+id+' NAME:'+text+' CHILD_NUM:'+child_num,
					   buttons: Ext.Msg.YESNOCANCEL,
					   animEl: 'elId',
					   icon: Ext.MessageBox.QUESTION
					});
				}
			}
		});	
	qudview.tree_add = new Ext.Action({
			text : '增加下级',
			iconCls : 'add',
			disabled : false,
			handler : function() {
				var node=qudview.treePanel.getSelectionModel().getSelectedNode();
				var id=node.id;				
			}
		});	
    qudview.rightmenu=new Ext.menu.Menu({
	items:[  
		qudview.tree_info,
		qudview.tree_add
    	]
    }); 
	
	qudview.treePanel.on("contextmenu",function(node,e)
        {
            node.select();
            e.preventDefault();      
                   
            qudview.rightmenu.showAt(e.getPoint());
        });
   
    qudview.treePanel.on('click', function(node){  
        // 单击事件						
		qudview.store.baseParams.sssjdm = node.id.replace("_common_", "");		
		qudview.store.load(); 
				
		Ext.getCmp("tipmsg").setText("节点:<font color=red>"+node.attributes.text+"</font> "+
									 "URL:<font color=red>"+node.attributes.url+"</font>"+
									 "子节点已知数:<font color=red>"+node.childNodes.length+"</font>");
		Ext.getCmp("tree_id").setValue(node.id.replace("_common_", ""));
		
		/** 增加列信息  */
		var ColumnCount=qudview.grid.getColumnModel().getColumnCount();
		var config = qudview.grid.getColumnModel().config;
		column = {header: 'TB', dataIndex: 'TB'};
		var c_num=0;
		for(var i=0;i<ColumnCount;i++){
			if(qudview.grid.getColumnModel().getDataIndex(i)=='TB')
				c_num=1;			
		}
		
		if(c_num==0){
			config.push(column);		
			qudview.grid.getColumnModel().setConfig(config);		
		}
		
    });   
        
	
	/**  ------------右击菜单-----------end  */
	

	qudview.alwaysFun = function() {
		Share.resetGrid(qudview.grid);
	};

	/**  节点详细信息  以及节点直接下属信息  begin*/
	
	/** 改变页的combo */
	qudview.pageSizeCombo = new Share.pageSizeCombo({
				value : '20',
				listeners : {
					select : function(comboBox) {
						qudview.pageSize = parseInt(comboBox.getValue());
						qudview.bbar.pageSize = parseInt(comboBox.getValue());
						qudview.store.baseParams.limit = qudview.pageSize;
						qudview.store.baseParams.start = 0;
						qudview.store.load();
					}
				}
			});
	// 覆盖已经设置的。具体设置以当前页面的pageSizeCombo为准
	qudview.pageSize = parseInt(qudview.pageSizeCombo.getValue());
	/** 基本信息-数据源 */
	qudview.store = new Ext.data.Store({
				autoLoad : false,
				remoteSort : true,
				baseParams : {
					start : 0,
					limit : qudview.pageSize
				},
				proxy : new Ext.data.HttpProxy({// 获取数据的方式
					method : 'POST',
					url : qudview.child_list
				}),
				reader : new Ext.data.JsonReader({// 数据读取器
					totalProperty : 'results', // 记录总数
					root : 'rows' // Json中的列表数据根节点
				}, ['ID', 'DM', 'MC', 'JC','TB','SSSJDM']),
				listeners : {
					'load' : function(store, records, options) {
					
						qudview.alwaysFun();
					},
					'update':function(store, records, options){
						
						Share.AjaxRequest({
							url : qudview.child_save,
							showMsg:false,
   							showWaiting:false,
							params : {
								id:records.get('ID'),
								dm:records.get('DM'),
								mc:records.get('MC'),
								jc:records.get('JC'),
								tb:records.get('TB'),
								sssjdm:records.get('SSSJDM')
							},
							callback : function(json) {								
								qudview.store.reload();
							}
						});
						
						
						
					}
				}
			});
	/** 基本信息-选择模式 */
	qudview.selModel = new Ext.grid.CheckboxSelectionModel({
			singleSelect : true
			
		});
		
	/** 基本信息-数据列 */
	qudview.colModel = new Ext.grid.ColumnModel({
			defaults : {
				sortable : true,
				width : 140
			},
			columns : [new Ext.grid.RowNumberer(),
					{
			//			hidden : true,
						header : 'ID',
						dataIndex : 'ID',
						width : 200
					}, {
						header : '代码',
						dataIndex : 'DM',
						width : 80,
						editor:Ext.form.TextField
					}, {
						header : '名称',
						dataIndex : 'MC',
						editor:Ext.form.TextField
					}, {
						header : '简称',
						dataIndex : 'JC',
						editor:Ext.form.TextField
					}, {
						header : '来源表',
						dataIndex : 'TB',
						width : 100
					}, {
						header : '所属上级代码',
						dataIndex : 'SSSJDM',
						width : 100,
						editor:Ext.form.TextField
					}]
		});
	/** 提示信息*/
	qudview.tipmsg={
		xtype : 'tbtext',
		text : '',
		align : 'right',
		id :'tipmsg'
		
	};
	qudview.tree_id={
		xtype : 'textfield',
		text : '',
		id : 'tree_id',
		readOnly :true
	};	
	qudview.printAction = new Ext.Action({
		text : '报表显示',
		iconCls : 'chart_pie',
		disabled : false,
		handler : function() {
			qudview.printFun();
		}
	});	
	qudview.printFun =function(){
		var records = qudview.grid.getSelectionModel().getSelections();
		if(records.length <= 0){
			Ext.Msg.alert('操作提示','请选择其中一条记录！',function(){
				return;
			});
		}
		else {
			var dm=records[0].data.DM;
			
			if(dm=="")
				dm="-1";
				
			window.open(qudview.report+"?param="+dm+"&time="+(new Date()).toString());
			
		}
		
		
	};
			
	/** 顶部工具栏*/
	qudview.tbar = ['TREE_ID:',qudview.tree_id, '-', qudview.printAction,'-',qudview.tipmsg];
	
	
	/** 底部工具条 */
	qudview.bbar = new Ext.PagingToolbar({
			pageSize : qudview.pageSize,
			store : qudview.store,
			displayInfo : true,
			// plugins : new Ext.ux.ProgressBarPager(), // 分页进度条
			items : ['-', '&nbsp;', qudview.pageSizeCombo]
		});
		
	/** 基本信息-表格 */
	qudview.editor = new Ext.ux.grid.RowEditor({
        saveText: 'Update'
    });
    
	qudview.grid = new Ext.grid.GridPanel({
			store : qudview.store,
			colModel : qudview.colModel,
			selModel : qudview.selModel,
			tbar : qudview.tbar,
			bbar : qudview.bbar,
			autoScroll : 'auto',
			region : 'center',
			loadMask : true,
			// autoExpandColumn :'fieldDesc',
			plugins: [qudview.editor],
			stripeRows : true,
			listeners : {},
			viewConfig : {
				forceFit : true
			}
		});
	
	/**  节点详细信息  以及节点直接下属信息 end*/
	qudview.myPanel = new Ext.Panel({
		id : '${param.id}' + '_panel',
		renderTo : '${param.id}',
		layout : 'border',
		boder : false,
		height : index.tabPanel.getInnerHeight() - 1,
		items : [qudview.treePanel,qudview.grid]
		});
	
	
	
	}
	
});



	   				
</script>
