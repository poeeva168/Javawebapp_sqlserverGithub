Ext.ns('Ext.ux.grid'); // 声明命名空间
/**
 * 动态生成列 
 **/
Ext.ux.grid.DynamicGrid = Ext.extend(Ext.grid.GridPanel, {
	 initComponent: function() {
	  //创建store
	  var ds = new Ext.data.Store({
	   url: this.storeUrl,
	   reader: new Ext.data.JsonReader()
	  });
	  
	  //设置默认配置
	  var config = {
	   viewConfig: {
	    forceFit: true
	   },
	   enableColLock: false,
	   loadMask: true,
	   border: true,
	   stripeRows: true,
	   ds: ds,
	   columns: []
	  };
	  
	  //给分页PagingToolbar绑定store
	  this.bbar.bindStore(ds, true);
	  
	  Ext.apply(this, config);
	  Ext.apply(this.initialConfig, config);
	  Ext.grid.DynamicGrid.superclass.initComponent.apply(this, arguments);
	 },
	 
	 onRender: function(ct, position) {
	  this.colModel.defaultSortable = true;
	  Ext.grid.DynamicGrid.superclass.onRender.call(this, ct, position);

	  this.el.mask('Loading...');
	  this.store.on('load', function() {
	   if (typeof(this.store.reader.jsonData.columns) === 'object') {
	    var columns = [];
	    
	    if (this.rowNumberer) {
	     columns.push(new Ext.grid.RowNumberer());
	    }
	    
	    if (this.checkboxSelModel) {
	     columns.push(new Ext.grid.CheckboxSelectionModel());
	    }
	    
	    Ext.each(this.store.reader.jsonData.columns, 
	     function(column) {
	      columns.push(column);
	     }
	    );
	     
	    this.getColumnModel().setConfig(columns);
	   }
	    
	   this.el.unmask();
	  }, this);
	  
	  this.store.load();
	 }
	}); 


/**
 * 
var dynamicGrid = new Ext.grid.DynamicGrid({   
        title: '测试动态列',   
        renderTo: 'dynamic-grid',   
        storeUrl: 'goods/dynamicGrid.do',   
        width : 600,   
        height: 200,   
        rowNumberer: true,   
        checkboxSelModel: true,   
        sm: new Ext.grid.CheckboxSelectionModel(),   
        bbar : new Ext.PagingToolbar({   
            pageSize : 5,   
            displayInfo : true,   
            displayMsg : '显示第{0}到{1}条数据,共{2}条',   
            emptyMsg : "没有数据",   
            beforePageText : "第",   
            afterPageText : '页 共{0}页'  
        })   
    }); 

Json代码  
{   
    'metaData': {   
        'totalProperty': 'total',   
        'root': 'records',   
        'id': 'id',   
        'fields': [   
            {'name': 'id', 'type': 'int'},   
            {'name': 'name', 'type': 'string'}   
        ]   
    },   
    'success': true,   
    'total': 50,   
    'records': [   
        {'id': '1', 'name': 'AAA'},   
        {'id': '2', 'name': 'BBB'}   
    ],   
    'columns': [   
        {'header': '#', 'dataIndex': 'id'},   
        {'header': 'User', 'dataIndex': 'name'}   
    ]   
} 
 * 
 * */
Ext.reg('dynamicgrid',Ext.ux.grid.DynamicGrid);