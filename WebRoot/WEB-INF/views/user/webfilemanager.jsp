<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<div id="${param.id}"></div>
<iframe id='xls' name='xls' style='display:none'></iframe>
<script type="text/javascript" >
yepnope({
	load : [	 	 
         //Ext.ux.grid.RowEditor
         "${ctx}/resources/extjs/ux/css/RowEditor.css",
         "${ctx}/resources/extjs/ux/RowEditor.js",
         //swfupload
         "${ctx}/resources/swfupload/css/icons.css",
		 "${ctx}/resources/swfupload/swfupload.js",
		 "${ctx}/resources/swfupload/uploaderPanel.js"
		],
	complete : function() {
			
	Ext.ns("Ext.Authority.filemanager"); // 自定义一个命名空间
	filemanager = Ext.Authority.filemanager; // 定义命名空间的别名
	filemanager = {
		getdirectories : ctx + '/filemanager/getdirectories',// 加载所有 文件目录信息
		getfiles : ctx + '/filemanager/getfiles',// 获取所有文件下文件夹及文件信息
		deleteFiles_url : ctx + '/filemanager/deleteFiles', //删除指定文件或者整个文件夹 
		uploadFiles_url : ctx + '/filemanager/uploadFiles',// 文件上传
		flashUrl : ctx + '/resources/swfupload/swfupload.swf', //文件上传flash地址
		downloadFiles_url : ctx + '/filemanager/downloadFiles',// 文件下载
		downloadFiles_zip_url : ctx + '/filemanager/downloadFiles_Zip',// 文件打包下载
		createFolder_url : ctx + '/filemanager/createFolder',// 新建文件夹
		getSpaceInfo_url : ctx + '/filemanager/getSpaceInfo',//磁盘可用空间大小
		paste_url	: ctx + '/filemanager/paste',//粘帖操作
		rename_url : ctx + '/filemanager/rename',//文件重命名
		server_url : ctx + '/filemanager/server',//文件提交到服务器
		
		bosdatadownload_url : ctx + '/filemanager/bosdatadownload',//文件提交到服务器
		
		isdisplay : eval('(${fields.isdisplay==null?"{}":fields.isdisplay})'),
		role : eval('(${fields.role==null?"{}":fields.role})'),
		user_role : "${sessionScope.CURRENT_USER_ROLE}",
		pagesizes:eval('(${fields.pagesizes==null?"{}":fields.pagesizes})'),
		pageSize : 20 // 每页显示的记录数
		
	};
	
	filemanager.treePanel = new Ext.tree.TreePanel({
			title : '目录信息',
			region : 'west',
			split : true,
			minSize : 200,
			maxSize : 900,
			useArrows : true,
			autoScroll : true,
			width : '25%',
	//		tbar : [filemanager.modulename],
			animate : true,
			enableDD : true,
			containerScroll : true,
			rootVisible : false,
			buttonAlign : 'left',
			frame : false,
			disabled : false,
			collapsible: true,
			root : {
				nodeType : 'async'
			},
			tools: [{
		        id: 'refresh',
		        handler: function () {
		            filemanager.treePanel.root.reload();
		            filemanager.spaceinfo();
		        }
		    }],
			loader: new Ext.tree.TreeLoader({
                  dataUrl:filemanager.getdirectories,
                  baseParams: {}
              })
		});	
	filemanager.spaceinfo=function(){
		Share.AjaxRequest({
			url : filemanager.getSpaceInfo_url,
			showMsg:false,
			showWaiting:false,							
			callback : function(json) {				
				filemanager.treePanel.setTitle("目录信息  <font color=red>"+json.msg+"</font>");
			}
		});
	};
	filemanager.spaceinfo();
	filemanager.treePanel.loader.on('beforeload',function(){
		
	});
    filemanager.treePanel.on('click', function(node){  
        // 单击事件						
		filemanager.store.baseParams.node = node.id.replace("_common_", "");		
		filemanager.store.load();
		
    });   
        
	/**  节点详细信息  以及节点直接下属信息  begin*/
	
	
	/** -------function 函数区--------------begin */
	
	filemanager.alwaysFun = function() {
		Share.resetGrid(filemanager.grid);
	};
	
	filemanager.formatIcon = function(_v, cellmeta, record){
		if(!record.data.leaf){
			return '<div class="folder" style="height: 16px;background-repeat: no-repeat;"/>' +
					'&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;文件夹</div>';
		}
		var extensionName = '';
		var returnValue = '';
		var index = _v.lastIndexOf('.');
		if(index == -1){
			return '<div class="db-ft-unknown-small" style="height: 16px;background-repeat: no-repeat;"/>' +
					'&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;' + extensionName.toUpperCase() + '</div>';
		}else{
			extensionName = _v.substring(index + 1);
			extensionName = extensionName == "html" ? "htm" : extensionName;
			extensionName = extensionName == "php" ? "htm" : extensionName;
			extensionName = extensionName == "jsp" ? "htm" : extensionName;
			var css = '.db-ft-' + extensionName.toLowerCase() + '-small';
			if(Ext.isEmpty(Ext.util.CSS.getRule(css),true)){
				returnValue = '<div class="db-ft-unknown-small" style="height: 16px;background-repeat: no-repeat;">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;' 
								+ extensionName.toUpperCase() + '</div>';
			}else{
				returnValue = '<div class="db-ft-' + extensionName.toLowerCase() + '-small" style="height: 16px;background-repeat: no-repeat;"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;' 
								+ extensionName.toUpperCase();	+ '</div>';
			}
			return returnValue;	
		}
	};
	
	filemanager.download = function(){
		var records = filemanager.grid.getSelectionModel().getSelections();
		if(records.length <= 0){
			Ext.Msg.alert('操作提示','至少选择一个文件或者文件夹');
		}else{
			if(records.length == 1 && records[0].data.leaf){
				//window.location = encodeURI('download.action?path=' + this.currentPath.substring(1) + '&name=' + records[0].data.fileName);
				window.open(filemanager.downloadFiles_url+"?path="+encodeURI(records[0].data.id),target='xls');
				//window.location = encodeURI(filemanager.downloadFiles_url+"?path="+ records[0].data.fileName);
				
			}else{				
				Ext.Msg.confirm('操作提示','是否打包下载?',function(){
					var paths = [];
					for(var i = 0; i < records.length; i++){
						paths.push(encodeURI(records[i].data.id));
					}
					window.open(filemanager.downloadFiles_zip_url+"?paths="+paths,target='xls');
	
				},this);
			}
		}
	};
	
	filemanager.cut_or_copy =function(cut_or_copy){
		var records = filemanager.grid.getSelectionModel().getSelections();
		if(records.length <= 0){
			Ext.Msg.alert('操作提示','至少选择一个文件或者文件夹');
		}
		else {
			Ext.getCmp("cut_copy_path").setValue(records[0].data.id);
			Ext.getCmp("cut_copy_method").setValue(cut_or_copy);			
		}	
	};
	
	filemanager.paste = function(){
		var cut_copy_path=Ext.getCmp("cut_copy_path").getValue();
		var cut_copy_method=Ext.getCmp("cut_copy_method").getValue();
		var cover_ =Ext.getCmp("cover").getValue();
		Share.AjaxRequest({
				url : filemanager.paste_url ,
				params : {
					node : filemanager.store.baseParams['node'],
					source : cut_copy_path,
					method : cut_copy_method,
					cover  : cover_
				},
				showMsg:false,
		   		showWaiting:true,
				callback : function(json) {
					Ext.getCmp("cut_copy_method").setValue("");
					Ext.Msg.show({msg:json.msg,buttons : Ext.Msg.OK});
					filemanager.refresh();
				}
		});
	};
	
	filemanager.rename = function (){
		var records = filemanager.grid.getSelectionModel().getSelections();
		if(records.length <= 0){
			Ext.Msg.alert('操作提示','至少选择一个文件或者文件夹');
		}
		else {
			Ext.Msg.prompt('文件重命名',"输入文件名<font color=red>带后缀</font>",function(btn, text){
			    if (btn == 'ok'&&text!=""&&text!=records[0].data.fileName){
			    	Share.AjaxRequest({
						url : filemanager.rename_url ,
						params : {
							filename : records[0].data.id,
							newname  : text
						},
						showMsg:false,
				   		showWaiting:true,
						callback : function(json) {
							filemanager.refresh();
						}
					});
			    }
			},this,false,records[0].data.fileName);	
		}
	};
	
	filemanager.compressionFiles = function(){
		var records = this.getSelectionModel().getSelections();
		if(records.length <= 0){
			Ext.Msg.alert('操作提示','至少选择一个文件！');
		}else{
			var paths = [];
			for(var i = 0; i < records.length; i++){
				paths.push(records[i].data.id);	
			}
			var mask = new Ext.LoadMask(this.el, {msg:"压缩中,请稍等..."});
			mask.show();
			Ext.Ajax.request({
				timeout : 60000,
				url : 'compressionFiles.action',
				callback : function(options,success,response){
					var result = Ext.util.JSON.decode(response.responseText);
					if(!result.success){
						Ext.Msg.show({
							title : '错误提示',
							msg : '文件压缩错误!',
							buttons : Ext.Msg.OK,
							icon : Ext.Msg.ERROR
						});		
					}else{
						this.refresh();
					}
					mask.hide();
				},
				params : {
					paths : paths,
					path : this.currentPath
				},
				scope : this
			});
		}
	};
	
	filemanager.decompressionFiles = function(){
		var records = this.getSelectionModel().getSelections();
		if(records.length <= 0){
			Ext.Msg.alert('操作提示','至少选择一个文件!',function(){
				return;
			});	
		}else{
			var paths = [];
			for(var i = 0; i< records.length; i++){
				paths.push(records[i].data.id);	
			}
			var mask = new Ext.LoadMask(this.el, {msg:"解压缩中,请稍等..."});
			mask.show();
			Ext.Ajax.request({
				url : 'decompressionFiles.action',
				callback : function(options,success,response){
					var result = Ext.util.JSON.decode(response.responseText);
					if(!result.success){
						Ext.Msg.show({
							title : '错误提示',
							msg : '文件解压缩错误!',
							buttons : Ext.Msg.OK,
							icon : Ext.Msg.ERROR
						});		
					}else{
						this.refresh();
					}
					mask.hide();
				},
				params : {
					paths : paths,
					path : this.currentPath
				},
				scope : this
			});
		}
	};
	
	filemanager.deleteFiles = function(){
		var records = filemanager.grid.getSelectionModel().getSelections();
		if(records.length <= 0){
			Ext.Msg.alert('操作提示','至少选择一个文件或者文件夹！',function(){
				return;
			});
		}else{
			Ext.Msg.confirm('操作确认','您确实要删除这些文件?',function(btn){
				if(btn == 'yes'){
					var paths = [];
					for(var i = 0; i < records.length; i++){
						paths.push(records[i].data.id);	
					}					
					Share.AjaxRequest({
						url : filemanager.deleteFiles_url ,
						params : {
							paths : paths
						},
						showMsg:false,
				   		showWaiting:true,
						callback : function(json) {
							filemanager.refresh();
						}
					});
									
				}
			},this);
		}
	};
	
	filemanager.createFolder = function(){
		Ext.Msg.prompt('新建文件夹','请输入文件夹名称',function(btn, text){
		    if (btn == 'ok'){
		    	Share.AjaxRequest({
					url : filemanager.createFolder_url ,
					params : {
						node : filemanager.store.baseParams['node'],
						folder : text
					},
					showMsg:false,
			   		showWaiting:true,
					callback : function(json) {
						filemanager.refresh();
					}
				});
		    }
		},this);
	};
	
	filemanager.showUploadDialog = function(){
		//alert("${pageContext.session.id}");
		//alert(Ext.util.Cookies.get('JSESSIONID'))
		new Ext.Window({
			width : 650,
			title : '上传示例',
			height : 300,
			layout : 'fit',
			items : [{
				xtype : 'SWFUploader',
				border : false,
				fileSize : 1024 * 550,// 限制文件大小550MB
				uploadUrl : filemanager.uploadFiles_url+";jsessionid=${pageContext.session.id}",
				flashUrl : filemanager.flashUrl,
				filePostName : 'file', // 后台接收参数
				fileTypes : '*.*',// 可上传文件类型 "*.log;*.txt"
				postParams : {
					node : filemanager.store.baseParams['node']
				}
			}]
		}).show();
	};
	
	filemanager.text_copy = function(grid, rowIndex, columnIndex, e){
		var record = grid.getStore().getAt(rowIndex);  // Get the Record
    	var fieldName = grid.getColumnModel().getDataIndex(columnIndex); // Get field name
    	var data = record.get(fieldName);
    	Share.copyToClipboard(data);
	
	};
	
	filemanager.server = function(){
		var records = filemanager.grid.getSelectionModel().getSelections();
		if(records.length <= 0){
			Ext.Msg.alert('操作提示','至少选择一个文件！',function(){
				return;
			});
		}else{
			Ext.Msg.confirm('操作确认','您确实要提交你选择的文件到服务器?',function(btn){
				if(btn == 'yes'){
					var paths = [];
					for(var i = 0; i < records.length; i++){
						paths.push(records[i].data.id);	
					}					
					Share.AjaxRequest({
						url : filemanager.server_url ,
						timeout : 300000,
						params : {
							paths : paths
						},
						showMsg:true,
				   		showWaiting:true,
						callback : function(json) {
							filemanager.refresh();
						}
					});
									
				}
			},this);
		}
	};
	
	filemanager.refresh = function(){
		filemanager.grid.getStore().reload();
	};
	
	filemanager.listRootFiles = function() { // 根目录
		filemanager.listFiles("");
	};
	
	filemanager.listParentFiles = function() { // 向上目录
		if (filemanager.grid.getStore().getCount() > 0) {// 有记录才继续
			var orgPath = filemanager.grid.getStore().getAt(0).data.id;
			var path = '\\' + orgPath;// 获得路径
			var index = -1;
			for (var i = 0; i < 2; i++) {
				index = path.lastIndexOf('\\');
				if (index != -1) {
					path = path.substring(0, index);
				}
			}
			index = orgPath.lastIndexOf('\\');
			if (index != -1) {
				filemanager.listFiles(path);// 获得上一级文件夹名称
			}
		} else {// 如果是空文件夹
			if (this.currentPath.length > 0) {
				var index = this.currentPath.lastIndexOf('\\');
				if (index != -1) {
					this.currentPath = this.currentPath.substring(0, index);
					this.listFiles(this.currentPath);
				} else {
					this.listFiles();
				}
			}
		}
	};
	
	filemanager.listFiles = function(_node) { // 获得文件夹中文件列表
		this.currentPath = '/' + (Ext.isEmpty(_node) ? '' : _node);
		filemanager.store.baseParams.node=_node;
		filemanager.grid.getStore().load();
	};
	
	filemanager.onEnterForSearch = function(field,e) {
		if (e.keyCode == 13) {   // 
		   var char_=new RegExp("^\\w*\\W*\\w*\\W*"+Ext.getCmp('search_filename').getValue()+"\\w*\\W*\\w*\\W*$");       	   
       //	   alert(char_);
       	   var idx = filemanager.store.find("fileName",char_);      
           if(idx!=-1){
	           filemanager.grid.getSelectionModel().clearSelections();
	           filemanager.grid.getView().focusRow(idx);
	           filemanager.grid.getSelectionModel().selectRow(idx,true);
	           //var record=filemanager.grid.getStore().getAt(idx);	           
           }
		}
	};
	
	filemanager.onRowClick = function(grid,rowIndex,e){
		
	};
	
	filemanager.onRowDblClick = function(grid, rowIndex) {
		if (!grid.getStore().getAt(rowIndex).data.leaf) {// 如果文件夹才获访问
			grid.listFiles(grid.getStore().getAt(rowIndex).data.id);
		}
	};
	
	filemanager.onRowContextMenu = function(grid, index, e){
		e.stopEvent();
		if (filemanager.grid.getSelectionModel().isSelected(index) !== true) {
			filemanager.grid.getSelectionModel().clearSelections();
			filemanager.grid.getSelectionModel().selectRow(index);
			filemanager.grid.fireEvent('rowclick',grid,index,e);
		}
		filemanager.gridContextMenu.showAt(e.getXY());
	};
	
	/** -------function 函数区--------------end */
	
	
	/** ----------Action 行为组-------------begin*/
	filemanager.actions = [
			new Ext.Action({
				text : '向上',
				iconCls : 'db-icn-back',
				handler : function() {
					filemanager.listParentFiles();
				},
				scope : this
			}),
			new Ext.Action({
				text : '根目录',
				iconCls : 'db-icn-world',
				handler : function() {
					filemanager.listRootFiles();
				},
				scope : this
			}),
			new Ext.Action({
				text : '新建文件夹',
				iconCls : 'db-icn-folder-new',
				handler : function(){
					filemanager.createFolder();
				},
				scope : this
			}),
			new Ext.Action({
				text : '删除',
				iconCls : 'db-icn-delete',
				handler : function(){
					filemanager.deleteFiles();
				},
				scope : this
			}),
			new Ext.Action({
				text : '压缩',
				iconCls : 'db-icn-folder-zip',
				handler : function(){
					filemanager.compressionFiles();
				},
				scope : this
			}),
			new Ext.Action({
				text : '解压缩',
				handler : function(){
					filemanager.decompressionFiles();
				},
				scope : this
			}),
			new Ext.Action({
				text : '刷新',
				iconCls : 'db-icn-refresh',
				handler : function(){
					filemanager.refresh();
				},
				scope : this
			}),
			new Ext.Action({
				text : '上传',
				iconCls : 'db-icn-upload',
				handler : function() {
					filemanager.showUploadDialog();
				},
				scope : this
			}),
			new Ext.Action({
				text : '下载',
				iconCls : 'db-icn-download',
				handler : function(){
					filemanager.download();
				},
				scope : this
			}),
			new Ext.Action({
				text : '剪切',
				iconCls : 'db-icn-cut',
				handler : function(){
					filemanager.cut_or_copy("cut");
				},
				scope : this
			}),
			new Ext.Action({
				text : '复制',
				iconCls : 'db-icn-copy',
				handler : function(){
					filemanager.cut_or_copy("copy");
				},
				scope : this
			}),
			new Ext.Action({
				text : '粘贴',
				iconCls : 'db-icn-paste',
				handler : function(){
					filemanager.paste();
				},
				scope : this
			}),
			new Ext.Action({//index = 12
				text : '重命名',
				iconCls : 'db-icn-rename',
				handler : function(){
					filemanager.rename();
				},
				scope : this
			}),
			new Ext.Action({//index = 13
				text : '提交服务器',
				iconCls : 'db-icn-server',
				handler : function(){
					filemanager.server();
				},
				scope : this
			})
		];
	
	
	/** ----------Action 行为组-------------end*/
	
	/** ----grid 右击菜单  */
	filemanager.gridContextMenu = new Ext.menu.Menu({
			items : [
				filemanager.actions[2],filemanager.actions[8],'-',
		//		filemanager.actions[4],filemanager.actions[5],'-',
				filemanager.actions[9],filemanager.actions[10],filemanager.actions[11],'-',
				filemanager.actions[3],filemanager.actions[12]
			]
		});
	
	
	/** 改变页的combo */
	filemanager.pageSizeCombo = new Share.pageSizeCombo({
				value : '20',
				listeners : {
					select : function(comboBox) {
						filemanager.pageSize = parseInt(comboBox.getValue());
						filemanager.bbar.pageSize = parseInt(comboBox.getValue());
						filemanager.store.baseParams.limit = filemanager.pageSize;
						filemanager.store.baseParams.start = 0;
						filemanager.store.load();
					}
				}
			});
	// 覆盖已经设置的。具体设置以当前页面的pageSizeCombo为准
	filemanager.pageSize = parseInt(filemanager.pageSizeCombo.getValue());
	/** 基本信息-数据源 */
	filemanager.store = new Ext.data.Store({
				autoLoad : false,
				remoteSort : false,
				baseParams : {
					start : 0,
					limit : filemanager.pageSize
				},
				proxy : new Ext.data.HttpProxy({// 获取数据的方式
					method : 'POST',
					url : filemanager.getfiles
				}),
				reader : new Ext.data.JsonReader({// 数据读取器
					totalProperty : 'results', // 记录总数
					root : 'rows' // Json中的列表数据根节点
				}, 
				['id', 'fileName',{name:'lastModifyDate',type : 'date',dateFormat: 'time'},'leaf','fileSize']),
				sortInfo : { 
					field : "fileName",
					direction: "asc"
				},
				listeners : {
					'load' : function(store, records, options) {
					
						filemanager.alwaysFun();
					}
				}
			});
	/** 基本信息-选择模式 */
	filemanager.selModel = new Ext.grid.CheckboxSelectionModel({
			singleSelect : true
			
		});
		
	/** 基本信息-数据列 */
	filemanager.colModel = new Ext.grid.ColumnModel({
			defaults : {
				sortable : true,
				width : 140
			},
			columns : [new Ext.grid.RowNumberer(),
					{
						hidden : true,
						header : 'ID',
						dataIndex : 'id',
						width : 200
					}, {
						header : '类型',
						dataIndex : 'fileName',
						width : 70,
						renderer : filemanager.formatIcon
					}, {
						header : '名称',
						width : 150,
						dataIndex : 'fileName'
					}, {
						header : '修改日期',
						width : 150,
						dataIndex : 'lastModifyDate',
						renderer : Ext.util.Format.dateRenderer('Y-m-d H:i:s')
					}, {
						header : '大小',
						dataIndex : 'fileSize',
						width : 150
					}]
		});
		
		
		
	/** 保存路径信息*/
	filemanager.cut_copy_path={
		xtype : 'textfield',
		text : '',
		hidden : true,
		align : 'right',
		id :'cut_copy_path'
	};
	
	/** 保存文件操作信息*/
	filemanager.cut_copy_method={
		xtype : 'textfield',
		text : '',
		hidden : true,
		align : 'right',
		id :'cut_copy_method'
	};
	
	filemanager.cover =  { 
		boxLabel: '覆盖', 
		name: 'cover', 
		id: 'cover', 
		inputValue: 'true', 
		xtype: 'checkbox', 
		checked: true 
	};
	
	/** 顶部工具栏*/
//	filemanager.tbar = ['TREE_ID:',filemanager.tree_id, '-', filemanager.printAction,'-',filemanager.tipmsg];

	
	filemanager.tbar = [
		filemanager.actions[0],filemanager.actions[1],filemanager.actions[6],'-',
		filemanager.actions[3],filemanager.actions[9],filemanager.actions[10],filemanager.cover,filemanager.actions[11],'-',
		filemanager.actions[7],filemanager.actions[8],'-',filemanager.actions[13],'-',{
			xtype : 'textfield',
			emptyText : '文件名定位',
			id : 'search_filename',
			listeners : {
				"specialkey" : function(field,e) {
					filemanager.onEnterForSearch(field,e);
				},
				scope : this
			}
	},filemanager.cut_copy_method,filemanager.cut_copy_path];
	
	
	/** 底部工具条 */
	filemanager.bbar = new Ext.PagingToolbar({
			pageSize : filemanager.pageSize,
			store : filemanager.store,
			displayInfo : true,
			// plugins : new Ext.ux.ProgressBarPager(), // 分页进度条
			items : ['-', '&nbsp;', filemanager.pageSizeCombo]
		});
		
	/** 基本信息-表格 */
	filemanager.editor = new Ext.ux.grid.RowEditor({
        saveText: 'Update'
    });
    
	filemanager.grid = new Ext.grid.GridPanel({
			store : filemanager.store,
			colModel : filemanager.colModel,
			selModel : filemanager.selModel,
			tbar : filemanager.tbar,
			bbar : filemanager.bbar,
			autoScroll : 'auto',
			region : 'center',
			loadMask : true,
			// autoExpandColumn :'fieldDesc',
			//plugins: [filemanager.editor],
			stripeRows : true,
			listeners : {
				'rowclick' : filemanager.onRowClick,
				'rowdblclick' : filemanager.onRowDblClick,
				'rowcontextmenu' : filemanager.onRowContextMenu,
				'contextmenu' : function(e){e.stopEvent();},
				'cellclick'   : function(grid, rowIndex, columnIndex, e){
						//filemanager.text_copy(grid, rowIndex, columnIndex, e);
				 }, 
				 scope : this
			},
			viewConfig : {
				forceFit : true
			}
		});
	
	
	
	/**  节点详细信息  以及节点直接下属信息 end*/
	filemanager.myPanel = new Ext.Panel({
		id : '${param.id}' + '_panel',
		renderTo : '${param.id}',
		layout : 'border',
		boder : false,
		height : index.tabPanel.getInnerHeight() - 1,
		items : [filemanager.treePanel,filemanager.grid]
		});
	
	}
	
});



	   				
</script>
