<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<%@ include file="/WEB-INF/views/commons/yepnope.jsp"%>
<html>
<script type="text/javascript" >
	yepnope({
	load : [       
		"${ctx}/resources/css/icons.css",
	 	//LockingGridView
		"${ctx}/resources/extjs/ux/css/LockingGridView.css",
		"${ctx}/resources/extjs/ux/LockingGridView.js",
		//MultiSelect
		"${ctx}/resources/extjs/ux/css/lovcombo.css",
		"${ctx}/resources/extjs/ux/Ext.ux.util.js",
		"${ctx}/resources/extjs/ux/Ext.ux.form.LovCombo.js",
		//Ext.ux.grid.RowEditor
        "${ctx}/resources/extjs/ux/css/RowEditor.css",
        "${ctx}/resources/extjs/ux/RowEditor.js"
		],
	complete : function() {
	
	Ext.ns("Ext.portal_for_gmq.xiangjy"); // 自定义一个命名空间
	xiangjy = Ext.portal_for_gmq.xiangjy; // 定义命名空间的别名
	xiangjy = {
		all : ctx + '/portal_for_gmq/xiangjy/all',// 加载所有
		save : ctx + "/portal_for_gmq/xiangjy/save",//保存	
		barcode : ctx + "/portal_for_gmq/xiangjy/barcode",//获取实际条码
		boxqty  : ctx + "/portal_for_gmq/xiangjy/boxqty",//获取装箱数量
		pagesizes:eval('(${fields.pagesizes==null?"{}":fields.pagesizes})'),
		pageSize : 100, // 每页显示的记录数
		BOXNO : '',
		BOXNO_CT : '0' ,
		report : ctx + '/portal_for_gmq/xiangjy/henlo_ireport_xiangjy',   //报表打印
	};

	/** 改变页的combo */
	xiangjy.pageSizeCombo = new Share.pageSizeCombo({
			value : '100',
			listeners : {
				select : function(comboBox) {
					xiangjy.pageSize = parseInt(comboBox.getValue());
					xiangjy.bbar.pageSize = parseInt(comboBox.getValue());
					xiangjy.store.baseParams.limit = xiangjy.pageSize;
					xiangjy.store.baseParams.start = 0;
					var BOXNO = Ext.util.Format.trim(Ext.getCmp('BOXNO').getValue());
					if(Ext.isEmpty(BOXNO))
						;
					else{
						xiangjy.store.load();
						Ext.getCmp('M_PRODUCT_ALIAS_NO').focus(1000);
						Share.AjaxRequest({
							url : xiangjy.boxqty,
							showMsg : false,
							params : {
								data : BOXNO
							},
							callback : function(json) {
								BOXNO_CT = json.msg		
								
							}
						});						
					}
					
				}
			}
		});
	// 覆盖已经设置的。具体设置以当前页面的pageSizeCombo为准
	xiangjy.pageSize = parseInt(xiangjy.pageSizeCombo.getValue());
	/** 基本信息-数据源 */
	xiangjy.store = new Ext.data.Store({
			autoLoad : false,
			remoteSort : false,
			baseParams : {
				head  : '',
				start : 0,
				limit : xiangjy.pageSize
			},
			proxy : new Ext.data.HttpProxy({// 获取数据的方式
				method : 'POST',
				url : xiangjy.all
			}),
			reader : new Ext.data.JsonReader({// 数据读取器
				totalProperty : 'results', // 记录总数
				root : 'rows' // Json中的列表数据根节点
			}, ['B_PO_BOXNO_ID', 'BOXNO','M_PRODUCTALIAS_ID','M_PRODUCT_ALIAS_NO','M_PRODUCT_NAME', 'M_PRODUCT_VALUE', 'COLOR_VALUE','SIZE_CODE',
				'QTY','QTY_QR'
				]),
			listeners : {
				'load' : function(store, records, options) {
					xiangjy.BOXNO = Ext.getCmp("BOXNO").getValue();
					if(!Ext.isEmpty(xiangjy.BOXNO))
						xiangjy.printAction.enable();
					xiangjy.disableFun();
					if(store.getCount()>0){
						xiangjy.alwaysFun();
						Ext.getCmp('M_PRODUCT_ALIAS_NO').focus(1000);
						
						Share.AjaxRequest({
							url : xiangjy.boxqty,
							showMsg : false,
							params : {
								BOXNO : xiangjy.BOXNO
							},
							callback : function(json) {
								if(json.success){
									xiangjy.BOXNO_CT = json.msg;	
								}								
								if(xiangjy.BOXNO_CT=='0'){
									Ext.Msg.alert('提示', '<font color=red >获取装箱标准为空或为0，不能自动提交 </font>');									
								}								
							}
						});
						
					}
					
				}
			}
		});
	/** 基本信息-选择模式 */
	xiangjy.selModel =  new Ext.grid.CheckboxSelectionModel();
	//xiangjy.selModel.sortLock();
	/** 基本信息-数据列 */
	xiangjy.colModel = new Ext.grid.ColumnModel({
			defaults : {
				sortable : true
			},
			columns : [xiangjy.selModel,new Ext.grid.RowNumberer(), {
						hidden : true,
						header : '箱号ID',
						dataIndex : 'B_PO_BOXNO_ID'
					},{
						header : '箱号',
						dataIndex : 'BOXNO',
						width: 150
					},{
						header : '条码ID',
						dataIndex : 'M_PRODUCTALIAS_ID',
						hidden : true,
					}, {
						header : '条码',
						dataIndex : 'M_PRODUCT_ALIAS_NO',
						renderer:function (value, cellmeta, record, rowIndex, columnIndex, store){
							if(record.data.QTY==0)
								return '<span style="font-weight:bolder;color:red">'+value+'</span>';
							else if(record.data.QTY==record.data.QTY_QR)
								return '<span style="font-weight:bolder;color:green">'+value+'</span>';
							else if(record.data.QTY<record.data.QTY_QR)
									return '<span style="font-weight:bolder;color:orange">'+value+'</span>';	
							else
								return value;
						}
					}, {
						header : '款号',
						dataIndex : 'M_PRODUCT_NAME'
					}, {
						header : '品名',
						dataIndex : 'M_PRODUCT_VALUE'
					}, {
						header : '颜色',
						dataIndex : 'COLOR_VALUE'
					}, {
						header : '尺寸',
						dataIndex : 'SIZE_CODE'
					}, {
						header : '数量',
						dataIndex : 'QTY'
					}, {
						header : '确认数量',
						dataIndex : 'QTY_QR',
						editor:new Ext.form.NumberField
					}]
		});

	/** 删除 */
	xiangjy.deleteAction = new Ext.Action({
			text : '删除',
			iconCls : 'field_delete',
			disabled : true,
			handler : function() {
				xiangjy.delFun();
			}
		});
	/** 提交*/
	xiangjy.saveAction = new Ext.Action({
			text : '提交',
			iconCls : 'save',
			disabled : true,
			handler : function() {
				xiangjy.saveFun();
			}
		});
	/** 查询 */
	xiangjy.search_boxno = new Ext.ux.form.SearchField({
			id:'BOXNO',
			name:'BOXNO',
			store : xiangjy.store,
			paramName : 'BOXNO',
			emptyText : '请输入箱号',
			style : 'margin-left: 5px;'
			
		});
	/** 条码输入框 */
	xiangjy.search_m_product_alias_no = new Ext.form.TextField({
		id : 'M_PRODUCT_ALIAS_NO',
		name : 'M_PRODUCT_ALIAS_NO',
		emptyText : '请输入条码',
		style : 'margin-left: 5px;',
		disabled : true,
		regex : /^\w+$/,
		regexText:"只能输入字母或数字",
		listeners:{
			"specialkey":function(field,e){
				if (e.keyCode == 13) {
					handler : xiangjy.checkm_product_alias_noFun(Ext.getCmp("M_PRODUCT_ALIAS_NO").getValue());
				}
			}
		}
	});
	/** Ireport print */
	xiangjy.printAction = new Ext.Action({
		text : '报表打印',
		iconCls : 'chart_pie',
		disabled : true,
		handler : function() {
			xiangjy.printFun();
		}
	});
	
		
	/** 提示 */
	xiangjy.tips = '&nbsp;<font color="red"><b>条码颜色不同表示 数量与确认数量 间的正值或负值差异</b></font>';
	/** 顶部工具栏 */
	xiangjy.tbar = ['箱号：',xiangjy.search_boxno,'-',' 条码：',xiangjy.search_m_product_alias_no,xiangjy.deleteAction,'-',xiangjy.printAction,'-',xiangjy.saveAction,xiangjy.tips];

	/** 底部工具条 */
	xiangjy.bbar = new Ext.PagingToolbar({
			pageSize : xiangjy.pageSize,
			store : xiangjy.store,
			displayInfo : true,
			// plugins : new Ext.ux.ProgressBarPager(), // 分页进度条
			items : ['-', '&nbsp;', xiangjy.pageSizeCombo]
		});
	xiangjy.editor = new Ext.ux.grid.RowEditor({
        saveText: 'Update'
    });
	/** 基本信息-表格 */
	xiangjy.grid = new Ext.grid.EditorGridPanel({
			store : xiangjy.store,
			colModel : xiangjy.colModel,
			selModel : xiangjy.selModel,
			tbar : xiangjy.tbar,
			bbar : xiangjy.bbar,
			autoScroll : 'auto',
			region : 'center',
			loadMask : true,
			// autoExpandColumn :'fieldDesc',
			stripeRows : true,
	//		plugins: [xiangjy.editor],
			listeners : {},
			viewConfig : {}
		});

	xiangjy.alwaysFun = function() {
		xiangjy.BOXNO = Ext.getCmp("BOXNO").getValue();
		Share.resetGrid(xiangjy.grid);
		xiangjy.deleteAction.enable();
		xiangjy.saveAction.enable();
		xiangjy.printAction.enable();
		xiangjy.search_m_product_alias_no.enable();
	};
	
	xiangjy.disableFun = function(){
		xiangjy.deleteAction.disable();
		xiangjy.saveAction.disable();
	};

	xiangjy.checkm_product_alias_noFun = function(e){
		var sm = xiangjy.grid.getSelectionModel();
		var store = xiangjy.grid.getStore();
		var view = xiangjy.grid.getView();
		var insert = 1;
		var qursl=0;
		var qty_qr_total = 0;
		var qty_total = 0;
		//发送Ajax请求 获取真正的条码
		Share.AjaxRequest({
			url : xiangjy.barcode,
			showMsg : false,
			params : {
				data : e
			},
			callback : function(json) {
				e = json.msg
				for (var i = 0; i < view.getRows().length; i++) {
					qty_qr_total=qty_qr_total+parseInt(store.getAt(i).get('QTY_QR'));
					qty_total=qty_total+parseInt(store.getAt(i).get('QTY'));
					if(e==store.getAt(i).get('M_PRODUCT_ALIAS_NO')){
						if(Ext.isEmpty(store.getAt(i).get('QTY_QR')))
							;
						else
							qursl = parseInt(store.getAt(i).get('QTY_QR'));
						store.getAt(i).set('QTY_QR',qursl+1);
						Ext.getCmp('M_PRODUCT_ALIAS_NO').setValue('');
						insert=0;
						break;
					}
				}
				if(insert==1&&!Ext.isEmpty(Ext.util.Format.trim(e))){
					var rec = new (store.recordType)();
					rec.set('BOXNO',xiangjy.BOXNO);
					rec.set('B_PO_BOXNO_ID','');
					rec.set('M_PRODUCT_ALIAS_NO',e);	
					rec.set('QTY',0);
					rec.set('QTY_QR',1);
					store.insert(0,rec);
					Ext.getCmp('M_PRODUCT_ALIAS_NO').setValue('');
				}
				Ext.getCmp('M_PRODUCT_ALIAS_NO').focus();
				//检测要不要自动提交
				if(qty_qr_total+1>xiangjy.BOXNO_CT&&xiangjy.BOXNO_CT>0){
					xiangjy.saveFun();
					return;
				}
				//当确认数量超出原装箱总数时，给出提示。
                if (qty_qr_total+1 > qty_total) {
                	Ext.Msg.alert('提示', '<font color=red >确认数量 大于 原始数量 </font>');
                	return ;
                }	
			}
		});		
	} 

	xiangjy.delFun = function() {
		Ext.Msg.confirm('提示', '确定要删除选中的记录吗?', function(btn, text) {
			if (btn == 'yes') {
				var sm = xiangjy.grid.getSelectionModel();
				var store = xiangjy.grid.getStore();
				var view = xiangjy.grid.getView();
				for (var i = 0; i < view.getRows().length; i++) {
					if (sm.isSelected(i)) {
						store.remove(store.getAt(i));
						i--;
						//ids.push(store.getAt(i).get('id'));
					}
				}
			}
		});
	};
	xiangjy.saveFun = function() {
		var evt  = {} ;
		var data = new Array();
		var sm = xiangjy.grid.getSelectionModel();
		var store = xiangjy.grid.getStore();
		var view = xiangjy.grid.getView();
		var confirm = 0;
		
		var qty = 0; var qty_qr = 0;
		for (var i = 0; i < view.getRows().length; i++) {
			var cell = {};
			cell.BOXNO = xiangjy.BOXNO;
			if(store.getAt(i).get('QTY')!=store.getAt(i).get('QTY_QR'))
				confirm=1;
			
			qty = qty+store.getAt(i).get('QTY');
			qty_qr = qty_qr+store.getAt(i).get('QTY_QR');
			
			cell.B_PO_BOXNO_ID = store.getAt(i).get('B_PO_BOXNO_ID');
			cell.M_PRODUCTALIAS_ID   = store.getAt(i).get('M_PRODUCTALIAS_ID');
			cell.M_PRODUCT_ALIAS_NO  = store.getAt(i).get('M_PRODUCT_ALIAS_NO');
			cell.QTY = store.getAt(i).get('QTY');
			cell.QTY_QR = store.getAt(i).get('QTY_QR');
			data.push(Ext.encode(cell));
		}
		evt.param = Ext.encode(data);
		
		if(confirm==1){			
			if(window.confirm('确定要提交记录?原始数量与确认数量，明细有不一致')){

             }else{
                return false;
            }
		}
		
		if(xiangjy.BOXNO_CT>0){
			if(qty_qr>xiangjy.BOXNO_CT){
				alert("确认数量大于装箱数量");
				return false;
			}
			
			if(qty_qr<xiangjy.BOXNO_CT){
				if(window.confirm('确定要提交记录?装箱标准: '+xiangjy.BOXNO_CT+' 确认数量 ：'+qty_qr+'')){

				}else{
	                return false;
	            }
			}
		}
		
		
		Share.AjaxRequest({
			url : xiangjy.save,
			params : {
				data : Ext.encode(data)
			},
			callback : function(json) {
				xiangjy.alwaysFun();
				xiangjy.store.reload();
				Ext.getCmp('BOXNO').focus(1000);
				Ext.getCmp('BOXNO').selectText();
			}
		});
		
		
	};
	/** ireport print function*/
	xiangjy.printFun =function(){
		var BOXNO = Ext.util.Format.trim(Ext.getCmp('BOXNO').getValue());
		if(Ext.isEmpty(BOXNO))
			Ext.Msg.alert('操作提示','箱号不能为空',function(){
				return;
		});
		else {
			window.open(xiangjy.report+"?param="+BOXNO+"&time="+(new Date()).toString());
		}				
	};

	xiangjy.myPanel = new Ext.Panel({
			id : 'xiangjy' + '_panel',
			renderTo : 'xiangjy',
			layout : 'border',
			boder : false,			
			items : [xiangjy.grid],
			listeners :{
			//进入页面执行事件设置高度和宽度
				'render':function(){
				    this.setWidth(document.body.offsetWidth+document.body.clientWidth-10);
				  	this.setHeight(document.body.offsetHeight+document.body.clientHeight-20);
				 },
				bodyresize:function() {
					
					//this.setWidth(window.screen.width-10);
				}
			}
		});
		//BX1309060000008
			
	}
});
</script>
<body>
	<div id="xiangjy" ></div>	
</body>
</html>
