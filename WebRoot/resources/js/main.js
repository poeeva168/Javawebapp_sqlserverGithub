Ext.ns("Ext.Authority.index"); // 自定义一个命名空间
index = Ext.Authority.index; // 定义命名空间的别名
index = {
    welcome: ctx + "/welcome",
    header: ctx + '/header',
    treeMenu: ctx + "/treeMenu",
    message: ctx + "/message/news"
};
// 设置主题
Share.swapStyle();
// 头部
index.headerPanel = new Ext.Panel({
    region: 'north',
    height: 65,
    border: false,
    margins: '0 0 0 0',
    collapseMode: 'mini',
    collapsible: true,
    bodyStyle: 'background-color:transparent;',
    autoLoad: {
        url: index.header,
        scripts: true,
        nocache: true
    }
});
index.menuTree = new Ext.tree.TreePanel({
    useArrows: true,
    // 设置为true将在树中使用Vista-style的箭头
    autoScroll: true,
    animate: true,
    // 设置为true以启用展开/折叠时的动画效果
    containerScroll: true,
    // 设置为true向ScrollManager注册此容器
    border: false,
    rootVisible: false,
    // 设置为false将隐藏root节点
    margins: '2 2 0 0',
    loader: new Ext.tree.TreeLoader({
        dataUrl: index.treeMenu,
        clearOnLoad: true
    }),
    root: {
        expanded: true,
        id: '0'
    },
    listeners: {
        'click': function (node, e) { // 点击事件
            if (node.attributes.url) { // 如果是链接 node.isLeaf()
                Share.openTab(node, ctx + node.attributes.url);
            } else {
                e.stopEvent();
            }
        }
    }
});
// 菜单面板
index.menuPanel = new Ext.Panel({
    region: 'west',
    title: '主菜单',
    iconCls: 'computer',
    margins: '0 2 0 0',
    layout: 'fit',
    width: 180,
    minSize: 100,
    maxSize: 300,
    split: true,
    collapsible: true,
    tools: [{
        id: 'refresh',
        handler: function () {
            index.menuTree.root.reload();
        }
    }],
    items: [index.menuTree]
});

// tab主面板
index.tabPanel = new Ext.TabPanel({
    id: 'mainTabPanel',
    region: 'center',
    activeTab: 0,
    deferredRender: false,
    enableTabScroll: true,
    // bodyStyle:'height:100%',
    defaults: {
        layout: 'fit',
        autoScroll: true
    },
    plugins: new Ext.ux.TabCloseMenu({
        closeTabText: '关闭标签页',
        closeOtherTabsText: '关闭其他标签页',
        closeAllTabsText: '关闭所有标签页'
    }),
    items: [{
        id: 'home',
        title: '我的主页',
        iconCls: 'home',
        closable: false,
        autoScroll: true,
        autoLoad: {
            url: index.welcome,
            scripts: true,
            nocache: true
        }
    }],
    listeners: {
        'bodyresize': function (panel, neww, newh) {
            // 自动调整tab下面的panel的大小
            var tab = panel.getActiveTab();
            var centerpanel = Ext.getCmp(tab.id + "_div_panel");
            if (centerpanel) {
                centerpanel.setHeight(newh - 2);
                centerpanel.setWidth(neww - 2);
            }
        }
    }
});

index.msgArea = new Ext.form.TextArea({
	id:'info',
    autoScroll: true,
    readOnly: true,
    region: 'center',
    
    value:''
});

index.msgWriter= new Ext.form.TextField({ //发布作者
	id:'username',
	fieldLabel:'用户',
	autoWidth:true,
	readOnly:true,
	value:''
});

index.msgIput = new Ext.form.TextArea({
	id:'content',
    autoScroll: true,
    fieldLabel:'内容',
    autoWidth:true,
    autoHeight:true,
    value:''
});

index.msgFormpanel=new Ext.form.FormPanel({
	frame : false,
	title : '消息发布区',
	bodyStyle : 'padding:0px;border:0px',
	autoHeight:true,
	labelWidth : 30,
	region: 'south',
	labelAlign : 'right',
	buttonAlign : 'center',
	items : [index.msgWriter,index.msgIput],
	buttons:[{
		formBind:true, 
		text : '提交',
		handler : function() {
			index.msgsend();
		}	
	}]
	
});

index.msgsend = function () {
    var time = new Date();
    var content = dwr.util.getValue("content");
    var name = dwr.util.getValue("username");
    var info = encodeURI(encodeURI(name + " :" + content));
    var msg = {"msg": info, "time": time};
    dwr.util.setValue("content", "");
    if (!!content) {
        ChatService.sendMessage(msg);
    } else {
        alert("发送的内容不能为空！");
    }
};

index.showMessage = function(data) {
	var dwrtest = HenloController.webtest("param");
	alert(dwrtest);
    var message = decodeURI(decodeURI(data.msg));
    if(message!=''){
    	index.msgPanel.expand(true);
    }
    var text = dwr.util.getValue("info");
    if (!!text) {  
        dwr.util.setValue("info", text + "\r\n" + data.time + "  " + message);
    } else {
        dwr.util.setValue("info", data.time + "  " + message);
    }
};

index.msgPanel = new Ext.Panel({
    layout: 'border',
    title: '消息窗口',
    region: 'east',
    collapseMode: 'mini',
    width: 200,
    minSize: 100,
    maxSize: 300,
    // True将会使panel折叠并且会自动把展开/折叠
    // (expand/collapse)按钮渲染到顶部工具按钮区域
    collapsible: true,
    collapsed: true,// true 折叠 false 展开    
    split: true,
    tbar: [{
        xtype: 'button',
        text: '清屏',
        iconCls: 'cancel',
        handler: function () {
            index.msgArea.reset();
        }
    }],
    items: [index.msgArea,index.msgFormpanel]
});




// 初期化页面Layout
index.viewport = new Ext.Viewport({
    layout: 'border',
    items: [index.headerPanel, index.menuPanel, index.tabPanel, index.msgPanel]
});

//自动任务调度
index.task = {
		run : function() {
			/*Share.AjaxRequest({
   				url : index.message,
   				params:'',
   				showMsg:false,
   				showWaiting:false,
   				callback : function(json) {
   					if(json.success&&json.msg>0){
	   					//消息提示框
	   					index.messageWin = new Ext.ux.ToastWindow({
	   					    title: '提示窗口',  
	   					    html: '消息内容',  
	   					    iconCls: 'tabs'  
	   					});
	   					index.messageWin.html='共有<font color=red>'+json.msg+'</font>条新消息,请打开<font color=red>消息管理</font>查看';
	   					index.messageWin.show(document);
	   					
	   					index.messageWin=null;
   					}  					
   				}
	   		});*/			
			
			Ext.Ajax.request({				
				url : index.message,
				success: function (response, options) {
					var json = Ext.decode(response.responseText);
					if(json.success&&json.msg>0){
	   					//消息提示框
	   					index.messageWin = new Ext.ux.ToastWindow({
	   					    title: '提示窗口',  
	   					    html: '消息内容',  
	   					    iconCls: 'tabs'  
	   					});
	   					index.messageWin.html='共有<font color=red>'+json.msg+'</font>条新消息,请打开<font color=red>消息管理</font>查看';
	   					index.messageWin.show(document);
	   					
	   					index.messageWin=null;
   					}
					
		        },
		        failure: function (response, options) {
		        	index.task_stop();            
		        }
				
			});

		},
		//scope : this,
		interval : 600000 // 60秒刷新一次
	};

// 定时执行任务 
Ext.TaskMgr.start(index.task);

index.task_stop = function () {
	Ext.TaskMgr.stop(index.task);
};
